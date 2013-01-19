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
package net.sf.etl.parsers.streams;

import net.sf.etl.parsers.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;


/**
 * <p>
 * This is an abstract parser that builds trees of objects basing on parser.
 * This class was created by refactoring common parts of BeansTermParser and
 * EMFTermParser. So might be still not generic enough for other purposes.
 * </p>
 * <p>
 * Note that abstract method of this parser are expected to throw an exception
 * if structural error occurs (like trying assigning to non existing feature of
 * the object)
 * </p>
 * <p>
 * Typical usage of the parsers derived from this one is the following:
 * </p>
 * <pre>
 * TermParser p = ... ; // configure term parser and start parsing
 * try {
 *     BeansTermParser beansParser = new BeansTermParser(p, null);
 *     while(beansParser.hasNext()) {
 *        MyBaseBeanType c = (MyBaseBeanType)beansParser.next();
 *     }
 * } finally {
 *     p.close();
 * }
 * </pre>
 *
 * @param <BaseObjectType> this is a base type for returned objects
 * @param <FeatureType>    this is a type for feature metatype used by objects
 * @param <MetaObjectType> this is a type for meta object type
 * @param <HolderType>     this is a holder type for collection properties
 * @author const
 * @see net.sf.etl.parsers.streams.beans.BeansTermParser
 */
public abstract class AbstractTreeParser<BaseObjectType, FeatureType, MetaObjectType, HolderType> {
    /**
     * a logger
     */
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(AbstractTreeParser.class.getName());
    /**
     * term parser
     */
    protected final TermParserReader parser;
    /**
     * This set contains namespaces ignored by parser
     */
    protected final HashSet<String> ignoredNamespaces = new HashSet<String>();
    /**
     * This is map from ignored object names to set of namespaces
     */
    final HashMap<String, Set<String>> ignoredObjects = new HashMap<String, Set<String>>();
    /**
     * flag indicating that parser had errors
     */
    protected boolean hadErrors = false;
    /**
     * If this flag is true, when default statement is encountered during
     * hasNext(), hasNext returns false (meaning that no more objects are
     * expected here).
     */
    private boolean abortOnDefault = false;
    /**
     * The current position policy
     */
    private PositionPolicy positionPolicy = PositionPolicy.EXPANDED;
    /**
     * The current system identifier
     */
    protected final String systemId;

    /**
     * A constructor
     *
     * @param parser a term parser
     */
    public AbstractTreeParser(TermParserReader parser) {
        super();
        this.parser = parser;
        this.systemId = parser.getSystemId();
    }

    /**
     * @return the system identifier for the file being parsed
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * @return true if there are more terms in the stream
     */
    public boolean hasNext() {
        while (true) {
            final TermToken current = parser.current();
            if (current.hasAnyErrors()) {
                hadErrors = true;
                handleErrorFromParser(current);
            }
            switch (current.kind()) {
                case OBJECT_START:
                    // if object should be ignored, skip object
                    if (isIgnorable(current.objectName())) {
                        skipObject();
                        break;
                    }
                    return !(abortOnDefault
                            && current.objectName().namespace().equals(StandardGrammars.DEFAULT_NS));
                case GRAMMAR_IS_LOADED:
                    handleLoadedGrammar((LoadedGrammarInfo) current.getStructureId());
                    advanceParser();
                    break;
                case EOF:
                    return false;
                default:
                    advanceParser();
            }
        }
    }

    /**
     * Handle loaded grammar
     *
     * @param loadedGrammarInfo the grammar information
     */
    protected void handleLoadedGrammar(LoadedGrammarInfo loadedGrammarInfo) {
        // do nothing
    }

    /**
     * Advance the parser
     *
     * @return the result of {@link TermParserReader#advance()}
     */
    protected boolean advanceParser() {
        return parser.advance();
    }

    /**
     * finish parsing the segment after root object is parsed.
     */
    private void finishSegment() {
        int segments = 0;
        while (true) {
            final TermToken current = parser.current();
            if (current.hasAnyErrors()) {
                hadErrors = true;
                handleErrorFromParser(current);
            }
            switch (current.kind()) {
                case STATEMENT_START:
                    segments++;
                    break;
                case STATEMENT_END:
                    if (segments == 0) {
                        return;
                    }
                    segments--;
                    break;
                case EOF:
                    throw new IllegalStateException("Segments should be properly nested.");
            }
            advanceParser();
        }
    }

