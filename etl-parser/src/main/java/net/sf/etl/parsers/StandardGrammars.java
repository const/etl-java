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

/**
 * This interface contains constants related to standard grammars. These
 * constants are mostly used in parser implementation. However they may be
 * useful for clients if they need to check for these special contexts.
 *
 * @author const
 */
public interface StandardGrammars {
    /**
     * Current grammar version
     */
    public static final String VERSION = "0.3.0";
    /**
     * Current grammar version
     */
    public static final String _VERSION = "0_3_0";
    /**
     * System ID of ETL grammar in ETL
     */
    public static final String ETL_GRAMMAR_SYSTEM_ID = StandardGrammars.class.getResource(
            "/net/sf/etl/grammars/grammar-" + _VERSION + ".g.etl").toString();
    /**
     * Public ID that resolves to current ETL grammar
     */
    public static final String ETL_GRAMMAR_PUBLIC_ID = "-//IDN etl.sf.net//ETL//Grammar " + VERSION + "//EN";
    /**
     * Namespace of objects in ETL grammar
     */
    public static final String ETL_GRAMMAR_NAMESPACE = "http://etl.sf.net/etl/grammar/" + VERSION;
    /**
     * namespace of Doctype object
     */
    public static final String DOCTYPE_NS = "http://etl.sf.net/etl/doctype/" + VERSION;

    /**
     * Name of doctype grammar
     */
    public static final String DOCTYPE_GRAMMAR_NAME = "net.sf.etl.grammars.DoctypeDeclaration";

    /**
     * System ID of doctype grammar
     */
    public static final String DOCTYPE_GRAMMAR_SYSTEM_ID = StandardGrammars.class.getResource(
            "/net/sf/etl/grammars/doctype-" + _VERSION + ".g.etl").toString();
    /**
     * namespace of default object
     */
    public static final String DEFAULT_NS = "http://etl.sf.net/etl/default/" + VERSION;

    /**
     * Name of default grammar
     */
    public static final String DEFAULT_GRAMMAR_NAME = "net.sf.etl.grammars.DefaultGrammar";

    /**
     * System ID of default grammar
     */
    public static final String DEFAULT_GRAMMAR_SYSTEM_ID = StandardGrammars.class.getResource(
            "/net/sf/etl/grammars/default-" + _VERSION + ".g.etl").toString();
    /**
     * Property name for system id property
     */
    public static final PropertyName DOCTYPE_GRAMMAR_DOCTYPE_TYPE = new PropertyName("Type");
    /**
     * Property name for system id property
     */
    public static final PropertyName DOCTYPE_GRAMMAR_DOCTYPE_SYSTEM_ID = new PropertyName("SystemId");

    /**
     * Property name for public id property
     */
    public static final PropertyName DOCTYPE_GRAMMAR_DOCTYPE_PUBLIC_ID = new PropertyName("PublicId");

    /**
     * Property name for context property
     */
    public static final PropertyName DOCTYPE_GRAMMAR_DOCTYPE_CONTEXT = new PropertyName("Context");
}
