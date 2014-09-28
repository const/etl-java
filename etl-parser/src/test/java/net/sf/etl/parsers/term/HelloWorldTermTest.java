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

import org.junit.Test;

/**
 * test HelloWorld.ej.etl structure reading
 *
 * @author const
 */
public class HelloWorldTermTest extends TermStructureTestCase {
    /**
     * namespace for EJ grammar
     */
    private static final String NS = "http://etl.sf.net/2006/samples/ej/0.1";
    /**
     * system id of minimal XJ grammar
     */
    private static final String MINIMAL_EJ_SYSTEM_ID = HelloWorldTermTest.class.getResource("hello/MinimalEJ.g.etl").toString();

    /**
     * check for identifier
     *
     * @param name text of identifier
     */
    private void identifier(final String name) {
        this.objectStart(NS, "Identifier");
        {
            propStart("Value");
            value(name);
            propEnd("Value");
        }
        this.objectEnd(NS, "Identifier");

    }

    /**
     * check for modifier
     *
     * @param name text of modifier
     */
    private void modifier(final String name) {
        this.objectStart(NS, "Modifier");
        {
            propStart("Value");
            value(name);
            propEnd("Value");
        }
        this.objectEnd(NS, "Modifier");
    }

    /**
     * check for documentation line
     *
     * @param text text of documentation line
     */
    private void docline(final String text) {
        this.objectStart(NS, "DocumentationLine");
        {
            propStart("Text");
            value(text);
            propEnd("Text");
        }
        this.objectEnd(NS, "DocumentationLine");

    }

    /**
     * read hello world from URI
     */
    @Test
    public void testHelloWorld() {
        startWithResource("hello/HelloWorld.ej.etl");
        walkThrougHelloWorld("\"MinimalEJ.g.etl\"");
    }

    /**
     * read hello world from URI and reader
     */
    @Test
    public void testHelloWorldReader() {
        startWithResourceAsReader("hello/HelloWorld.ej.etl");
        walkThrougHelloWorld("\"MinimalEJ.g.etl\"");
    }

    /**
     * read hello world
     *
     * @param grammarRef reference to grammar.
     */
    private void walkThrougHelloWorld(final String grammarRef) {
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
    private void readTopLevelClassStatement() { // NOPMD
        this.objectStart(NS, "TopLevelClassifier");
        {
            propStart("Classifier");
            this.objectStart(NS, "ClassStatement");
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
                    this.objectStart(NS, "MethodStatement");
                    {
                        {
                            listStart("Documentation");
                            docline("/// Application entry point");
                            docline("/// @param args application arguments");
                            listEnd("Documentation");
                        }
                        {
                            listStart("AttributeSets");
                            objectStart(NS, "AttributeSet");
                            listStart("Attributes");
                            identifier("SampleAttribute");
                            listEnd("Attributes");
                            objectEnd(NS, "AttributeSet");
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
                            this.objectStart(NS, "Parameter");
                            {
                                propStart("Classifier");
                                this.objectStart(NS, "ApplySquareOp");
                                {
                                    propStart("Functor");
                                    readPrimitiveType("array");
                                    propEnd("Functor");
                                    this.listStart("Args");
                                    identifier("String");
                                    this.listEnd("Args");
                                }
                                this.objectEnd(NS, "ApplySquareOp");
                                propEnd("Classifier");
                                identifierProp("Name", "args");
                            }
                            this.objectEnd(NS, "Parameter");
                            listEnd("Parameters");
                        }
                        {
                            this.propStart("Body");
                            this.objectStart(NS, "MethodBlock");
                            {
                                listStart("Content");
                                this.objectStart(NS, "ExpressionStatement");
                                {
                                    this.propStart("Expression");
                                    this.objectStart(NS, "ApplyRoundOp");
                                    {
                                        propStart("Functor");

                                        this.objectStart(NS, "AccessOp");
                                        {
                                            propStart("Accessed");
                                            this.objectStart(NS, "AccessOp");
                                            {
                                                identifierProp("Accessed",
                                                        "System");
                                                identifierProp("Feature", "out");
                                            }
                                            this.objectEnd(NS, "AccessOp");
                                            propEnd("Accessed");
                                            identifierProp("Feature", "println");
                                        }
                                        this.objectEnd(NS, "AccessOp");
                                        propEnd("Functor");
                                        listStart("Args");
                                        this.objectStart(NS, "StringLiteral");
                                        {
                                            propStart("Value");
                                            value("\"Hello, World!\"");
                                            propEnd("Value");
                                        }
                                        this.objectEnd(NS, "StringLiteral");
                                        listEnd("Args");
                                    }
                                    this.objectEnd(NS, "ApplyRoundOp");
                                    this.propEnd("Expression");
                                }
                                this.objectEnd(NS, "ExpressionStatement");
                                listEnd("Content");
                            }
                            this.objectEnd(NS, "MethodBlock");
                            this.propEnd("Body");
                        }
                    }
                    this.objectEnd(NS, "MethodStatement");
                    this.listEnd("Contents");
                }
            }
            this.objectEnd(NS, "ClassStatement");
            propEnd("Classifier");
        }
        this.objectEnd(NS, "TopLevelClassifier");
    }

    /**
     * Read primitive type
     *
     * @param name a name of primitive type
     */
    private void readPrimitiveType(final String name) {
        this.objectStart(NS, "PrimitiveType");
        {
            propStart("Name");
            value(name);
            propEnd("Name");
        }
        this.objectEnd(NS, "PrimitiveType");
    }

    /**
     * Read package statement
     */
    private void readPackageStatement() {
        this.objectStart(NS, "PackageStatement");
        {
            identifierProp("Name", "test");
        }
        this.objectEnd(NS, "PackageStatement");
    }

    /**
     * @param name       a name of property
     * @param identifier identifier value
     */
    private void identifierProp(final String name, final String identifier) {
        propStart(name);
        identifier(identifier);
        propEnd(name);
    }

    /**
     * Test how default grammar works. Default context of grammar is used.
     */
    @Test
    public void testDefaultGrammarDefaultContext() {
        final String text = "package test;";
        startWithStringAndDefaultGrammar(text, MINIMAL_EJ_SYSTEM_ID, null, null);
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
    @Test
    public void testDefaultGrammarNewContext() {
        final String text = "test;";
        startWithStringAndDefaultGrammar(text, MINIMAL_EJ_SYSTEM_ID, null, "Code");
        boolean errorExit = true;
        try {
            this.objectStart(NS, "ExpressionStatement");
            {
                propStart("Expression");
                identifier("test");
                propEnd("Expression");
            }
            this.objectEnd(NS, "ExpressionStatement");
            errorExit = false;
        } finally {
            endParsing(errorExit);
        }
    }
}
