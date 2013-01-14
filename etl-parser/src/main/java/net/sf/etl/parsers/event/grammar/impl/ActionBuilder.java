/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2013 Constantine A Plotnikov
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
package net.sf.etl.parsers.event.grammar.impl;

import net.sf.etl.parsers.*;
import net.sf.etl.parsers.event.grammar.Keyword;
import net.sf.etl.parsers.event.grammar.LookAheadSet;
import net.sf.etl.parsers.event.grammar.impl.flattened.DefinitionView;
import net.sf.etl.parsers.event.grammar.impl.flattened.GrammarView;
import net.sf.etl.parsers.event.grammar.impl.flattened.WrapperLink;
import net.sf.etl.parsers.event.grammar.impl.nodes.*;
import net.sf.etl.parsers.event.impl.term.action.ActionStateFactory;
import net.sf.etl.parsers.event.impl.term.action.CallAction;
import net.sf.etl.parsers.event.impl.term.action.ReturnAction;
import net.sf.etl.parsers.event.impl.term.action.buildtime.ActionLinker;
import net.sf.etl.parsers.event.impl.util.ListStack;
import net.sf.etl.parsers.event.unstable.model.grammar.Element;
import net.sf.etl.parsers.event.unstable.model.grammar.Wrapper;

import java.util.ArrayList;
import java.util.Set;

/**
 * This utility class is used to build state machines. Its methods use terms of
 * LL1 grammar. However it has strong dependency on grammar AST and compiler
 * classes.
 *
 * @author const
 */
public class ActionBuilder {
    /**
     * the stack of nodes
     */
    private final ListStack<Node> stack = new ListStack<Node>();
    /**
     * the node to return
     */
    private Node returnNode;
    /**
     * the stack of definitions
     */
    private final ArrayList<DefinitionView> definitionStack = new ArrayList<DefinitionView>();
    /**
     * the context compiler, it is used for error reporting
     */
    private final ContextBuilder contextBuilder;
    /**
     * The state factory
     */
    private ActionStateFactory targetFactory;
    /**
     * The call nodes that reference this state builder factory
     */
    private final ArrayList<CallAction> referrers = new ArrayList<CallAction>();

    /**
     * A constructor for builder
     *
     * @param contextBuilder the parent builder
     */
    public ActionBuilder(ContextBuilder contextBuilder) {
        this.contextBuilder = contextBuilder;
    }

    /**
     * Start node
     *
     * @param n the node to start
     */
    private void startNode(Element e, Node n) {
        startNode(e.location, n);
    }

    /**
     * Start node
     *
     * @param n the node to start
     */
    private void startNode(SourceLocation location, Node n) {
        setNodeContext(location, n);
        stack.push(n);
    }


    /**
     * Supply context information to node
     *
     * @param location the element location
     * @param n        the node to update
     */
    private void setNodeContext(SourceLocation location, Node n) {
        n.source = location;
        n.setBuilder(this);
        if (!definitionStack.isEmpty()) {
            n.setDefinition(topDefinition());
        }
    }

