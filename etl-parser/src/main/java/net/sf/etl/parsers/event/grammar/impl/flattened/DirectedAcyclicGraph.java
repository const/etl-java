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
package net.sf.etl.parsers.event.grammar.impl.flattened;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * This class represents directed acyclic graph. The implementation is biased
 * toward small collections and maintains transitive closure for all nodes.
 * </p>
 * <p>
 * It is also impossible to remove nodes from graph.
 * </p>
 * <p>
 * IMPORTANT: This class is not thread safe and it is not designed to be
 * reusable outside of this package.
 * </p>
 *
 * @param <E> element type
 * @author const
 */
// NOTE if this would be too slow, use BitSet based implementation. Nodes are
// never removed from grammar. So it should not be too complex to implement.
// Each node would have four bitset that represent immediate and all parents
// and children by index.
public final class DirectedAcyclicGraph<E> {
    /**
     * Rank comparator. It compares two nodes by rank. It is used to sort nodes
     * topologically.
     */
    private static final Comparator<Node<?>> RANK_COMPARATOR = new Comparator<Node<?>>() {
        public int compare(final Node<?> o1, final Node<?> o2) {
            return o1.rank - o2.rank;
        }

    };
    /**
     * The map from objects to nodes.
     */
    private final Map<E, Node<E>> objects = new IdentityHashMap<E, Node<E>>();

    /**
     * This call represent directed acyclic graph and also provides a number of
     * operations on it.
     */
    public DirectedAcyclicGraph() {
        super();
    }

    /**
     * Get node for object. If node already exists it is returned, otherwise it
     * is created.
     *
     * @param o the object to be wrapped into node
     * @return the new node
     */
    public Node<E> getNode(final E o) {
        Node<E> rc = objects.get(o);
        if (rc == null) {
            rc = new Node<E>(this, o);
            objects.put(o, rc);
        }
        return rc;
    }

    /**
     * This method minimizes amount of immediate parents and children for all
     * nodes.
     */
    public void minimizeImmediate() {
        for (final Node<E> n : objects.values()) {
            n.minimizeImmediate();
        }
    }

    /**
     * @return sort nodes topologically
     */
    public List<Node<E>> topologicalSortNodes() {
        final ArrayList<Node<E>> rc = new ArrayList<Node<E>>(objects.values());
        Collections.sort(rc, RANK_COMPARATOR);
        return rc;
    }

    /**
     * @return list of nodes sorted topologically (parents are first)
     */
    public List<E> topologicalSortObjects() {
        final List<E> rc = new ArrayList<E>();
        for (final Node<E> n : topologicalSortNodes()) {
            rc.add(n.value);
        }
        return rc;
    }

    /**
     * Node in the graph.
     *
     * @param <E> the element type
     */
    public static final class Node<E> {
        /**
         * The graph.
         */
        private final DirectedAcyclicGraph<E> dag;
        /**
         * The value wrapped into node.
         */
        private final E value;
        /**
         * The collection of immediate parents.
         */
        private final Set<Node<E>> immediateParents = new HashSet<Node<E>>();
        /**
         * The collection of all parents.
         */
        private final Set<Node<E>> allParents = new HashSet<Node<E>>();
        /**
         * The collection of immediate children.
         */
        private final Set<Node<E>> immediateChildren = new HashSet<Node<E>>();
        /**
         * The collection of all children.
         */
        private final Set<Node<E>> allChildren = new HashSet<Node<E>>();
        /**
         * The node rank.
         */
        private int rank = 0;

        /**
         * The constructor.
         *
         * @param dag   the graph that holds the node
         * @param value the value in the node
         */
        public Node(final DirectedAcyclicGraph<E> dag, final E value) {
            this.dag = dag;
            this.value = value;
        }

        /**
         * minimize number of immediate children and parents of all nodes. This
         * is an optimization step in order not to consider indirect imports.
         */
        void minimizeImmediate() {
            // minimize children
            final HashSet<Node<E>> children = new HashSet<Node<E>>(
                    immediateChildren);
            for (final Node<E> child : immediateChildren) {
                children.removeAll(child.allChildren);
            }
            immediateChildren.retainAll(children);

            // minimize parents
            final HashSet<Node<E>> parents = new HashSet<Node<E>>(
                    immediateParents);
            for (final Node<E> parent : immediateParents) {
                parents.removeAll(parent.allParents);
            }
            immediateParents.retainAll(parents);

        }

        /**
         * Check if the parent.
         *
         * @param parent the node to be checked
         * @return true if parent is actually a parent node
         */
        public boolean hasImmediateParent(final E parent) {
            return hasImmediateParentNode(dag.getNode(parent));
        }

        /**
         * Check if the parent node.
         *
         * @param parent the node to be checked
         * @return true if parent is actually a parent node
         */
        public boolean hasImmediateParentNode(final Node<E> parent) {
            return immediateParents.contains(parent);
        }

        /**
         * Check if the parent node.
         *
         * @param parent the node to be checked
         * @return true if parent is actually a parent node
         */
        public boolean hasParentNode(final Node<E> parent) {
            return allParents.contains(parent);
        }

