/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2009 Constantine A Plotnikov
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
     * this state indicate that object is starting
     */
    OBJECT_START,
    /**
     * this state indicate that object has ended
     */
    OBJECT_END,
    /**
     * this state indicate that attributes for object starting, this state
     * happens before any properties and after doc comments.
     */
    ATTRIBUTES_START,
    /**
     * this state indicate that attributes for object ended
     */
    ATTRIBUTES_END,
    /**
     * this state indicate that simple property started
     */
    PROPERTY_START,
    /**
     * this state indicate that simple property ended
     */
    PROPERTY_END,
    /**
     * this state indicate that list property starting, one list property could
     * start and end several times and could interleave with other single value
     * properties and list properties.
     */
    LIST_PROPERTY_START,
    /**
     * this state indicate that list property ended
     */
    LIST_PROPERTY_END,
    /**
     * This is a value token that is a part of some property.
     */
    VALUE,
    /**
     * This is a start of a partial value token. Note that new line character is
     * not reported.
     */
    VALUE_START,
    /**
     * This is a part of a partial value token. Note that new line character is
     * not reported.
     */
    VALUE_PART,
    /**
     * This is a end of the partial value token. Note that new line character is
     * not reported.
     */
    VALUE_END,

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
     * structural token that is required by syntax but which's value is not
     * mapped to any property. Modifiers w/o property are mapped to this event
     * too.
     */
    STRUCTURAL,
    /**
     * this state indicate that expression is starting
     */
    EXPRESSION_START,
    /**
     * this state indicate that expression has ended
     */
    EXPRESSION_END,
    /**
     * this state indicate that modifiers are starting
     */
    MODIFIERS_START,
    /**
     * this state indicate that modifiers have ended
     */
    MODIFIERS_END,
    /**
     * this state indicate that source has ended
     */
    EOF,
    /**
     * Start of section with doc-comments.
     */
    DOC_COMMENT_START,
    /**
     * End of section with doc-comments
     */
    DOC_COMMENT_END,
    /**
     * start of segment
     */
    SEGMENT_START,
    /**
     * end of segment
     */
    SEGMENT_END,
    /**
     * start of block
     */
    BLOCK_START,
    /**
     * end of block
     */
    BLOCK_END,
    /**
     * This event indicates that some grammar have been loaded. Namespace of
     * event is a declared URI of the grammar and name is a name of root context
     * for the grammar.
     */
    GRAMMAR_IS_LOADED,
    /**
     * an syntax error
     */
    SYNTAX_ERROR,
    /**
     * an segment syntax error
     */
    SEGMENT_ERROR,
    /**
     * an lexical error
     */
    LEXICAL_ERROR,
    /**
     * an grammar error, this error is generated during grammar loading or
     * compilation
     */
    GRAMMAR_ERROR,
}
