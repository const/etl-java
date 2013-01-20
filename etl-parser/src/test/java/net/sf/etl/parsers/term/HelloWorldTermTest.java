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
package net.sf.etl.parsers.term;

/**
 * test HelloWorld.ej.etl structure reading
 *
 * @author const
 */
public class HelloWorldTermTest extends TermStructureTestCase {
    /**
     * namespace for EJ grammar
     */
    private static final String ns = "http://etl.sf.net/2006/samples/ej/0.1";
    /**
     * system id of minimal XJ grammar
     */
    private static final String MinimalEJ_SYSTEM_ID = HelloWorldTermTest.class.getResource("hello/MinimalEJ.g.etl").toString();

    /**
     * check for identifier
     *
     * @param name text of identifier
     */
    private void identifier(String name) {
        this.objectStart(ns, "Identifier");
        {
            propStart("Value");
            value(name);
            propEnd("Value");
        }
        this.objectEnd(ns, "Identifier");

    }

    /**
     * check for modifier
     *
     * @param name text of modifier
     */
    private void modifier(String name) {
        this.objectStart(ns, "Modifier");
        {
            propStart("Value");
            value(name);
            propEnd("Value");
        }
        this.objectEnd(ns, "Modifier");
    }

    /**
     * check for documentation line
     *
     * @param text text of documentation line
     */
    private void docline(String text) {
        this.objectStart(ns, "DocumentationLine");
        {
            propStart("Text");
            value(text);
            propEnd("Text");
        }
        this.objectEnd(ns, "DocumentationLine");

    }

    /**
     * read hello world from URI
     */
    public void testHelloWorld() {
        startWithResource("hello/HelloWorld.ej.etl");
        walkThrougHelloWorld("\"MinimalEJ.g.etl\"");
    }

    /**
     * read hello world from URI and reader
     */
    public void testHelloWorldReader() {
        startWithResourceAsReader("hello/HelloWorld.ej.etl");
        walkThrougHelloWorld("\"MinimalEJ.g.etl\"");
    }

    /**
     * read hello world
     *
     * @param grammarRef reference to grammar.
     */
    private void walkThrougHelloWorld(String grammarRef) {
        boolean errorExit = true;
        try {
            readDocType(grammarRef, null);
            readPackageStatement();
            readTopLevelClassStatement();
            errorExit = false;
        } finally {
            endParsing(errorExit);
        }

    }

