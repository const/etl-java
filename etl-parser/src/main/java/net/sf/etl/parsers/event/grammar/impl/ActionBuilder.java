/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2022 Konstantin Plotnikov
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.sf.etl.parsers.event.grammar.impl; // NOPMD

import net.sf.etl.parsers.DefinitionContext;
import net.sf.etl.parsers.DefinitionInfo;
import net.sf.etl.parsers.ExpressionContext;
import net.sf.etl.parsers.ObjectName;
import net.sf.etl.parsers.PropertyName;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.SyntaxRole;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.TokenKey;
import net.sf.etl.parsers.event.grammar.Keyword;
import net.sf.etl.parsers.event.grammar.LookAheadSet;
import net.sf.etl.parsers.event.grammar.impl.flattened.DefinitionView;
import net.sf.etl.parsers.event.grammar.impl.flattened.GrammarView;
import net.sf.etl.parsers.event.grammar.impl.flattened.WrapperLink;
import net.sf.etl.parsers.event.grammar.impl.nodes.BlockNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.CallNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.ChoiceNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.CommitMarkNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.DisableSoftEndsNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.ErrorNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.FallbackObjectNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.FirstChoiceNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.GroupNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.KeywordScopeNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.MarkedNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.Node;
import net.sf.etl.parsers.event.grammar.impl.nodes.ObjectNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.PropertyNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.RepeatNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.ScopeNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.SequenceNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.TermContextScope;
import net.sf.etl.parsers.event.grammar.impl.nodes.TokenNode;
import net.sf.etl.parsers.event.impl.term.action.ActionStateFactory;
import net.sf.etl.parsers.event.impl.term.action.CallAction;
import net.sf.etl.parsers.event.impl.term.action.RecoveryVoteAction;
import net.sf.etl.parsers.event.impl.term.action.ReturnAction;
import net.sf.etl.parsers.event.impl.term.action.buildtime.ActionLinker;
import net.sf.etl.parsers.event.impl.util.ListStack;
import net.sf.etl.parsers.event.unstable.model.grammar.Element;
import net.sf.etl.parsers.event.unstable.model.grammar.Wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This utility class is used to build state machines. Its methods use terms of
 * LL1 grammar. However it has strong dependency on grammar AST and compiler
 * classes.
 *
 * @author const
 */
public final class ActionBuilder { //NOPMD
    /**
     * the stack of nodes.
     */
    private final ListStack<Node> stack = new ListStack<Node>();
    /**
     * the stack of definitions.
     */
    private final List<DefinitionView> definitionStack = new ArrayList<DefinitionView>();
    /**
     * the context compiler, it is used for error reporting.
     */
    private final ContextBuilder contextBuilder;
    /**
     * The call nodes that reference this state builder factory.
     */
    private final List<CallAction> referrers = new ArrayList<CallAction>();
    /**
     * the node to return.
     */
    private Node returnNode;
    /**
     * The state factory.
     */
    private ActionStateFactory targetFactory;

    /**
     * A constructor for builder.
     *
     * @param contextBuilder the parent builder
     */
    public ActionBuilder(final ContextBuilder contextBuilder) {
        this.contextBuilder = contextBuilder;
    }

    /**
     * Start node.
     *
     * @param e the element
     * @param n the node to start
     */
    private void startNode(final Element e, final Node n) {
        startNode(e.getLocation(), n);
    }

    /**
     * Start node.
     *
     * @param location the definition location
     * @param n        the node to start
     */
    private void startNode(final SourceLocation location, final Node n) {
        setNodeContext(location, n);
        stack.push(n);
    }


    /**
     * Supply context information to node.
     *
     * @param location the element location
     * @param n        the node to update
     */
    private void setNodeContext(final SourceLocation location, final Node n) {
        n.setSource(location);
        n.setBuilder(this);
        if (!definitionStack.isEmpty()) {
            n.setDefinition(topDefinition());
        }
    }

    /**
     * End node.
     *
     * @param <T> the node type
     * @param cls the class of node to end
     * @return a node that have been finished to be created
     */
    private <T extends Node> T endNode(final Class<T> cls) {
        final Node value = stack.pop();
        if (value.getClass() != cls) {
            throw new IllegalStateException("The top of type " + value.getClass().getName()
                    + " does not match expected " + cls.getName());
        }
        final T ct = cls.cast(value);
        if (stack.isEmpty()) {
            assert returnNode == null : "there already is return node " + returnNode;
            returnNode = ct;
        } else {
            processNode(ct);
        }
        return ct;
    }

