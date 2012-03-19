package net.sf.etl.parsers.event.lexer;

import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.Tokens;
import org.junit.Test;

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
