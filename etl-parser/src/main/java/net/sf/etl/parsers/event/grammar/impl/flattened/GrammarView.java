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

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.GrammarId;
import net.sf.etl.parsers.GrammarInfo;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.event.grammar.impl.flattened.DirectedAcyclicGraph.Node;
import net.sf.etl.parsers.event.unstable.model.grammar.Context;
import net.sf.etl.parsers.event.unstable.model.grammar.Element;
import net.sf.etl.parsers.event.unstable.model.grammar.Grammar;
import net.sf.etl.parsers.event.unstable.model.grammar.GrammarImport;
import net.sf.etl.parsers.event.unstable.model.grammar.GrammarInclude;
import net.sf.etl.parsers.event.unstable.model.grammar.GrammarMember;
import net.sf.etl.parsers.event.unstable.model.grammar.GrammarRef;
import net.sf.etl.parsers.event.unstable.model.grammar.Namespace;
import net.sf.etl.parsers.literals.LiteralUtils;
import net.sf.etl.parsers.literals.StringInfo;
import net.sf.etl.parsers.literals.StringParser;
import net.sf.etl.parsers.resource.ResolvedObject;
import net.sf.etl.parsers.resource.ResourceDescriptor;
import net.sf.etl.parsers.resource.ResourceRequest;
import net.sf.etl.parsers.resource.ResourceUsage;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A view of individual grammar.
 *
 * @author const
 */
public final class GrammarView implements Serializable { // NOPMD
    /**
     * The gatherer for grammar imports.
     */
    private static final GrammarImportDefinitionGatherer IMPORT_DEFINITION_GATHERER =
            new GrammarImportDefinitionGatherer();
    /**
     * A DAG along with context include paths.
     */
    private final DirectedAcyclicGraph<ContextView> contextContextIncludeDAG = new DirectedAcyclicGraph<>();
    /**
     * The grammar assembly for this grammar view.
     */
    private final GrammarAssembly assembly;
    /**
     * The grammar accessible through this view.
     */
    private final Grammar grammar;
    /**
     * The map from context name to context view.
     */
    private final Map<String, ContextView> contexts = new HashMap<>(); // NOPMD
    /**
     * The map from local name to imported grammar view.
     */
    private final Map<String, GrammarImportView> importedGrammars = new HashMap<String, GrammarImportView>(); // NOPMD
    /**
     * The set of all included grammar views.
     */
    private final Node<GrammarView> includeNode;
    /**
     * The map from local name to namespace URI.
     */
    private final Map<String, String> namespaceDeclarations = new HashMap<String, String>(); // NOPMD
    /**
     * The system id for this grammar.
     */
    private final String systemId;
    /**
     * The name of grammar.
     */
    private final String grammarName;
    /**
     * The first {@link ResolvedObject} with which this grammar was found.
     */
    private final ResolvedObject<Grammar> firstResolved;
    /**
     * The collection of grammar references.
     */
    private final Map<GrammarRef, GrammarId> references = // NOPMD
            new IdentityHashMap<>();
    /**
     * The grammars used for creating this grammar.
     */
    private final Set<ResolvedObject<GrammarView>> usedGrammars = new HashSet<>();
    /**
     * The failed grammars.
     */
    private final List<GrammarAssembly.FailedGrammar> failedGrammars =
            new ArrayList<>();
    /**
     * The grammar info.
     */
    private final GrammarInfo grammarInfo;
    /**
     * The default context for this grammar.
     */
    private ContextView defaultContext;
    /**
     * The default namespace prefix, null if there is no default namespace.
     */
    private Token defaultNamespacePrefix;

