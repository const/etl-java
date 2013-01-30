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

package net.sf.etl.parsers.event.grammar;

import net.sf.etl.parsers.DefinitionContext;
import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.ExpressionContext;
import net.sf.etl.parsers.resource.ResourceDescriptor;

import java.util.List;

/**
 * <p>The compiled grammar that could be used to create parsers. This grammar is a result of compilation
 * of grammar AST trees using {@link GrammarCompilerEngine}.</p>
 * <p>The object is immutable and serializable. However serialized objects of this class will not be compatible
 * with future minor versions of the the library and even between different versions of JDK. So use serialization
 * only for caching purposes.</p>
 */
public interface CompiledGrammar {

    /**
     * Get other compiled grammars that were produced as result of compilation of this grammar
     * because they were referenced from this grammar. These grammars could be also registered
     * in the cache. Other grammars usually have ResourceDescriptor that is subset of this grammar
     * resource descriptor, but in case of mutually referencing grammars it is not so.
     *
     * @return other compiled grammars
     */
    List<CompiledGrammar> getOtherGrammars();

    /**
     * @return the descriptor for this grammar
     */
    ResourceDescriptor getDescriptor();

    /**
     * @return the list of errors associated with this grammar. Note that even if grammar errors present,
     *         the parsing could be partially done by supplied parsers. Erroneous contexts will just have
     *         default parser associated with them.
     */
    ErrorInfo getErrors();

    /**
     * @return the default context for this grammar
     */
    DefinitionContext getDefaultContext();

    /**
     * @return get all available statement contexts
     */
    List<DefinitionContext> getStatementContexts();

    /**
     * @return expression contexts (for all actually used combinations of host contexts and expression contexts,
     *         and actually defined precedence levels)
     */
    List<ExpressionContext> getExpressionContexts();

    /**
     * Get keyword context for the definition context. This keyword context could be used for syntax highlighters and
     * to check if name conflicts with some keyword defined in the context for source transformation tools.
     *
     * @param context the keyword context
     * @return keyword context or null, if context does not have keywords defined.
     */
    KeywordContext getKeywordContext(DefinitionContext context);

    /**
     * @return the statement sequence parser with default context
     */
    TermParserStateFactory statementSequenceParser();

    /**
     * The statement parser with the specified context. Note that the context is not necessary from this grammar,
     * it could be from reused grammars imported by this one.
     *
     * @param context the statement context
     * @return the statement context
     * @throws IllegalArgumentException if context is not defined within the grammar
     */
    TermParserStateFactory statementSequenceParser(DefinitionContext context);

    /**
     * The statement parser with the specified context. Note that the context is not necessary from this grammar,
     * it could be from reused grammars imported by this one.
     *
     * @param context the statement context
     * @return the statement context
     * @throws IllegalArgumentException if context is not defined within the grammar
     */
    TermParserStateFactory statementParser(DefinitionContext context);

    /**
     * The parser for expression context hosted within some statement context
     *
     * @param context the context
     * @return the expression parser
     * @throws IllegalArgumentException if context is not defined within the grammar
     */
    TermParserStateFactory expressionParser(ExpressionContext context);

    /**
     * @return if true, the script mode should be used if mode for the source is not specified
     */
    boolean isScript();
}
