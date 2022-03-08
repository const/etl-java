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

package net.sf.etl.utils.xml;

import net.sf.etl.utils.AbstractFileConverter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.Locale;

/**
 * The configuration.
 */
public final class ETL2XMLConfig extends AbstractFileConverter.BaseConfig {

    /**
     * The constructor.
     *
     * @param commandLine the command line
     */
    public ETL2XMLConfig(final CommandLine commandLine) {
        super(commandLine);
    }

    /**
     * @return the xml options
     */
    public static Options getXmlOptions() {
        final Options options = getBaseOptions();
        options.addOption("f", "format", true, "output format ('presentation', 'text', 'xmi', 'html', 'tree'");
        options.addOption("s", "style", true, "style file name");
        options.addOption("T", "style-type", true, "style media type");
        options.addOption("a", "avoid-attributes", false, "avoid usage of xml attributes in xmi output.");
        return options;
    }

    /**
     * @return the format
     */
    public ETL2XML.OutputFormat getFormat() {
        return ETL2XML.OutputFormat.valueOf(getCommandLine().getOptionValue('f').toUpperCase(Locale.US));
    }

    /**
     * @return the style file name
     */
    public String getStyle() {
        return getCommandLine().getOptionValue('s');
    }

    /**
     * @return the style type
     */
    public String getStyleType() {
        return getCommandLine().getOptionValue('T');
    }

    /**
     * @return true if avoid attributes option is set
     */
    public boolean isAvoidAttributes() {
        return getCommandLine().hasOption('a');
    }
}
