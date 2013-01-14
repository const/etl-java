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

import net.sf.etl.parsers.DefinitionContext;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.event.grammar.impl.flattened.DirectedAcyclicGraph.DefinitionGatherer;
import net.sf.etl.parsers.event.grammar.impl.flattened.DirectedAcyclicGraph.ImportDefinitionGatherer;
import net.sf.etl.parsers.event.grammar.impl.flattened.DirectedAcyclicGraph.Node;
import net.sf.etl.parsers.event.unstable.model.grammar.*;

import java.util.*;

/**
 * This is a view of context belonging to grammar
 *
 * @author const
 */
public class ContextView {
    /**
     * this is gather of imports by grammar include direction
     */
    private static final IncludeGathererByGrammarInclude INCLUDE_GATHERER_BY_GRAMMAR_INCLUDE = new IncludeGathererByGrammarInclude();
    /**
     * this is gather of imports by grammar include direction
     */
    private static final IncludeGathererByContextInclude INCLUDE_GATHERER_BY_CONTEXT_INCLUDE = new IncludeGathererByContextInclude();
    /**
     * this is gather of imports by grammar include direction
     */
    private static final ImportGathererByGrammarInclude IMPORT_GATHERER_BY_GRAMMAR_INCLUDE = new ImportGathererByGrammarInclude();
    /**
     * this is gather of definitions by grammar include direction
     */
    private static final DefinitionGathererByGrammarInclude DEFINITION_GATHERER_BY_GRAMMAR_INCLUDE = new DefinitionGathererByGrammarInclude();
    /**
     * this is gather of imports by grammar include direction
     */
    private static final ImportGathererByContextInclude IMPORT_GATHERER_BY_CONTEXT_INCLUDE = new ImportGathererByContextInclude();
    /**
     * this is gather of definitions by grammar include direction
     */
    private static final DefinitionGathererByContextInclude DEFINITION_GATHERER_BY_CONTEXT_INCLUDE = new DefinitionGathererByContextInclude();
    /**
     * The tree map from precedence to level view object
     */
    private final TreeMap<Integer, OpLevel> operatorLevels = new TreeMap<Integer, OpLevel>();
    /**
     * The attributes specification for context
     */
    private AttributesView attributes;
    /**
     * The documentation specification for context
     */
    private DocumentationView documentation;
    /**
     * The statements defined in the context
     */
    private final HashSet<StatementView> statements = new HashSet<StatementView>();
    /**
     * The map from name to definition
     */
    private final HashMap<String, DefView> defs = new HashMap<String, DefView>();
    /**
     * DAG node in context include hierarchy
     */
    private final Node<ContextView> contextIncludesNode;
    /**
     * The map from included context name to context include views
     */
    private final Map<String, ContextIncludeView> contextIncludes = new HashMap<String, ContextIncludeView>();
    /**
     * The DAG node in context include along with grammar include hierarchy
     */
    private final Node<ContextView> grammarIncludesNode;
    /**
     * The map that contains imports. key is local name of context
     */
    private final Map<String, ContextImportView> imports = new HashMap<String, ContextImportView>();
    /**
     * The map that definitions. key is name of definition.
     */
    private final Map<String, DefinitionView> definitions = new HashMap<String, DefinitionView>();
    /**
     * The context name
     */
    private final String name;
    /**
     * The grammar view for this context view
     */
    private final GrammarView grammar;
    /**
     * The context object for this context view. It may be null for inherited contexts.
     */
    private final Context context;
    /**
     * The context object for this context view. On which errors are reported. If
     * there are no real context object, a fake on is created and positioned at
     * the end of the grammar.
     */
    private final Element reportingContext;
    /**
     * The definition context
     */
    private final DefinitionContext definitionContext;

    /**
     * A constructor for view
     *
     * @param context                the context that is wrapped by this view. It may be null in case
     *                               if grammar does not have a view defined.
     * @param grammar                the grammar view that contains this context view
     * @param name                   the name of context
     * @param contextGrammarIncludes the context grammar include graph
     * @param contextContextIncludes the context content include graph
     */
    public ContextView(GrammarView grammar, String name, Context context,
                       DirectedAcyclicGraph<ContextView> contextGrammarIncludes,
                       DirectedAcyclicGraph<ContextView> contextContextIncludes) {
        super();
        this.context = context;
        this.grammar = grammar;
        this.name = name;
        this.grammarIncludesNode = contextGrammarIncludes.getNode(this);
        this.contextIncludesNode = contextContextIncludes.getNode(this);
        if (context != null) {
            reportingContext = context;
        } else {
            reportingContext = grammar.getGrammar();
        }
        this.definitionContext = new DefinitionContext(grammar.grammarInfo(), name);
    }

