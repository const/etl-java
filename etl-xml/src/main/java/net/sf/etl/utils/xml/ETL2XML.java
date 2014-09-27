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
public class ETL2XML extends AbstractFileConverter {

    /**
     * name of style file
     */
    protected String styleFileName;

    /**
     * type of style file
     */
    protected String styleFileType;

    /**
     * kind of output
     */
    protected String outputKind;

    /**
     * instance of transformer
     */
    protected Templates templates;

    /**
     * If true attributes are avoided
     */
    boolean avoidAttributes;


    /**
     * a constructor
     *
     * @param args program arguments.
     */
    public ETL2XML(String[] args) {
        super();
        try {
            start(args);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * application entry point
     *
     * @param args program arguments.
     */
    public static void main(String[] args) {
        new ETL2XML(args);
    }

    @Override
    protected void processContent(OutputStream outStream, TermParserReader p) throws Exception {
        if ("-tree".equals(this.outputKind)) {
            final TreeOutput out = new TreeOutput();
            out.process(p, outStream);
        } else if ("-xmi".equals(this.outputKind)) {
            final XMLOutput out = new XMIOutput(this.avoidAttributes);
            out.process(p, outStream);
        } else if ("-html".equals(this.outputKind)
                || "-text".equals(this.outputKind)) {
            final StringWriter sw = new StringWriter();
            final XMLOutput out = new PresentationOutput(null, null);
            out.process(p, sw);
            // resolve stylesheets for the resolver
            if (templates == null) {
                final CatalogResolver resolver = new CatalogResolver(configuration.getCatalog(styleFileName));
                String transform;
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
            t.transform(new StreamSource(new StringReader(sw.toString())), new StreamResult(outStream));
        } else {
            final XMLOutput out = new PresentationOutput(this.styleFileName, this.styleFileType);
            out.process(p, outStream);
        }
    }

    @Override
    protected int handleCustomOption(String[] args, int i) throws Exception {
        if ("-presentation".equals(args[i]) || "-text".equals(args[i])
                || "-xmi".equals(args[i]) || "-html".equals(args[i])
                || "-tree".equals(args[i])) {
            if (this.outputKind == null) {
                this.outputKind = args[i];
            } else {
                System.err.println(args[i] + " option ignored because there is already active option " + outputKind);
            }
        } else if ("-style".equals(args[i])) {
            if (this.styleFileName == null) {
                this.styleFileName = args[i + 1];
                i++;
            } else {
                System.err.println(args[i] + " option ignored because there is already active style file " + styleFileName);
                i++;
            }
        } else if ("-styleType".equals(args[i])) {
            if (this.styleFileType == null) {
                this.styleFileType = args[i + 1];
                i++;
            } else {
                System.err.println(args[i] + " option ignored because there is already active style file type " + styleFileType);
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
