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

package net.sf.etl.parsers.event.lexer;

import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.characters.Whitespaces;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Test for spaces and comments
 */
public class SpacesAndCommentsTest extends LexerTestCase {
    @Test
    public void simpleSpaces() {
        single(" \t ", Tokens.WHITESPACE);
    }

    @Test
    public void newlines() {
        TextPos n1 = new TextPos(2, 1, 1);
        TextPos n2 = new TextPos(2, 1, 2);
        Token t = single("\n", Tokens.NEWLINE);
        assertEquals(n1, t.end());
        t = single("\r", Tokens.NEWLINE);
        assertEquals(n1, t.end());
        t = single("\r\n", Tokens.NEWLINE);
        assertEquals(n2, t.end());
        t = single("\u000B", Tokens.NEWLINE);
        assertEquals(n1, t.end());
        t = single("\u000C", Tokens.NEWLINE);
        assertEquals(n1, t.end());
        t = single("\u0085", Tokens.NEWLINE);
        assertEquals(n1, t.end());
        t = single("\u2028", Tokens.NEWLINE);
        assertEquals(n1, t.end());
        t = single("\u2029", Tokens.NEWLINE);
        assertEquals(n1, t.end());
        sequence("\n\r\r\n\u000B\u000C\u0085\u2029\u2028", Tokens.NEWLINE, "\n", "\r", "\r\n", "\u000B",
                "\u000C", "\u0085", "\u2029", "\u2028");
    }

    @Test
    public void newLineSplitTest() {
        assertEquals(Arrays.asList("", "", "test", ""), Whitespaces.splitNewLines("\r\n\rtest\n"));
        assertEquals(Arrays.asList("test", "a"), Whitespaces.splitNewLines("test\r\na"));
    }

    /**
     * test line comments
     */
    @Test
    public void testLines() {
        single("// text # /*  // /// aaa", Tokens.LINE_COMMENT);
        single("//", Tokens.LINE_COMMENT);
        sequenceText("// text # /*  // /// aaa\n", "// text # /*  // /// aaa", "\n");
        sequenceText("//\n", "//", "\n");
        single("#! text # /*  // /// aaa", Tokens.LINE_COMMENT);
        single("#!", Tokens.LINE_COMMENT);
        sequenceText("#! text # /*  // /// aaa\n", "#! text # /*  // /// aaa", "\n");
        sequenceText("#!\n", "#!", "\n");
        single("/// text # /*  // /// aaa", Tokens.DOC_COMMENT);
        single("///", Tokens.DOC_COMMENT);
        sequenceText("/// text # /*  // /// aaa\n", "/// text # /*  // /// aaa", "\n");
        sequenceText("///\n", "///", "\n");
    }

    /**
     * test block comment
     */
    @Test
    public void testBlockComments() {
        single("/* text # /* * /  // /// aaa */", Tokens.BLOCK_COMMENT);
        single("/* text # /*\n* /  //\r\n ///\n\r aaa */", Tokens.BLOCK_COMMENT);
    }

}
