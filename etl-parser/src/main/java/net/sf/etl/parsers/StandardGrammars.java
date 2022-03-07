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
package net.sf.etl.parsers;

import java.net.URL;

/**
 * This interface contains constants related to standard grammars. These
 * constants are mostly used in parser implementation. However they may be
 * useful for clients if they need to check for these special contexts.
 *
 * @author const
 */
public final class StandardGrammars {
    /**
     * Current grammar version.
     */
    public static final String VERSION = "0.3.0";
    /**
     * Namespace of objects in ETL grammar.
     */
    public static final String ETL_GRAMMAR_NAMESPACE = "http://etl.sf.net/etl/grammar";
    /**
     * Name of etl grammar.
     */
    public static final GrammarId ETL_GRAMMAR_ID = new GrammarId("ETL.Grammar", VERSION);
    /**
     * URL of the grammar.
     */
    public static final URL ETL_GRAMMAR_URL = getStandardGrammarUrl(ETL_GRAMMAR_ID);
    /**
     * namespace of default object.
     */
    public static final String DEFAULT_NS = "http://etl.sf.net/etl/default";
    /**
     * Name of default grammar.
     */
    public static final GrammarId DEFAULT_GRAMMAR_ID = new GrammarId("ETL.DefaultGrammar", VERSION);
    /**
     * System ID of default grammar.
     */
    public static final URL DEFAULT_GRAMMAR_URL = getStandardGrammarUrl(DEFAULT_GRAMMAR_ID);
    /**
     * namespace of Doctype object.
     */
    public static final String DOCTYPE_NS = "http://etl.sf.net/etl/doctype";
    /**
     * Name of doctype grammar.
     */
    public static final GrammarId DOCTYPE_GRAMMAR_ID = new GrammarId("ETL.DoctypeDeclaration", VERSION);
    /**
     * System ID of doctype grammar.
     */
    public static final URL DOCTYPE_GRAMMAR_URL = getStandardGrammarUrl(DOCTYPE_GRAMMAR_ID);
    /**
     * Property name for type property.
     */
    public static final PropertyName DOCTYPE_GRAMMAR_DOCTYPE_TYPE = new PropertyName("Type");
    /**
     * Property name for qualified name property.
     */
    public static final PropertyName DOCTYPE_GRAMMAR_DOCTYPE_QUALIFIED_NAME = new PropertyName("QualifiedName");
    /**
     * Property name for context property.
     */
    public static final PropertyName DOCTYPE_GRAMMAR_DOCTYPE_VERSION = new PropertyName("Version");
    /**
     * Property name for context property.
     */
    public static final PropertyName DOCTYPE_GRAMMAR_DOCTYPE_CONTEXT = new PropertyName("Context");
    /**
     * The request type for the grammar associated with the ETL source.
     */
    public static final String GRAMMAR_REQUEST_TYPE = "http://etl.sf.net/document_type";
    /**
     * The used grammar request type.
     */
    public static final String USED_GRAMMAR_REQUEST_TYPE = "http://etl.sf.net/used_grammar";
    /**
     * RDDL grammar nature.
     */
    public static final String GRAMMAR_NATURE = "http://etl.sf.net/etl/grammar-definition";

    /**
     * Private constructor for utility class.
     */
    private StandardGrammars() {
        // do nothing
    }

    /**
     * Get grammar within classpath.
     *
     * @param classLoader the classloader
     * @param grammarId   the grammar ID
     * @return URL of the grammar
     */
    public static URL getGrammarResource(ClassLoader classLoader, GrammarId grammarId) {
        String path = grammarId.getResourcePath();
        URL resource = classLoader.getResource(path);
        if (resource == null) {
            throw new IllegalStateException("Grammar is not found in the classpath: " + path);
        }
        return resource;
    }

    /**
     * Get URL for standard grammar.
     *
     * @param grammarId the grammar id
     * @return the grammar URL.
     */
    public static URL getStandardGrammarUrl(GrammarId grammarId) {
        return getGrammarResource(StandardGrammars.class.getClassLoader(), grammarId);
    }
}
