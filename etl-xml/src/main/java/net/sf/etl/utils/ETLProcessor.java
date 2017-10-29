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

package net.sf.etl.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * The dispatcher for ETL. It is common etlp command with the following subcommands.
 */
public final class ETLProcessor {
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger("");

    /**
     * The private constructor for utility class.
     */
    private ETLProcessor() {
        // do nothing
    }

    /**
     * The application entry point.
     *
     * @param args the application arguments.
     */
    public static void main(final String[] args) {
        final Map<String, Command> commandMap = loadSubCommands();
        if (args.length == 0) {
            usage(commandMap);
            return;
        }
        final Command command = commandMap.get(args[0]);
        if (command == null) {
            LOG.error("The command " + args[0] + " not found");
            usage(commandMap);
            return;
        }
        final String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);
        try {
            final Class<?> commandClass = Class.forName(command.className);
            final Method main = commandClass.getMethod("main", String[].class);
            main.invoke(null, (Object) subArgs);
        } catch (Exception ex) { // NOPMD
            LOG.error("Subcommand failed " + command, ex);
        }
    }

    /**
     * Print usage.
     *
     * @param commandMap the command map.
     */
    private static void usage(final Map<String, Command> commandMap) {
        LOG.info("Usage: etlp <sub-command> <command args...>");
        final Collection<Command> sorted = new TreeMap<String, Command>(commandMap).values();
        int size = 0;
        for (final Command command : sorted) {
            size = Math.max(size, command.name.length());
        }
        final String format = "  %-" + size + "s  %s";
        for (final Command command : sorted) {
            LOG.info(String.format(format, command.name, command.description));
        }
    }

    /**
     * @return the loaded sub commands
     */
    private static Map<String, Command> loadSubCommands() {
        try {
            final Enumeration<URL> resources = ETLProcessor.class.getClassLoader().getResources(
                    "META-INF/etlp/etlp-commands.xml");
            final Map<String, Command> map = new HashMap<String, Command>(); // NOPMD
            final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(false);
            builderFactory.setCoalescing(true);
            builderFactory.setIgnoringComments(true);
            while (resources.hasMoreElements()) {
                final URL url = resources.nextElement();
                final Document document = builderFactory.newDocumentBuilder().parse(url.toURI().toString());
                final NodeList commands = document.getElementsByTagName("command");
                for (int i = 0; i < commands.getLength(); i++) {
                    final Element command = (Element) commands.item(i);
                    final String name = command.getAttribute("name");
                    final String className = command.getAttribute("class");
                    final String description = command.getTextContent();
                    map.put(name, new Command(name, className, description)); // NOPMD
                }
            }
            return map;
        } catch (RuntimeException e) { // NOPMD
            throw e; // NOPMD
        } catch (Exception e) { // NOPMD
            throw new IllegalStateException("Subcommands could not be loaded", e);
        }
    }

    /**
     * The command information.
     */
    private static final class Command {
        /**
         * The name.
         */
        private final String name;
        /**
         * The class name.
         */
        private final String className;
        /**
         * The description.
         */
        private final String description;

        /**
         * The constructor.
         *
         * @param name        the name
         * @param className   the class name
         * @param description the description
         */
        private Command(final String name, final String className, final String description) {
            this.name = name;
            this.className = className;
            this.description = description;
        }

        @Override
        public String toString() {
            return "Command{"
                    + "name='" + name + '\''
                    + ", className='" + className + '\''
                    + ", description='" + description + '\''
                    + '}';
        }
    }
}