    /**
     * The constructor.
     *
     * @param assembly the grammar assembly to which this grammar view belongs
     * @param grammar  the grammar for this grammar view
     */
    public GrammarView(final GrammarAssembly assembly, final ResolvedObject<Grammar> grammar) {
        this.assembly = assembly;
        this.grammar = grammar.getObject();
        this.firstResolved = grammar;
        this.systemId = grammar.getDescriptor().getSystemId();
        this.grammarName = createGrammarName(this.grammar);
        this.includeNode = assembly.getIncludeNode(this);
        this.grammarInfo = new GrammarInfo(systemId, grammarName, parseVersion(grammar.getObject().getVersion()));
    }

    /**
     * Get grammar name from grammar.
     *
     * @param grammar a grammar to examining
     * @return the qualified name of the grammar as a string.
     */
    private static String createGrammarName(final Grammar grammar) {
        final StringBuilder rc = new StringBuilder();
        boolean isFirst = true;
        for (final Token s : grammar.getName()) {
            if (isFirst) {
                isFirst = false;
            } else {
                rc.append('.');
            }
            rc.append(s.text());
        }
        return rc.toString();
    }

    /**
     * Parse version.
     *
     * @param version the version to parse
     * @return the parsed version
     */
    private String parseVersion(final Token version) {
        if (version == null) {
            return null;
        }
        final StringInfo parse = new StringParser(version.text(), version.start(), systemId).parse();
        error(parse.getErrors());
        return parse.getText();
    }

    /**
     * @return the created descriptor for the grammar view.
     */
    public ResourceDescriptor createDescriptor() {
        return createDescriptor(new HashSet<GrammarView>());
    }

    /**
     * Create the descriptor.
     *
     * @param visited the visited grammars
     * @return the descriptor
     */
    private ResourceDescriptor createDescriptor(final Set<GrammarView> visited) {
        final ResourceDescriptor resource = firstResolved.getDescriptor();
        if (visited.contains(this)) {
            return new ResourceDescriptor(resource.getSystemId(), resource.getType(), resource.getVersion());
        } else {
            visited.add(this);
            final ArrayList<ResourceUsage> resourceUsages = new ArrayList<ResourceUsage>();
            resourceUsages.addAll(resource.getUsedResources());
            for (final ResolvedObject<GrammarView> used : usedGrammars) {
                resourceUsages.addAll(used.getResolutionHistory());
                resourceUsages.add(new ResourceUsage(
                        used.getObject().createDescriptor(visited),
                        StandardGrammars.USED_GRAMMAR_REQUEST_TYPE));
            }
            for (final GrammarAssembly.FailedGrammar failedGrammar : failedGrammars) {
                resourceUsages.addAll(failedGrammar.usedResources());
            }
            visited.remove(this);
            return new ResourceDescriptor(resource.getSystemId(), resource.getType(), resource.getVersion(),
                    resourceUsages);
        }
    }

    /**
     * @return grammars referenced from this grammar
     */
    public Set<GrammarId> referencedGrammars() {
        final HashSet<GrammarId> requests = new HashSet<GrammarId>();
        for (final GrammarMember m : grammar.getContent()) {
            if (m instanceof GrammarImport) {
                var request = toReference((GrammarImport) m);
                if (request != null) {
                    requests.add(request);
                }
            } else if (m instanceof GrammarInclude) {
                var request = toReference((GrammarInclude) m);
                if (request != null) {
                    requests.add(request);
                }
            }
        }
        return requests;
    }

    /**
     * Create resource reference from grammar reference.
     *
     * @param ref the grammar reference
     * @return the resource reference
     */
    private GrammarId toReference(final GrammarRef ref) {
        return references.computeIfAbsent(ref, r -> {
            var parsed = GrammarId.parse(ref.getLocation(), ref.getQualifiedName(), ref.getVersion());
            if (parsed.errors() != null) {
                error(parsed.errors());
            }
            return parsed.result();
        });
    }

    /**
     * Parse string token.
     *
     * @param value the value to parse
     * @return the parsed value
     */
    private StringInfo parse(final Token value) {
        return value == null ? null : new StringParser(value.text(), value.start(), getSystemId()).parse();
    }