    /**
     * Set abort on object from the namespace of default grammar
     * {@link StandardGrammars#DEFAULT_NS}. Encountering objects from this
     * namespace usually means that loading grammar has failed, so further
     * processing of the source rarely makes sense.
     *
     * @param value if true {@link #hasNext()} is aborted.
     */
    public void setAbortOnDefaultGrammar(boolean value) {
        abortOnDefault = value;
    }

    /**
     * Get the next object from the stream. Note the method skips until the end
     * of the segments, so the errors could be attributed to the correct
     * statement object.
     *
     * @return the next object in the stream
     */
    public BaseObjectType next() {
        if (!hasNext()) {
            throw new IllegalStateException("there are not next object");
        }
        BaseObjectType rc = parseObject();
        finishSegment();
        return rc;
    }

    /**
     * Check if object with specified object name should be ignored
     *
     * @param name the name to check
     * @return true if object should be ignored
     */
    protected boolean isIgnorable(ObjectName name) {
        // check if namespace is ignored
        if (ignoredNamespaces.contains(name.namespace())) {
            return true;
        }
        // check if specific object is ignored
        final Set<String> ns = ignoredObjects.get(parser.current().objectName().name());
        return ns != null && ns.contains(name.namespace());
    }

    /**
     * Skip object in the grammar
     */
    protected void skipObject() {
        int objectCount = 0;
        while (true) {
            final TermToken current = parser.current();
            if (current.hasAnyErrors()) {
                hadErrors = true;
                handleErrorFromParser(current);
            }
            switch (current.kind()) {
                case OBJECT_START:
                    objectCount++;
                    break;
                case OBJECT_END:
                    objectCount--;
                    if (objectCount == 0) {
                        // exit skipping
                        return;
                    }
                    break;
                case EOF:
                    log.severe("EOF while skipping object. Possibly bug in grammar compiler");
                    return;
            }
            advanceParser();
        }
    }

    /**
     * Ignore objects from specified namespace.
     *
     * @param ns namespace to be ignored
     */
    public void ignoreNamespace(String ns) {
        ignoredNamespaces.add(ns);
    }

    /**
     * @return true if there were errors during parsing process
     */
    public boolean hadErrors() {
        return hadErrors;
    }

    /**
     * Ignore specific object kind. Primary candidates for such ignoring are
     * doctype and blank statements.
     *
     * @param namespace the namespace
     * @param name      the name in namespace
     */
    public void ignoreObjects(String namespace, String name) {
        Set<String> namespaces = ignoredObjects.get(name);
        if (namespaces == null) {
            namespaces = new HashSet<String>();
            ignoredObjects.put(name, namespaces);
        }
        namespaces.add(namespace);
    }

    /**
     * Parse object
     *
     * @return parsed object or null if object cannot be parsed for some reason
     */
    private BaseObjectType parseObject() {
        assert parser.current().kind() == Terms.OBJECT_START : "parser is not over object" + parser.current();
        // create instance
        final ObjectName name = parser.current().objectName();
        final MetaObjectType metaObject = getMetaObject(name);
        final BaseObjectType rc = createInstance(metaObject, name);
        final Object startValue = setObjectStartPos(rc, metaObject, parser.current());
        objectStarted(rc);
        advanceParser();
        int extraObjects = 0;
        loop:
        while (true) {
            final TermToken current = parser.current();
            if (current.hasAnyErrors()) {
                hadErrors = true;
                handleErrorFromParser(current);
            }
            switch (current.kind()) {
                case OBJECT_END:
                    if (extraObjects > 0) {
                        extraObjects--;
                    } else {
                        break loop;
                    }
                case VALUE:
                    handleUnexpectedValue(parser, current);
                    advanceParser();
                    break;
                case OBJECT_START:
                    handleUnexpectedObjectStart(parser, current);
                    extraObjects++;
                    advanceParser();
                    break;
                case PROPERTY_START:
                case LIST_PROPERTY_START:
                    parseProperty(rc, metaObject);
                    break;
                default:
                    advanceParser();
                    break;
            }
        }
        assert parser.current().kind() == Terms.OBJECT_END : "parser is not over end: " + parser.current();
        assert parser.current().objectName().equals(name) : "type name does not match ";
        setObjectEndPos(rc, metaObject, startValue, parser.current());
        advanceParser();
        objectEnded(rc);
        return rc;
    }