        /**
         * Add parent node.
         *
         * @param parent the new parent for this node
         * @return true if node is added, false if adding node would have create
         * cycle in the graph
         */
        public boolean addParent(final E parent) {
            return addParentNode(dag.getNode(parent));
        }

        /**
         * Add pair to the graph.
         *
         * @param parent the parent node
         * @param child  the child node
         * @return true if the pair was created or false if it would have created cycle
         */
        private boolean addPair(final Node<E> parent, final Node<E> child) {
            if (child == parent) {
                return false;
            } else if (child.allChildren.contains(parent)) {
                return false;
            } else if (child.immediateParents.contains(parent)) {
                return true;
            } else {
                // establish child link
                parent.immediateChildren.add(child);
                parent.allChildren.add(child);
                parent.allChildren.addAll(child.allChildren);
                for (final Node<E> grandParentNode : parent.allParents) {
                    grandParentNode.allChildren.add(child);
                    grandParentNode.allChildren.addAll(child.allChildren);
                }
                // establish parent link
                child.immediateParents.add(parent);
                child.allParents.add(parent);
                child.allParents.addAll(parent.allParents);
                for (final Node<E> grandChildNode : child.allChildren) {
                    grandChildNode.allParents.add(parent);
                    grandChildNode.allParents.addAll(parent.allParents);
                }
                // propagate rank update
                child.updateRank(parent.rank + 1);
                return true;
            }
        }

        /**
         * Update rank.
         *
         * @param newRank new rank that node should have
         */
        private void updateRank(final int newRank) {
            if (rank < newRank) {
                rank = newRank;
                for (final Node<E> n : immediateChildren) {
                    n.updateRank(rank + 1);
                }
            }
        }

        /**
         * Add child node.
         *
         * @param child the new child node
         * @return true if node is added, false if adding node would have create        cycle in the graph
         */
        public boolean addChild(final E child) {
            return addPair(this, dag.getNode(child));
        }

        /**
         * @return Returns the value.
         */
        public E getValue() {
            return value;
        }

        /**
         * @return immediate parents iterable.
         */
        public Iterable<E> immediateParents() {
            return new Iterable<E>() {
                @Override
                public Iterator<E> iterator() {
                    return immediateParentsIterator();
                }
            };
        }

        /**
         * @return iterator over immediate parents
         */
        public Iterator<E> immediateParentsIterator() {
            return new NodeUnwrapIterator<E>(immediateParents.iterator());
        }

        /**
         * Add parent node.
         *
         * @param parent an new parent for this node
         * @return true if node is added, false if adding node would have create
         * cycle in the graph
         */
        public boolean addParentNode(final Node<E> parent) {
            return addPair(parent, this);
        }
    }

    /**
     * This iterator iterates over values contained in nodes.
     *
     * @param <E> the element type
     * @author const
     */
    private static class NodeUnwrapIterator<E> implements Iterator<E> {
        /**
         * the iterator over collection of nodes.
         */
        private final Iterator<Node<E>> i;

        /**
         * The constructor from collection iterator.
         *
         * @param i the unwrapped iterator
         */
        public NodeUnwrapIterator(final Iterator<Node<E>> i) {
            super();
            this.i = i;
        }

        @Override
        public void remove() {
            i.remove();
        }

        @Override
        public boolean hasNext() {
            return i.hasNext();
        }

        @Override
        public E next() {
            return i.next().getValue();
        }
    }

    /**
     * This is basic definition gatherer algorithm implementation over DAG. A
     * set of abstract methods is quite ad hoc, and it will be possibly refined
     * later.
     *
     * @param <DefinitionHolder> the holder node that holds definitions
     * @param <DefinitionKey>    the key that identifies definition within holder
     * @param <Definition>       the definition
     * @author const
     */
    public abstract static class DefinitionGatherer<DefinitionHolder, DefinitionKey, Definition> {

