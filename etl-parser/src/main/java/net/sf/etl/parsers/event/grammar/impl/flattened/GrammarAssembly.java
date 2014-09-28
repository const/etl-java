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

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.event.unstable.model.grammar.Element;
import net.sf.etl.parsers.event.unstable.model.grammar.Grammar;
import net.sf.etl.parsers.resource.ResolvedObject;
import net.sf.etl.parsers.resource.ResourceReference;
import net.sf.etl.parsers.resource.ResourceRequest;
import net.sf.etl.parsers.resource.ResourceUsage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The assembly of grammars, this class used to gather to manage process
 * of loading the grammars and building their views.
 */
public final class GrammarAssembly {
    /**
     * The grammar inclusion DAG.
     */
    private final DirectedAcyclicGraph<GrammarView> grammarIncludeDAG = new DirectedAcyclicGraph<GrammarView>();
    /**
     * The context inclusions along with grammars.
     */
    private final DirectedAcyclicGraph<ContextView> contextGrammarIncludeDAG = new DirectedAcyclicGraph<ContextView>();
    /**
     * The collection of errors.
     */
    private final List<ErrorInfo> errors = new ArrayList<ErrorInfo>();
    /**
     * All resource requests.
     */
    private final Set<ResourceReference> allResourceReferences = new HashSet<ResourceReference>();
    /**
     * All unresolved resource requests.
     */
    private final Set<ResourceRequest> unresolvedResourceRequests = new HashSet<ResourceRequest>();
    /**
     * All loaded grammar views.
     */
    private final Map<String, GrammarView> grammarViews = new HashMap<String, GrammarView>(); // NOPMD
    /**
     * The resolutions.
     */
    private final Map<ResourceReference, ResolvedObject<Grammar>> resolutions = // NOPMD
            new HashMap<ResourceReference, ResolvedObject<Grammar>>();
    /**
     * The failed grammars.
     */
    private final Map<ResourceRequest, FailedGrammar> failed = new HashMap<ResourceRequest, FailedGrammar>(); // NOPMD
    /**
     * The initial reference.
     */
    private ResourceRequest initialRequest;


    /**
     * @return the context grammar include DAG
     */
    public DirectedAcyclicGraph<ContextView> contextGrammarIncludeDAG() {
        return contextGrammarIncludeDAG;
    }

    /**
     * Get resolved grammar.
     *
     * @param systemId the system id
     * @return the grammar
     */
    public ResolvedObject<Grammar> resolvedGrammar(final String systemId) {
        final GrammarView grammarView = grammarViews.get(systemId);
        return grammarView == null ? null : grammarView.getFirstResolved();
    }

    /**
     * The provided grammar.
     *
     * @param grammar       the provided grammar
     * @param grammarErrors the errors collected while loading the grammar
     */
    public void provide(final ResolvedObject<Grammar> grammar, final ErrorInfo grammarErrors) {
        final String systemId = grammar.getDescriptor().getSystemId();
        GrammarView grammarView = grammarViews.get(systemId);
        if (grammarView == null) {
            grammarView = new GrammarView(this, grammar);
            grammarViews.put(systemId, grammarView);
            final Set<ResourceReference> references = grammarView.referencedGrammars();
            references.removeAll(allResourceReferences);
            allResourceReferences.addAll(references);
            for (final ResourceReference reference : references) {
                unresolvedResourceRequests.add(new ResourceRequest(reference, // NOPMD
                        StandardGrammars.USED_GRAMMAR_REQUEST_TYPE));
            }
        }
        resolutions.put(grammar.getRequest().getReference(), grammar);
        unresolvedResourceRequests.remove(grammar.getRequest());
        error(grammarErrors);
    }

    /**
     * Get or create code for the grammar view.
     *
     * @param grammarView the grammar view
     * @return the included node
     */
    public DirectedAcyclicGraph.Node<GrammarView> getIncludeNode(final GrammarView grammarView) {
        return grammarIncludeDAG.getNode(grammarView);
    }

    /**
     * @return the set of unresolved resource requests
     */
    public Set<ResourceRequest> unresolved() {
        return unresolvedResourceRequests;
    }

    /**
     * @return the grammars
     */
    public Collection<GrammarView> grammars() {
        return grammarViews.values();
    }

    /**
     * Resolve grammar.
     *
     * @param reference the resource reference
     * @return the grammar view to use
     */
    public ResolvedObject<GrammarView> resolveGrammar(final ResourceReference reference) {
        final ResolvedObject<Grammar> grammarResolvedObject = resolutions.get(reference);
        if (grammarResolvedObject == null) {
            return null;
        }
        final GrammarView view = grammarViews.get(grammarResolvedObject.getDescriptor().getSystemId());
        return new ResolvedObject<GrammarView>(grammarResolvedObject.getRequest(),
                grammarResolvedObject.getResolutionHistory(),
                grammarResolvedObject.getDescriptor(), view);
    }

