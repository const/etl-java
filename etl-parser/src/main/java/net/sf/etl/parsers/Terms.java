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
 * This is enumeration of event kinds available through term parser.
 *
 * @author const
 */
public enum Terms {
    /**
     * this state indicate that object is starting.
     */
    OBJECT_START,
    /**
     * this state indicate that object has ended.
     */
    OBJECT_END,
    /**
     * this state indicate that attributes for object starting, this state
     * happens before any properties and after doc comments.
     */
    ATTRIBUTES_START,
    /**
     * this state indicate that attributes for object ended.
     */
    ATTRIBUTES_END,
    /**
     * this state indicate that simple property started.
     */
    PROPERTY_START,
    /**
     * this state indicate that simple property ended.
     */
    PROPERTY_END,
    /**
     * this state indicate that list property starting, one list property could
     * start and end several times and could interleave with other single value
     * properties and list properties.
     */
    LIST_PROPERTY_START,
    /**
     * this state indicate that list property ended.
     */
    LIST_PROPERTY_END,
    /**
     * This is a value token that is a part of some property.
     */
    VALUE,
    /**
     * The same as {@link PhraseTokens#CONTROL}.
     */
    CONTROL,
    /**
     * The same as {@link PhraseTokens#IGNORABLE}.
     */
    IGNORABLE,
    /**
     * this token kind is reported only if report_ignorable is true It is
     * structural token that is required by syntax but whose value is not
     * mapped to any property. Modifiers w/o property are mapped to this event
     * too.
     */
    STRUCTURAL,
    /**
     * this state indicate that expression is starting.
     */
    EXPRESSION_START,
    /**
     * this state indicate that expression has ended.
     */
    EXPRESSION_END,
    /**
     * this state indicate that modifiers are starting.
     */
    MODIFIERS_START,
    /**
     * this state indicate that modifiers have ended.
     */
    MODIFIERS_END,
    /**
     * this state indicate that source has ended.
     */
    EOF,
    /**
     * Start of section with doc-comments.
     */
    DOC_COMMENT_START,
    /**
     * End of section with doc-comments.
     */
    DOC_COMMENT_END,
    /**
     * start of segment.
     */
    STATEMENT_START,
    /**
     * end of segment.
     */
    STATEMENT_END,
    /**
     * start of block.
     */
    BLOCK_START,
    /**
     * end of block.
     */
    BLOCK_END,
    /**
     * This event indicates that some grammar have been loaded. The structure id is an instance of
     * {@link LoadedGrammarInfo}.
     */
    GRAMMAR_IS_LOADED,
    /**
     * The syntax error (in case if it is not possible to meaningfully associate error with other token).
     */
    SYNTAX_ERROR
}