    /**
     * This method examines grammar and gathers initial information about it.
     */
    public void prepareContexts() {
        for (final GrammarMember m : grammar.getContent()) {
            if (m instanceof GrammarImport) {
                final GrammarImport gi = (GrammarImport) m;
                final GrammarView importedGrammar = getReferencedGrammar(gi);
                if (importedGrammar == null) {
                    failedGrammars.add(assembly.failure(
                            new ResourceRequest(toReference(gi),
                                    firstResolved.getDescriptor().getSystemId(),
                                    firstResolved.getRequest().contextUrl()))); // NOPMD
                } else if (importedGrammars.containsKey(gi.getName().text())) {
                    error(gi, "grammar.Grammar.duplicateImport", gi.getName());
                } else {
                    importedGrammars.put(gi.getName().text(), new GrammarImportView(this, // NOPMD
                            gi, importedGrammar));
                }
            } else if (m instanceof GrammarInclude) {
                final GrammarInclude gi = (GrammarInclude) m;
                final GrammarView includedGrammar = getReferencedGrammar(gi);
                if (includedGrammar == null) {
                    failedGrammars.add(assembly.failure(
                            new ResourceRequest(toReference(gi),
                                    firstResolved.getDescriptor().getSystemId(),
                                    firstResolved.getRequest().contextUrl()))); // NOPMD
                } else if (includeNode.hasImmediateParent(includedGrammar)) {
                    error(gi, "grammar.Grammar.duplicateInclude");
                } else if (!includeNode.addParent(includedGrammar)) {
                    error(gi, "grammar.Grammar.cyclicInclude");
                }
            } else if (m instanceof Namespace) {
                final Namespace ns = (Namespace) m;
                if (namespaceDeclarations.containsKey(ns.getPrefix().text())) {
                    error(ns, "grammar.Grammar.duplicateNamespaceDeclaration",
                            ns.getPrefix(), namespaceDeclarations.get(ns.getPrefix().text()));
                }
                try {
                    // attempt to parse string as URI and return error if it is
                    // impossible
                    new URI(LiteralUtils.parseString(ns.getUri().text())); // NOPMD
                } catch (final Exception ex) { // NOPMD
                    error(ns, "grammar.Grammar.invalidUriInNamespaceDeclaration",
                            ns.getPrefix(), ns.getUri());
                }
                namespaceDeclarations.put(ns.getPrefix().text(), ns.getUri().text());
                if (ns.getDefaultModifier() != null) {
                    if (defaultNamespacePrefix != null) {
                        error(ns, "grammar.Grammar.duplicateDefaultNamespace",
                                defaultNamespacePrefix.text(),
                                namespaceDeclarations.get(defaultNamespacePrefix.text()));
                    } else {
                        defaultNamespacePrefix = ns.getPrefix();
                    }
                }
            } else if (m instanceof Context) {
                final Context c = (Context) m;
                if (contexts.containsKey(c.getName())) {
                    error(c, "grammar.Grammar.duplicateContext", c.getName());
                } else {
                    getOrCreateContext(c.getName(), c);
                }
            }
        }
    }

    /**
     * Report non fatal grammar error.
     *
     * @param e       the element in error
     * @param errorId the error identifier
     * @param args    the error arguments
     */
    public void error(final Element e, final String errorId, final Object... args) {
        assembly.error(e, errorId, args);
    }


    /**
     * Add error.
     *
     * @param error the error to add
     */
    public void error(final ErrorInfo error) {
        assembly.error(error);
    }

    /**
     * Add new error related to token in this grammar.
     *
     * @param token   the token
     * @param errorId the error id
     * @param arg     the error arg
     */
    public void error(final Token token, final String errorId, final Object arg) {
        assembly.error(new ErrorInfo(errorId,
                Collections.singletonList(arg),
                new SourceLocation(token.start(), token.end(), systemId),
                null));
    }