    /**
     * Helper method that adds ct to current top node.
     *
     * @param ct the node to add
     */
    private void processNode(final Node ct) {
        final Node top = stack.peek();
        if (top instanceof GroupNode) {
            final GroupNode sn = (GroupNode) top;
            sn.nodes().add(ct);
        } else if (top instanceof ScopeNode) {
            final GroupNode sn = (GroupNode) ((ScopeNode) top).innerNode();
            sn.nodes().add(ct);
        } else {
            assert false : "unknown top node" + top;
        }
    }

    /**
     * Write current single node.
     *
     * @param e the element
     * @param n node to write
     */
    private void singleNode(final Element e, final Node n) {
        singleNode(e.getLocation(), n);
    }

    /**
     * Write current single node.
     *
     * @param location the definition location
     * @param n        node to write
     */
    private void singleNode(final SourceLocation location, final Node n) {
        setNodeContext(location, n);
        if (stack.isEmpty()) {
            assert returnNode == null : "there already is return node";
            returnNode = n;
        } else {
            processNode(n);
        }
    }


    /**
     * Start keyword scope node.
     *
     * @param e the element
     */
    public void startKeywords(final Element e) {
        startNode(e, new KeywordScopeNode());
    }

    /**
     * End keyword scope node.
     */
    public void endKeywords() {
        endNode(KeywordScopeNode.class);
    }

    /**
     * start a first choice node.
     *
     * @param e the element
     */
    public void startFirstChoice(final Element e) {
        startNode(e, new FirstChoiceNode());
    }

    /**
     * end the first choice node.
     *
     * @return just closed node
     */
    public FirstChoiceNode endFirstChoice() {
        return endNode(FirstChoiceNode.class);
    }

    /**
     * start a choice node.
     *
     * @param e the element
     */
    public void startChoice(final Element e) {
        startNode(e, new ChoiceNode());
    }

    /**
     * end the choice node.
     */
    public void endChoice() {
        endNode(ChoiceNode.class);
    }

    /**
     * start a repeat node.
     *
     * @param e the element
     */
    public void startRepeat(final Element e) {
        startNode(e, new RepeatNode());
    }

    /**
     * end the repeat node.
     */
    public void endRepeat() {
        endNode(RepeatNode.class);
    }

    /**
     * create a block node.
     *
     * @param e       the element
     * @param context a context of the block
     */
    public void block(final Element e, final DefinitionContext context) {
        singleNode(e, new BlockNode(context));
    }

    /**
     * Create a call node.
     *
     * @param e       the element
     * @param builder a builder for factory associated with the node
     */
    public void call(final Element e, final ActionBuilder builder) {
        final CallNode n = new CallNode(builder);
        singleNode(e, n);
    }

    /**
     * start an object node.
     *
     * @param e    the element
     * @param name the name of object
     */
    public void startObject(final Element e, final ObjectName name) {
        startNode(e, new ObjectNode(name, false, null));
    }

    /**
     * end the object node.
     *
     * @return the object node that have been created
     */
    public ObjectNode endObject() {
        return endNode(ObjectNode.class);
    }

    /**
     * start a property node.
     *
     * @param e      the element
     * @param name   the name of property
     * @param isList flag indicating if it is a list property
     */
    public void startProperty(final Element e, final PropertyName name, final boolean isList) {
        startProperty(e.getLocation(), name, isList);
    }

    /**
     * Start a property node.
     *
     * @param location the element location
     * @param name     the name of property
     * @param isList   flag indicating if it is a list property
     */
    private void startProperty(final SourceLocation location, final PropertyName name, final boolean isList) {
        startNode(location, new PropertyNode(name, isList, false));
    }

    /**
     * start a property node at mark.
     *
     * @param e      the element
     * @param name   the name of property
     * @param isList flag indicating if it is a list property
     */
    public void startPropertyAtMark(final Element e, final PropertyName name, final boolean isList) {
        startNode(e, new PropertyNode(name, isList, true));
    }

    /**
     * end the property node.
     */
    public void endProperty() {
        endNode(PropertyNode.class);
    }

    /**
     * start a sequence node.
     *
     * @param e the element
     */
    public void startSequence(final Element e) {
        startNode(e, new SequenceNode());
    }

    /**
     * end the sequence node.
     */
    public void endSequence() {
        endNode(SequenceNode.class);
    }

    /**
     * start an expression node.
     *
     * @param e       the element
     * @param context the context for the node
     */
    public void startExpression(final Element e, final ExpressionContext context) {
        startNode(e, new TermContextScope(Terms.EXPRESSION_START, Terms.EXPRESSION_END, context));
    }

    /**
     * end the expression node.
     */
    public void endExpression() {
        endNode(TermContextScope.class);
    }

