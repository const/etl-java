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
package net.sf.etl.parsers.event.grammar.impl.flattened; // NOPMD

import net.sf.etl.parsers.DefinitionContext;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.event.grammar.impl.flattened.DirectedAcyclicGraph.DefinitionGatherer;
import net.sf.etl.parsers.event.grammar.impl.flattened.DirectedAcyclicGraph.ImportDefinitionGatherer;
import net.sf.etl.parsers.event.grammar.impl.flattened.DirectedAcyclicGraph.Node;
import net.sf.etl.parsers.event.unstable.model.grammar.Associativity;
import net.sf.etl.parsers.event.unstable.model.grammar.Context;
import net.sf.etl.parsers.event.unstable.model.grammar.ContextImport;
import net.sf.etl.parsers.event.unstable.model.grammar.ContextInclude;
import net.sf.etl.parsers.event.unstable.model.grammar.ContextMember;
import net.sf.etl.parsers.event.unstable.model.grammar.Element;
import net.sf.etl.parsers.event.unstable.model.grammar.RefOp;
import net.sf.etl.parsers.event.unstable.model.grammar.SyntaxDefinition;
import net.sf.etl.parsers.event.unstable.model.grammar.Wrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * This is a view of context belonging to grammar.
 *
 * @author const
 */
public final class ContextView { // NOPMD
    /**
     * this is gather of imports by grammar include direction.
     */
    private static final IncludeGathererByGrammarInclude INCLUDE_GATHERER_BY_GRAMMAR_INCLUDE
            = new IncludeGathererByGrammarInclude();
    /**
     * this is gather of imports by grammar include direction.
     */
    private static final IncludeGathererByContextInclude INCLUDE_GATHERER_BY_CONTEXT_INCLUDE
            = new IncludeGathererByContextInclude();
    /**
     * this is gather of imports by grammar include direction.
     */
    private static final ImportGathererByGrammarInclude IMPORT_GATHERER_BY_GRAMMAR_INCLUDE
            = new ImportGathererByGrammarInclude();
    /**
     * this is gather of definitions by grammar include direction.
     */
    private static final DefinitionGathererByGrammarInclude DEFINITION_GATHERER_BY_GRAMMAR_INCLUDE
            = new DefinitionGathererByGrammarInclude();
    /**
     * this is gather of imports by grammar include direction.
     */
    private static final ImportGathererByContextInclude IMPORT_GATHERER_BY_CONTEXT_INCLUDE
            = new ImportGathererByContextInclude();
    /**
     * this is gather of definitions by grammar include direction.
     */
    private static final DefinitionGathererByContextInclude DEFINITION_GATHERER_BY_CONTEXT_INCLUDE
            = new DefinitionGathererByContextInclude();
    /**
     * The tree map from precedence to level view object.
     */
    private final NavigableMap<Integer, OpLevel> operatorLevels = new TreeMap<>();
    /**
     * The statements defined in the context.
     */
    private final Set<StatementView> statements = new HashSet<>();
    /**
     * The map from name to definition.
     */
    private final Map<String, DefView> defs = new HashMap<>(); // NOPMD
    /**
     * The map for choice definitions.
     */
    private final Map<String, ChoiceDefView> choiceDefs = new HashMap<>(); // NOPMD
    /**
     * The map for choice cases definitions.
     */
    private final Map<String, List<ChoiceCaseDefView>> choiceCaseDefs = // NOPMD
            new HashMap<>();
    /**
     * DAG node in context include hierarchy.
     */
    private final Node<ContextView> contextIncludesNode;
    /**
     * The map from included context name to context include views.
     */
    private final Map<String, ContextIncludeView> contextIncludes = new HashMap<>(); // NOPMD
    /**
     * The DAG node in context include along with grammar include hierarchy.
     */
    private final Node<ContextView> grammarIncludesNode;
    /**
     * The map that contains imports. key is local name of context.
     */
    private final Map<String, ContextImportView> imports = new HashMap<>(); // NOPMD
    /**
     * The map that definitions. key is name of definition.
     */
    private final Map<String, DefinitionView> definitions = new HashMap<>(); // NOPMD
    /**
     * The context name.
     */
    private final String name;
    /**
     * The grammar view for this context view.
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
     * The definition context.
     */
    private final DefinitionContext definitionContext;
    /**
     * The attributes specification for context.
     */
    private AttributesView attributes;
    /**
     * The documentation specification for context.
     */
    private DocumentationView documentation;

