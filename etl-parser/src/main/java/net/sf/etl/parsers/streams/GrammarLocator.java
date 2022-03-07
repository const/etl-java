package net.sf.etl.parsers.streams;

import net.sf.etl.parsers.resource.ResolvedObject;
import net.sf.etl.parsers.resource.ResourceRequest;

/**
 * The interface that locates grammar basing on request.
 */
public interface GrammarLocator {
    /**
     * The grammar loader.
     *
     * @param resourceRequest the resource request
     * @return the URL of grammar or null if the grammar could not be found
     */
    ResolvedObject<String> resolve(ResourceRequest resourceRequest);
}