    /**
     * start an attributes node.
     *
     * @param e       the element
     * @param context the context for the node
     */
    public void startAttributes(final Element e, final DefinitionInfo context) {
        startNode(e, new TermContextScope(Terms.ATTRIBUTES_START, Terms.ATTRIBUTES_END, context));
    }

    /**
     * end the attributes node.
     */
    public void endAttributes() {
        endNode(TermContextScope.class);
    }

    /**
     * start a doc comments node.
     *
     * @param e       the element
     * @param context the context for the node
     */
    public void startDocComment(final Element e, final DefinitionInfo context) {
        startNode(e, new TermContextScope(Terms.DOC_COMMENT_START, Terms.DOC_COMMENT_END, context));
    }

    /**
     * end the doc comments node.
     */
    public void endDocComment() {
        endNode(TermContextScope.class);
    }

    /**
     * start a modifiers node.
     *
     * @param e the element
     */
    public void startModifiers(final Element e) {
        startNode(e, new TermContextScope(Terms.MODIFIERS_START, Terms.MODIFIERS_END, null, false));
    }

    /**
     * end the modifiers node.
     */
    public void endModifiers() {
        endNode(TermContextScope.class);
    }

    /**
     * Create node that matches specified text.
     *
     * @param e     the element
     * @param kind  the term kind of node
     * @param role  the role of the node
     * @param token the text of the node (must be in the same source as element)
     */
    public void tokenText(final Element e, final Terms kind, final SyntaxRole role, final Token token) {
        final TokenKey key = token.key();
        switch (key.kind()) {
            case STRING:
            case INTEGER_WITH_SUFFIX:
            case FLOAT_WITH_SUFFIX:
            case IDENTIFIER:
            case INTEGER:
            case FLOAT:
            case GRAPHICS:
            case BRACKET:
            case COMMA:
                break;
            default:
                throw new IllegalArgumentException("Unexpected token kind: "
                        + token.key() + " for value: "
                        + token.text());
        }
        token(new SourceLocation(token.start(), token.end(), e.getLocation().systemId()),
                kind, role, key, token.text());
    }

    /**
     * Generic method that add token node.
     *
     * @param location the element location
     * @param kind     the term kind
     * @param role     the syntax role
     * @param tokenKey the token kind
     * @param text     the text of token
     */
    private void token(final SourceLocation location, final Terms kind, final SyntaxRole role,
                       final TokenKey tokenKey, final String text) {
        singleNode(location, new TokenNode(kind, role, tokenKey, text));
    }

    /**
     * A error node.
     *
     * @param e         the element
     * @param errorId   the error id
     * @param errorArgs the error arguments
     */
    public void error(final Element e, final String errorId, final Object... errorArgs) {
        singleNode(e, new ErrorNode(errorId, errorArgs));
    }

    /**
     * Create a node that matches specified token kind.
     *
     * @param e        the element
     * @param kind     the term kind of node
     * @param role     the role of the node
     * @param tokenKey the token kind
     */
    public void tokenText(final Element e, final Terms kind, final SyntaxRole role, final TokenKey tokenKey) {
        token(e.getLocation(), kind, role, tokenKey, null);
    }

    /**
     * Create node that matches specified token.
     *
     * @param e    the element
     * @param kind the term kind of node
     * @param role the role of the node
     */
    public void anyToken(final Element e, final Terms kind, final SyntaxRole role) {
        token(e.getLocation(), kind, role, null, null);
    }

    /**
     * Start marked region node.
     *
     * @param e the element
     */
    public void startMarked(final Element e) {
        startNode(e, new MarkedNode());
    }

    /**
     * End marked region node.
     */
    public void endMarked() {
        endNode(MarkedNode.class);
    }

    /**
     * Create commit mark node.
     *
     * @param e the element
     */
    public void commitMark(final Element e) {
        singleNode(e, new CommitMarkNode());
    }

    /**
     * Start compiling a definition.
     *
     * @param view the definition view to compile
     */
    public void startDefinition(final DefinitionView view) {
        definitionStack.add(view);
    }

    /**
     * Start a statement.
     *
     * @param e    the element
     * @param view the statement definition
     */
    public void startStatement(final Element e, final DefinitionView view) {
        startDefinition(view);
        startNode(e, new TermContextScope(Terms.STATEMENT_START, Terms.STATEMENT_END,
                view.definitionInfo(), TermContextScope.MarkMode.BEFORE_MARK));
    }

    /**
     * End the statement.
     */
    public void endStatement() {
        endNode(TermContextScope.class);
        endDefinition();
    }

    /**
     * End the definition.
     */
    public void endDefinition() {
        definitionStack.remove(definitionStack.size() - 1);
    }

