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
package net.sf.etl.utils.etl2beans;

import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.event.tree.BeansObjectFactory;
import net.sf.etl.parsers.streams.TermParserReader;
import net.sf.etl.parsers.streams.TreeParserReader;
import net.sf.etl.utils.ETL2AST;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.XMLEncoder;
import java.io.OutputStream;
import java.util.Map.Entry;

/**
 * This class converts ETL source to serialized JavaBeans.
 *
 * @author const
 */
public final class ETL2Beans extends ETL2AST<ETL2BeansConfig> {
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ETL2Beans.class);

    /**
     * Application entry point.
     *
     * @param args application arguments
     */
    public static void main(final String[] args) {
        try {
            new ETL2Beans().start(args);
        } catch (final Exception ex) { // NOPMD
            LOG.error("Execution failed", ex);
        }
    }

    @Override
    protected ETL2BeansConfig parseConfig(final CommandLine commandLine) {
        return new ETL2BeansConfig(commandLine);
    }

    @Override
    protected Options getOptions() {
        return ETL2BeansConfig.getBeansOptions();
    }

    @Override
    protected void processContent(final OutputStream stream, final TermParserReader p)
            throws Exception {
        final BeansObjectFactory bp = new BeansObjectFactory(getConfig().getClassloader());
        configureStandardOptions(bp);
        for (final Entry<String, String> e : getConfig().getPackageMap().entrySet()) {
            bp.mapNamespaceToPackage(e.getKey(), e.getValue());
        }
        final XMLEncoder en = new XMLEncoder(stream);
        try {
            en.setPersistenceDelegate(TextPos.class, new PersistenceDelegate() {
                @Override
                protected Expression instantiate(final Object oldInstance, final Encoder encoder) {
                    final TextPos pos = (TextPos) oldInstance;
                    return new Expression(oldInstance, oldInstance.getClass(), "new", new Object[]{
                            pos.line(), pos.column(), pos.offset()
                    });
                }
            });
            final TreeParserReader<Object> parser = new TreeParserReader<Object>(p, bp);
            while (parser.advance()) {
                en.writeObject(parser.current());
            }
        } finally {
            en.close();
        }
    }
}