        /**
         * Gather definitions related to definition holders. The algorithm
         * assumes that it has been already applied to all immediate parents of
         * this node in topological sort order.
         *
         * @param sourceNode a node for which definitions will be gathered.
         */
        public final void gatherDefinitions(final DefinitionHolder sourceNode) {
            // get map with definitions considered native.
            final Map<DefinitionKey, Definition> existingDefinitions = definitionMap(sourceNode);
            // this map contains definitions gathered from parent holders
            final Map<DefinitionKey, HashSet<Definition>> allDefinitions =
                    new HashMap<DefinitionKey, HashSet<Definition>>();
            // iterate gather all immediate parents.
            for (final DefinitionHolder parentHolder : getHolderNode(sourceNode).immediateParents()) {
                for (Definition definitionFromParent : definitionMap(parentHolder).values()) {
                    final DefinitionKey definitionFromParentKey = definitionKey(definitionFromParent);
                    // any processing is done only if we do not already have the
                    // node
                    if (!existingDefinitions.containsKey(definitionFromParentKey)) {
                        HashSet<Definition> definitions = allDefinitions.get(definitionFromParentKey);
                        if (definitions == null) {
                            definitions = new HashSet<Definition>();
                            allDefinitions.put(definitionFromParentKey, definitions);
                        }
                        // definition is ignored if it is already available by
                        // some other path
                        if (!definitions.contains(definitionFromParent)) {
                            // check if there are definitions hidden by this
                            // nodes or definitions that hide this node.
                            final Iterator<Definition> k = definitions.iterator();
                            while (k.hasNext()) {
                                final Definition existingDefinition = k.next();
                                if (definitionNode(existingDefinition).hasParentNode(
                                        definitionNode(definitionFromParent))) {
                                    // new definition is hidden by the
                                    // definition that is already in the set, so
                                    // the new definition will be ignored
                                    definitionFromParent = null;
                                    break;
                                } else if (definitionNode(definitionFromParent).hasParentNode(
                                        definitionNode(existingDefinition))) {
                                    // new definition hides the definition from
                                    // the set, the existing definition will be
                                    // removed.
                                    k.remove();
                                }
                            }
                            // add node if no node that hides it has been
                            // detected
                            if (definitionFromParent != null) {
                                definitions.add(definitionFromParent);
                            }
                        }
                    }
                }
            }
            // see if there is any conflict and add good imports
            for (final Map.Entry<DefinitionKey, HashSet<Definition>> e : allDefinitions.entrySet()) {
                final HashSet<Definition> v = e.getValue();
                if (v.size() != 1) {
                    reportDuplicates(sourceNode, e.getKey(), v);
                }
                // add arbitrary definition for the set. Note that
                // reportDuplicates() method has a chance to resolve conflict.
                existingDefinitions.put(e.getKey(), includingDefinition(sourceNode, v.iterator().next()));
            }
        }

        /**
         * When object is included from parent holder, this callback method give
         * subclasses a chance to perform an additional processing on the node
         * or to replace it with derived node.
         *
         * @param sourceHolder a new holder for the definition
         * @param object       an object to process
         * @return a processed object
         */
        protected abstract Definition includingDefinition(DefinitionHolder sourceHolder, Definition object);

        /**
         * Report problem with definitions. The method checks if there is an
         * actually conflict (for example if definitions are the same, there is
         * no conflict). The method also has a chance to resolve conflict by
         * removing objects from set.
         *
         * @param sourceHolder   a source holder for definition.
         * @param key            a key for which conflict exists
         * @param duplicateNodes a set of duplicate definitions
         */
        protected abstract void reportDuplicates(DefinitionHolder sourceHolder,
                                                 DefinitionKey key, HashSet<Definition> duplicateNodes);

        /**
         * Get an DAG node for definition holder.
         *
         * @param definitionHolder a definition holder
         * @return a actual node that contains definition
         */
        protected abstract Node<DefinitionHolder> getHolderNode(DefinitionHolder definitionHolder);

        /**
         * Get the defining DAG node for definition.
         *
         * @param definition a definition to examine
         * @return the defining DAG node for definition
         */
        protected abstract Node<DefinitionHolder> definitionNode(Definition definition);

        /**
         * Get key of the definition.
         *
         * @param definition a definition to examine
         * @return the key that identifies definition
         */
        protected abstract DefinitionKey definitionKey(Definition definition);

        /**
         * Get the map that contains definition that are directly contained by
         * definition holder object. The method updates the definition map After
         * it stops working
         *
         * @param holder a holder object to examine
         * @return the map of immediate definitions.
         */
        protected abstract Map<DefinitionKey, Definition> definitionMap(DefinitionHolder holder);

    }

    /**
     * Base class for import definition gatherer. It adds standard error
     * reporting mechanism.
     *
     * @param <DefinitionHolder> a holder node that holds definitions
     * @param <DefinitionKey>    key that identifies definition within holder
     * @param <Definition>       definition
     * @param <ImportedObject>   an object imported though definition
     * @author const
     */
    public abstract static class ImportDefinitionGatherer<DefinitionHolder, DefinitionKey, Definition, ImportedObject>
            extends DefinitionGatherer<DefinitionHolder, DefinitionKey, Definition> {
        @Override
        protected final void reportDuplicates(final DefinitionHolder sourceHolder, final DefinitionKey key,
                                              final HashSet<Definition> duplicateNodes) {
            ImportedObject importedObject = null;
            // in case of imports there is no conflict if all imports point to
            // the same place
            for (final Definition gi : duplicateNodes) {
                if (importedObject == null) {
                    importedObject = importedObject(gi);
                } else if (importedObject != importedObject(gi)) {
                    // error is only reported if imports are pointing to
                    // different locations
                    reportDuplicateImportError(sourceHolder, key);
                    break;
                }
            }
        }

        /**
         * This method is used to report duplicates.
         *
         * @param sourceHolder definition holder node
         * @param key          a key for which error happened
         */
        protected abstract void reportDuplicateImportError(DefinitionHolder sourceHolder, DefinitionKey key);

        /**
         * Get imported object.
         *
         * @param importDefinition an import definition
         * @return an object that is being imported by this definition
         */
        protected abstract ImportedObject importedObject(Definition importDefinition);
    }

}
