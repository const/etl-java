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
package net.sf.etl.parsers.term.firstchoice;

import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.event.tree.FieldObjectFactory;
import net.sf.etl.parsers.event.tree.ObjectFactory;
import net.sf.etl.parsers.term.FieldsTermCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * A test that checks first choice cases
 *
 * @author const
 */
public class FirstChoiceTest extends FieldsTermCase<BaseNode> {

    /**
     * Test first choice implementation
     */
    @Test
    public void testFirstChoice() {
        startWithResource("FirstChoice.f.etl");
        boolean errorExit = true;
        try {
            // id; //first
            checkStatement(First.class, "id");
            // 1.0l; // first
            checkStatement(First.class, "1.0l");
            // 1.0L; // first
            checkStatement(First.class, "1.0L");
            // "test2"; // first
            checkStatement(First.class, "\"test2\"");
            // ++; // first
            checkStatement(First.class, "++");
            // 'ququ'; // first
            checkStatement(First.class, "'ququ'");
            // UTF8`s`; // first
            checkStatement(First.class, "UTF8's'");
            // utf8`s`; // first
            checkStatement(First.class, "utf8's'");
            // "test"; // second
            checkStatement(Second.class, "\"test\"");
            // 1; // second
            checkStatement(Second.class, "1");
            // 1.0; // second
            checkStatement(Second.class, "1.0");
            // `s`; // second
            checkStatement(Second.class, "'s'");
            // ; // second
            checkStatement(Second.class, null);
            // """night,
            // morning,
            // day,
            // evening"""; // second
            checkStatement(Second.class,
                    "\"\"\"night,\nmorning,\nday,\nevening\"\"\"", true);
            // UTF8```first
            // second```; // second
            checkStatement(Second.class, "UTF8'''first\nsecond'''", true);
            errorExit = false;
        } finally {
            endParsing(errorExit);
        }

    }

    /**
     * check statement
     *
     * @param type an expected type
     * @param text an expected text
     */
    private void checkStatement(final Class<?> type, final String text) {
        checkStatement(type, text, false);
    }

    /**
     * check statement
     *
     * @param type              an expected type
     * @param text              an expected text
     * @param normalizeNewlines if true normalize newlines in the text
     */
    private void checkStatement(final Class<?> type, final String text,
                                final boolean normalizeNewlines) {
        assertTrue(parser.advance());
        final Statement s = (Statement) parser.current(); // NOPMD
        if (type != null) {
            assertNotNull(s.value);
            assertSame("Token: " + s.value.text, type, s.value.getClass());
            if (text != null) {
                assertNotNull(s.value.text);
                assertEquals(text, normalizeNewlines ? s.value.text.text().replaceAll("\r\n", "\n") : s.value.text.text());
            }
        } else {
            assertNull(s.value);
        }
    }

    @Override
    protected FieldObjectFactory<BaseNode> createFieldTermParser() {
        final FieldObjectFactory<BaseNode> rc = super.createFieldTermParser();
        rc.ignoreNamespace(StandardGrammars.DOCTYPE_NS);
        rc.mapNamespaceToPackage(
                "http://etl.sf.net/2006/samples/firstChoice/0.1",
                "net.sf.etl.parsers.term.firstchoice");
        rc.setPosPolicy(ObjectFactory.PositionPolicyPositions.get());
        return rc;
    }
}
