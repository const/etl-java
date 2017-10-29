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

package net.sf.etl.xml_catalog.event.engine; //NOPMD

import net.sf.etl.xml_catalog.event.CatalogFile;
import net.sf.etl.xml_catalog.event.CatalogRequest;
import net.sf.etl.xml_catalog.event.CatalogResolutionEvent;
import net.sf.etl.xml_catalog.event.CatalogResourceUsage;
import net.sf.etl.xml_catalog.event.CatalogResult;
import net.sf.etl.xml_catalog.event.CatalogResultTrace;
import net.sf.etl.xml_catalog.event.engine.impl.step.EntityResolutionStep;
import net.sf.etl.xml_catalog.event.engine.impl.step.ResolutionStep;
import net.sf.etl.xml_catalog.event.engine.impl.step.TR9401ResolutionStep;
import net.sf.etl.xml_catalog.event.engine.impl.step.URIResolutionStep;
import net.sf.etl.xml_catalog.event.entries.tr9401.DTDDeclEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.DoctypeEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.DocumentEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.EntityEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.LinkTypeEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.NotationEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.SGMLDeclEntry;
import net.sf.etl.xml_catalog.util.PublicId;
import net.sf.etl.xml_catalog.util.URIUtil;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * The event-driven resolver for catalogs.
 * TODO what if systemId is a file on file system that exists?
 */
public final class CatalogEngine {
    /**
     * The already processed catalogs, in order to prevent infinite loops.
     */
    private final Set<String> processedCatalogs = new HashSet<String>();
    /**
     * The request stack for the catalogs.
     */
    private final List<CatalogRequest> requestStack = new ArrayList<CatalogRequest>();
    /**
     * The catalog result.
     */
    private CatalogResult result;
    /**
     * The current catalog request.
     */
    private CatalogRequest currentRequest;
    /**
     * The current catalog file.
     */
    private CatalogFile currentFile;
    /**
     * The current catalog trace.
     */
    private List<CatalogResourceUsage> currentResolutionResources;
    /**
     * The current catalog problem.
     */
    private Throwable currentProblem;
    /**
     * The current resolution trace.
     */
    private CatalogResultTrace previousTrace;
    /**
     * The resolution process.
     */
    private DocumentProcessor resolutionProcess;
    /**
     * The current warning.
     */
    private CatalogResultTrace.Warning warning;

    /**
     * Start processing new catalog request.
     *
     * @param request the request to set
     * @return false if catalog is already processed
     */
    private boolean startRequest(final CatalogRequest request) {
        if (processedCatalogs.contains(request.getSystemId())) {
            return false;
        }
        currentRequest = request;
        processedCatalogs.add(request.getSystemId());
        return true;
    }

    /**
     * Enlist warning.
     *
     * @param info the information
     */
    private void warning(final String info) {
        warning = new CatalogResultTrace.Warning(info, warning);
    }

    /**
     * Ensure that resolution process is not yet started.
     *
     * @param initialCatalog the initial catalog
     */
    private void startResolve(final CatalogRequest initialCatalog) {
        if (resolutionProcess != null) {
            throw new IllegalStateException("The resolution process is not yet started!");
        }
        startRequest(initialCatalog);
    }

    /**
     * Start resolution of entity against catalog.
     *
     * @param initialCatalog the initial catalog to start resolution
     * @param publicId       the public identifier for the resource
     * @param systemId       the public identifier for the resource
     * @param baseUri        the base uri
     * @param preferPublic   the value of prefer public attribute
     */
    public void startResolveEntity(final CatalogRequest initialCatalog, final String publicId, final String systemId,
                                   final String baseUri, final boolean preferPublic) {
        startResolve(initialCatalog);
        if (systemId == null && publicId == null) {
            resolutionProcess = new NullProcessor();
        } else {
            resolutionProcess = new EntityProcessor(publicId, systemId, baseUri, preferPublic);
        }
    }

    /**
     * Start resolution of URI catalog.
     *
     * @param initialCatalog the initial catalog to start resolution
     * @param uri            the URI to resolve
     * @param nature         the uri nature (RDDL) (nullable)
     * @param purpose        the uri purpose (RDDL) (nullable)
     * @param baseUri        the base uri
     */
    public void startResolveURI(final CatalogRequest initialCatalog, final String uri, //NOPMD
                                final String nature, final String purpose, final String baseUri) {

        startRequest(initialCatalog);
        if (uri == null) {
            resolutionProcess = new NullProcessor();
        } else {
            resolutionProcess = new UriProcessor(uri, nature, purpose, baseUri);
        }
    }

