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

package net.sf.etl.utils.etl2beans;

import net.sf.etl.utils.ETL2AST;
import net.sf.etl.utils.InvalidOptionValueException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The configuration.
 */
public final class ETL2BeansConfig extends ETL2AST.BaseASTConfig {
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ETL2BeansConfig.class);
    /**
     * The package map.
     */
    private Map<String, String> packageMap;
    /**
     * The class loader.
     */
    private ClassLoader classLoader;

    /**
     * The constructor.
     *
     * @param commandLine the command line
     */
    public ETL2BeansConfig(final CommandLine commandLine) {
        super(commandLine);
    }

    /**
     * @return the options
     */
    public static Options getBeansOptions() {
        final Options options = getAstOptions();
        options.addOption("m", "map", true, "map from namespace uri to package name <ns>=<package name>");
        options.addOption("c", "classpath", true, "the classpath roots (might be separated by '"
                + File.pathSeparator + "'). It must be file or directory.");
        return options;
    }

    /**
     * @return the classloader for beans.
     */
    public ClassLoader getBeansClassloader() {
        if (classLoader == null) {
            final ArrayList<URL> urls = new ArrayList<URL>();
            final String[] cs = getCommandLine().getOptionValues('c');
            if (cs != null && cs.length > 0) {
                for (final String c : cs) {
                    final String[] pe = c.split(File.pathSeparator);
                    for (final String p : pe) {
                        final String tp = p.trim();
                        if (tp.length() != 0) {
                            final File file = new File(tp);
                            if (file.exists()) { // NOPMD
                                try {
                                    urls.add(file.getAbsoluteFile().toURI().toURL());
                                } catch (MalformedURLException e) {
                                    LOG.error("Bad classpath element: " + p, e);
                                }
                            } else {
                                LOG.warn("Classpath element " + p + " does not exists, ignoring.");
                            }
                        }
                    }
                }
            }
            LOG.info("Classpath: " + urls);
            if (!urls.isEmpty()) {
                classLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                    @Override
                    public ClassLoader run() {
                        return new URLClassLoader(
                                urls.toArray(new URL[urls.size()]), ETL2BeansConfig.class.getClassLoader());
                    }
                });
            } else {
                classLoader = ETL2BeansConfig.class.getClassLoader();
            }
        }
        return classLoader;
    }

    /**
     * @return the package map
     */
    public Map<String, String> getPackageMap() {
        if (packageMap == null) {
            final String[] ms = getCommandLine().getOptionValues('m');
            if (ms == null || ms.length == 0) {
                packageMap = Collections.emptyMap();
            } else {
                packageMap = new HashMap<String, String>();
                for (final String m : ms) {
                    final int p = m.lastIndexOf('=');
                    if (p == -1) {
                        throw new InvalidOptionValueException("Invalid mapping syntax (expected <ns>=<package name>): "
                                + m);
                    }
                    packageMap.put(m.substring(0, p), m.substring(p + 1));
                }
            }
        }
        return packageMap;
    }
}
