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

package net.sf.etl.xml_catalog.event;

import net.sf.etl.xml_catalog.blocking.provider.CatalogProviders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default settings for the catalog. The default implementation logs on {@link Logger}.
 */
public final class DefaultCatalogContext implements CatalogContext {
    /**
     * The current instance.
     */
    public static final DefaultCatalogContext INSTANCE = new DefaultCatalogContext();
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCatalogContext.class);
    /**
     * The default value of prefers public attribute.
     */
    private final boolean prefersPublic;

    /**
     * The catalog settings, with default value of prefers public.
     */
    public DefaultCatalogContext() {
        this(Boolean.parseBoolean(
                System.getProperty(CatalogProviders.PREFER_PUBLIC_PROPERTY, CatalogProviders.PREFER_PUBLIC_DEFAULT)));
    }

    /**
     * The constructor from the value.
     *
     * @param prefersPublic the value of the attribute
     */
    public DefaultCatalogContext(final boolean prefersPublic) {
        this.prefersPublic = prefersPublic;
    }

    @Override
    public boolean prefersPublic() {
        return prefersPublic;
    }

    /**
     * This method is invoked by providers when catalog is loaded by actual providers.
     *
     * @param event the event
     */
    @Override
    public void catalogLoaded(final CatalogResolutionEvent event) {
        if (LOG.isDebugEnabled()) {
            //noinspection ThrowableResultOfMethodCallIgnored
            if (event.getProblem() != null) {
                if (event.getFile() == null) {
                    LOG.debug("Catalog " + event.getRequest().getSystemId() + " failed to load: "
                            + event.getResolutionHistory(), event.getProblem());
                } else {
                    LOG.debug("Problems loading catalog " + event.getRequest().getSystemId() + ": "
                            + event.getResolutionHistory(), event.getProblem());
                }
            } else {
                LOG.debug("The catalog " + event.getRequest() + " is loaded");
            }
        }
    }
}