    /**
     * End node
     *
     * @param <T> the node type
     * @param cls the class of node to end
     * @return a node that have been finished to be created
     */
    private <T extends Node> T endNode(Class<T> cls) {
        final Node value = stack.pop();
        if (value.getClass() != cls) {
            throw new IllegalStateException("The top of type " + value.getClass().getName() +
                    " does not match expected " + cls.getName());
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
     * Helper method that adds ct to current top node
     *
     * @param ct the node to add
     */
    private void processNode(Node ct) {
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
     * Write current single node
     *
     * @param e the element
     * @param n node to write
     */
    private void singleNode(Element e, Node n) {
        singleNode(e.location, n);
    }

    private void singleNode(SourceLocation location, Node n) {
        setNodeContext(location, n);
        if (stack.isEmpty()) {
            assert returnNode == null : "there already is return node";
            returnNode = n;
        } else {
            processNode(n);
        }
    }


    /**
     * Start keyword scope node
     *
     * @param e the element
     */
    public void startKeywords(Element e) {
        startNode(e, new KeywordScopeNode());
    }

    /**
     * End keyword scope node
     */
    public void endKeywords() {
        endNode(KeywordScopeNode.class);
    }

    /**
     * start choice node
     *
     * @param e the element
     */
    public void startFirstChoice(Element e) {
        startNode(e, new FirstChoiceNode());
    }

    /**
     * end choice node
     *
     * @return just closed node
     */
    public FirstChoiceNode endFirstChoice() {
        return endNode(FirstChoiceNode.class);
    }

    /**
     * start choice node
     *
     * @param e the element
     */
    public void startChoice(Element e) {
        startNode(e, new ChoiceNode());
    }

    /**
     * end choice node
     */
    public void endChoice() {
        endNode(ChoiceNode.class);
    }

    /**
     * start repeat node
     *
     * @param e the element
     */
    public void startRepeat(Element e) {
        startNode(e, new RepeatNode());
    }

    /**
     * end repeat node
     */
    public void endRepeat() {
        endNode(RepeatNode.class);
    }

    /**
     * start repeat node
     *
     * @param e       the element
     * @param context a context of the block
     */
    public void block(Element e, DefinitionContext context) {
        singleNode(e, new BlockNode(context));
    }

    /**
     * Create call node
     *
     * @param e       the element
     * @param builder a builder for factory associated with the node
     */
    public void call(Element e, ActionBuilder builder) {
        final CallNode n = new CallNode(builder);
        singleNode(e, n);
    }

    /**
     * start object node
     *
     * @param e    the element
     * @param name the name of object
     */
    public void startObject(Element e, ObjectName name) {
        startNode(e, new ObjectNode(name, false, null));
    }

    /**
     * end object node
     *
     * @return the object node that have been created
     */
    public ObjectNode endObject() {
        return endNode(ObjectNode.class);
    }

    /**
     * start property node
     *
     * @param e      the element
     * @param name   the name of property
     * @param isList flag indicating if it is a list property
     */
    public void startProperty(Element e, PropertyName name, boolean isList) {
        startProperty(e.location, name, isList);
    }

    /**
     * Start property node
     *
     * @param location the element location
     * @param name     the name of property
     * @param isList   flag indicating if it is a list property
     */
    private void startProperty(SourceLocation location, PropertyName name, boolean isList) {
        startNode(location, new PropertyNode(name, isList, false));
    }

    /**
     * start property node at mark
     *
     * @param e      the element
     * @param name   the name of property
     * @param isList flag indicating if it is a list property
     */
    public void startPropertyAtMark(Element e, PropertyName name, boolean isList) {
        startNode(e, new PropertyNode(name, isList, true));
    }

    /**
     * end property node
     */
    public void endProperty() {
        endNode(PropertyNode.class);
    }

    /**
     * start sequence node
     *
     * @param e the element
     */
    public void startSequence(Element e) {
        startNode(e, new SequenceNode());
    }

    /**
     * end sequence node
     */
    public void endSequence() {
        endNode(SequenceNode.class);
    }

    /**
     * start expression node
     *
     * @param e       the element
     * @param context the context for the node
     */
    public void startExpression(Element e, ExpressionContext context) {
        startNode(e, new TermContextScope(Terms.EXPRESSION_START, Terms.EXPRESSION_END, context));
    }

    /**
     * end expression node
     */
    public void endExpression() {
        endNode(TermContextScope.class);
    }

    /**
     * start attributes node
     *
     * @param e       the element
     * @param context the context for the node
     */
    public void startAttributes(Element e, DefinitionInfo context) {
        startNode(e, new TermContextScope(Terms.ATTRIBUTES_START, Terms.ATTRIBUTES_END, context));
    }

    /**
     * end attributes node
     */
    public void endAttributes() {
        endNode(TermContextScope.class);
    }

    /**
     * start attributes node
     *
     * @param e       the element
     * @param context the context for the node
     */
    public void startDocComment(Element e, DefinitionInfo context) {
        startNode(e, new TermContextScope(Terms.DOC_COMMENT_START, Terms.DOC_COMMENT_END, context));
    }

    /**
     * end attributes node
     */
    public void endDocComment() {
        endNode(TermContextScope.class);
    }

    /**
     * start modifiers node
     *
     * @param e the element
     */
    public void startModifiers(Element e) {
        startNode(e, new TermContextScope(Terms.MODIFIERS_START, Terms.MODIFIERS_END, null));
    }

    /**
     * end modifiers node
     */
    public void endModifiers() {
        endNode(TermContextScope.class);
    }

    /**
     * Create node that matches specified text
     *
     * @param e     the element
     * @param kind  the term kind of node
     * @param role  the role of the node
     * @param token the text of the node (must be in the same source as element)
     */
    public void tokenText(Element e, Terms kind, SyntaxRole role, Token token) {
        TokenKey key = token.key();
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
        token(new SourceLocation(token.start(), token.end(), e.location.systemId()), kind, role, key, token.text());
    }

    /**
     * Generic method that add token node
     *
     * @param location the element location
     * @param kind     the term kind
     * @param role     the syntax role
     * @param tokenKey the token kind
     * @param text     the text of token
     */
    private void token(SourceLocation location, Terms kind, SyntaxRole role, TokenKey tokenKey, String text) {
        singleNode(location, new TokenNode(kind, role, tokenKey, text));
    }

    /**
     * A error node
     *
     * @param errorId   the error id
     * @param errorArgs the error arguments
     */
    public void error(Element e, String errorId, Object... errorArgs) {
        singleNode(e, new ErrorNode(errorId, errorArgs));
    }

    /**
     * Create node that matches specified token kind
     *
     * @param e        the element
     * @param kind     the term kind of node
     * @param role     the role of the node
     * @param tokenKey the token kind
     */
    public void tokenText(Element e, Terms kind, SyntaxRole role, TokenKey tokenKey) {
        token(e.location, kind, role, tokenKey, null);
    }

    /**
     * Create node that matches specified token
     *
     * @param e    the element
     * @param kind the term kind of node
     * @param role the role of the node
     */
    public void anyToken(Element e, Terms kind, SyntaxRole role) {
        token(e.location, kind, role, null, null);
    }

    /**
     * start marked region node
     *
     * @param e the element
     */
    public void startMarked(Element e) {
        startNode(e, new MarkedNode());
    }

    /**
     * end marked region node
     */
    public void endMarked() {
        endNode(MarkedNode.class);
    }

    /**
     * create commit mark node
     *
     * @param e the element
     */
    public void commitMark(Element e) {
        singleNode(e, new CommitMarkNode());
    }

    /**
     * Start compiling definition
     *
     * @param view the definition view to compile
     */
    public void startDefinition(DefinitionView view) {
        definitionStack.add(view);
    }

    /**
     * Start statement
     *
     * @param e    the element
     * @param view the statement definition
     */
    public void startStatement(Element e, DefinitionView view) {
        startDefinition(view);
        startNode(e, new TermContextScope(Terms.STATEMENT_START, Terms.STATEMENT_END, view.definitionInfo(), TermContextScope.MarkMode.BEFORE_MARK));
    }

    /**
     * End statement
     */
    public void endStatement() {
        endNode(TermContextScope.class);
        endDefinition();
    }

    /**
     * The end definition
     */
    public void endDefinition() {
        definitionStack.remove(definitionStack.size() - 1);
    }

    /**
     * Start object at mark
     *
     * @param e        the element
     * @param name     the object to start
     * @param wrappers the wrappers for this object node
     */
    public void startObjectAtMark(Element e, ObjectName name, WrapperLink wrappers) {
        startNode(e, new ObjectNode(name, true, wrappers));
    }

    /**
     * Start fallback scope, the scope has to be initialized at some time
     *
     * @param e the element
     * @return the created node
     */
    public FallbackObjectNode startFallbackScope(Element e) {
        final FallbackObjectNode rc = new FallbackObjectNode();
        startNode(e, rc);
        return rc;
    }

    /**
     * End fallback scope
     */
    public void endFallbackScope() {
        endNode(FallbackObjectNode.class);
    }

    /**
     * Start object at mark
     *
     * @param e    the element
     * @param name the object to start
     */
    public void startObjectAtMark(Element e, ObjectName name) {
        startNode(e, new ObjectNode(name, true, null));
    }

    /**
     * Start wrapper
     *
     * @param w the wrapper to start
     */
    public void startWrapper(Wrapper w) {
        if (w != null) {
            final DefinitionView d = topDefinition();
            startObject(w.object, d.convertName(w.object));
            startProperty(new SourceLocation(w.property.start(), w.property.end(), w.location.systemId()),
                    new PropertyName(w.property.text()), false);
        }
    }

    /**
     * End wrapper
     *
     * @param w the wrapper to end
     */
    public void endWrapper(Wrapper w) {
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
     * Build lookahead
     *
     * @param visitedBuilders the list of visited builders
     * @return built look ahead
     */
    public LookAheadSet buildLookAhead(Set<ActionBuilder> visitedBuilders) {
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
     * Build state machine
     */
    public void buildActions() {
        returnNode = returnNode.flatten();
        targetFactory = new ActionStateFactory(returnNode.buildActions(this, new ReturnAction(true), new ReturnAction(false)));
        for (CallAction referrer : referrers) {
            referrer.stateFactory = targetFactory;
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
     * Collect keywords directly visible to the caller, keywords inside the block are not collected
     *
     * @param keywords the keywords
     * @param visited  the visited builders
     */
    public void collectKeywords(Set<Keyword> keywords, Set<ActionBuilder> visited) {
        if (visited.contains(this)) {
            return;
        }
        visited.add(this);
        returnNode.collectKeywords(keywords, visited);
    }

    /**
     * Link to the call node
     *
     * @param callNode the call node
     */
    public void link(CallAction callNode) {
        if (targetFactory != null) {
            callNode.stateFactory = targetFactory;
        } else {
            referrers.add(callNode);
        }
    }
}