    // CHECKSTYLE:OFF

    /**
     * Resolve resource.
     *
     * @param initialCatalog the initial catalog to start resolution
     * @param uri            the resource namespace
     * @param nature         the nature
     * @param purpose        the purpose
     * @param publicId       the public id
     * @param systemId       the system id
     * @param baseUri        the base URI to use
     * @param preferPublic   the initial value of prefer public
     */
    public void startResource(final CatalogRequest initialCatalog, final String uri, //NOPMD
                              final String nature, final String purpose,
                              final String publicId, final String systemId,
                              final String baseUri, final boolean preferPublic) {
        startRequest(initialCatalog);
        if (uri == null && systemId == null && publicId == null) {
            resolutionProcess = new NullProcessor();
        } else {
            resolutionProcess = new EntityUriProcessor(uri, nature, purpose, publicId, systemId, baseUri, preferPublic);
        }
    }
    // CHECKSTYLE:ON


    /**
     * Start doctype resolution.
     *
     * @param initialCatalog      the initial catalog
     * @param name                the entity name
     * @param publicId            the public id
     * @param systemId            the system id
     * @param baseUri             the base URI to use
     * @param defaultPreferPublic the default prefer public value
     */
    public void startDoctype(final CatalogRequest initialCatalog, final String name, //NOPMD
                             final String publicId, final String systemId,
                             final String baseUri, final boolean defaultPreferPublic) {
        startRequest(initialCatalog);
        if (name == null && systemId == null && publicId == null) {
            resolutionProcess = new NullProcessor();
        } else {
            resolutionProcess = new TR9401Processor(publicId, systemId, baseUri, defaultPreferPublic, "doctype", name);
        }
    }

    /**
     * Start notation resolution.
     *
     * @param initialCatalog      the initial catalog
     * @param name                the entity name
     * @param publicId            the public id
     * @param systemId            the system id
     * @param baseUri             the base URI to use
     * @param defaultPreferPublic the default prefer public value
     */
    public void startNotation(final CatalogRequest initialCatalog, final String name, // NOPMD
                              final String publicId, final String systemId,
                              final String baseUri, final boolean defaultPreferPublic) {
        startRequest(initialCatalog);
        if (name == null && systemId == null && publicId == null) {
            resolutionProcess = new NullProcessor();
        } else {
            resolutionProcess = new TR9401Processor(publicId, systemId, baseUri, defaultPreferPublic, "notation", name);
        }
    }


    /**
     * Catalog that should be resolved.
     *
     * @return the catalog
     */
    public CatalogRequest getCatalogRequest() {
        if (currentRequest == null) {
            throw new IllegalStateException("The resolver is not requesting anything");
        }
        if (currentFile != null) {
            throw new IllegalStateException("The resolution has been already provided");
        }
        return currentRequest;
    }

    /**
     * Resolve with resolution event.
     *
     * @param event the event to use
     */
    public void resolve(final CatalogResolutionEvent event) {
        if (event.getFile() != null) {
            resolve(event.getRequest(), event.getResolutionHistory(), event.getFile());
        } else {
            missing(event.getRequest(), event.getResolutionHistory(), event.getProblem());
        }
    }