    /**
     * Gather content from context
     */
    private void loadContent() {
        assert context != null;
        for (final ContextMember m : context.content) {
            if (m instanceof ContextInclude) {
                final ContextInclude ci = (ContextInclude) m;
                final ContextView referencedContext = grammar.context(ci.contextName);
                if (referencedContext == null) {
                    error(ci, "grammar.Context.ContextInclude.missingContext", ci.contextName);
                    continue;
                }
                // check if duplicate include
                if (contextIncludes.containsKey(referencedContext.name())) {
                    error(ci, "grammar.Context.ContextInclude.duplicateInclude", ci.contextName);
                    continue;
                }
                // create wrapper link
                WrapperLink wrapperLink = null;
                for (final ListIterator<Wrapper> j = ci.wrappers.listIterator(ci.wrappers.size()); j.hasPrevious(); ) {
                    final Wrapper w = j.previous();
                    if (w.object != null && w.object.name != null && w.object.prefix != null && w.property != null) {
                        final String name = w.object.name.text();
                        final String prefix = w.object.prefix.text();
                        final String property = w.property.text();
                        final String namespace = grammar.namespace(prefix);
                        if (namespace == null) {
                            error(w, "grammar.Wrapper.undefinedWrapperPrefix", prefix);
                        } else {
                            wrapperLink = new WrapperLink(wrapperLink, namespace, name, property,
                                    w.object.sourceLocation(),
                                    new SourceLocation(w.property.start(), w.property.end(), w.location.systemId()));
                        }
                    } else {
                        // there were syntax error
                    }
                }
                contextIncludes.put(referencedContext.name(), new ContextIncludeView(ci, this, referencedContext, wrapperLink));
            } else if (m instanceof ContextImport) {
                final ContextImport ci = (ContextImport) m;
                final GrammarView referencedGrammar;
                if (ci.grammarName == null) {
                    referencedGrammar = grammar;
                } else {
                    referencedGrammar = grammar.getImportedGrammar(ci.grammarName);
                    if (referencedGrammar == null) {
                        error(ci, "grammar.Context.ContextImport.missingGrammarImport", ci.grammarName);
                        continue;
                    }
                }
                final ContextView referencedContext = referencedGrammar.context(ci.contextName);
                if (referencedContext == null) {
                    if (referencedGrammar == grammar) {
                        error(ci, "grammar.Context.ContextImport.missingContext", ci.contextName);
                    } else {
                        error(ci, "grammar.Context.ContextImport.missingContextInGrammar",
                                ci.contextName, referencedGrammar.getSystemId());
                    }
                    continue;
                }
                if (imports.containsKey(ci.localName)) {
                    error(ci, "grammar.Context.ContextImport.duplicateImport", ci.contextName);
                    continue;
                }
                final ContextImportView view = new ContextImportView(ci, this, referencedContext);
                imports.put(view.localName(), view);
            } else if (m instanceof SyntaxDefinition) {
                final SyntaxDefinition def = (SyntaxDefinition) m;
                if (definitions.containsKey(def.name.text())) {
                    error(def, "grammar.Context.Definition.duplicateDefinition", def.name);
                    continue;
                }
                definitions.put(def.name.text(), DefinitionView.get(this, def));
            } else {
                assert false : "Unknown type of context content" + m.getClass().getCanonicalName();
            }
        }
    }

    /**
     * @return the name of context
     */
    public String name() {
        return name;
    }

    /**
     * Associate this context with context from parent grammar
     *
     * @param pgc the context from parent grammar
     */
    public void processGrammarInclude(ContextView pgc) {
        // add parent along with grammar include path
        final boolean rc = grammarIncludesNode.addParentNode(pgc.grammarIncludesNode);
        assert rc : "It is assumed that adding parent will never fail "
                + "because graph following grammar include hierarchy "
                + "that already does not have cycles";
    }

