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
import net.sf.etl.parsers.event.grammar.impl.flattened.DirectedAcyclicGraph.DefinitionGatherer;
import net.sf.etl.parsers.event.grammar.impl.flattened.DirectedAcyclicGraph.ImportDefinitionGatherer;
import net.sf.etl.parsers.event.grammar.impl.flattened.DirectedAcyclicGraph.Node;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests for gatherer algorithm
 *
 * @author const
 */
public class GathererTest {
    /**
     * This method tests gatherer algorithm
     */
    @Test
    public void testGatherer() {
        final DirectedAcyclicGraph<Holder> graph = new DirectedAcyclicGraph<Holder>();
        final Holder root = new Holder(graph);
        root.makeDef("a");
        root.makeDef("b");
        root.makeDef("c");
        root.makeDef("d");
        final Holder m1 = new Holder(graph);
        m1.node.addParent(root);
        m1.makeDef("a");
        m1.makeDef("d");
        m1.makeDef("e");
        m1.makeDef("f");
        final Holder m2 = new Holder(graph);
        m2.node.addParent(root);
        m2.makeDef("b");
        m2.makeDef("d");
        m2.makeDef("e");
        m2.makeDef("f");
        final Holder leaf = new Holder(graph);
        leaf.node.addParent(m1);
        leaf.node.addParent(m2);
        leaf.makeDef("d");
        leaf.makeDef("e");
        leaf.makeDef("g");
        final Gatherer g = new Gatherer();
        final List<Holder> l = graph.topologicalSortObjects();
        for (final Holder aL : l) {
            g.gatherDefinitions(aL);
        }
        assertSame(leaf.def("a").holder, m1);
        assertSame(leaf.def("b").holder, m2);
        assertSame(leaf.def("c").holder, root);
        assertSame(leaf.def("d").holder, leaf);
        assertSame(leaf.def("e").holder, leaf);
        assertTrue(leaf.def("f").holder == m1 || leaf.def("f").holder == m2);
        assertEquals(1, g.duplicates.size());
        final Set<Definition> s = new HashSet<Definition>();
        s.add(m1.def("f"));
        s.add(m2.def("f"));
        assertEquals(s, g.duplicates.get("f").duplicates);
        assertSame(leaf.def("g").holder, leaf);
    }

    /**
     * Test for import gatherer subclass. The test more or less emulates
     * situation with ContextImportViews when they are gathered along with
     * grammar include path.
     */
    @Test
    public void testImportGatherer() {
        final DirectedAcyclicGraph<Holder> graph = new DirectedAcyclicGraph<Holder>();
        final Holder root = new Holder(graph);
        root.makeImport("a", root);
        root.makeImport("b", root);
        root.makeImport("c", root);
        root.makeImport("d", root);
        final Holder m1 = new Holder(graph);
        m1.node.addParent(root);
        m1.makeImport("a", m1);
        m1.makeImport("d", m1);
        m1.makeImport("e", m1);
        m1.makeImport("f", m1);
        m1.makeImport("z", root);
        final Holder m2 = new Holder(graph);
        m2.node.addParent(root);
        m2.makeImport("b", m2);
        m2.makeImport("d", m2);
        m2.makeImport("e", m2);
        m2.makeImport("f", m2);
        m2.makeImport("z", root);
        final Holder leaf = new Holder(graph);
        leaf.node.addParent(m1);
        leaf.node.addParent(m2);
        leaf.makeImport("d", leaf);
        leaf.makeImport("e", leaf);
        leaf.makeImport("g", leaf);
        final ImportGatherer g = new ImportGatherer();
        final List<Holder> l = graph.topologicalSortObjects();
        for (final Holder aL : l) {
            g.gatherDefinitions(aL);
        }
        assertSame(leaf.importDef("a").referencedHolder, m1);
        assertSame(leaf.importDef("b").referencedHolder, m2);
        assertSame(leaf.importDef("c").referencedHolder, root);
        assertSame(leaf.importDef("d").referencedHolder, leaf);
        assertSame(leaf.importDef("e").referencedHolder, leaf);
        assertTrue(leaf.importDef("f").referencedHolder == m1
                || leaf.importDef("f").referencedHolder == m2);
        assertEquals(1, g.duplicates.size());
        assertEquals("f", g.duplicates.get("f").key);
        assertSame(leaf.importDef("g").referencedHolder, leaf);
        assertSame(leaf.importDef("z").referencedHolder, root);
    }

    /**
     * test definition class
     */
    private class Definition {
        /**
         * a key of definition
         */
        public final String key;
        /**
         * a holder
         */
        public final Holder holder;

        /**
         * A constructor
         *
         * @param holder a holder of definition
         * @param key    a key of definition
         */
        private Definition(final Holder holder, final String key) {
            this.key = key;
            this.holder = holder;
        }
    }

    /**
     * test definition class
     */
    private class ImportDefinition extends Definition {
        /**
         * an imported holder
         */
        public final Holder referencedHolder;
        /**
         * an original definition
         */
        public final ImportDefinition original;

