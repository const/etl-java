/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2022 Konstantin Plotnikov
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

package net.sf.etl.xml_catalog.impl;

import net.sf.etl.xml_catalog.blocking.provider.CatalogResolutionCache;
import net.sf.etl.xml_catalog.event.CatalogRequest;
import net.sf.etl.xml_catalog.event.CatalogResolutionEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The test for catalog cache
 */
public class CatalogCacheTest {

    @Test
    public void simpleTest() {
        final CatalogRequest r1 = new CatalogRequest("r1");
        final CatalogResolutionEvent e1 = new CatalogResolutionEvent(r1, null, new IOException());
        final CatalogRequest r2 = new CatalogRequest("r2");
        final CatalogResolutionEvent e2 = new CatalogResolutionEvent(r2, null, new IOException());
        final CatalogResolutionCache cache = new CatalogResolutionCache();
        assertEquals(CatalogResolutionCache.DEFAULT_CACHE_SIZE, cache.getCacheSize());
        assertEquals(0, cache.getCurrentSize());
        cache.cacheFile(e1);
        assertEquals(1, cache.getCurrentSize());
        final CatalogResolutionEvent file = cache.getFile(r1);
        assertEquals(e1, file);
        cache.cacheFile(e2);
        assertEquals(2, cache.getCurrentSize());
        assertEquals(e1, cache.getFile(r1));
        assertEquals(e2, cache.getFile(r2));
        cache.setCacheSize(1);
        assertTrue(cache.getFile(r1) != null || cache.getFile(r2) != null);
        assertEquals(1, cache.getCurrentSize());
        cache.clear();
        assertEquals(0, cache.getCurrentSize());
    }
}
