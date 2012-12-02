/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2012 Constantine A Plotnikov
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

package net.sf.etl.parsers.event.impl.term;

import net.sf.etl.parsers.*;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.grammar.TermParserStateFactory;
import net.sf.etl.parsers.event.impl.term.action.*;
import net.sf.etl.parsers.event.impl.term.action.buildtime.ActionLinker;
import net.sf.etl.parsers.event.impl.term.action.buildtime.UnreachableAction;
import net.sf.etl.parsers.resource.ResourceDescriptor;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of the default grammar that is used when actual grammar could not be found
 */
public class DefaultCompiledGrammar implements CompiledGrammar {
    /**
     * The grammar instance
     */
    public static final CompiledGrammar INSTANCE = new DefaultCompiledGrammar();
    /**
     * Statement contexts for the grammar
     */
    private final static List<DefinitionContext> STATEMENT_CONTEXTS = Collections.singletonList(StandardGrammars.DEFAULT_GRAMMAR_CONTEXT);
    /**
     * Factory instance
     */
    private final static TermParserStateFactory DEFAULT_GRAMMAR_STATE_FACTORY = makeStateFactory();

    /**
     * @return make state factory
     */
    private static TermParserStateFactory makeStateFactory() {
        ActionLinker actionLinker = new ActionLinker();
        ActionStateFactory statement = new ActionStateFactory(createStatement(actionLinker));
        StatementSequenceStateFactory statements = new StatementSequenceStateFactory(statement);
        BlockStateFactory block = new BlockStateFactory(StandardGrammars.DEFAULT_GRAMMAR_CONTEXT, statements);
        actionLinker.resolveBlock(StandardGrammars.DEFAULT_GRAMMAR_CONTEXT, block);
        return statements;
    }

    @Override
    public List<CompiledGrammar> getOtherGrammars() {
        return Collections.emptyList();
    }

    @Override
    public ResourceDescriptor getDescriptor() {
        return new ResourceDescriptor(StandardGrammars.DEFAULT_GRAMMAR_SYSTEM_ID, GRAMMAR_REQUEST_TYPE, null);
    }

    @Override
    public ErrorInfo getErrors() {
        return null;
    }

    @Override
    public DefinitionContext getDefaultContext() {
        return StandardGrammars.DEFAULT_GRAMMAR_CONTEXT;
    }

    @Override
    public List<DefinitionContext> getStatementContexts() {
        return STATEMENT_CONTEXTS;
    }

    @Override
    public List<ExpressionContext> getExpressionContexts() {
        return Collections.emptyList();
    }

    @Override
    public TermParserStateFactory statementSequenceParser() {
        return DEFAULT_GRAMMAR_STATE_FACTORY;
    }

    @Override
    public TermParserStateFactory statementSequenceParser(DefinitionContext context) {
        if (context.equals(getDefaultContext()))
            return DEFAULT_GRAMMAR_STATE_FACTORY;
        throw new IllegalArgumentException("Unknown context: " + context);
    }

    @Override
    public TermParserStateFactory expressionParser(ExpressionContext context) {
        throw new IllegalArgumentException("Unknown context: " + context);
    }


