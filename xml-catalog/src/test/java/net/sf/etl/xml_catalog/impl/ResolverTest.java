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

import net.sf.etl.xml_catalog.blocking.BlockingCatalog;
import net.sf.etl.xml_catalog.blocking.CatalogResolver;
import net.sf.etl.xml_catalog.blocking.provider.CatalogProviders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Base resolver test
 */
public class ResolverTest {
    private CatalogResolver resolver;

    @BeforeEach
    public void init() {
        resolver = new CatalogResolver(
                new BlockingCatalog(CatalogProviders.DEFAULT_ROOT_REQUEST,
                        CatalogProviders.createCachedCatalog(
                                CatalogProviders.createDefaultCatalogProvider(ResolverTest.class))));
    }

    @Test
    public void domTest() throws ParserConfigurationException, IOException, SAXException {
        // TODO test failures!!!
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(true);
        documentBuilderFactory.setNamespaceAware(true);
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        documentBuilder.setEntityResolver(resolver);
        final ArrayList<SAXParseException> ex = new ArrayList<SAXParseException>();
        documentBuilder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(final SAXParseException exception) throws SAXException {
                if (!exception.getMessage().contains("rddl")) {
                    ex.add(exception);
                }
            }

            @Override
            public void error(final SAXParseException exception) throws SAXException {
                if (!exception.getMessage().contains("rddl")) {
                    ex.add(exception);
                }
            }

            @Override
            public void fatalError(final SAXParseException exception) throws SAXException {
                if (!exception.getMessage().contains("rddl")) {
                    ex.add(exception);
                }
            }
        });
        final Document document = documentBuilder.parse(RuntimeCatalogProviderTest.CORE_CATALOG.toString());
        document.getDocumentElement();
        assertEquals(0, ex.size(), ex.toString());
    }

}
