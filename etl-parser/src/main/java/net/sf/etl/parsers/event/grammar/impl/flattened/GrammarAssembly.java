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

import java.util.*;

/**
 * The assembly of grammars, this class used to gather to manage process
 * of loading the grammars and building their views.
 */
public class GrammarAssembly {
    /**
     * The grammar inclusion DAG
     */
    private final DirectedAcyclicGraph<GrammarView> grammarIncludeDAG = new DirectedAcyclicGraph<GrammarView>();
    /**
     * The context inclusions along with grammars
     */
    private final DirectedAcyclicGraph<ContextView> contextGrammarIncludeDAG = new DirectedAcyclicGraph<ContextView>();
    /**
     * The collection of errors
     */
    private final ArrayList<ErrorInfo> errors = new ArrayList<ErrorInfo>();
    /**
     * All resource requests
     */
    private final HashSet<ResourceReference> allResourceReferences = new HashSet<ResourceReference>();
    /**
     * All resource requests
     */
    private final HashSet<ResourceRequest> unresolvedResourceRequests = new HashSet<ResourceRequest>();
    /**
     * All loaded grammar views
     */
    private final HashMap<String, GrammarView> grammarViews = new HashMap<String, GrammarView>();
    /**
     * The resolutions
     */
    private final HashMap<ResourceReference, ResolvedObject<Grammar>> resolutions = new HashMap<ResourceReference, ResolvedObject<Grammar>>();
    /**
     * The failed grammars
     */
    private HashMap<ResourceRequest, FailedGrammar> failed = new HashMap<ResourceRequest, FailedGrammar>();


    /**
     * @return the context grammar include DAG
     */
    public DirectedAcyclicGraph<ContextView> contextGrammarIncludeDAG() {
        return contextGrammarIncludeDAG;
    }

    /**
     * Get resolved grammar
     *
     * @param systemId the system id
     * @return the grammar
     */
    public ResolvedObject<Grammar> resolvedGrammar(String systemId) {
        final GrammarView grammarView = grammarViews.get(systemId);
        return grammarView == null ? null : grammarView.getFirstResolved();
    }

    /**
     * The provided grammar
     *
     * @param grammar the provided grammar
     * @param errors  the errors collected while loading the grammar
     */
    public void provide(ResolvedObject<Grammar> grammar, ErrorInfo errors) {
        final String systemId = grammar.getDescriptor().getSystemId();
        GrammarView grammarView = grammarViews.get(systemId);
        if (grammarView == null) {
            grammarView = new GrammarView(this, grammar);
            grammarViews.put(systemId, grammarView);
            final Set<ResourceReference> references = grammarView.referencedGrammars();
            references.removeAll(allResourceReferences);
            allResourceReferences.addAll(references);
            for (ResourceReference reference : references) {
                unresolvedResourceRequests.add(new ResourceRequest(reference, StandardGrammars.USED_GRAMMAR_REQUEST_TYPE));
            }
        }
        resolutions.put(grammar.getRequest().getReference(), grammar);
        unresolvedResourceRequests.remove(grammar.getRequest());
        error(errors);
    }

    /**
     * Get or create code for the grammar view
     *
     * @param grammarView the grammar view
     * @return the included node
     */
    public DirectedAcyclicGraph.Node<GrammarView> getIncludeNode(GrammarView grammarView) {
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
     * Resolve grammar
     *
     * @param reference the resource reference
     * @return the grammar view to use
     */
    public ResolvedObject<GrammarView> resolveGrammar(ResourceReference reference) {
        final ResolvedObject<Grammar> grammarResolvedObject = resolutions.get(reference);
        if (grammarResolvedObject == null) {
            return null;
        }
        final GrammarView view = grammarViews.get(grammarResolvedObject.getDescriptor().getSystemId());
        return new ResolvedObject<GrammarView>(grammarResolvedObject.getRequest(), grammarResolvedObject.getResolutionHistory(), grammarResolvedObject.getDescriptor(), view);
    }

    /**
     * @return true if there were errors
     */
    public boolean hadErrors() {
        return !errors.isEmpty();
    }

    /**
     * Add error
     *
     * @param element the element in error
     * @param errorId the error id
     * @param args    the error args
     */
    public void error(Element element, String errorId, Object[] args) {
        error(new ErrorInfo(errorId, Arrays.asList(args), element.location, null));
    }

    /**
     * Add error to the list of errors
     *
     * @param error the error to add
     */
    public void error(ErrorInfo error) {
        // TODO errors are specific to grammar view rather than a single big pile
        if (error != null) {
            errors.add(error);
        }
    }

    /**
     * Start loading grammars
     *
     * @param reference the resource reference
     */
    public void start(ResourceRequest reference) {
        unresolvedResourceRequests.add(reference);
        allResourceReferences.add(reference.getReference());
    }

    /**
     * Fail loading resource request
     *
     * @param request the resource request
     * @param errors  the list of errors associated with the resource request
     */
    public void fail(ResourceRequest request, Collection<ResourceUsage> resources, ErrorInfo errors) {
        unresolvedResourceRequests.remove(request);
        failed.put(request, new FailedGrammar(request, errors, resources));
        error(errors);
    }

    /**
     * The failed grammar
     *
     * @param request the request
     * @return the failed grammar
     */
    public FailedGrammar failure(ResourceRequest request) {
        return failed.get(request);
    }


    public void flatten() {
        /**
         TODO make sense of check below
         if (rootGrammar.isAbstract()) {
         error("grammar.AbstractRootGrammar", rootGrammar.getSystemId(),
         source.getPublicId());
         }
         **/
        for (GrammarView grammarView : grammarViews.values()) {
            grammarView.prepareContexts();
        }
        if (hadErrors()) {
            return;
        }
        // M1: All referenced grammars are loaded.
        grammarIncludeDAG.minimizeImmediate();
        final List<GrammarView> grammars = grammarIncludeDAG
                .topologicalSortObjects();
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
        final List<ContextView> contextsByGrammarInclude = contextGrammarIncludeDAG
                .topologicalSortObjects();
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

    public ErrorInfo getErrors() {
        return ErrorInfo.merge(errors);
    }

    /**
     * The failed grammar
     */
    public static class FailedGrammar {
        /**
         * The request
         */
        public final ResourceRequest request;
        /**
         * the error information
         */
        public final ErrorInfo errors;
        /**
         * The resources consulted to receive the failure
         */
        public final Collection<ResourceUsage> usedResources;

        /**
         * The constructor
         *
         * @param request       the request
         * @param errors        the error information
         * @param usedResources the used resources
         */
        public FailedGrammar(ResourceRequest request, ErrorInfo errors, Collection<ResourceUsage> usedResources) {
            this.request = request;
            this.errors = errors;
            this.usedResources = usedResources;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("FailedGrammar");
            sb.append("{request=").append(request);
            sb.append(", errors=").append(errors);
            sb.append(", usedResources=").append(usedResources);
            sb.append('}');
            return sb.toString();
        }
    }
}