    /**
     * Get grammar referenced by grammar import or include.
     *
     * @param gr grammar reference
     * @return view of referenced grammar
     */
    public GrammarView getReferencedGrammar(final GrammarRef gr) {
        final ResolvedObject<GrammarView> resolvedObject =
                assembly.resolveGrammar(toReference(gr));
        if (resolvedObject == null) {
            assert assembly.hadErrors();
            return null;
        }
        usedGrammars.add(resolvedObject);
        return resolvedObject.getObject();
    }

    /**
     * @return the grammar AST object
     */
    public Grammar getGrammar() {
        return grammar;
    }

    /**
     * Gather grammar imports related to grammar.
     */
    public void gatherImports() {
        IMPORT_DEFINITION_GATHERER.gatherDefinitions(this);
    }

    /**
     * Build context using grammar include relationship.
     */
    public void buildContexts() {
        for (final GrammarView pg : includeNode.immediateParents()) {
            for (final ContextView pgc : pg.contexts.values()) {
                final ContextView lc = getOrCreateContext(pgc.name());
                lc.processGrammarInclude(pgc);
            }
        }
    }

    /**
     * Get or create context by name.
     *
     * @param name the name of context
     * @return the context for the specified name
     */
    public ContextView getOrCreateContext(final String name) {
        return getOrCreateContext(name, null);
    }

    /**
     * Get context by name.
     *
     * @param name the name of context
     * @param c    the context
     * @return the context for the specified name
     */
    private ContextView getOrCreateContext(final String name, final Context c) {
        ContextView lc = contexts.get(name);
        if (lc == null) {
            lc = new ContextView(this, name, c, assembly.contextGrammarIncludeDAG(), contextContextIncludeDAG());
            contexts.put(lc.name(), lc);
        }
        return lc;
    }

    /**
     * Get imported grammar.
     *
     * @param grammarImportName the grammar import name
     * @return the imported grammar or null if grammar does not exists
     */
    public GrammarView getImportedGrammar(final String grammarImportName) {
        final GrammarImportView importView = importedGrammars.get(grammarImportName);
        return importView == null ? null : importView.getImportedGrammar();
    }

    /**
     * Get current context without creating it on demand.
     *
     * @param contextName the context name
     * @return the context that exists or null.
     */
    public ContextView context(final String contextName) {
        return contexts.get(contextName);
    }

    /**
     * @return the system id of the grammar
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * @return the DAG for context include hierarchy
     */
    public DirectedAcyclicGraph<ContextView> contextContextIncludeDAG() {
        return contextContextIncludeDAG;
    }

    /**
     * @return true if wrapped grammar is abstract
     */
    public boolean isAbstract() {
        return grammar.getAbstractModifier() != null;
    }

    /**
     * flatten grammar according to internal extension constructs.
     */
    public void flattenGrammar() {
        assert !isAbstract() : "This method should not be called for abstract grammars";
        // check if imported grammars are non abstract
        for (final GrammarImportView grammarImport : importedGrammars.values()) {
            if (grammarImport.getImportedGrammar().isAbstract()) {
                if (grammarImport.getSourceGrammar() == this) {
                    error(grammarImport.getGrammarImport(),
                            "grammar.GrammarImport.importOfAbstractGrammar",
                            grammarImport.getGrammarImport().getName(),
                            grammarImport.getImportedGrammar().getSystemId());
                } else {
                    error(grammar,
                            "grammar.GrammarImport.includedImportOfAbstractGrammar",
                            grammarImport.getSourceGrammar().getSystemId(),
                            grammarImport.getGrammarImport().getName(),
                            grammarImport.getImportedGrammar().getSystemId());
                }
            }
        }
        // minimize immediate include references.
        contextContextIncludeDAG.minimizeImmediate();
        final List<ContextView> sortedContexts = contextContextIncludeDAG.topologicalSortObjects();
        for (final ContextView v : sortedContexts) {
            // Gather imports and definitions according to context inclusion
            // hierarchy
            v.implementContextInclude();
            // Sort non abstract context definitions
            if (!v.isAbstract()) {
                v.sortDefinitionsByCategories();
                if (v.isDefault()) {
                    if (defaultContext == null) {
                        defaultContext = v;
                    } else {
                        v.error("grammar.Context.duplicateDefault", v.name());
                    }
                }
            } else {
                if (v.isDefault()) {
                    v.error("grammar.Context.abstractDefault", v.name());
                }
            }
        }
    }