    /**
     * This method is called when object is about start being processed
     *
     * @param object the object to be processed
     */
    protected void objectStarted(BaseObjectType object) {
    }

    /**
     * This method is called when object was stopped to be processed
     *
     * @param object the object that was processed
     */
    protected void objectEnded(BaseObjectType object) {
    }

    /**
     * The field name from property name
     *
     * @param name the field name
     * @return the adjusted field name
     */
    protected static String lowerCaseFeatureName(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        final int first = name.codePointAt(0);
        if (Character.isUpperCase(first)) {
            name = new StringBuilder().
                    appendCodePoint(Character.toLowerCase(first)).
                    append(name.substring(Character.charCount(first))).
                    toString();
        }
        return name;
    }

    /**
     * Parse property
     *
     * @param rc         the object to parse
     * @param metaObject the metaobject associated with object
     */
    protected void parseProperty(BaseObjectType rc, MetaObjectType metaObject) {
        assert parser.current().kind() == Terms.PROPERTY_START
                || parser.current().kind() == Terms.LIST_PROPERTY_START : "parser is not over property: "
                + parser.current();

        final FeatureType f = getPropertyMetaObject(rc, metaObject, parser
                .current());
        final boolean isList = parser.current().kind() == Terms.LIST_PROPERTY_START;
        final HolderType holder = isList ? startListCollection(rc, metaObject,
                f) : null;

        advanceParser();
        int extraObjects = 0;
        loop:
        while (true) {
            final TermToken current = parser.current();
            if (current.hasAnyErrors()) {
                hadErrors = true;
                handleErrorFromParser(current);
            }
            switch (current.kind()) {
                case PROPERTY_END:
                case LIST_PROPERTY_END:
                    if (extraObjects > 0) {
                        extraObjects--;
                    } else {
                        break loop;
                    }
                case PROPERTY_START:
                case LIST_PROPERTY_START:
                    handleUnexpectedPropertyStart(parser, current);
                    extraObjects++;
                    advanceParser();
                    break;
                case OBJECT_START: {
                    if (isIgnorable(current.objectName())) {
                        skipObject();
                        break;
                    }
                    final Object v = parseObject();
                    if (isList) {
                        addToFeature(rc, f, holder, v);
                    } else {
                        setToFeature(rc, f, v);
                    }
                    break;
                }
                case VALUE: {
                    final Token value = current.token().token();
                    if (isList) {
                        addValueToFeature(rc, f, holder, value);
                    } else {
                        setValueToFeature(rc, f, value);
                    }
                    advanceParser();
                    break;
                }
                default:
                    advanceParser();
                    break;
            }
        }
        if (isList) {
            endListCollection(rc, metaObject, f, holder);
        }

    }

    /**
     * Handle error from parser
     *
     * @param errorToken a token to be reported
     */
    protected void handleErrorFromParser(TermToken errorToken) {
        if (log.isLoggable(Level.SEVERE)) {
            log.severe("Error is detected during parsing file " + parser.getSystemId() + ": " + errorToken);
        }
    }

    /**
     * Handle unexpected property start. Default implementation throws an
     * exception. This means a serious bug in grammar. However, subclasses might
     * override this method to support some other policy.
     *
     * @param parser the term parser
     * @param token  the token
     */
    protected void handleUnexpectedPropertyStart(TermParserReader parser, TermToken token) {
        throw new ParserException("Unexpected property start inside property:" + token);
    }

    /**
     * Handle unexpected property end. Default implementation throws an
     * exception. This means a serious bug in grammar. However, subclasses might
     * override this method to support some other policy.
     *
     * @param parser the term parser
     * @param token  the token
     */
    protected void handleUnexpectedObjectStart(TermParserReader parser, TermToken token) {
        throw new ParserException("Unexpected object start inside object:" + token);
    }

