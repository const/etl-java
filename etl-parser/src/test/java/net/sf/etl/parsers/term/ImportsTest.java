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
package net.sf.etl.parsers.term;

import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.event.tree.BeansObjectFactory;
import net.sf.etl.parsers.event.tree.ObjectFactory;
import net.sf.etl.parsers.term.beans.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test on imports
 *
 * @author const
 */
public class ImportsTest extends BeansTermCase {

    /**
     * namespace used by main grammar
     */
    private static final String MAIN_NS = "http://etl.sf.net/2006/samples/imports/Main/0.1";

    /**
     * Test grammar with imports
     */
    @Test
    public void testImports() {
        startWithResource("imports/Test.i.etl");
        boolean errorExit = true;
        try {
            // let a = 5;
            assertTrue(parser.advance());
            final LetStatement let_a = (LetStatement) parser.current();
            assertEquals("a", let_a.getName());
            final IntegerLiteral let_a_value = (IntegerLiteral) let_a
                    .getValue();
            assertEquals(5, let_a_value.getValue());
            // let b = a;
            assertTrue(parser.advance());
            final LetStatement let_b = (LetStatement) parser.current();
            assertEquals("let b = a", let_b.statementText());
            assertEquals("b", let_b.getName());
            final Identifier let_b_value = (Identifier) let_b.getValue();
            assertEquals("a", let_b_value.getName());
            // a + b / 2 + {let c = 3; c * (a-b+1); };
            assertTrue(parser.advance());
            final ExpressionStatement expr = (ExpressionStatement) parser.current();
            final PlusOp exprPlus = (PlusOp) expr.getValue();
            final BlockExpression bl = (BlockExpression) exprPlus.getSummands()[1];
            final LetStatement let_c = (LetStatement) bl.getContent()[0];
            assertEquals("c", let_c.getName());
            errorExit = false;
        } finally {
            endParsing(errorExit);
        }
    }

    @Override
    protected BeansObjectFactory createBeansTermParser() {
        final BeansObjectFactory rc = new BeansObjectFactory(getClass().getClassLoader()) {
            @Override
            public void handleErrorFromParser(TermToken errorToken) {
                fail("Errors from parser are not expected: " + errorToken);
            }
        };
        rc.setPosPolicy(ObjectFactory.PositionPolicy.POSITIONS);
        rc.ignoreNamespace(StandardGrammars.DOCTYPE_NS);
        rc.mapNamespaceToPackage(
                "http://etl.sf.net/2006/samples/imports/Expression/0.1",
                Expression.class.getPackage().getName());
        rc.mapNameToClass(MAIN_NS, "LetStatement", LetStatement.class);
        rc.mapNameToClass(MAIN_NS, "ExpressionStatement",
                ExpressionStatement.class);
        return rc;
    }
}