        /**
         * A constructor
         *
         * @param holder           a holder of definition
         * @param key              a key of definition
         * @param referencedHolder referenced object
         */
        public ImportDefinition(final Holder holder, final String key, final Holder referencedHolder) {
            super(holder, key);
            this.referencedHolder = referencedHolder;
            this.original = this;
        }

        /**
         * A constructor
         *
         * @param holder a holder of definition
         * @param def    an original definition
         */
        public ImportDefinition(final Holder holder, final ImportDefinition def) {
            super(holder, def.key);
            this.referencedHolder = def.referencedHolder;
            this.original = def.original;
        }

    }

    /**
     * Test holder class
     */
    private class Holder {
        /**
         * a node of this holder
         */
        public final Node<Holder> node;
        /**
         * a definition map of this holder
         */
        public final Map<String, Definition> definitions = new HashMap<String, Definition>(); // NOPMD
        /**
         * a definition map of this holder
         */
        public final Map<String, ImportDefinition> imports = new HashMap<String, ImportDefinition>(); // NOPMD

        /**
         * @param graph a graph to which this holder belongs
         */
        public Holder(final DirectedAcyclicGraph<Holder> graph) {
            super();
            this.node = graph.getNode(this);
        }

        /**
         * get definition
         *
         * @param key a key
         * @return created definition;
         */
        public Definition def(final String key) {
            return definitions.get(key);
        }

        /**
         * get import definition
         *
         * @param key a key
         * @return created definition;
         */
        public ImportDefinition importDef(final String key) {
            return imports.get(key);
        }

        /**
         * Make definition
         *
         * @param key a key
         * @return created definition;
         */
        public Definition makeDef(final String key) {
            final Definition rc = new Definition(this, key);
            definitions.put(key, rc);
            return rc;
        }

        /**
         * Make definition
         *
         * @param key a key
         * @param ref referenced context
         * @return created definition;
         */
        public ImportDefinition makeImport(final String key, final Holder ref) {
            final ImportDefinition rc = new ImportDefinition(this, key, ref);
            imports.put(key, rc);
            return rc;
        }

    }

    /**
     * Test gatherer over holder
     */
    private class Gatherer extends DefinitionGatherer<Holder, String, Definition> {
        /**
         * duplicate nodes
         */
        public final Map<Object, DuplicateInfo> duplicates = new HashMap<Object, DuplicateInfo>(); // NOPMD

        @Override
        protected Definition includingDefinition(final Holder sourceHolder, final Definition object) {
            return object;
        }

        @Override
        protected void reportDuplicates(final Holder sourceHolder, final String key,
                                        final Set<Definition> duplicateNodes) {
            final DuplicateInfo d = new DuplicateInfo();
            d.key = key;
            d.duplicateSourceHolder = sourceHolder;
            d.duplicates = duplicateNodes;
            duplicates.put(key, d);
        }

        @Override
        protected Node<Holder> getHolderNode(final Holder definitionHolder) {
            return definitionHolder.node;
        }

        @Override
        protected Node<Holder> definitionNode(final Definition definition) {
            return definition.holder.node;
        }

        @Override
        protected String definitionKey(final Definition definition) {
            return definition.key;
        }

        @Override
        protected Map<String, Definition> definitionMap(final Holder holder) {
            return holder.definitions;
        }

    }

    /**
     * Test gatherer over holder
     */
    private class ImportGatherer extends
            ImportDefinitionGatherer<Holder, String, ImportDefinition, Holder> {
        /**
         * duplicate nodes
         */
        public final Map<Object, DuplicateInfo> duplicates = new HashMap<Object, DuplicateInfo>(); // NOPMD

        @Override
        protected Node<Holder> getHolderNode(final Holder definitionHolder) {
            return definitionHolder.node;
        }

        @Override
        protected Node<Holder> definitionNode(final ImportDefinition definition) {
            return definition.original.holder.node;
        }

        @Override
        protected String definitionKey(final ImportDefinition definition) {
            return definition.key;
        }

        @Override
        protected Map<String, ImportDefinition> definitionMap(final Holder holder) {
            return holder.imports;
        }

        @Override
        protected void reportDuplicateImportError(final Holder sourceHolder,
                                                  final String key) {
            final DuplicateInfo d = new DuplicateInfo();
            d.key = key;
            d.duplicateSourceHolder = sourceHolder;
            duplicates.put(key, d);

        }

        @Override
        protected Holder importedObject(final ImportDefinition importDefinition) {
            return importDefinition.referencedHolder;
        }

        @Override
        protected ImportDefinition includingDefinition(final Holder sourceHolder, final ImportDefinition object) {
            return new ImportDefinition(sourceHolder, object);
        }

    }

    /**
     * This class represent duplicate node information reported by gatherer.
     */
    private class DuplicateInfo {
        /**
         * source holder reported for duplicates
         */
        public Object key;
        /**
         * source holder reported for duplicates
         */
        public Object duplicateSourceHolder;
        /**
         * duplicate nodes
         */
        public Set<Definition> duplicates;

    }
}
