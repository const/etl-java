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

package net.sf.etl.xml_catalog.blocking;

import net.sf.etl.xml_catalog.event.CatalogResult;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * The catalog resolver suitable for the most XML files.
 */
public final class CatalogResolver implements URIResolver, EntityResolver, EntityResolver2, LSResourceResolver {
    /**
     * The catalog provider.
     */
    private final BlockingCatalog catalog;

    /**
     * The constructor.
     *
     * @param catalog the blocking catalog
     */
    public CatalogResolver(final BlockingCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public InputSource getExternalSubset(final String name, final String baseURI) throws SAXException, IOException {
        final CatalogResult result = catalog.resolveDoctype(name, null, null, baseURI);
        return toInputSource(result, null);
    }

    @Override
    public InputSource resolveEntity(final String name, final String publicId, final String baseURI, // NOPMD
                                     final String systemId) throws SAXException, IOException {
        final CatalogResult result = catalog.resolveDoctype(name, publicId, systemId, baseURI);
        return toInputSource(result, systemId);
    }

    @Override
    public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
        final CatalogResult result = catalog.resolveEntity(publicId, systemId, null);
        return toInputSource(result, systemId);
    }

    @Override
    public LSInput resolveResource(final String type, final String namespaceURI, // NOPMD
                                   final String publicId, final String systemId, final String baseURI) {
        final CatalogResult result = catalog.resolveResource(namespaceURI, type, null, publicId, systemId, baseURI);
        final String resultSystemId = result.getResolution();
        return systemId == null ? null : new LSInput() {
            @Override
            public Reader getCharacterStream() {
                return null; // never return it
            }

            @Override
            public void setCharacterStream(final Reader characterStream) {
                // do nothing
            }

            @Override
            public InputStream getByteStream() {
                return null; // never return it
            }

            @Override
            public void setByteStream(final InputStream byteStream) {
                // do nothing
            }

            @Override
            public String getStringData() {
                return null; // never return it
            }

            @Override
            public void setStringData(final String stringData) {
                // do nothing
            }

            @Override
            public String getSystemId() {
                return resultSystemId;
            }

            @Override
            public void setSystemId(final String systemId) {
                // do nothing
            }

            @Override
            public String getPublicId() {
                return null;
            }

            @Override
            public void setPublicId(final String publicId) {
                // do nothing
            }

            @Override
            public String getBaseURI() {
                return baseURI;
            }

            @Override
            public void setBaseURI(final String baseURI) {
                // do nothing
            }

            @Override
            public String getEncoding() {
                return null; // never returned
            }

            @Override
            public void setEncoding(final String encoding) {
                // do nothing
            }

            @Override
            public boolean getCertifiedText() { //NOPMD
                return false; // TODO not sure if it is a correct value
            }

            @Override
            public void setCertifiedText(final boolean certifiedText) {
                // do nothing
            }
        };
    }

    @Override
    public Source resolve(final String href, final String base) throws TransformerException {
        final CatalogResult result = catalog.resolveEntity(null, href, base);
        final String resultSystemId = result.getResolution();
        return new StreamSource(resultSystemId);
    }

    /**
     * Convert catalog result to input source.
     *
     * @param result   the result
     * @param systemId the system id
     * @return the input source
     */
    private InputSource toInputSource(final CatalogResult result, final String systemId) {
        return result.getResolution() != null
                ? new InputSource(result.getResolution())
                : systemId != null ? new InputSource(systemId) : null;
    }
}