    /**
     * @return true if there were errors
     */
    public boolean hadErrors() {
        return !errors.isEmpty();
    }

    /**
     * Add error.
     *
     * @param element the element in error
     * @param errorId the error id
     * @param args    the error args
     */
    public void error(final Element element, final String errorId, final Object... args) {
        error(new ErrorInfo(errorId, Arrays.asList(args), element == null ? null : element.getLocation(), null));
    }

    /**
     * Add error to the list of errors.
     *
     * @param error the error to add
     */
    public void error(final ErrorInfo error) {
        // TODO errors are specific to grammar view rather than a single big pile
        if (error != null) {
            errors.add(error);
        }
    }

    /**
     * Start loading grammars.
     *
     * @param reference the resource reference
     */
    public void start(final ResourceRequest reference) {
        initialRequest = reference;
        unresolvedResourceRequests.add(reference);
        allResourceReferences.add(reference.getReference());
    }

    /**
     * Fail loading resource request.
     *
     * @param request       the resource request
     * @param resources     the used resources
     * @param grammarErrors the list of errors associated with the resource request
     */
    public void fail(final ResourceRequest request, final Collection<ResourceUsage> resources,
                     final ErrorInfo grammarErrors) {
        unresolvedResourceRequests.remove(request);
        failed.put(request, new FailedGrammar(request, grammarErrors, new ArrayList<ResourceUsage>(resources)));
        error(grammarErrors);
    }

    /**
     * The failed grammar.
     *
     * @param request the request
     * @return the failed grammar
     */
    public FailedGrammar failure(final ResourceRequest request) {
        return failed.get(request);
    }


    /**
     * Flatten the grammars.
     */
    public void flatten() { // NOPMD
        final ResolvedObject<Grammar> rootGrammar = resolutions.get(initialRequest.getReference());
        if (rootGrammar != null && rootGrammar.getObject() != null
                && rootGrammar.getObject().getAbstractModifier() != null) {
            error(rootGrammar.getObject(), "grammar.AbstractRootGrammar",
                    rootGrammar.getDescriptor().getSystemId());
        }
        for (final GrammarView grammarView : grammarViews.values()) {
            grammarView.prepareContexts();
        }
        if (hadErrors()) {
            return;
        }
        // M1: All referenced grammars are loaded.
        grammarIncludeDAG.minimizeImmediate();
        final List<GrammarView> grammars = grammarIncludeDAG.topologicalSortObjects();
        for (final GrammarView v : grammars) {
            v.gatherImports();
        }
        if (hadErrors()) {
            return;
        }
        // M2: All grammars has set of imports built.
        for (final GrammarView v : grammars) {
            v.buildContexts();
        }
        if (hadErrors()) {
            return;
        }
        // M3: All contexts for grammars are created, include relationships are
        // created.
        contextGrammarIncludeDAG.minimizeImmediate();
        final List<ContextView> contextsByGrammarInclude = contextGrammarIncludeDAG.topologicalSortObjects();
        for (final ContextView v : contextsByGrammarInclude) {
            v.implementGrammarInclude();
        }
        if (hadErrors()) {
            return;
        }
        // M4: Imports and definitions are gathered by direction of grammar include
        // After this step it is not necessary to process abstract grammars and all
        // processing is done locally to grammars because it does not have to
        // deal with cross grammar definitions.
        for (final GrammarView v : grammars) {
            if (!v.isAbstract()) {
                v.flattenGrammar();
            }
        }
        // NOTE POST 0.2: Should be there validation phase?
    }

    /**
     * @return all errors
     */
    public ErrorInfo getErrors() {
        return ErrorInfo.merge(errors);
    }

    /**
     * Get failed grammar information.
     *
     * @param reference the reference
     * @return the failed grammar information
     */
    public FailedGrammar getFailedGrammar(final ResourceRequest reference) {
        return failed.get(reference);
    }

    /**
     * The failed grammar.
     */
    public static final class FailedGrammar {
        /**
         * The request.
         */
        private final ResourceRequest request;
        /**
         * the error information.
         */
        private final ErrorInfo errors;
        /**
         * The resources consulted to receive the failure.
         */
        private final List<ResourceUsage> usedResources;

        /**
         * The constructor.
         *
         * @param request       the request
         * @param errors        the error information
         * @param usedResources the used resources
         */
        public FailedGrammar(final ResourceRequest request, final ErrorInfo errors,
                             final List<ResourceUsage> usedResources) {
            this.request = request;
            this.errors = errors;
            this.usedResources = usedResources;
        }

        @Override
        public String toString() {
            return "FailedGrammar{request=" + request + ", errors=" + errors + ", usedResources=" + usedResources + '}';
        }

        /**
         * @return the request
         */
        public ResourceRequest getRequest() {
            return request;
        }

        /**
         * @return the errors
         */
        public ErrorInfo getErrors() {
            return errors;
        }

        /**
         * @return the used resources
         */
        public List<ResourceUsage> getUsedResources() {
            return usedResources;
        }
    }
}