    /**
     * Read top level class statement
     */
    private void readTopLevelClassStatement() {
        this.objectStart(ns, "TopLevelClassifier");
        {
            propStart("Classifier");
            this.objectStart(ns, "ClassStatement");
            {
                {
                    listStart("Documentation");
                    docline("/// Classical \"Hello, World!\" program.");
                    listEnd("Documentation");
                }
                {
                    propStart("VisibilityModifier");
                    modifier("public");
                    propEnd("VisibilityModifier");
                }
                {
                    identifierProp("Name", "HelloWorld");
                }
                {
                    this.listStart("Contents");
                    this.objectStart(ns, "MethodStatement");
                    {
                        {
                            listStart("Documentation");
                            docline("/// Application entry point");
                            docline("/// @param args application arguments");
                            listEnd("Documentation");
                        }
                        {
                            listStart("AttributeSets");
                            objectStart(ns, "AttributeSet");
                            listStart("Attributes");
                            identifier("SampleAttribute");
                            listEnd("Attributes");
                            objectEnd(ns, "AttributeSet");
                            listEnd("AttributeSets");
                        }

                        {
                            propStart("StaticModifier");
                            modifier("static");
                            propEnd("StaticModifier");
                            propStart("VisibilityModifier");
                            modifier("public");
                            propEnd("VisibilityModifier");
                        }
                        {
                            propStart("ReturnType");
                            readPrimitiveType("void");
                            propEnd("ReturnType");
                            identifierProp("Name", "main");
                            listStart("Parameters");
                            this.objectStart(ns, "Parameter");
                            {
                                propStart("Classifier");
                                this.objectStart(ns, "ApplySquareOp");
                                {
                                    propStart("Functor");
                                    readPrimitiveType("array");
                                    propEnd("Functor");
                                    this.listStart("Args");
                                    identifier("String");
                                    this.listEnd("Args");
                                }
                                this.objectEnd(ns, "ApplySquareOp");
                                propEnd("Classifier");
                                identifierProp("Name", "args");
                            }
                            this.objectEnd(ns, "Parameter");
                            listEnd("Parameters");
                        }
                        {
                            this.propStart("Body");
                            this.objectStart(ns, "MethodBlock");
                            {
                                listStart("Content");
                                this.objectStart(ns, "ExpressionStatement");
                                {
                                    this.propStart("Expression");
                                    this.objectStart(ns, "ApplyRoundOp");
                                    {
                                        propStart("Functor");

                                        this.objectStart(ns, "AccessOp");
                                        {
                                            propStart("Accessed");
                                            this.objectStart(ns, "AccessOp");
                                            {
                                                identifierProp("Accessed",
                                                        "System");
                                                identifierProp("Feature", "out");
                                            }
                                            this.objectEnd(ns, "AccessOp");
                                            propEnd("Accessed");
                                            identifierProp("Feature", "println");
                                        }
                                        this.objectEnd(ns, "AccessOp");
                                        propEnd("Functor");
                                        listStart("Args");
                                        this.objectStart(ns, "StringLiteral");
                                        {
                                            propStart("Value");
                                            value("\"Hello, World!\"");
                                            propEnd("Value");
                                        }
                                        this.objectEnd(ns, "StringLiteral");
                                        listEnd("Args");
                                    }
                                    this.objectEnd(ns, "ApplyRoundOp");
                                    this.propEnd("Expression");
                                }
                                this.objectEnd(ns, "ExpressionStatement");
                                listEnd("Content");
                            }
                            this.objectEnd(ns, "MethodBlock");
                            this.propEnd("Body");
                        }
                    }
                    this.objectEnd(ns, "MethodStatement");
                    this.listEnd("Contents");
                }
            }
            this.objectEnd(ns, "ClassStatement");
            propEnd("Classifier");
        }
        this.objectEnd(ns, "TopLevelClassifier");
    }

    /**
     * Read primitive type
     *
     * @param name a name of primitive type
     */
    private void readPrimitiveType(String name) {
        this.objectStart(ns, "PrimitiveType");
        {
            propStart("Name");
            value(name);
            propEnd("Name");
        }
        this.objectEnd(ns, "PrimitiveType");
    }

    /**
     * Read package statement
     */
    private void readPackageStatement() {
        this.objectStart(ns, "PackageStatement");
        {
            identifierProp("Name", "test");
        }
        this.objectEnd(ns, "PackageStatement");
    }

    /**
     * @param name       a name of property
     * @param identifier identifier value
     */
    private void identifierProp(String name, String identifier) {
        propStart(name);
        identifier(identifier);
        propEnd(name);
    }

    /**
     * Test how default grammar works. Default context of grammar is used.
     */
    public void testDefaultGrammarDefaultContext() {
        final String text = "package test;";
        startWithStringAndDefaultGrammar(text, MinimalEJ_SYSTEM_ID, null, null);
        boolean errorExit = true;
        try {
            readPackageStatement();
            errorExit = false;
        } finally {
            endParsing(errorExit);
        }
    }

    /**
     * Test how default grammar works. Non default context of grammar is used.
     */
    public void testDefaultGrammarNewContext() {
        final String text = "test;";
        startWithStringAndDefaultGrammar(text, MinimalEJ_SYSTEM_ID, null, "Code");
        boolean errorExit = true;
        try {
            this.objectStart(ns, "ExpressionStatement");
            {
                propStart("Expression");
                identifier("test");
                propEnd("Expression");
            }
            this.objectEnd(ns, "ExpressionStatement");
            errorExit = false;
        } finally {
            endParsing(errorExit);
        }
    }
}
