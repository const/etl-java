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

package net.sf.etl.xml_catalog.blocking.provider;

import net.sf.etl.xml_catalog.event.CatalogRequest;
import net.sf.etl.xml_catalog.event.CatalogResolutionEvent;
import net.sf.etl.xml_catalog.event.CatalogResourceUsage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The catalog cache by system id. Note it does not track dependencies between catalogs.
 * Only dependencies of the catalog itself.
 */
public final class CatalogResolutionCache {
    /**
     * The maximum size of the cache.
     */
    public static final int DEFAULT_CACHE_SIZE = 256;
    /**
     * The cache size.
     */
    private final Map<String, CatalogResolutionEvent> cache = // NOPMD
            new LinkedHashMap<String, CatalogResolutionEvent>(DEFAULT_CACHE_SIZE);
    /**
     * The dependencies in the cache.
     */
    private final Map<String, Set<String>> dependencies = new HashMap<String, Set<String>>(); // NOPMD
    /**
     * The cache size.
     */
    private int cacheSize = DEFAULT_CACHE_SIZE;

    /**
     * @return the cache size
     */
    public int getCacheSize() {
        synchronized (cache) {
            return cacheSize;
        }
    }

    /**
     * Set cache size.
     *
     * @param cacheSize new cache size
     */
    public void setCacheSize(final int cacheSize) {
        synchronized (cache) {
            this.cacheSize = cacheSize;
            if (cacheSize < 1) {
                dependencies.clear();
                cache.clear();
            } else {
                int size = cache.size();
                for (final Iterator<String> i = cache.keySet().iterator(); i.hasNext() && size > cacheSize; size--) {
                    i.next();
                    i.remove();
                }
            }
        }
    }

    /**
     * @return the current amount of entries in the cache
     */
    public int getCurrentSize() {
        synchronized (cache) {
            return cache.size();
        }
    }

    /**
     * Get file from the cache.
     *
     * @param request the request for the catalog
     * @return the file in cache or null
     */
    public CatalogResolutionEvent getFile(final CatalogRequest request) {
        synchronized (cache) {
            final CatalogResolutionEvent event = cache.remove(request.getSystemId());
            if (event != null) {
                // add as last
                cache.put(request.getSystemId(), event);
            }
            return event == null ? null : new CatalogResolutionEvent(
                    request, event.getFile(), event.getProblem(), event.getResolutionHistory());
        }
    }

    /**
     * Cache the file.
     *
     * @param event the file event to cache
     */
    public void cacheFile(final CatalogResolutionEvent event) {
        synchronized (cache) {
            if (cache.size() >= cacheSize && !cache.isEmpty()) {
                expireFile(cache.entrySet().iterator().next().getKey());
            }
            if (cacheSize > 0) {
                final String systemId = event.getRequest().getSystemId();
                cache.put(systemId, new CatalogResolutionEvent(null, event.getFile(), event.getProblem(),
                        event.getResolutionHistory()));
                if (event.getFile() != null) {
                    registerForUsedResources(systemId, event.getFile().getUsedResources());
                }
                registerForUsedResources(systemId, event.getResolutionHistory());
            }
        }
    }

    /**
     * Register system id for the specified resource usage.
     *
     * @param systemId      the system id
     * @param usedResources the used resources
     */
    private void registerForUsedResources(final String systemId, final List<CatalogResourceUsage> usedResources) {
        for (final CatalogResourceUsage usage : usedResources) {
            Set<String> systemIds = dependencies.get(usage.getSystemId());
            if (systemIds == null) {
                systemIds = new HashSet<String>(); // NOPMD
                dependencies.put(usage.getSystemId(), systemIds);
            }
            systemIds.add(systemId);
        }
    }

    /**
     * Expire the file from the cache.
     *
     * @param systemId the system id
     */
    public void expireFile(final String systemId) {
        synchronized (cache) {
            final CatalogResolutionEvent event = cache.remove(systemId);
            if (event != null) {
                clearResourceUsage(systemId, event.getResolutionHistory());
                if (event.getFile() != null) {
                    clearResourceUsage(systemId, event.getFile().getUsedResources());
                }
            }
            final Set<String> systemIds = dependencies.remove(systemId);
            if (systemIds != null) {
                for (final String id : systemIds.toArray(new String[systemIds.size()])) { // NOPMD
                    expireFile(id);
                }
            }
        }
    }

    /**
     * Clear resource usage.
     *
     * @param systemId      the system id
     * @param resourceUsage the resource usage
     */
    private void clearResourceUsage(final String systemId, final List<CatalogResourceUsage> resourceUsage) {
        for (final CatalogResourceUsage usage : resourceUsage) {
            final Set<String> systemIds = dependencies.get(usage.getSystemId());
            if (systemIds != null) {
                systemIds.remove(systemId);
                if (systemIds.isEmpty()) {
                    dependencies.remove(usage.getSystemId());
                }
            }
        }
    }

    /**
     * Clear the cache.
     */
    public void clear() {
        synchronized (cache) {
            dependencies.clear();
            cache.clear();
        }
    }
}