    /**
     * Handle unexpected value. Default implementation throws an exception. This
     * means a serious bug in grammar. However, subclasses might
     * override this method to support some other policy.
     *
     * @param parser the term parser
     * @param token  the token
     */
    protected void handleUnexpectedValue(TermParserReader parser, TermToken token) {
        throw new ParserException("Unexpected value inside object:" + token);
    }

    /**
     * Parse value to fit to feature
     *
     * @param rc    the context object
     * @param f     the feature that will be used to set or add this value
     * @param value the value to parse
     * @return parsed value
     */
    protected Object parseValue(BaseObjectType rc, FeatureType f, Token value) {
        return value.text();
    }

    /**
     * Set value to feature
     *
     * @param rc    the object
     * @param f     the feature to update
     * @param value the value to set
     */
    private void setValueToFeature(BaseObjectType rc, FeatureType f, Token value) {
        setToFeature(rc, f, parseValue(rc, f, value));

    }

    /**
     * Add value to feature
     *
     * @param rc     the object
     * @param f      the feature to update
     * @param holder the collection
     * @param value  the value to add
     */
    private void addValueToFeature(BaseObjectType rc, FeatureType f, HolderType holder, Token value) {
        addToFeature(rc, f, holder, parseValue(rc, f, value));
    }

    /**
     * Set object to feature
     *
     * @param rc an object
     * @param f  a feature to update
     * @param v  a value to set
     */
    protected abstract void setToFeature(BaseObjectType rc, FeatureType f, Object v);

    /**
     * Add object to feature
     *
     * @param rc     the object
     * @param f      the feature to update
     * @param holder the collection objects
     * @param v      the value to add
     */
    protected abstract void addToFeature(BaseObjectType rc, FeatureType f, HolderType holder, Object v);

    /**
     * Start list collection. Note that this method has been created primarily
     * because of beans parser. That parses need to update array. So to reduce
     * array creation it is possible to create an array list from current array
     * and than convert it back to array.
     *
     * @param rc         the object
     * @param metaObject the metaobject
     * @param f          the feature to be updated
     * @return the collection
     */
    protected abstract HolderType startListCollection(BaseObjectType rc, MetaObjectType metaObject, FeatureType f);

    /**
     * Finish list collection
     *
     * @param rc         the object
     * @param metaObject the type of object
     * @param f          the feature to update
     * @param holder     the holder of values
     */
    protected abstract void endListCollection(BaseObjectType rc, MetaObjectType metaObject, FeatureType f, HolderType holder);

    /**
     * get feature meta object
     *
     * @param rc         the object
     * @param metaObject the metaobject to examine
     * @param token      the token that contains LIST_PROPERTY_START or PROPERTY_START events.
     * @return a feature object
     */
    protected FeatureType getPropertyMetaObject(BaseObjectType rc, MetaObjectType metaObject, TermToken token) {
        return getPropertyMetaObject(rc, metaObject, token.propertyName().name());
    }

    /**
     * get feature meta object
     *
     * @param rc         the object
     * @param metaObject the metaobject to examine
     * @param name       the name of property.
     * @return a feature object
     */
    protected abstract FeatureType getPropertyMetaObject(BaseObjectType rc, MetaObjectType metaObject, String name);

    /**
     * Set start position in object. Default implementation tries to set
     * properties startLine, startColumn, and startOffset with corresponding
     * values.
     *
     * @param rc         the object
     * @param metaObject the meta object
     * @param token      teh start object token
     * @return the value to be passed to
     *         {@link #setObjectEndPos(Object, Object, Object, TermToken)}, the
     *         default implementation returns the start position.
     */
    protected Object setObjectStartPos(BaseObjectType rc, MetaObjectType metaObject, TermToken token) {
        final TextPos pos = token.start();
        switch (positionPolicy) {
            case EXPANDED:
                final FeatureType startLineFeature = getPropertyMetaObject(rc, metaObject, "startLine");
                setToFeature(rc, startLineFeature, pos.line());
                final FeatureType startColumnFeature = getPropertyMetaObject(rc, metaObject, "startColumn");
                setToFeature(rc, startColumnFeature, pos.column());
                final FeatureType startOffsetFeature = getPropertyMetaObject(rc, metaObject, "startOffset");
                setToFeature(rc, startOffsetFeature, pos.offset());
                break;
            case POSITIONS:
                final FeatureType startFeature = getPropertyMetaObject(rc, metaObject, "start");
                setToFeature(rc, startFeature, pos);
                break;
        }
        return pos;
    }