    /**
     * Report non fatal grammar error
     *
     * @param errorId the error identifier
     * @param args    the error arguments
     */
    public void error(String errorId, Object... args) {
        error(reportingContext, errorId, args);
    }

    /**
     * @return the reporting context
     */
    public Element reportingContext() {
        return reportingContext;
    }


    /**
     * Report non fatal grammar error
     *
     * @param e       the element in error
     * @param errorId the error identifier
     * @param args    the error arguments
     */
    public void error(Element e, String errorId, Object... args) {
        grammar.error(e, errorId, args);
    }

    /**
     * Report non fatal grammar error
     *
     * @param e       the element in error
     * @param errorId the error identifier
     */
    public void error(Element e, String errorId) {
        grammar.error(e, errorId);
    }

    /**
     * Gather content according to grammar include hierarchy
     */
    public void implementGrammarInclude() {
        if (context != null) {
            // load content of this context
            loadContent();
        }
        INCLUDE_GATHERER_BY_GRAMMAR_INCLUDE.gatherDefinitions(this);
        // finalize includes
        for (final ContextIncludeView civ : contextIncludes.values()) {
            if (!contextIncludesNode
                    .addParentNode(civ.referencedContext().contextIncludesNode)) {
                Element e;
                if (civ.originalDefinition().definingContext() == this) {
                    // if defined in this context
                    e = civ.contextIncludeElement();
                } else {
                    e = reportingContext;
                }
                error(e, "grammar.Context.ContextInclude.cyclicContextInclude",
                        civ.referencedContext().name());
            }
        }

        IMPORT_GATHERER_BY_GRAMMAR_INCLUDE.gatherDefinitions(this);
        DEFINITION_GATHERER_BY_GRAMMAR_INCLUDE.gatherDefinitions(this);
    }

    /**
     * implement context include
     */
    public void implementContextInclude() {
        INCLUDE_GATHERER_BY_CONTEXT_INCLUDE.gatherDefinitions(this);
        IMPORT_GATHERER_BY_CONTEXT_INCLUDE.gatherDefinitions(this);
        DEFINITION_GATHERER_BY_CONTEXT_INCLUDE.gatherDefinitions(this);
    }

    /**
     * @return grammar that holds this context
     */
    public GrammarView grammar() {
        return grammar;
    }

    /**
     * @return true if wrapped context is abstract
     */
    public boolean isDefault() {
        return context != null && context.defaultModifier != null;
    }