    /**
     * Start an object at mark.
     *
     * @param e        the element
     * @param name     the object to start
     * @param wrappers the wrappers for this object node
     */
    public void startObjectAtMark(final Element e, final ObjectName name, final WrapperLink wrappers) {
        startNode(e, new ObjectNode(name, true, wrappers));
    }

    /**
     * Start fallback scope, the scope has to be initialized at some time.
     *
     * @param e the element
     * @return the created node
     */
    public FallbackObjectNode startFallbackScope(final Element e) {
        final FallbackObjectNode rc = new FallbackObjectNode();
        startNode(e, rc);
        return rc;
    }

    /**
     * End fallback scope.
     */
    public void endFallbackScope() {
        endNode(FallbackObjectNode.class);
    }

    /**
     * Start disable soft ends.
     *
     * @param e the element
     */
    public void startDisableSoftEnds(final Element e) {
        startNode(e, new DisableSoftEndsNode());
    }

    /**
     * End fallback scope.
     */
    public void endDisableSoftEnds() {
        endNode(DisableSoftEndsNode.class);
    }


    /**
     * Start object at mark.
     *
     * @param e    the element
     * @param name the object to start
     */
    public void startObjectAtMark(final Element e, final ObjectName name) {
        startNode(e, new ObjectNode(name, true, null));
    }

    /**
     * Start wrapper.
     *
     * @param w the wrapper to start
     */
    public void startWrapper(final Wrapper w) {
        if (w != null) {
            final DefinitionView d = topDefinition();
            startObject(w.getObject(), d.convertName(w.getObject()));
            startProperty(new SourceLocation(w.getProperty().start(), w.getProperty().end(),
                            w.getLocation().systemId()),
                    new PropertyName(w.getProperty().text()), false);
        }
    }

    /**
     * End wrapper.
     *
     * @param w the wrapper to end
     */
    public void endWrapper(final Wrapper w) {
        if (w != null) {
            endProperty();
            endObject();
        }
    }

    /**
     * @return top definition on definition stack
     */
    public DefinitionView topDefinition() {
        if (definitionStack.isEmpty()) {
            throw new IllegalStateException(
                    "[BUG]Obtaining definition when no definition is active.");
        } else {
            return definitionStack.get(definitionStack.size() - 1);
        }
    }

    /**
     * @return the state factory factory created using this builder
     */
    public ActionStateFactory targetFactory() {
        if (targetFactory == null) {
            throw new IllegalStateException("The target factory is not yet built!");
        }
        return this.targetFactory;
    }

    /**
     * @return context associated with builder
     */
    public DefinitionContext context() {
        return this.contextBuilder.termContext();
    }

    /**
     * Build lookahead.
     *
     * @param visitedBuilders the list of visited builders
     * @return built look ahead
     */
    public LookAheadSet buildLookAhead(final Set<ActionBuilder> visitedBuilders) {
        if (visitedBuilders.add(this)) {
            try {
                return returnNode.buildLookAhead(visitedBuilders);
            } finally {
                visitedBuilders.remove(this);
            }
        } else {
            final GrammarView v = contextBuilder.grammarBuilder().grammarView();
            final Element e = v.getGrammar();
            contextBuilder.error(this, e, "grammar.Context.cyclicContext", contextBuilder.name(), v.getSystemId());
            return new LookAheadSet();
        }

    }

    /**
     * Build state machine.
     */
    public void buildActions() {
        returnNode = returnNode.flatten();
        returnNode = returnNode.inferSoftBreaks();
        targetFactory = new ActionStateFactory(
                returnNode.buildActions(this,
                        new ReturnAction(returnNode.getSource(), true),
                        new ReturnAction(returnNode.getSource(), false),
                        RecoveryVoteAction.NO_RECOVERY));
        for (final CallAction referrer : referrers) {
            referrer.setStateFactory(targetFactory);
        }
        referrers.clear();
    }

    /**
     * @return a context builder that created this state machine
     */
    public ContextBuilder contextBuilder() {
        return contextBuilder;
    }

    /**
     * @return the linker for context builder
     */
    public ActionLinker getLinker() {
        return contextBuilder.getLinker();
    }

    /**
     * Collect keywords directly visible to the caller, keywords inside the block are not collected.
     *
     * @param keywords the keywords
     * @param visited  the visited builders
     */
    public void collectKeywords(final Set<Keyword> keywords, final Set<ActionBuilder> visited) {
        if (visited.contains(this)) {
            return;
        }
        visited.add(this);
        returnNode.collectKeywords(keywords, visited);
    }

    /**
     * Link to the call node.
     *
     * @param callNode the call node
     */
    public void link(final CallAction callNode) {
        if (targetFactory != null) {
            callNode.setStateFactory(targetFactory);
        } else {
            referrers.add(callNode);
        }
    }
}