    /**
     * Set end position in object. Default implementation tries to set
     * properties endLine, endColumn, and endOffset with corresponding values.
     *
     * @param rc         the object
     * @param metaObject the meta object
     * @param startValue the value returned from
     *                   {@link #setObjectStartPos(Object, Object, TermToken)}
     * @param token      the end object token
     */
    protected void setObjectEndPos(BaseObjectType rc, MetaObjectType metaObject, Object startValue, TermToken token) {
        final TextPos pos = token.start();
        switch (positionPolicy) {
            case EXPANDED:
                final FeatureType endLineFeature = getPropertyMetaObject(rc, metaObject, "endLine");
                setToFeature(rc, endLineFeature, pos.line());
                final FeatureType endColumnFeature = getPropertyMetaObject(rc, metaObject, "endColumn");
                setToFeature(rc, endColumnFeature, pos.column());
                final FeatureType endOffsetFeature = getPropertyMetaObject(rc, metaObject, "endOffset");
                setToFeature(rc, endOffsetFeature, pos.offset());
                break;
            case POSITIONS:
                final FeatureType endFeature = getPropertyMetaObject(rc, metaObject, "end");
                setToFeature(rc, endFeature, pos);
                break;
            case SOURCE_LOCATION:
                final FeatureType locationFeature = getPropertyMetaObject(rc, metaObject, "location");
                setToFeature(rc, locationFeature, new SourceLocation((TextPos) startValue, pos, systemId));
                break;
            default:
                throw new IllegalStateException("Unknown or unsupported position policy: " + positionPolicy);
        }
    }

    /**
     * Set policy on how text position is reported to AST. If the neither policy
     * defined in the enumeration {@link PositionPolicy} suits the AST classes,
     * a custom policy could be implemented by overriding the methods
     * {@link #setObjectStartPos(Object, Object, TermToken)} and
     * {@link #setObjectEndPos(Object, Object, Object, TermToken)}.
     *
     * @param policy new value of policy
     */
    public void setPosPolicy(PositionPolicy policy) {
        if (policy == null) {
            throw new NullPointerException("The null policy is not allowed");
        }
        this.positionPolicy = policy;
    }

    /**
     * Get meta object by name. Metaobject can be anything that can be used to
     * create class. For example BeansTermParser uses BeanInfo as meta object.
     *
     * @param name the object to be mapped to metaobject
     * @return an meta object
     */
    protected abstract MetaObjectType getMetaObject(ObjectName name);

    /**
     * Create instance of object from meta object
     *
     * @param metaObject a metaobject
     * @param name       a name of object
     * @return new instance
     */
    protected abstract BaseObjectType createInstance(MetaObjectType metaObject, ObjectName name);

    /**
     * Predefined position setting policies. They determine how start/end
     * positions are saved in AST. It is possible to create a custom the policy
     * by overriding the methods
     * {@link AbstractTreeParser#setObjectStartPos(Object, Object, TermToken)}
     * and
     * {@link AbstractTreeParser#setObjectEndPos(Object, Object, Object, TermToken)}
     * .
     */
    public enum PositionPolicy {
        /**
         * Use field {@code startLine} (int), {@code startColumn}(int), {@code
         * startOffset}(long), {@code endLine}, {@code endColumn}, {@code
         * endOffset}
         */
        EXPANDED,
        /**
         * Use fields {@code start} and {@code end} (both are {@link TextPos})
         */
        POSITIONS,
        /**
         * Use the field {@code location} of type {@link SourceLocation}.
         */
        SOURCE_LOCATION,
    }
}