    /**
     * Sorts definitions by categories
     */
    public void sortDefinitionsByCategories() {
        assert !isAbstract() : "This method should not be called for abstract contexts";
        for (final ContextImportView v : imports.values()) {
            if (v.referencedContext().isAbstract()) {
                if (v.definingContext() == this) {
                    error(v.definition(), "grammar.Context.ContextImport.nonAbstractContextImportsAbstractContext",
                            v.localName(), v.referencedContext().grammar()
                            .getSystemId(), v.referencedContext()
                            .name());
                } else {
                    error("grammar.Context.ContextImport.includedImportsAbstractContext",
                            name(),
                            v.definingContext().grammar().getSystemId(),
                            v.definingContext().name(),
                            v.localName(),
                            v.referencedContext().grammar().getSystemId(),
                            v.referencedContext().name());
                }
            }
        }
        for (final DefinitionView m : definitions.values()) {
            if (m instanceof OpDefinitionView) {
                final OpDefinitionView op = (OpDefinitionView) m;
                final Associativity associativity = op.associativity();
                if (associativity == null) {
                    // the definition is invalid. there likely were a parse
                    // error. So nothing is reported here.
                    continue;
                }
                final Integer precedence = op.precedence();
                OpLevel level = operatorLevels.get(precedence);
                if (level == null) {
                    level = new OpLevel();
                    level.precedence = precedence;
                    operatorLevels.put(precedence, level);
                }
                if (Associativity.F != associativity && 0 == precedence) {
                    error(op.definition(), "grammar.Context.OperatorDefinition.wrongPrecedenceAssociativity",
                            precedence, associativity);
                    continue;
                }
                switch (associativity) {
                    case F:
                        level.f.add(op);
                        break;
                    case FX:
                        level.fx.add(op);
                        break;
                    case FY:
                        level.fy.add(op);
                        break;
                    case YF:
                        level.yf.add(op);
                        break;
                    case YFX:
                        level.yfx.add(op);
                        break;
                    case XF:
                        level.xf.add(op);
                        break;
                    case XFX:
                        level.xfx.add(op);
                        break;
                    case XFY:
                        level.xfy.add(op);
                        break;
                    case YFY:
                        level.yfy.add(op);
                        break;
                    default:
                        throw new RuntimeException("[BUG] Unsupported precedence level: " + associativity);
                }
            } else if (m instanceof AttributesView) {
                final AttributesView view = (AttributesView) m;
                if (attributes == null) {
                    attributes = view;
                } else {
                    error("grammar.Context.multipleAttributesSpecifications",
                            attributes.definition().name, view.definition().name);
                }
            } else if (m instanceof DocumentationView) {
                final DocumentationView view = (DocumentationView) m;
                if (documentation == null) {
                    documentation = view;
                } else {
                    error("grammar.Context.multipleDocumentationSpecifications",
                            documentation.definition().name, view.definition().name);
                }
            } else if (m instanceof StatementView) {
                final StatementView view = (StatementView) m;
                statements.add(view);
            } else if (m instanceof DefView) {
                final DefView view = (DefView) m;
                defs.put(view.definition().name.text(), view);
            } else {
                throw new RuntimeException("[BUG] Unsupported definition kind: " + m.getClass().getName());
            }
        }
        if (operatorLevels.get(0) != null) {
            // Establish links between operator levels
            OpLevel previous = null;
            for (final OpLevel current : operatorLevels.values()) {
                current.previousLevel = previous;
                if (previous != null) {
                    previous.nextLevel = current;
                }
                previous = current;
            }
        } else {
            if (!operatorLevels.isEmpty()) {
                error("grammar.Context.noPrimaryLevelInExpressions", name());
                operatorLevels.clear();
            }
        }
    }

    /**
     * @return documentation specification for this context or null if no
     *         documentation is specified
     */
    public DocumentationView documentation() {
        return documentation;
    }

    /**
     * @return attributes specification for this context or null if no
     *         attributes specification is specified
     */
    public AttributesView attributes() {
        return attributes;
    }

    /**
     * Get definition for name
     *
     * @param name a name of definition
     * @return a definition or null if definition is not found
     */
    public DefView def(String name) {
        return defs.get(name);
    }

    /**
     * @return a set of statements
     */
    public Set<StatementView> statements() {
        return statements;
    }

    /**
     * @return operator level for all expressions or null if expressions are not
     *         allowed in this context
     */
    public OpLevel allExpressionsLevel() {
        if (operatorLevels.isEmpty()) {
            return null;
        }
        return operatorLevels.get(operatorLevels.lastKey());
    }

    /**
     * Resolve reference in the context.
     *
     * @param d an actual definition that holds this reference
     * @param s a reference to resolve
     * @return a DefView or null if reference cannot be resolved.
     */
    public DefView def(DefinitionView d, RefOp s) {
        final DefView rc = defs.get(s.name.text());
        if (rc == null) {
            d.definingContext().error(s, "grammar.Ref.danglingRef", s.name);
        }
        return rc;
    }

    /**
     * Get a chain of wrapper links for including context
     *
     * @param ci a included context
     * @return a chain of wrappers
     */
    public WrapperLink includeWrappers(ContextView ci) {
        if (ci == this) {
            return null;
        }
        final ContextIncludeView view = contextInclude(ci);
        return view.wrappers();
    }

    /**
     * @param ci a context view
     * @return context include
     */
    public ContextIncludeView contextInclude(ContextView ci) {
        return contextIncludes.get(ci.name());
    }

    /**
     * Get context import view by name
     *
     * @param ci a context import name
     * @return a context import view
     */
    public ContextImportView contextImport(String ci) {
        return imports.get(ci);
    }

    /**
     * Get definition by name. Used for testing
     *
     * @param name a name of definition
     * @return a definition for specified name or null.
     */
    public DefinitionView definition(String name) {
        return definitions.get(name);
    }

    /**
     * @return the definition context
     */
    public DefinitionContext getDefinitionContext() {
        return definitionContext;
    }

