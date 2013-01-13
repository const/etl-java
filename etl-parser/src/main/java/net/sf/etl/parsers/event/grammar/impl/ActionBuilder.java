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
    private void startNode(Node n) {
        setNodeContext(n);
        stack.push(n);
    }

    /**
     * Supply context information to node
     *
     * @param n the node to update
     */
    private void setNodeContext(Node n) {
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
        // TODO for recovery sequences and choices need to be flattened, since recovery will recover only to next node in the sequence
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
     * @param n node to write
     */
    private void singleNode(Node n) {
        setNodeContext(n);
        if (stack.isEmpty()) {
            assert returnNode == null : "there already is return node";
            returnNode = n;
        } else {
            processNode(n);
        }
    }


    /**
     * Start keyword scope node
     */
    public void startKeywords() {
        startNode(new KeywordScopeNode());
    }

    /**
     * End keyword scope node
     */
    public void endKeywords() {
        endNode(KeywordScopeNode.class);
    }

    /**
     * start choice node
     */
    public void startFirstChoice() {
        startNode(new FirstChoiceNode());
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
     */
    public void startChoice() {
        startNode(new ChoiceNode());
    }

    /**
     * end choice node
     */
    public void endChoice() {
        endNode(ChoiceNode.class);
    }

    /**
     * start repeat node
     */
    public void startRepeat() {
        startNode(new RepeatNode());
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
     * @param context a context of the block
     */
    public void startBlock(DefinitionContext context, SourceLocation source) {
        // TODO fix it, it is non scope node
        final BlockNode n = new BlockNode(context);
        n.source = source;
        startNode(n);
    }

    /**
     * end repeat node
     */
    public void endBlock() {
        endNode(BlockNode.class);
    }

    /**
     * Create call node
     *
     * @param builder a builder for factory associated with the node
     */
    public void call(ActionBuilder builder, SourceLocation location) {
        final CallNode n = new CallNode(builder);
        n.source = location;
        singleNode(n);
    }

    /**
     * start object node
     *
     * @param name the name of object
     */
    public void startObject(ObjectName name) {
        startNode(new ObjectNode(name, false, null));
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
     * @param name   the name of property
     * @param isList flag indicating if it is a list property
     */
    public void startProperty(PropertyName name, boolean isList) {
        startNode(new PropertyNode(name, isList, false));
    }

    /**
     * start property node at mark
     *
     * @param name   the name of property
     * @param isList flag indicating if it is a list property
     */
    public void startPropertyAtMark(PropertyName name, boolean isList) {
        startNode(new PropertyNode(name, isList, true));
    }

    /**
     * end property node
     */
    public void endProperty() {
        endNode(PropertyNode.class);
    }

    /**
     * start sequence node
     */
    public void startSequence() {
        startNode(new SequenceNode());
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
     * @param context the context for the node
     */
    public void startExpression(ExpressionContext context) {
        startNode(new TermContextScope(Terms.EXPRESSION_START, Terms.EXPRESSION_END, context));
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
     * @param context the context for the node
     */
    public void startAttributes(DefinitionInfo context) {
        startNode(new TermContextScope(Terms.ATTRIBUTES_START, Terms.ATTRIBUTES_END, context));
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
     * @param context the context for the node
     */
    public void startDocComment(DefinitionInfo context) {
        startNode(new TermContextScope(Terms.DOC_COMMENT_START, Terms.DOC_COMMENT_END, context));
    }

    /**
     * end attributes node
     */
    public void endDocComment() {
        endNode(TermContextScope.class);
    }

    /**
     * start modifiers node
     */
    public void startModifiers() {
        startNode(new TermContextScope(Terms.MODIFIERS_START, Terms.MODIFIERS_END, null));
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
     * @param kind  the term kind of node
     * @param role  the role of the node
     * @param token the text of the node
     */
    public void tokenText(Terms kind, SyntaxRole role, Token token) {
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
        token(kind, role, key, token.text());
    }

    /**
     * Generic method that add token node
     *
     * @param kind     the term kind
     * @param role     the syntax role
     * @param tokenKey the token kind
     * @param text     the text of token
     */
    private void token(Terms kind, SyntaxRole role, TokenKey tokenKey, String text) {
        singleNode(new TokenNode(kind, role, tokenKey, text));
    }

    /**
     * A error node
     *
     * @param errorId   the error id
     * @param errorArgs the error arguments
     */
    public void error(String errorId, Object... errorArgs) {
        singleNode(new ErrorNode(errorId, errorArgs));
    }

    /**
     * Create node that matches specified token kind
     *
     * @param kind     the term kind of node
     * @param role     the role of the node
     * @param tokenKey the token kind
     */
    public void tokenText(Terms kind, SyntaxRole role, TokenKey tokenKey) {
        token(kind, role, tokenKey, null);
    }

    /**
     * Create node that matches specified token
     *
     * @param kind the term kind of node
     * @param role the role of the node
     */
    public void anyToken(Terms kind, SyntaxRole role) {
        token(kind, role, null, null);
    }

    /**
     * start marked region node
     */
    public void startMarked() {
        startNode(new MarkedNode());
    }

    /**
     * end marked region node
     */
    public void endMarked() {
        endNode(MarkedNode.class);
    }

    /**
     * create commit mark node
     */
    public void commitMark() {
        singleNode(new CommitMarkNode());
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
     * @param view the statement definition
     */
    public void startStatement(DefinitionView view) {
        startDefinition(view);
        startNode(new TermContextScope(Terms.STATEMENT_START, Terms.STATEMENT_END, view.definitionInfo(), TermContextScope.MarkMode.BEFORE_MARK));
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
     * @param name     the object to start
     * @param wrappers the wrappers for this object node
     */
    public void startObjectAtMark(ObjectName name, WrapperLink wrappers) {
        startNode(new ObjectNode(name, true, wrappers));
    }

    /**
     * Start fallback scope, the scope has to be initialized at some time
     *
     * @return the created node
     */
    public FallbackObjectNode startFallbackScope() {
        final FallbackObjectNode rc = new FallbackObjectNode();
        startNode(rc);
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
     * @param name the object to start
     */
    public void startObjectAtMark(ObjectName name) {
        startNode(new ObjectNode(name, true, null));
    }

    /**
     * Start wrapper
     *
     * @param w the wrapper to start
     */
    public void startWrapper(Wrapper w) {
        if (w != null) {
            final DefinitionView d = topDefinition();
            startObject(d.convertName(w.object));
            startProperty(new PropertyName(w.property.text()), false);
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
