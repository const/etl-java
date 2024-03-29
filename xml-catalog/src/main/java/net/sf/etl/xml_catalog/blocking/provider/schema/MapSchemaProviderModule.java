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

package net.sf.etl.xml_catalog.blocking.provider.schema;

import net.sf.etl.xml_catalog.blocking.provider.CatalogProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * The map-based schema provider implementation.
 */
public class MapSchemaProviderModule implements SchemaProviderModule {
    /**
     * The catalog provider for file lists.
     */
    private final Map<String, CatalogProvider> catalogProviders = new HashMap<>(); // NOPMD

    @Override
    public final String[] supportedUriSchemas() {
        return catalogProviders.keySet().toArray(new String[0]);
    }

    @Override
    public final CatalogProvider getProvider(final String schema) {
        final CatalogProvider catalogProvider = catalogProviders.get(schema);
        if (catalogProvider == null) {
            throw new IllegalArgumentException("The catalog provider is not supported: " + schema);
        }
        return catalogProvider;
    }

    /**
     * @return the catalog provider for file lists.
     */
    protected final Map<String, CatalogProvider> getCatalogProviders() {
        return catalogProviders;
    }
}