    /**
     * A constructor for view.
     *
     * @param context                the context that is wrapped by this view. It may be null in case
     *                               if grammar does not have a view defined.
     * @param grammar                the grammar view that contains this context view
     * @param name                   the name of context
     * @param contextGrammarIncludes the context grammar include graph
     * @param contextContextIncludes the context content include graph
     */
    public ContextView(final GrammarView grammar, final String name, final Context context,
                       final DirectedAcyclicGraph<ContextView> contextGrammarIncludes,
                       final DirectedAcyclicGraph<ContextView> contextContextIncludes) {
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
     * Gather content from context.
     */
    private void loadContent() {
        assert context != null;
        for (final ContextMember m : context.getContent()) {
            if (m instanceof ContextInclude ci) {
                final ContextView referencedContext = grammar.context(ci.getContextName());
                if (referencedContext == null) {
                    error(ci, "grammar.Context.ContextInclude.missingContext", ci.getContextName());
                    continue;
                }
                // check if duplicate include
                if (contextIncludes.containsKey(referencedContext.name())) {
                    error(ci, "grammar.Context.ContextInclude.duplicateInclude", ci.getContextName());
                    continue;
                }
                // create wrapper link
                WrapperLink wrapperLink = null;
                final ListIterator<Wrapper> j = ci.getWrappers().listIterator(ci.getWrappers().size());
                while (j.hasPrevious()) {
                    final Wrapper w = j.previous();
                    if (w.getObject() != null && w.getObject().getName() != null && w.getObject().getPrefix() != null
                            && w.getProperty() != null) {
                        // otherwise there were syntax error
                        final String objectName = w.getObject().getName().text();
                        final String prefix = w.getObject().getPrefix().text();
                        final String property = w.getProperty().text();
                        final String namespace = grammar.namespace(prefix);
                        if (namespace == null) {
                            error(w, "grammar.Wrapper.undefinedWrapperPrefix", prefix);
                        } else {
                            wrapperLink = new WrapperLink(wrapperLink, namespace, objectName, property, // NOPMD
                                    w.getObject().getSourceLocation(),
                                    new SourceLocation(w.getProperty().start(), w.getProperty().end(), // NOPMD
                                            w.getLocation().systemId()));
                        }
                    }
                }
                contextIncludes.put(referencedContext.name(),
                        new ContextIncludeView(ci, this, referencedContext, wrapperLink)); // NOPMD
            } else if (m instanceof ContextImport ci) {
                final GrammarView referencedGrammar;
                if (ci.getGrammarName() == null) {
                    referencedGrammar = grammar;
                } else {
                    referencedGrammar = grammar.getImportedGrammar(ci.getGrammarName());
                    if (referencedGrammar == null) {
                        error(ci, "grammar.Context.ContextImport.missingGrammarImport", ci.getGrammarName());
                        continue;
                    }
                }
                final ContextView referencedContext = referencedGrammar.context(ci.getContextName());
                if (referencedContext == null) {
                    if (referencedGrammar == grammar) { // NOPMD
                        error(ci, "grammar.Context.ContextImport.missingContext", ci.getContextName());
                    } else {
                        error(ci, "grammar.Context.ContextImport.missingContextInGrammar",
                                ci.getContextName(), referencedGrammar.getSystemId());
                    }
                    continue;
                }
                if (imports.containsKey(ci.getLocalName())) {
                    error(ci, "grammar.Context.ContextImport.duplicateImport", ci.getContextName());
                    continue;
                }
                final ContextImportView view = new ContextImportView(ci, this, referencedContext); // NOPMD
                imports.put(view.localName(), view);
            } else if (m instanceof SyntaxDefinition def) {
                if (definitions.containsKey(def.getName().text())) {
                    error(def, "grammar.Context.Definition.duplicateDefinition", def.getName());
                    continue;
                }
                definitions.put(def.getName().text(), DefinitionView.get(this, def));
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
     * Associate this context with context from parent grammar.
     *
     * @param pgc the context from parent grammar
     */
    public void processGrammarInclude(final ContextView pgc) {
        // add parent along with grammar include path
        final boolean rc = grammarIncludesNode.addParentNode(pgc.grammarIncludesNode);
        assert rc : "It is assumed that adding parent will never fail "
                + "because graph following grammar include hierarchy "
                + "that already does not have cycles";
    }

    /**
     * Report non fatal grammar error.
     *
     * @param errorId the error identifier
     * @param args    the error arguments
     */
    public void error(final String errorId, final Object... args) {
        error(reportingContext, errorId, args);
    }

    /**
     * @return the reporting context
     */
    public Element reportingContext() {
        return reportingContext;
    }


    /**
     * Report non fatal grammar error.
     *
     * @param e       the element in error
     * @param errorId the error identifier
     * @param args    the error arguments
     */
    public void error(final Element e, final String errorId, final Object... args) {
        grammar.error(e, errorId, args);
    }

    /**
     * Report non fatal grammar error.
     *
     * @param e       the element in error
     * @param errorId the error identifier
     */
    public void error(final Element e, final String errorId) {
        grammar.error(e, errorId);
    }

    /**
     * Gather content according to grammar include hierarchy.
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
     * implement context include.
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
        return context != null && context.getDefaultModifier() != null;
    }

    /**
     * Sorts definitions by categories.
     */
    public void sortDefinitionsByCategories() { // NOPMD
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
            if (m instanceof OpDefinitionView op) {
                final Associativity associativity = op.associativity();
                if (associativity == null) {
                    // the definition is invalid. there likely were a parse
                    // error. So nothing is reported here.
                    continue;
                }
                final Integer precedence = op.precedence();
                OpLevel level = operatorLevels.get(precedence);
                if (level == null) {
                    level = new OpLevel(); // NOPMD
                    level.setPrecedence(precedence);
                    operatorLevels.put(precedence, level);
                }
                if (Associativity.F != associativity && 0 == precedence) {
                    error(op.operator(), "grammar.Context.OperatorDefinition.wrongPrecedenceAssociativity",
                            precedence, associativity);
                    continue;
                }
                switch (associativity) {
                    case F:
                        level.getF().add(op);
                        break;
                    case FX:
                        level.getFX().add(op);
                        break;
                    case FY:
                        level.getFY().add(op);
                        break;
                    case YF:
                        level.getYF().add(op);
                        break;
                    case YFX:
                        level.getYFX().add(op);
                        break;
                    case XF:
                        level.getXF().add(op);
                        break;
                    case XFX:
                        level.getXFX().add(op);
                        break;
                    case XFY:
                        level.getXFY().add(op);
                        break;
                    case YFY:
                        level.getYFY().add(op);
                        break;
                    default:
                        throw new IllegalStateException("[BUG] Unsupported precedence level: " + associativity);
                }
            } else if (m instanceof AttributesView) {
                final AttributesView view = (AttributesView) m;
                if (attributes == null) {
                    attributes = view;
                } else {
                    error("grammar.Context.multipleAttributesSpecifications",
                            attributes.definition().getName(), view.definition().getName());
                }
            } else if (m instanceof DocumentationView) {
                final DocumentationView view = (DocumentationView) m;
                if (documentation == null) {
                    documentation = view;
                } else {
                    error("grammar.Context.multipleDocumentationSpecifications",
                            documentation.definition().getName(), view.definition().getName());
                }
            } else if (m instanceof StatementView) {
                final StatementView view = (StatementView) m;
                statements.add(view);
            } else if (m instanceof DefView) {
                final DefView view = (DefView) m;
                defs.put(view.definition().getName().text(), view);
            } else if (m instanceof ChoiceDefView) {
                final ChoiceDefView view = (ChoiceDefView) m;
                choiceDefs.put(view.name(), view);
            } else if (m instanceof ChoiceCaseDefView) {
                final ChoiceCaseDefView view = (ChoiceCaseDefView) m;
                final String choiceName = view.choiceName();
                if (choiceName != null) {
                    // the name == null only in the case of syntax error, so it was reported earlier
                    List<ChoiceCaseDefView> choice = choiceCaseDefs.get(choiceName);
                    if (choice == null) {
                        choice = new ArrayList<ChoiceCaseDefView>(); // NOPMD
                        choiceCaseDefs.put(choiceName, choice);
                    }
                    choice.add(view);
                }
            } else {
                throw new IllegalStateException("[BUG] Unsupported definition kind: " + m.getClass().getName());
            }
        }
        // validate choices
        if (!isAbstract()) {
            for (final Map.Entry<String, ChoiceDefView> entry : choiceDefs.entrySet()) {
                if (!choiceCaseDefs.containsKey(entry.getKey())) {
                    error(reportingContext, "grammar.Context.missingCaseForChoice", name(), entry.getKey(),
                            entry.getValue().definition().getLocation().toShortString());
                }
            }
            for (final Map.Entry<String, List<ChoiceCaseDefView>> entry : choiceCaseDefs.entrySet()) {
                if (!choiceDefs.containsKey(entry.getKey())) {
                    error(reportingContext, "grammar.Context.missingChoiceForCase", name(),
                            entry.getKey(), entry.getValue().size(), entry.getValue().get(0).name(),
                            entry.getValue().get(0).definition().getLocation().toShortString());
                }
            }
        }
        if (operatorLevels.get(0) != null) {
            // Establish links between operator levels
            OpLevel previous = null;
            for (final OpLevel current : operatorLevels.values()) {
                current.setPreviousLevel(previous);
                if (previous != null) {
                    previous.setNextLevel(current);
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
     * documentation is specified
     */
    public DocumentationView documentation() {
        return documentation;
    }

    /**
     * @return attributes specification for this context or null if no
     * attributes specification is specified
     */
    public AttributesView attributes() {
        return attributes;
    }

    /**
     * Get definition for name.
     *
     * @param defName a name of definition
     * @return a definition or null if definition is not found
     */
    public DefView def(final String defName) {
        return defs.get(defName);
    }

    /**
     * Get choice def view.
     *
     * @param choiceName the name of the choice
     * @return the choice
     */
    public List<ChoiceCaseDefView> choice(final String choiceName) {
        return choiceCaseDefs.get(choiceName);
    }

    /**
     * @return a set of statements
     */
    public Set<StatementView> statements() {
        return statements;
    }

    /**
     * @return operator level for all expressions or null if expressions are not
     * allowed in this context
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
    public DefView def(final DefinitionView d, final RefOp s) {
        final DefView rc = defs.get(s.getName().text());
        if (rc == null && !choiceCaseDefs.containsKey(s.getName().text())) {
            d.definingContext().error(s, "grammar.Ref.danglingRef", s.getName());
        }
        return rc;
    }

    /**
     * Resolve reference in the context.
     *
     * @param d an actual definition that holds this reference
     * @param s a reference to resolve
     * @return a DefView or null if reference cannot be resolved.
     */
    public List<ChoiceCaseDefView> choices(final DefinitionView d, final RefOp s) {
        final List<ChoiceCaseDefView> rc = choiceCaseDefs.get(s.getName().text());
        if (rc == null && !defs.containsKey(s.getName().text())) {
            d.definingContext().error(s, "grammar.Ref.danglingRef", s.getName());
        }
        return rc != null ? rc : Collections.emptyList();
    }

    /**
     * Get a chain of wrapper links for including context.
     *
     * @param ci a included context
     * @return a chain of wrappers
     */
    public WrapperLink includeWrappers(final ContextView ci) {
        if (ci == this) {
            return null;
        }
        final ContextIncludeView view = contextInclude(ci);
        return view.wrappers();
    }

    /**
     * Get context include.
     *
     * @param ci a context view
     * @return context include
     */
    public ContextIncludeView contextInclude(final ContextView ci) {
        return contextIncludes.get(ci.name());
    }

    /**
     * Get context import view by name.
     *
     * @param ci a context import name
     * @return a context import view
     */
    public ContextImportView contextImport(final String ci) {
        return imports.get(ci);
    }

    /**
     * Get definition by name. Used for testing.
     *
     * @param definitionName a name of definition
     * @return a definition for specified name or null.
     */
    public DefinitionView definition(final String definitionName) {
        return definitions.get(definitionName);
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
            return context.getAbstractModifier() != null;
        } else {
            // If context is included from parent grammar and at least one of
            // immediate parents is abstract, treat this context as abstract
            // too.
            for (final ContextView v : grammarIncludesNode.immediateParents()) {
                if (v.isAbstract()) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Abstract class for context import gatherers.
     */
    private abstract static class ContextImportGatherer extends
            ImportDefinitionGatherer<ContextView, String, ContextImportView, ContextView> {

        /**
         * @return the errorId of duplicate import
         */
        protected abstract String duplicateErrorId();

        @Override
        protected void reportDuplicateImportError(final ContextView sourceHolder, final String key) {
            sourceHolder.error(duplicateErrorId(), key);
        }

        @Override
        protected ContextView importedObject(final ContextImportView importDefinition) {
            return importDefinition.referencedContext();
        }

        @Override
        protected String definitionKey(final ContextImportView definition) {
            return definition.localName();
        }
    }

    /**
     * Gatherer of definitions by grammar include.
     */
    private abstract static class ContextDefinitionGatherer extends
            DefinitionGatherer<ContextView, String, DefinitionView> {
        /**
         * Error id.
         */
        private final String errorId;

        /**
         * The constructor.
         *
         * @param id id of duplicate definitions error
         */
        protected ContextDefinitionGatherer(final String id) {
            this.errorId = id;
        }


        @Override
        protected void reportDuplicates(final ContextView sourceHolder, final String key,
                                        final Set<DefinitionView> duplicateNodes) {
            sourceHolder.error(errorId, key);
        }


        @Override
        protected String definitionKey(final DefinitionView definition) {
            return definition.name();
        }


        @Override
        protected Map<String, DefinitionView> definitionMap(final ContextView holder) {
            return holder.definitions;
        }
    }

    /**
     * Gatherer of definitions by grammar include.
     */
    private static final class DefinitionGathererByGrammarInclude extends ContextDefinitionGatherer {

        /**
         * The constructor.
         */
        private DefinitionGathererByGrammarInclude() {
            super("grammar.Context.Definition.duplicateDefinitionByGrammarInclude");
        }

        @Override
        protected Node<ContextView> getHolderNode(final ContextView definitionHolder) {
            return definitionHolder.grammarIncludesNode;
        }

        @Override
        protected Node<ContextView> definitionNode(final DefinitionView definition) {
            return definition.originalDefinition().definingContext().grammarIncludesNode;
        }

        @Override
        protected DefinitionView includingDefinition(final ContextView sourceHolder, final DefinitionView object) {
            return DefinitionView.get(sourceHolder, object);
        }
    }

    /**
     * Gatherer of definitions by context include.
     */
    private static final class DefinitionGathererByContextInclude extends ContextDefinitionGatherer {

        /**
         * The constructor.
         */
        private DefinitionGathererByContextInclude() {
            super("grammar.Context.Definition.duplicateDefinitionByContextInclude");
        }

        @Override
        protected DefinitionView includingDefinition(final ContextView sourceHolder, final DefinitionView object) {
            return object;
        }

        @Override
        protected Node<ContextView> getHolderNode(final ContextView definitionHolder) {
            return definitionHolder.contextIncludesNode;
        }

        @Override
        protected Node<ContextView> definitionNode(final DefinitionView definition) {
            return definition.includingContext().contextIncludesNode;
        }
    }

    /**
     * Gatherer imports by grammar include hierarchy.
     */
    private static final class ImportGathererByGrammarInclude extends ContextImportGatherer {

        @Override
        protected ContextImportView includingDefinition(final ContextView sourceHolder,
                                                        final ContextImportView object) {
            return new ContextImportView(sourceHolder, object);
        }

        @Override
        protected Node<ContextView> getHolderNode(final ContextView definitionHolder) {
            return definitionHolder.grammarIncludesNode;
        }

        @Override
        protected Node<ContextView> definitionNode(final ContextImportView definition) {
            return definition.originalDefinition().definingContext().grammarIncludesNode;
        }

        @Override
        protected Map<String, ContextImportView> definitionMap(final ContextView sourceHolder) {
            return sourceHolder.imports;
        }

        @Override
        protected String duplicateErrorId() {
            return "grammar.Context.ContextImport.duplicateImportByGrammarInclude";
        }
    }

    /**
     * Gatherer imports by grammar include hierarchy.
     */
    private static final class ImportGathererByContextInclude extends
            ContextImportGatherer {
        @Override
        protected ContextImportView includingDefinition(final ContextView sourceHolder,
                                                        final ContextImportView object) {
            return object;
        }

        @Override
        protected Node<ContextView> getHolderNode(final ContextView definitionHolder) {
            return definitionHolder.contextIncludesNode;
        }

        @Override
        protected Node<ContextView> definitionNode(final ContextImportView definition) {
            return definition.includingContext().contextIncludesNode;
        }

        @Override
        protected Map<String, ContextImportView> definitionMap(final ContextView sourceHolder) {
            return sourceHolder.imports;
        }

        @Override
        protected String duplicateErrorId() {
            return "grammar.Context.ContextImport.duplicateImportByGrammarInclude";
        }
    }

    /**
     * Gatherer of include definitions by grammar include.
     *
     * @author const
     */
    private abstract static class ContextIncludeGatherer extends
            DefinitionGatherer<ContextView, String, ContextIncludeView> {
        @Override
        protected String definitionKey(final ContextIncludeView definition) {
            return definition.referencedContext().name();
        }

        @Override
        protected Map<String, ContextIncludeView> definitionMap(final ContextView holder) {
            return holder.contextIncludes;
        }

        @Override
        protected void reportDuplicates(final ContextView holder, final String key,
                                        final Set<ContextIncludeView> duplicateNodes) {
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
     * Gatherer of include definitions by grammar include.
     *
     * @author const
     */
    private static class IncludeGathererByGrammarInclude extends ContextIncludeGatherer {
        @Override
        protected ContextIncludeView includingDefinition(final ContextView holder,
                                                         final ContextIncludeView definition) {
            return new ContextIncludeView(holder, definition);
        }

        @Override
        protected String errorId() {
            return "grammar.Context.ContextImport.duplicateWrappersByGrammarInclude";
        }

        @Override
        protected Node<ContextView> getHolderNode(final ContextView holder) {
            return holder.grammarIncludesNode;
        }

        @Override
        protected Node<ContextView> definitionNode(final ContextIncludeView definition) {
            return definition.originalDefinition().definingContext().grammarIncludesNode;
        }
    }

    /**
     * A gatherer of include definitions by grammar include.
     *
     * @author const
     */
    private static class IncludeGathererByContextInclude extends ContextIncludeGatherer {
        @Override
        protected ContextIncludeView includingDefinition(final ContextView holder,
                                                         final ContextIncludeView definition) {
            final ContextIncludeView wrappingInclude = holder.contextIncludes.get(definition.includingContext().name());
            assert wrappingInclude != null : "that is immediately included context";
            return new ContextIncludeView(wrappingInclude, definition);
        }

        @Override
        protected String errorId() {
            return "grammar.Context.ContextImport.duplicateWrappersByContextInclude";
        }

        @Override
        protected Node<ContextView> getHolderNode(final ContextView holder) {
            return holder.contextIncludesNode;
        }

        @Override
        protected Node<ContextView> definitionNode(final ContextIncludeView definition) {
            return definition.wrappedDefinition().includingContext().contextIncludesNode;
        }
    }
}