    /**
     * @return true if wrapped context is abstract
     */
    public boolean isAbstract() {
        if (context != null) {
            return context.abstractModifier != null;
        } else {
            // If context is included from parent grammar and at least one of
            // immediate parents is abstract, treat this context as abstract
            // too.
            for (final Iterator<ContextView> i = grammarIncludesNode.immediateParentsIterator(); i.hasNext(); ) {
                final ContextView v = i.next();
                if (v.isAbstract()) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Abstract class for context import gatherers
     */
    private abstract static class ContextImportGatherer extends
            ImportDefinitionGatherer<ContextView, String, ContextImportView, ContextView> {

        /**
         * @return the errorId of duplicate import
         */
        protected abstract String duplicateErrorId();

        @Override
        protected void reportDuplicateImportError(ContextView sourceHolder, String key) {
            sourceHolder.error(duplicateErrorId(), key);
        }

        @Override
        protected ContextView importedObject(ContextImportView importDefinition) {
            return importDefinition.referencedContext();
        }

        @Override
        protected String definitionKey(ContextImportView definition) {
            return definition.localName();
        }
    }

    /**
     * Gatherer of definitions by grammar include.
     */
    private static abstract class ContextDefinitionGatherer extends
            DefinitionGatherer<ContextView, String, DefinitionView> {
        /**
         * Error id
         */
        final String errorId;

        /**
         * The constructor
         *
         * @param id id of duplicate definitions error
         */
        public ContextDefinitionGatherer(String id) {
            this.errorId = id;
        }


        @Override
        protected void reportDuplicates(ContextView sourceHolder, String key, HashSet<DefinitionView> duplicateNodes) {
            sourceHolder.error(errorId, key);
        }


        @Override
        protected String definitionKey(DefinitionView definition) {
            return definition.name();
        }


        @Override
        protected Map<String, DefinitionView> definitionMap(ContextView holder) {
            return holder.definitions;
        }
    }

    /**
     * Gatherer of definitions by grammar include.
     */
    private static class DefinitionGathererByGrammarInclude extends ContextDefinitionGatherer {

        /**
         * The constructor
         */
        public DefinitionGathererByGrammarInclude() {
            super("grammar.Context.Definition.duplicateDefinitionByGrammarInclude");
        }

        @Override
        protected Node<ContextView> getHolderNode(ContextView definitionHolder) {
            return definitionHolder.grammarIncludesNode;
        }

        @Override
        protected Node<ContextView> definitionNode(DefinitionView definition) {
            return definition.originalDefinition().definingContext().grammarIncludesNode;
        }

        @Override
        protected DefinitionView originalDefinition(DefinitionView def) {
            return def.originalDefinition();
        }

        @Override
        protected DefinitionView includingDefinition(ContextView sourceHolder, DefinitionView object) {
            return DefinitionView.get(sourceHolder, object);
        }
    }

    /**
     * Gatherer of definitions by context include.
     */
    private static class DefinitionGathererByContextInclude extends ContextDefinitionGatherer {

        /**
         * The constructor
         */
        public DefinitionGathererByContextInclude() {
            super("grammar.Context.Definition.duplicateDefinitionByContextInclude");
        }

        @Override
        protected Node<ContextView> getHolderNode(ContextView definitionHolder) {
            return definitionHolder.contextIncludesNode;
        }

        @Override
        protected Node<ContextView> definitionNode(DefinitionView definition) {
            return definition.includingContext().contextIncludesNode;
        }
    }

    /**
     * Gatherer imports by grammar include hierarchy
     */
    private static class ImportGathererByGrammarInclude extends ContextImportGatherer {

        @Override
        protected ContextImportView originalDefinition(ContextImportView def) {
            return (def).originalDefinition();
        }

        @Override
        protected ContextImportView includingDefinition(ContextView sourceHolder, ContextImportView object) {
            return new ContextImportView(sourceHolder, object);
        }

        @Override
        protected Node<ContextView> getHolderNode(ContextView definitionHolder) {
            return definitionHolder.grammarIncludesNode;
        }

        @Override
        protected Node<ContextView> definitionNode(ContextImportView definition) {
            return definition.originalDefinition().definingContext().grammarIncludesNode;
        }

        @Override
        protected Map<String, ContextImportView> definitionMap(
                ContextView sourceHolder) {
            return sourceHolder.imports;
        }

        @Override
        protected String duplicateErrorId() {
            return "grammar.Context.ContextImport.duplicateImportByGrammarInclude";
        }
    }

    /**
     * Gatherer imports by grammar include hierarchy
     */
    private static class ImportGathererByContextInclude extends
            ContextImportGatherer {
        @Override
        protected Node<ContextView> getHolderNode(ContextView definitionHolder) {
            return definitionHolder.contextIncludesNode;
        }

        @Override
        protected Node<ContextView> definitionNode(ContextImportView definition) {
            return definition.includingContext().contextIncludesNode;
        }

        @Override
        protected Map<String, ContextImportView> definitionMap(ContextView sourceHolder) {
            return sourceHolder.imports;
        }

        @Override
        protected String duplicateErrorId() {
            return "grammar.Context.ContextImport.duplicateImportByGrammarInclude";
        }
    }

    /**
     * Gatherer of include definitions by grammar include
     *
     * @author const
     */
    private static abstract class ContextIncludeGatherer extends
            DefinitionGatherer<ContextView, String, ContextIncludeView> {
        @Override
        protected String definitionKey(ContextIncludeView definition) {
            return definition.referencedContext().name();
        }

        @Override
        protected Map<String, ContextIncludeView> definitionMap(ContextView holder) {
            return holder.contextIncludes;
        }

        @Override
        protected void reportDuplicates(ContextView holder, String key, HashSet<ContextIncludeView> duplicateNodes) {
            final HashSet<WrapperLink> wrappers = new HashSet<WrapperLink>();
            boolean isFirst = true;
            WrapperLink firstWrapper = null;
            for (final ContextIncludeView include : duplicateNodes) {
                if (isFirst) {
                    firstWrapper = include.wrappers();
                    isFirst = false;
                    wrappers.add(firstWrapper);
                } else {
                    if (!wrappers.contains(include.wrappers())) {
                        holder.error(errorId(), key, String
                                .valueOf(firstWrapper), String.valueOf(include
                                .wrappers()));
                        wrappers.add(include.wrappers());
                    }
                }
            }
        }

        /**
         * @return errorId for error reporting
         */
        protected abstract String errorId();
    }

    /**
     * Gatherer of include definitions by grammar include
     *
     * @author const
     */
    private static class IncludeGathererByGrammarInclude extends ContextIncludeGatherer {
        @Override
        protected ContextIncludeView includingDefinition(ContextView holder, ContextIncludeView definition) {
            return new ContextIncludeView(holder, definition);
        }

        @Override
        protected ContextIncludeView originalDefinition(ContextIncludeView definition) {
            return definition.originalDefinition();
        }

        @Override
        protected String errorId() {
            return "grammar.Context.ContextImport.duplciateWrappersByGrammarInclude";
        }

        @Override
        protected Node<ContextView> getHolderNode(ContextView holder) {
            return holder.grammarIncludesNode;
        }

        @Override
        protected Node<ContextView> definitionNode(ContextIncludeView definition) {
            return definition.originalDefinition().definingContext().grammarIncludesNode;
        }
    }

    /**
     * A gatherer of include definitions by grammar include
     *
     * @author const
     */
    private static class IncludeGathererByContextInclude extends ContextIncludeGatherer {
        @Override
        protected ContextIncludeView includingDefinition(ContextView holder, ContextIncludeView definition) {
            final ContextIncludeView wrappingInclude = holder.contextIncludes.get(definition.includingContext().name());
            assert wrappingInclude != null : "that is immediatly included context";
            return new ContextIncludeView(wrappingInclude, definition);
        }

        @Override
        protected ContextIncludeView originalDefinition(ContextIncludeView definition) {
            return definition.wrappedDefinition();
        }

        @Override
        protected String errorId() {
            return "grammar.Context.ContextImport.duplciateWrappersByContextInclude";
        }

        @Override
        protected Node<ContextView> getHolderNode(ContextView holder) {
            return holder.contextIncludesNode;
        }

        @Override
        protected Node<ContextView> definitionNode(ContextIncludeView definition) {
            return definition.wrappedDefinition().includingContext().contextIncludesNode;
        }
    }
}
