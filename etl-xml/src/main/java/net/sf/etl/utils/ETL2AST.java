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
package net.sf.etl.utils;

import net.sf.etl.parsers.event.tree.ObjectFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This is an abstract class that helps to implement tools that convert AST to
 * other forms using abstract tree parser.
 *
 * @param <C> the configuration type
 * @author const
 */
public abstract class ETL2AST<C extends ETL2AST.BaseASTConfig> extends AbstractFileConverter<C> {
    /**
     * Configure parser with standard options.
     *
     * @param p the parser to configure
     */
    @SuppressWarnings("unchecked")
    protected final void configureStandardOptions(final ObjectFactory<?, ?, ?, ?> p) {
        // ignore namespace
        for (final String ignoredNamespace : getConfig().getIgnoredNamespaces()) {
            p.ignoreNamespace(ignoredNamespace);
        }
        // specify text position flag
        p.setPosPolicy(getConfig().getPositionPolicy());
    }

    /**
     * The AST configuration.
     */
    public static class BaseASTConfig extends BaseConfig {
        /**
         * The constructor.
         *
         * @param commandLine the command line
         */
        public BaseASTConfig(final CommandLine commandLine) {
            super(commandLine);
        }

        /**
         * @return the AST options
         */
        public static Options getAstOptions() {
            final Options options = BaseConfig.getBaseOptions();
            options.addOption("I", "ignore-namespace", true, "ignore all objects from specified namespace URL");
            options.addOption("p", "position-policy", true, "use the specified position policy "
                    + "('location', 'positions' (default), 'expanded', or class name), or 'none'");
            return options;
        }

        /**
         * @return the list of ignored namespaces
         */
        public final List<String> getIgnoredNamespaces() {
            final String[] ignored = getCommandLine().getOptionValues('I');
            return ignored == null ? Collections.<String>emptyList() : Arrays.asList(ignored);
        }

        /**
         * @return the position policy
         */
        public final ObjectFactory.PositionPolicy getPositionPolicy() {
            final String policyName = getCommandLine().getOptionValue('p');
            final ObjectFactory.PositionPolicy policy;
            if (policyName == null || "positions".equalsIgnoreCase(policyName)) {
                policy = ObjectFactory.PositionPolicyPositions.get();
            } else if ("location".equalsIgnoreCase(policyName)) {
                policy = ObjectFactory.PositionPolicyLocation.get();
            } else if ("expanded".equalsIgnoreCase(policyName)) {
                policy = ObjectFactory.PositionPolicyExpanded.get();
            } else if ("none".equalsIgnoreCase(policyName)) {
                policy = ObjectFactory.PositionPolicyNone.get();
            } else {
                try {
                    policy = (ObjectFactory.PositionPolicy) Class.forName(policyName).getConstructor().newInstance();
                } catch (Exception ex) { // NOPMD
                    throw new InvalidOptionValueException("The policy cannot be instantiated: " + policyName, ex);
                }
            }
            return policy;
        }
    }
}