    private static Action createStatement(ActionLinker linker) {
        StructuralTokenAction stStart = new StructuralTokenAction(Terms.STATEMENT_START, StandardGrammars.DEFAULT_GRAMMAR_STATEMENT_DEFINITION_INFO);
        StructuralTokenAction start = new StructuralTokenAction(Terms.OBJECT_START, StandardGrammars.DEFAULT_GRAMMAR_STATEMENT);
        stStart.next = start;
        // parse documentation comments if present
        TokensChoiceAction docTest = new TokensChoiceAction();
        start.next = docTest;
        DisableSoftEndAction disableSoftEnd = new DisableSoftEndAction();
        docTest.next.put(Tokens.DOC_COMMENT, disableSoftEnd);
        StructuralTokenAction docInfoStart = new StructuralTokenAction(Terms.DOC_COMMENT_START, StandardGrammars.DEFAULT_GRAMMAR_DOCUMENTATION_DEFINITION_INFO);
        disableSoftEnd.next = docInfoStart;
        StructuralTokenAction docsValuesStart = new StructuralTokenAction(Terms.LIST_PROPERTY_START, StandardGrammars.DEFAULT_GRAMMAR_STATEMENT_DOCUMENTATION);
        docInfoStart.next = docsValuesStart;
        StructuralTokenAction docsValueObjectStart = new StructuralTokenAction(Terms.OBJECT_START, StandardGrammars.DEFAULT_GRAMMAR_STATEMENT_DOCUMENTATION_OBJECT);
        docsValuesStart.next = docsValueObjectStart;
        StructuralTokenAction docsValueObjectPropStart = new StructuralTokenAction(Terms.PROPERTY_START, StandardGrammars.DEFAULT_GRAMMAR_STATEMENT_DOCUMENTATION_VALUE);
        docsValueObjectStart.next = docsValueObjectPropStart;
        ReportTokenAction reportDoc = new ReportTokenAction(Terms.VALUE, SyntaxRole.DOCUMENTATION);
        docsValueObjectPropStart.next = reportDoc;
        AdvanceAction advanceDoc = new AdvanceAction(false);
        reportDoc.next = advanceDoc;
        StructuralTokenAction docsValueObjectPropEnd = new StructuralTokenAction(Terms.PROPERTY_END, StandardGrammars.DEFAULT_GRAMMAR_STATEMENT_DOCUMENTATION_VALUE);
        advanceDoc.next = docsValueObjectPropEnd;
        StructuralTokenAction docsValueObjectEnd = new StructuralTokenAction(Terms.OBJECT_END, StandardGrammars.DEFAULT_GRAMMAR_STATEMENT_DOCUMENTATION);
        docsValueObjectPropEnd.next = docsValueObjectEnd;
        TokensChoiceAction docsEndTest = new TokensChoiceAction();
        docsValueObjectEnd.next = docsEndTest;
        docsEndTest.next.put(Tokens.DOC_COMMENT, docsValueObjectStart);
        StructuralTokenAction docsValuesEnd = new StructuralTokenAction(Terms.LIST_PROPERTY_END, StandardGrammars.DEFAULT_GRAMMAR_STATEMENT_DOCUMENTATION);
        docsEndTest.fallback = docsValuesEnd;
        StructuralTokenAction docInfoEnd = new StructuralTokenAction(Terms.DOC_COMMENT_END, StandardGrammars.DEFAULT_GRAMMAR_DOCUMENTATION_DEFINITION_INFO);
        docsValuesEnd.next = docInfoEnd;
        EnableSoftEndAction enableSoftEnd = new EnableSoftEndAction();
        docInfoEnd.next = enableSoftEnd;
        // parse values
        StructuralTokenAction contentStart = new StructuralTokenAction(Terms.LIST_PROPERTY_START, StandardGrammars.DEFAULT_GRAMMAR_STATEMENT_CONTENT);
        enableSoftEnd.next = contentStart;
        docTest.fallback = contentStart;
        PhraseTokenChoiceAction contentChoiceAction = new PhraseTokenChoiceAction();
        contentStart.next = contentChoiceAction;
        // values block
        StructuralTokenAction contentTokensStart = new StructuralTokenAction(Terms.OBJECT_START, StandardGrammars.DEFAULT_GRAMMAR_TOKENS);
        contentChoiceAction.next.put(PhraseTokens.SIGNIFICANT, contentTokensStart);
        StructuralTokenAction contentTokensValuesStart = new StructuralTokenAction(Terms.LIST_PROPERTY_START, StandardGrammars.DEFAULT_GRAMMAR_TOKENS_VALUES);
        contentTokensStart.next = contentTokensValuesStart;
        ReportTokenAction reportContentToken = new ReportTokenAction(Terms.VALUE, SyntaxRole.PRIMARY);
        contentTokensValuesStart.next = reportContentToken;
        AdvanceAction advanceAction = new AdvanceAction();
        reportContentToken.next = advanceAction;
        PhraseTokenChoiceAction contentTokensChoiceAction = new PhraseTokenChoiceAction();
        advanceAction.next = contentTokensChoiceAction;
        contentTokensChoiceAction.next.put(PhraseTokens.SIGNIFICANT, reportContentToken);
        StructuralTokenAction contentTokensValuesEnd = new StructuralTokenAction(Terms.LIST_PROPERTY_START, StandardGrammars.DEFAULT_GRAMMAR_TOKENS_VALUES);
        contentTokensChoiceAction.fallback = contentTokensValuesEnd;
        StructuralTokenAction contentTokensEnd = new StructuralTokenAction(Terms.OBJECT_END, StandardGrammars.DEFAULT_GRAMMAR_TOKENS);
        contentTokensValuesEnd.next = contentTokensEnd;
        contentTokensEnd.next = contentChoiceAction;
        // block
        StructuralTokenAction blockObjectStart = new StructuralTokenAction(Terms.OBJECT_START, StandardGrammars.DEFAULT_GRAMMAR_BLOCK);
        contentTokensChoiceAction.next.put(PhraseTokens.START_BLOCK, reportContentToken);
        StructuralTokenAction blockObjectContentStart = new StructuralTokenAction(Terms.LIST_PROPERTY_START, StandardGrammars.DEFAULT_GRAMMAR_BLOCK_CONTENT);
        blockObjectStart.next = blockObjectContentStart;
        CallAction blockCall = new CallAction();
        blockObjectContentStart.next = blockCall;
        blockCall.failure = new UnreachableAction();
        linker.linkBlock(blockCall, StandardGrammars.DEFAULT_GRAMMAR_CONTEXT);
        StructuralTokenAction blockObjectContentEnd = new StructuralTokenAction(Terms.LIST_PROPERTY_END, StandardGrammars.DEFAULT_GRAMMAR_BLOCK_CONTENT);
        blockCall.success = blockObjectContentEnd;
        StructuralTokenAction blockObjectEnd = new StructuralTokenAction(Terms.OBJECT_END, StandardGrammars.DEFAULT_GRAMMAR_BLOCK);
        blockObjectContentEnd.next = blockObjectEnd;
        blockObjectEnd.next = contentChoiceAction;
        // end content
        StructuralTokenAction contentEnd = new StructuralTokenAction(Terms.LIST_PROPERTY_END, StandardGrammars.DEFAULT_GRAMMAR_STATEMENT_CONTENT);
        contentChoiceAction.fallback = contentEnd;
        SimpleAction end = new StructuralTokenAction(Terms.OBJECT_END, StandardGrammars.DEFAULT_GRAMMAR_STATEMENT);
        contentEnd.next = end;
        StructuralTokenAction stEnd = new StructuralTokenAction(Terms.STATEMENT_END, StandardGrammars.DEFAULT_GRAMMAR_STATEMENT_DEFINITION_INFO);
        end.next = stEnd;
        stEnd.next = new ReturnAction();
        return stStart;
    }

}