    /**
     * The requested resolved catalog is resolved.
     *
     * @param request             the catalog request
     * @param resolutionResources the catalog resolution resources
     * @param catalogFile         the resolved catalog file with loaded dom model
     */
    public void resolve(final CatalogRequest request, final List<CatalogResourceUsage> resolutionResources,
                        final CatalogFile catalogFile) {
        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null");
        }
        if (catalogFile == null) {
            throw new IllegalArgumentException("The catalog file cannot be null");
        }
        if (!request.equals(currentRequest)) {
            throw new IllegalArgumentException("The catalog " + request + " has not been requested yet.");
        }
        if (currentFile != null || currentProblem != null) {
            throw new IllegalStateException("The catalog has been already provided");
        }
        currentResolutionResources = resolutionResources;
        currentFile = catalogFile;
    }

    /**
     * The requested catalog file is missing.
     *
     * @param request             the catalog request
     * @param resolutionResources the catalog resolution resources
     * @param problem             the exception that caused abandoning the process
     */
    public void missing(final CatalogRequest request, final List<CatalogResourceUsage> resolutionResources,
                        final Throwable problem) {
        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null");
        }
        if (!request.equals(currentRequest)) {
            throw new IllegalArgumentException("The catalog " + request + " has not been requested yet.");
        }
        if (currentFile != null || currentProblem != null) {
            throw new IllegalStateException("The catalog has been already provided");
        }
        currentResolutionResources = resolutionResources;
        currentProblem = problem;
        startNextCatalog();
    }

    /**
     * Set result.
     *
     * @param uri      the result URI
     * @param location the location
     */
    private void setResult(final String uri, final String location) {
        finishCurrent(location);
        requestStack.clear();
        result = new CatalogResult(uri, true, previousTrace);
    }


    /**
     * Finish work on teh current catalog.
     *
     * @param location the location
     */
    private void finishCurrent(final String location) {
        previousTrace = new CatalogResultTrace(currentRequest, currentResolutionResources,
                currentFile, currentProblem, location, previousTrace, warning);
        currentResolutionResources = null;
        currentProblem = null;
        currentFile = null;
        currentRequest = null;
        warning = null;
    }


    /**
     * Start processing of the next catalog.
     */
    private void startNextCatalog() {
        if (currentFile != null) {
            addCatalogsToStack(currentFile.getNextCatalogs());
        }
        finishCurrent(null);
        processNextCatalogs();
    }

    /**
     * Process next catalogs.
     */
    private void processNextCatalogs() {
        while (!requestStack.isEmpty()) {
            final CatalogRequest request = requestStack.remove(requestStack.size() - 1);
            if (startRequest(request)) {
                break;
            }
        }
        if (currentRequest == null) {
            result = new CatalogResult(resolutionProcess.getFallbackResult(), false, previousTrace);
        }
    }

    /**
     * Start delegation.
     *
     * @param requests the requests
     */
    private void startDelegation(final List<CatalogRequest> requests) {
        requestStack.clear();
        addCatalogsToStack(requests);
        finishCurrent(null);
        processNextCatalogs();
    }

    /**
     * Add catalogs to the stack, so they will be processed in the same order.
     *
     * @param requests the requests
     */
    private void addCatalogsToStack(final List<CatalogRequest> requests) {
        final ListIterator<CatalogRequest> i = requests.listIterator(requests.size());
        while (i.hasPrevious()) {
            requestStack.add(i.previous());
        }
    }

    /**
     * Process available catalogs.
     *
     * @return the processing result
     */
    public CatalogEngineStatus process() {
        return process(true);
    }

    /**
     * Process the catalog.
     *
     * @param repeatAllowed if repeating is allowed.
     * @return the engine status
     */
    private CatalogEngineStatus process(final boolean repeatAllowed) {
        if (resolutionProcess == null) {
            return CatalogEngineStatus.NOT_STARTED;
        }
        if (result != null) {
            return CatalogEngineStatus.RESULT_AVAILABLE;
        }
        if (currentRequest != null && currentFile == null) {
            return CatalogEngineStatus.CATALOG_NEEDED;
        }
        resolutionProcess.process(currentFile);
        if (repeatAllowed) {
            // something should have changed as result of processing the file
            return process(false);
        } else {
            throw new IllegalStateException("[BUG] The processor has done nothing!");
        }
    }

    /**
     * @return get catalog result after status is {@link CatalogEngineStatus}
     */
    public CatalogResult result() {
        if (result == null) {
            throw new IllegalStateException("Result is not yet available!");
        }
        return result;
    }

    /**
     * The document processor.
     */
    private abstract class DocumentProcessor {
        /**
         * Process a document node.
         *
         * @param file the node to process
         */
        protected abstract void process(final CatalogFile file);

        /**
         * Parse node and do common processing.
         *
         * @param process the resolution step
         * @param file    the file to process
         * @return if processor has done something and work on this step should stopped
         */
        protected final boolean parseNode(final ResolutionStep process, final CatalogFile file) {
            process.resolve(file.getRootEntry());
            final URI uri = process.getResolvedUri();
            if (uri != null) {
                setResult(uri.toASCIIString(), process.getResolvedEntry().getId());
                return true;
            }
            final List<CatalogRequest> delegates = process.getResolvedDelegates();
            if (delegates != null) {
                startDelegation(delegates);
                return true;
            }
            return false;
        }

        /**
         * Make relative URI and generate a warning if failed.
         *
         * @param name      the name
         * @param reference the URI
         * @param base      the base to resolve it
         * @return the relative form of URI
         */
        protected final String makeRelativeUri(final String name, final String reference, final String base) {
            if (reference != null) {
                String normalized = URIUtil.normalizeURI(reference);
                if (!normalized.equals(reference)) {
                    warning(name + " was normalized to " + normalized + " from " + reference);
                }
                try {
                    final URI uri = URI.create(normalized);
                    if (!uri.isAbsolute() && !uri.isOpaque()) {
                        URI baseUri;
                        if (base == null) {
                            // the current directory
                            baseUri = new File(".").toURI();
                        } else {
                            normalized = URIUtil.normalizeURI(base);
                            if (normalized.equals(reference)) {
                                warning("Base for " + name + " was normalized to " + normalized + " from " + base);
                            }
                            baseUri = URI.create(normalized);
                        }
                        normalized = baseUri.resolve(uri).toString();
                    }
                } catch (Throwable ex) {
                    warning("Failed to resolve " + name + ": " + ex + " " + reference
                            + (base == null ? "" : " (" + base + ")"));
                }
                return normalized;
            } else {
                return null;
            }
        }

        /**
         * @return the fallback result
         */
        public String getFallbackResult() { // NOPMD
            return null;
        }
    }

    /**
     * The process that just stops the process.
     */
    private final class NullProcessor extends DocumentProcessor {

        @Override
        protected void process(final CatalogFile file) {
            warning("All significant arguments are null, there is nothing to do");
            startDelegation(Collections.<CatalogRequest>emptyList());
        }
    }

    /**
     * The entity processor.
     */
    private class EntityProcessor extends DocumentProcessor {
        /**
         * The value of prefer public attribute.
         */
        private final boolean preferPublic;
        /**
         * The system id.
         */
        private String systemId;
        /**
         * The public id.
         */
        private String publicId;

        /**
         * The constructor for the entity processor. It normalize the urls and public ids that it receives.
         * It also tries to resolve system id relatively to the base.
         *
         * @param publicId     the public id
         * @param systemId     the system id
         * @param base         the base URI
         * @param preferPublic the value of prefer public attribute
         */
        protected EntityProcessor(final String publicId, final String systemId, final String base,
                                  final boolean preferPublic) {
            this.publicId = PublicId.normalize(publicId);
            this.systemId = makeRelativeUri("systemId", systemId, base);
            if (PublicId.isPublicIdURN(this.systemId)) {
                final String systemPublicId = PublicId.decodeURN(this.systemId);
                this.systemId = null;
                if (this.publicId == null) {
                    this.publicId = systemPublicId;
                } else {
                    if (!publicId.equals(systemPublicId)) {
                        warning("The public id produced from system '" + systemPublicId
                                + "' is different from the specified public id '" + publicId + "'");
                    }
                }
            }
            this.preferPublic = preferPublic;
        }


        @Override
        protected void process(final CatalogFile file) {
            if (!processEntityStep(file)) {
                startNextCatalog();
            }
        }

        /**
         * Do one entity step.
         *
         * @param file the file to process
         * @return true if parsed something
         */
        protected final boolean processEntityStep(final CatalogFile file) {
            final EntityResolutionStep entityResolutionStep = new EntityResolutionStep(
                    publicId, systemId, preferPublic);
            if (parseNode(entityResolutionStep, file)) {
                systemId = entityResolutionStep.getSystemId();
                publicId = entityResolutionStep.getPublicId();
                return true;
            }
            return false;
        }

        /**
         * @return the fallback result
         */
        public String getFallbackResult() {
            return systemId;
        }

        /**
         * @return the system id
         */
        protected String getSystemId() {
            return systemId;
        }

        /**
         * @return the public id
         */
        protected String getPublicId() {
            return publicId;
        }

        /**
         * @return the prefer public
         */
        protected boolean isPreferPublic() {
            return preferPublic;
        }
    }


    /**
     * Plain URI processor that consider entities.
     */
    private final class EntityUriProcessor extends EntityProcessor {
        /**
         * The URI to resolve.
         */
        private final String uri;
        /**
         * The nature of URI to resolve.
         */
        private final String nature;
        /**
         * The purpose of URI to resolve.
         */
        private final String purpose;

        /**
         * The constructor.
         *
         * @param uri          the uri to resolve
         * @param nature       the nature of uri
         * @param purpose      the purpose of uri
         * @param publicId     the public id (might be null)
         * @param systemId     the system id (might be null)
         * @param baseUri      the base uri (might be null)
         * @param preferPublic the default value of prefer public attribute
         */
        private EntityUriProcessor(final String uri, final String nature, final String purpose, final String publicId,
                                   final String systemId, final String baseUri, final boolean preferPublic) {
            super(publicId, systemId, baseUri, preferPublic);
            this.uri = makeRelativeUri("uri", uri, baseUri);
            this.nature = makeRelativeUri("nature", nature, baseUri);
            this.purpose = makeRelativeUri("purpose", purpose, baseUri);
        }

        @Override
        protected void process(final CatalogFile file) {
            if ((getPublicId() == null && getSystemId() == null || !processEntityStep(file))
                    && !parseNode(new URIResolutionStep(uri, nature, purpose), file)) {
                startNextCatalog();
            }
        }
    }

    /**
     * Plain URI processor that does not consider entities.
     */
    private final class UriProcessor extends DocumentProcessor {
        /**
         * The URI to resolve.
         */
        private final String uri;
        /**
         * The nature of URI to resolve.
         */
        private final String nature;
        /**
         * The purpose of URI to resolve.
         */
        private final String purpose;

        /**
         * The constructor.
         *
         * @param uri     the uri to resolve
         * @param nature  the nature of uri
         * @param purpose the purpose of uri
         * @param baseUri the base URI
         */
        private UriProcessor(final String uri, final String nature, final String purpose, final String baseUri) {
            this.uri = makeRelativeUri("uri", uri, baseUri);
            this.nature = makeRelativeUri("nature", nature, baseUri);
            this.purpose = makeRelativeUri("purpose", purpose, baseUri);
        }

        @Override
        protected void process(final CatalogFile file) {
            if (!parseNode(new URIResolutionStep(uri, nature, purpose), file)) {
                startNextCatalog();
            }
        }
    }

    /**
     * The process or TR9401 elements in the catalog along with entities.
     */
    private final class TR9401Processor extends EntityProcessor {
        /**
         * The type of element.
         */
        private final Class<?> type;
        /**
         * The name of element or null.
         */
        private final String name;

        /**
         * The constructor.
         *
         * @param publicId     the public id (might be null)
         * @param systemId     the system id (might be null)
         * @param baseUri      the base uri (might be null)
         * @param preferPublic the default value of prefer public attribute
         * @param type         the type of TR9401 entity to resolve
         * @param name         the name of TR9401 entity to resolve
         */
        private TR9401Processor(final String publicId, final String systemId, final String baseUri,
                                final boolean preferPublic, final String type, final String name) {
            super(publicId, systemId, baseUri, preferPublic);
            this.type = convertType(type);
            this.name = name;
        }

        /**
         * Covert the type to class.
         *
         * @param tagName the type
         * @return the resulting type
         */
        private Class<?> convertType(final String tagName) {
            if ("doctype".equals(tagName)) {
                return DoctypeEntry.class;
            } else if ("document".equals(tagName)) {
                return DocumentEntry.class;
            } else if ("dtddecl".equals(tagName)) {
                return DTDDeclEntry.class;
            } else if ("entity".equals(tagName)) {
                return EntityEntry.class;
            } else if ("linktype".equals(tagName)) {
                return LinkTypeEntry.class;
            } else if ("notation".equals(tagName)) {
                return NotationEntry.class;
            } else if ("sgmldecl".equals(tagName)) {
                return SGMLDeclEntry.class;
            } else {
                throw new IllegalArgumentException("Invalid TR9401 tag name: " + tagName);
            }
        }

        @Override
        protected void process(final CatalogFile file) {
            if ((getPublicId() == null && getSystemId() == null || !processEntityStep(file))
                    && !parseNode(new TR9401ResolutionStep(type, name, false, isPreferPublic()), file)) {
                startNextCatalog();
            }
        }
    }
}
