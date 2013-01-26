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
package net.sf.etl.utils;

import net.sf.etl.parsers.*;
import net.sf.etl.parsers.streams.TermParserReader;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashSet;

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
public class FormatSource extends AbstractFileConverter {
    /**
     * A string used to build indent sources
     */
    String indentationString = "\t";
    /**
     * current indentation level
     */
    int indentLevel = 0;
    /**
     * true if there already were non whitespace tokens on the current line
     */
    boolean wereTokens = false;
    /**
     * true if the new line is needed to start next line
     */
    boolean needNewLine = false;
    /**
     * if true there was a new line in input between printed token and a new
     * token
     */
    boolean wasNewLine = true;
    /**
     * if true, the next space character is suppressed
     */
    boolean spaceSuppressed = false;
    /**
     * an output
     */
    PrintWriter out;
    /**
     * last token printed
     */
    private Token lastPrinted;
    /**
     * graphics around which space is suppressed
     */
    private final HashSet<String> graphicsWithSuppressedSpace = new HashSet<String>();

    /**
     * A constructor
     */
    public FormatSource() {
        // FIXME make configuration. Possibly more flexible configuration is
        // required, for example one that consider context of the expression.

        graphicsWithSuppressedSpace.add(".");
        graphicsWithSuppressedSpace.add(":");
    }

    /**
     * Application entry point
     *
     * @param args application arguments
     */
    public static void main(String[] args) {
        try {
            new FormatSource().start(args);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    protected void processContent(OutputStream out, TermParserReader p)
            throws Exception {
        // FIXME encoding
        this.out = new PrintWriter(out);
        formatBlockContent(p);
        this.out.flush();
    }

    /**
     * Format content of the block or top level source
     *
     * @param p a term parser
     */
    private void formatBlockContent(TermParserReader p) {
        while (p.current().kind() != Terms.EOF
                && p.current().kind() != Terms.BLOCK_END) {
            TermToken tt = p.current();
            Token tk = token(tt);
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
     * Get lexical token from term token
     *
     * @param tt a term token
     * @return a token from lexer or null
     */
    private Token token(TermToken tt) {
        return tt.hasLexicalToken() ? tt.token().token() : null;
    }

    /**
     * Process ignorable token
     *
     * @param p  a parser
     * @param tk a token
     */
    private void processIgnorable(TermParserReader p, Token tk) {
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
        }
        // Whatever token was, advance to the next token.
        p.advance();
    }

    /**
     * Format the segment
     *
     * @param p the parser
     */
    private void formatSegment(TermParserReader p) {
        startIndentedLine();
        consume(p, Terms.STATEMENT_START);
        while (p.current().kind() != Terms.STATEMENT_END) {
            TermToken tt = p.current();
            Token tk = token(tt);
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
            }

        }
        printControl(";");
        consume(p, Terms.STATEMENT_END);
    }

    /**
     * Format attributes
     *
     * @param p a parser
     */
    private void formatAttributes(TermParserReader p) {
        consume(p, Terms.ATTRIBUTES_START);
        int objects = 0;
        while (p.current().kind() != Terms.ATTRIBUTES_END) {
            TermToken tt = p.current();
            Token tk = token(tt);
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
            }
        }
        consume(p, Terms.ATTRIBUTES_END);
        startIndentedLine();
    }

    /**
     * Process a token inside segment contents. The methods processes either a
     * single token or the block.
     *
     * @param p a a parser
     */
    private void formatSegmentContent(TermParserReader p) {
        TermToken tt = p.current();
        Token tk = token(tt);
        switch (tt.kind()) {
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
                    }
                }
                p.advance();
        }
    }

    /**
     * Consume term token of the specified kind
     *
     * @param p    a parser
     * @param kind the expected token kind
     */
    private void consume(TermParserReader p, Terms kind) {
        if (p.current().kind() != kind) {
            throw new IllegalStateException("The current token " + p.current()
                    + " does not match expected kind " + kind);
        }
        p.advance();
    }

    /**
     * Format documentation comments
     *
     * @param p a parser
     */
    private void formatDocComments(TermParserReader p) {
        consume(p, Terms.DOC_COMMENT_START);
        while (p.current().kind() != Terms.DOC_COMMENT_END) {
            TermToken tt = p.current();
            Token tk = token(tt);
            switch (tt.kind()) {
                case IGNORABLE:
                    processIgnorable(p, tk);
                    break;
                case VALUE:
                    if (wereTokens) {
                        startIndentedLine();
                    }
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
     * Start comment inside block content
     *
     * @param tt a term token
     */
    private void startBlockContentComment(Token tt) {
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
     * force new line
     */
    private void forceNewLine() {
        out.print('\n');
        needNewLine = false;
    }

    /**
     * Print the text
     *
     * @param text a text to print
     */
    private void print(Token text) {
        lastPrinted = text;
        printControl(text.text());
    }

    /**
     * Print the text
     *
     * @param text a text to print
     */
    private void printControl(String text) {
        out.print(text);
        wasNewLine = false;
        spaceSuppressed = false;
        wereTokens = true;
    }

    /**
     * print single space character
     */
    private void space() {
        if (wereTokens && !spaceSuppressed) {
            out.print(' ');
        }
    }

    /**
     * start a line
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
     * print single space character
     */
    private void startIndentedLine() {
        startLine();
        for (int i = 0; i < indentLevel; i++) {
            out.print(indentationString);
        }
    }
}
