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
package net.sf.etl.utils.xml;

import net.sf.etl.parsers.streams.TermParserReader;
import net.sf.etl.utils.AbstractFileConverter;
import org.apache_extras.xml_catalog.blocking.CatalogResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Converter from ETL source to different XML presentations.
 *
 * @author const
 */
public final class ETL2XML extends AbstractFileConverter {
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ETL2XML.class);
    /**
     * the name of style file.
     */
    private String styleFileName;

    /**
     * the type of style file.
     */
    private String styleFileType;

    /**
     * the kind of output.
     */
    private String outputKind;

    /**
     * the instance of transformer.
     */
    private Templates templates;

    /**
     * If true attributes are avoided.
     */
    private boolean avoidAttributes;


    /**
     * The constructor.
     *
     * @param args the program arguments.
     */
    public ETL2XML(final String[] args) {
        super();
        try {
            start(args);
        } catch (final Exception ex) { // NOPMD
            LOG.error("Processing failed", ex);
        }
    }

    /**
     * The application entry point.
     *
     * @param args the program arguments.
     */
    public static void main(final String[] args) {
        new ETL2XML(args);
    }

    @Override
    protected void processContent(final OutputStream stream, final TermParserReader p) throws Exception {
        if ("-tree".equals(this.outputKind)) {
            final TreeOutput out = new TreeOutput();
            out.process(p, stream);
        } else if ("-xmi".equals(this.outputKind)) {
            final XMLOutput out = new XMIOutput(this.avoidAttributes);
            out.process(p, stream);
        } else if ("-html".equals(this.outputKind)
                || "-text".equals(this.outputKind)) {
            final StringWriter sw = new StringWriter();
            final XMLOutput out = new PresentationOutput(null, null);
            out.process(p, sw);
            // resolve stylesheets for the resolver
            if (templates == null) {
                final CatalogResolver resolver = new CatalogResolver(getConfiguration().getCatalog(styleFileName));
                final String transform;
                if (styleFileName == null) {
                    // TODO resolve by extension
                    transform = getClass().getResource("/net/sf/etl/util/xslt/generic-outline.xsl").toString();
                } else if (new File(styleFileName).exists()) {
                    transform = new File(styleFileName).toURI().toString();
                } else {
                    transform = styleFileName;
                }
                final Source source = resolver.resolve(transform, null);
                final TransformerFactory tf = TransformerFactory.newInstance();
                tf.setURIResolver(resolver);
                templates = tf.newTemplates(source);
            }
            final Transformer t = templates.newTransformer();
            t.transform(new StreamSource(new StringReader(sw.toString())), new StreamResult(stream));
        } else {
            final XMLOutput out = new PresentationOutput(this.styleFileName, this.styleFileType);
            out.process(p, stream);
        }
    }

    @Override
    protected int handleCustomOption(final String[] args, final int start) throws Exception { // NOPMD
        int i = start;
        if ("-presentation".equals(args[i]) || "-text".equals(args[i])
                || "-xmi".equals(args[i]) || "-html".equals(args[i])
                || "-tree".equals(args[i])) {
            if (this.outputKind == null) {
                this.outputKind = args[i];
            } else {
                LOG.error(args[i] + " option ignored because there is already active option " + outputKind);
            }
        } else if ("-style".equals(args[i])) {
            if (this.styleFileName == null) {
                this.styleFileName = args[i + 1];
                i++;
            } else {
                LOG.error(args[i] + " option ignored because there is already active style file " + styleFileName);
                i++;
            }
        } else if ("-styleType".equals(args[i])) {
            if (this.styleFileType == null) {
                this.styleFileType = args[i + 1];
                i++;
            } else {
                LOG.error(args[i] + " option ignored because there is already active style file type "
                        + styleFileType);
                i++;
            }
        } else if ("-avoid-attributes".equals(args[i])) {
            this.avoidAttributes = true;
        } else {
            return super.handleCustomOption(args, i);
        }
        return i;
    }
}
