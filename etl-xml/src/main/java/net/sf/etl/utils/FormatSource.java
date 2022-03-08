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
package net.sf.etl.utils; // NOPMD

import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.streams.TermParserReader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * This class implements default ETL source code formatting. Formatting is very
 * trivial right now. The formatting is done as the following:
 * <ul>
 * <li>
 * <p>
 * Blocks are formatted as the following:
 * </p>
 * <p/>
 * <pre>
 * start {
 *    text;
 * } then {
 *    text;
 * } end;
 * </pre>
 * <p/>
 * </li>
 * <p/>
 * <li>The whitespace is ignored.</li>
 * <li>Attributes are started on the new line. After the end of each attribute
 * object a new line is forced.</li>
 * <li>The documentation comments are indented and they always put on the
 * separate line.</li>
 * <li>The line and block comments are not touched, if they start at the
 * beginning of the line. Otherwise they are indented to the current level if
 * there were no tokens on the current line. For multiline block comments,
 * additional parts are not touched.</li>
 * <li>If the line comment is inside segment, then the segment is continued on
 * the current indentation level on the next line.</li>
 * <li>The tab character is used for indentation.</li>
 * </ul>
 *
 * @author const
 */
public final class FormatSource extends AbstractFileConverter<AbstractFileConverter.BaseConfig> { // NOPMD
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FormatSource.class);
    /**
     * The string used to build indent sources.
     */
    private static final String INDENTATION_STRING = "\t";
    /**
     * The graphics around which space is suppressed.
     */
    private final Set<String> graphicsWithSuppressedSpace = new HashSet<>();
    /**
     * The current indentation level.
     */
    private int indentLevel;
    /**
     * true if there already were non whitespace tokens on the current line.
     */
    private boolean wereTokens;
    /**
     * true if the new line is needed to start next line.
     */
    private boolean needNewLine;
    /**
     * if true there was a new line in input between printed token and a new
     * token.
     */
    private boolean wasNewLine = true;
    /**
     * if true, the next space character is suppressed.
     */
    private boolean spaceSuppressed;
    /**
     * The output.
     */
    private PrintWriter out;
    /**
     * The last token printed.
     */
    private Token lastPrinted;

    /**
     * The constructor.
     */
    public FormatSource() {
        // FIXME make configuration. Possibly more flexible configuration is
        // required, for example one that consider context of the expression.

        graphicsWithSuppressedSpace.add(".");
        graphicsWithSuppressedSpace.add(":");
    }

    /**
     * The application entry point.
     *
     * @param args the application arguments
     */
    public static void main(final String[] args) {
        try {
            new FormatSource().start(args);
        } catch (Throwable t) { // NOPMD
            LOG.error("Processing failed", t);
            System.exit(1);
        }
    }

    @Override
    protected BaseConfig parseConfig(final CommandLine commandLine) {
        return new BaseConfig(commandLine);
    }

    @Override
    protected Options getOptions() {
        return BaseConfig.getBaseOptions();
    }

    @Override
    protected void processContent(final OutputStream stream, final TermParserReader p)
            throws Exception {
        // TODO encoding
        this.out = new PrintWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));
        formatBlockContent(p);
        this.out.flush();
    }

    /**
     * Format content of the block or top level source.
     *
     * @param p the term parser
     */
    private void formatBlockContent(final TermParserReader p) {
        while (p.current().kind() != Terms.EOF
                && p.current().kind() != Terms.BLOCK_END) {
            final TermToken tt = p.current();
            final Token tk = token(tt);
            switch (tt.kind()) {
                case IGNORABLE:
                    processIgnorable(p, tk);
                    break;
                case CONTROL:
                    // Whatever token was, advance to the next token.
                    // Note that actual '{', '}' and ';' tokens are printed
                    // by the block and segment parsing code.
                    p.advance();
                    break;
                case STATEMENT_START:
                    formatSegment(p);
                    break;
                case GRAMMAR_IS_LOADED:
                    // FIXME log?
                    p.advance();
                    break;
                case SYNTAX_ERROR:
                    // FIXME REPORT
                    p.advance();
                    break;
                default:
                    assert false : "it should be never encountered here: " + tt;
                    break;
            }
        }
    }

    /**
     * Get lexical token from term token.
     *
     * @param tt the term token
     * @return the token from lexer or null
     */
    private Token token(final TermToken tt) {
        return tt.hasLexicalToken() ? tt.token().token() : null;
    }

    /**
     * Process ignorable token.
     *
     * @param p  the parser
     * @param tk the token
     */
    private void processIgnorable(final TermParserReader p, final Token tk) {
        if (tk != null) {
            switch (tk.kind()) {
                case NEWLINE:
                    wasNewLine = true;
                    break;
                case DOC_COMMENT:
                    // Note if doc comment is classified as ignorable, then it is
                    // encountered in the context where doc comments cannot happen and
                    // it should be treated the same as a line comment.
                case LINE_COMMENT:
                    startBlockContentComment(tk);
                    print(tk);
                    forceNewLine();
                    break;
                case BLOCK_COMMENT:
                    startBlockContentComment(tk);
                    print(tk);
                    break;
                default:
                    break;
            }
        }
        // Whatever token was, advance to the next token.
        p.advance();
    }

    /**
     * Format the segment.
     *
     * @param p the parser
     */
    private void formatSegment(final TermParserReader p) {
        startIndentedLine();
        consume(p, Terms.STATEMENT_START);
        while (p.current().kind() != Terms.STATEMENT_END) {
            final TermToken tt = p.current();
            final Token tk = token(tt);
            switch (tt.kind()) {
                case CONTROL:
                    p.advance();
                    break;
                case IGNORABLE:
                    processIgnorable(p, tk);
                    break;
                case DOC_COMMENT_START:
                    formatDocComments(p);
                    break;
                case ATTRIBUTES_START:
                    formatAttributes(p);
                    break;
                default:
                    formatSegmentContent(p);
                    break;
            }

        }
        printControl(";");
        consume(p, Terms.STATEMENT_END);
    }

    /**
     * Format attributes.
     *
     * @param p the parser
     */
    private void formatAttributes(final TermParserReader p) {
        consume(p, Terms.ATTRIBUTES_START);
        int objects = 0;
        while (p.current().kind() != Terms.ATTRIBUTES_END) {
            final TermToken tt = p.current();
            final Token tk = token(tt);
            switch (tt.kind()) {
                case CONTROL:
                    p.advance();
                    break;
                case IGNORABLE:
                    processIgnorable(p, tk);
                    break;
                case OBJECT_START:
                    objects++;
                    p.advance();
                    break;
                case OBJECT_END:
                    objects--;
                    if (objects == 0) {
                        forceNewLine();
                    }
                    p.advance();
                    break;
                default:
                    formatSegmentContent(p);
                    break;
            }
        }
        consume(p, Terms.ATTRIBUTES_END);
        startIndentedLine();
    }

    /**
     * Process a token inside segment contents. The methods processes either a
     * single token or the block.
     *
     * @param p the parser
     */
    private void formatSegmentContent(final TermParserReader p) {
        final TermToken tt = p.current();
        final Token tk = token(tt);
        switch (tt.kind()) { // NOPMD
            case CONTROL:
                p.advance();
                break;
            case IGNORABLE:
                processIgnorable(p, tk);
                break;
            case BLOCK_START:
                space();
                consume(p, Terms.BLOCK_START);
                printControl("{");
                forceNewLine();
                indentLevel++;
                formatBlockContent(p);
                indentLevel--;
                startIndentedLine();
                printControl("}");
                consume(p, Terms.BLOCK_END);
                break;
            default:
                if (tk != null) {
                    switch (tk.kind()) {
                        case BRACKET:
                            print(tk);
                            spaceSuppressed = true;
                            break;
                        case COMMA:
                            print(tk);
                            break;
                        case GRAPHICS:
                            if (graphicsWithSuppressedSpace.contains(tk.text())) {
                                if (lastPrinted.kind() == Tokens.GRAPHICS) {
                                    spaceSuppressed = false;
                                    space();
                                }
                                print(tk);
                                spaceSuppressed = true;
                            } else {
                                if (lastPrinted.kind() == Tokens.GRAPHICS) {
                                    spaceSuppressed = false;
                                }
                                space();
                                print(tk);
                            }
                            break;
                        default:
                            space();
                            print(tk);
                            break;
                    }
                }
                p.advance();
                break;
        }
    }

    /**
     * Consume term token of the specified kind.
     *
     * @param p    the parser
     * @param kind the expected token kind
     */
    private void consume(final TermParserReader p, final Terms kind) {
        if (p.current().kind() != kind) {
            throw new IllegalStateException("The current token " + p.current()
                    + " does not match expected kind " + kind);
        }
        p.advance();
    }

    /**
     * Format documentation comments.
     *
     * @param p the parser
     */
    private void formatDocComments(final TermParserReader p) {
        consume(p, Terms.DOC_COMMENT_START);
        while (p.current().kind() != Terms.DOC_COMMENT_END) {
            final TermToken tt = p.current();
            final Token tk = token(tt);
            switch (tt.kind()) {
                case IGNORABLE:
                    processIgnorable(p, tk);
                    break;
                case VALUE:
                    if (wereTokens) {
                        startIndentedLine();
                    }
                    assert tk != null;
                    print(tk);
                    forceNewLine();
                    p.advance();
                    break;
                default:
                    p.advance();
                    break;
            }
        }
        consume(p, Terms.DOC_COMMENT_END);
        startIndentedLine();
    }

    /**
     * Start comment inside block content.
     *
     * @param tt the term token
     */
    private void startBlockContentComment(final Token tt) {
        if (wereTokens && !wasNewLine) {
            space();
        } else {
            if (tt.start().column() != TextPos.START_COLUMN) {
                startIndentedLine();
            } else {
                startLine();
            }
        }
    }

    /**
     * force new line.
     */
    private void forceNewLine() {
        out.print('\n');
        needNewLine = false;
    }

    /**
     * Print the text.
     *
     * @param text the text to print
     */
    private void print(final Token text) {
        lastPrinted = text;
        printControl(text.text());
    }

    /**
     * Print the text.
     *
     * @param text the text to print
     */
    private void printControl(final String text) {
        out.print(text);
        wasNewLine = false;
        spaceSuppressed = false;
        wereTokens = true;
    }

    /**
     * print single space character.
     */
    private void space() {
        if (wereTokens && !spaceSuppressed) {
            out.print(' ');
        }
    }

    /**
     * start a line.
     */
    private void startLine() {
        if (needNewLine) {
            out.print('\n');
        } else {
            needNewLine = true;
        }
        wasNewLine = false;
        wereTokens = false;
    }

    /**
     * print single space character.
     */
    private void startIndentedLine() {
        startLine();
        for (int i = 0; i < indentLevel; i++) {
            out.print(INDENTATION_STRING);
        }
    }
}