    /**
     * Get string that decodes to namespace URI.
     *
     * @param prefix the namespace prefix
     * @return the namespace for the prefix
     */
    public String namespace(final String prefix) {
        final String namespace = namespaceDeclarations.get(prefix);
        if (namespace == null) {
            return null;
        }
        return LiteralUtils.parseString(namespace);
    }

    /**
     * @return the prefix of the default namespace
     */
    public Token defaultNamespacePrefix() {
        return defaultNamespacePrefix;
    }

    /**
     * @return the collection of contexts
     */
    public Collection<ContextView> contexts() {
        return contexts.values();
    }

    /**
     * @return the grammar name.
     */
    public String grammarName() {
        return grammarName;
    }


    /**
     * Note that this method should be called only after all dependencies are
     * loaded.
     *
     * @return grammars on which this grammar depends, including itself
     */
    public Collection<GrammarView> getGrammarDependencies() {
        final HashSet<GrammarView> visitedGrammars = new HashSet<GrammarView>();
        this.getGrammarDependencies(visitedGrammars);
        return visitedGrammars;
    }

    /**
     * Visit grammars on which this grammar view depends.
     *
     * @param visitedGrammars the collection to which grammars are gathered.
     */
    private void getGrammarDependencies(final Set<GrammarView> visitedGrammars) {
        // If grammar is already visited, dependencies either already here
        // or are in process being added.
        if (!visitedGrammars.contains(this)) {
            visitedGrammars.add(this);
            // get dependencies of the parent grammars
            for (final GrammarView v : includeNode.immediateParents()) {
                v.getGrammarDependencies(visitedGrammars);
            }
            // get dependencies of imported grammar
            for (final GrammarImportView v : importedGrammars.values()) {
                v.getImportedGrammar().getGrammarDependencies(visitedGrammars);
            }
        }
        // In unlikely case when this method is bottleneck,
        // caching might be introduced.
    }

    /**
     * @return the first resolved object
     */
    public ResolvedObject<Grammar> getFirstResolved() {
        return firstResolved;
    }

    /**
     * @return the grammar information
     */
    public GrammarInfo grammarInfo() {
        return grammarInfo;
    }

    /**
     * The gatherer algorithm object gathers definition across definition
     * holders organized as DAG.
     *
     * @author const
     */
    private static class GrammarImportDefinitionGatherer extends
            DirectedAcyclicGraph.ImportDefinitionGatherer<GrammarView, String, GrammarImportView, GrammarView> {
        @Override
        protected GrammarImportView includingDefinition(final GrammarView sourceHolder,
                                                        final GrammarImportView object) {
            return object;
        }

        @Override
        protected Node<GrammarView> getHolderNode(final GrammarView definitionHolder) {
            return definitionHolder.includeNode;
        }

        @Override
        protected Node<GrammarView> definitionNode(final GrammarImportView definition) {
            return definition.getSourceGrammar().includeNode;
        }

        @Override
        protected String definitionKey(final GrammarImportView definition) {
            return definition.getGrammarImport().getName().text();
        }

        @Override
        protected Map<String, GrammarImportView> definitionMap(final GrammarView holder) {
            return holder.importedGrammars;
        }

        @Override
        protected void reportDuplicateImportError(final GrammarView sourceHolder, final String key) {
            sourceHolder.error(sourceHolder.getGrammar(), "grammar.Grammar.duplicateIncludedImportNames", key);
        }

        @Override
        protected GrammarView importedObject(final GrammarImportView importDefinition) {
            return importDefinition.getImportedGrammar();
        }
    }
}
