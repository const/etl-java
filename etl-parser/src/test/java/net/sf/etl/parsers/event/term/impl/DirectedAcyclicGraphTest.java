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
package net.sf.etl.parsers.event.term.impl;

import net.sf.etl.parsers.event.grammar.impl.flattened.DirectedAcyclicGraph;
import net.sf.etl.parsers.event.grammar.impl.flattened.DirectedAcyclicGraph.Node;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test for directed acyclic graph
 *
 * @author const
 */
public class DirectedAcyclicGraphTest {

    /**
     * Test get node functionality
     */
    @Test
    public void testGetNode() {
        final DirectedAcyclicGraph<Object> dag = new DirectedAcyclicGraph<Object>();
        final Object o1 = new Object();
        final Object o2 = new Object();
        final Node<Object> n = dag.getNode(o1);
        assertSame(n, dag.getNode(o1));
        assertNotSame(n, dag.getNode(o2));
    }

    /**
     * Test get node functionality
     */
    @Test
    public void testOperations() {
        final DirectedAcyclicGraph<Object> dag = new DirectedAcyclicGraph<Object>();
        final Object o1 = new Object();
        final Object o2 = new Object();
        final Object o3 = new Object();
        final Node<Object> n1 = dag.getNode(o1);
        final Node<Object> n2 = dag.getNode(o2);
        final Node<Object> n3 = dag.getNode(o3);
        assertTrue(n1.addParent(o2));
        assertFalse(n1.addChild(o2));
        assertFalse(n1.addChild(o1));
        assertTrue(n2.addParent(o3));
        assertFalse(n3.addParent(o1));
        assertTrue(n1.addParent(o3));
        // check topological sort
        final List<Node<Object>> l = dag.topologicalSortNodes();
        assertSame(l.get(0), n3);
        assertSame(l.get(1), n2);
        assertSame(l.get(2), n1);
        // checking minimize operation
        assertTrue(n1.hasImmediateParent(o2));
        assertTrue(n1.hasImmediateParent(o3));
        dag.minimizeImmediate();
        assertTrue(n1.hasImmediateParent(o2));
        assertFalse(n1.hasImmediateParent(o3));

    }

}
