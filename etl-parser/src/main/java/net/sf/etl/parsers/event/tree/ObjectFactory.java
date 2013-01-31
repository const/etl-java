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
package net.sf.etl.parsers.event.tree;

import net.sf.etl.parsers.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;


/**
 * This is an abstract factory for objects created by tree parser, it provides some default behavior
 * to support parsing process, but all that behavior could be customized.
 *
 * @param <BaseObjectType> this is a base type for returned objects
 * @param <FeatureType>    this is a type for feature metatype used by objects
 * @param <MetaObjectType> this is a type for meta object type
 * @param <HolderType>     this is a holder type for collection properties
 * @author const
 */
public abstract class ObjectFactory<BaseObjectType, FeatureType, MetaObjectType, HolderType> {
    /**
     * a logger
     */
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(ObjectFactory.class.getName());
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
    protected String systemId;

    /**
     * @return the system identifier for the file being parsed
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * @param systemId the system id
     */
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    /**
     * Handle any token
     *
     * @param token the token
     */
    public void handleToken(TermToken token) {
        if (token.hasAnyErrors()) {
            hadErrors = true;
            handleErrorFromParser(token);
        }
    }

    /**
     * Handle loaded grammar
     *
     * @param token             the token
     * @param loadedGrammarInfo the grammar information
     */
    public void handleLoadedGrammar(TermToken token, LoadedGrammarInfo loadedGrammarInfo) {
        // do nothing
    }


    /**
     * Set abort on object from the namespace of default grammar
     * {@link net.sf.etl.parsers.StandardGrammars#DEFAULT_NS}.
     * Encountering objects from this namespace usually means that
     * loading grammar has failed, so further processing of
     * the source rarely makes sense.
     *
     * @param value if true, parsing is aborted on default object
     */
    protected void setAbortOnDefaultGrammar(boolean value) {
        abortOnDefault = value;
    }


    /**
     * Check if object with specified object name should be ignored
     *
     * @param token the token name
     * @param name  the name to check
     * @return true if object should be ignored
     */
    public boolean isIgnorable(TermToken token, ObjectName name) {
        if (abortOnDefault && StandardGrammars.DEFAULT_NS.equals(name.namespace())) {
            throw new ParserException("The default namespace is encountered: " + token);
        }
        // check if namespace is ignored
        if (ignoredNamespaces.contains(name.namespace())) {
            return true;
        }
        // check if specific object is ignored
        final Set<String> ns = ignoredObjects.get(name.name());
        return ns != null && ns.contains(name.namespace());
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
     * This method is called when object is about start being processed
     *
     * @param object the object to be processed
     * @param token  the token
     */
    public void objectStarted(BaseObjectType object, TermToken token) {
    }

    /**
     * This method is called when object was stopped to be processed
     *
     * @param object the object that was processed
     * @param token  the token
     */
    public void objectEnded(BaseObjectType object, TermToken token) {
    }

    /**
     * Handle error from parser
     *
     * @param errorToken a token to be reported
     */
    public void handleErrorFromParser(TermToken errorToken) {
        if (log.isLoggable(Level.SEVERE)) {
            log.severe("Error is detected during parsing file " + systemId + ": " + errorToken);
        }
    }

    /**
     * Handle unexpected property start. Default implementation throws an
     * exception. This means a serious bug in grammar. However, subclasses might
     * override this method to support some other policy.
     *
     * @param token the token
     */
    public void handleUnexpectedPropertyStart(TermToken token) {
        throw new ParserException("Unexpected property start inside property:" + token);
    }

    /**
     * Handle unexpected property end. Default implementation throws an
     * exception. This means a serious bug in grammar. However, subclasses might
     * override this method to support some other policy.
     *
     * @param token the token
     */
    public void handleUnexpectedObjectStart(TermToken token) {
        throw new ParserException("Unexpected object start inside object:" + token);
    }

    /**
     * Handle unexpected value. Default implementation throws an exception. This
     * means a serious bug in grammar. However, subclasses might
     * override this method to support some other policy.
     *
     * @param token the token
     */
    public void handleUnexpectedValue(TermToken token) {
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
    public Object parseValue(BaseObjectType rc, FeatureType f, Token value) {
        return value.text();
    }

    /**
     * Set value to feature
     *
     * @param rc    the object
     * @param f     the feature to update
     * @param value the value to set
     */
    public void setValueToFeature(BaseObjectType rc, FeatureType f, Token value) {
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
    public void addValueToFeature(BaseObjectType rc, FeatureType f, HolderType holder, Token value) {
        addToFeature(rc, f, holder, parseValue(rc, f, value));
    }

    /**
     * Set object to feature
     *
     * @param rc an object
     * @param f  a feature to update
     * @param v  a value to set
     */
    public abstract void setToFeature(BaseObjectType rc, FeatureType f, Object v);

    /**
     * Add object to feature
     *
     * @param rc     the object
     * @param f      the feature to update
     * @param holder the collection objects
     * @param v      the value to add
     */
    public abstract void addToFeature(BaseObjectType rc, FeatureType f, HolderType holder, Object v);

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
    public abstract HolderType startListCollection(BaseObjectType rc, MetaObjectType metaObject, FeatureType f);

    /**
     * Finish list collection
     *
     * @param rc         the object
     * @param metaObject the type of object
     * @param f          the feature to update
     * @param holder     the holder of values
     */
    public abstract void endListCollection(BaseObjectType rc, MetaObjectType metaObject, FeatureType f, HolderType holder);

    /**
     * get feature meta object
     *
     * @param rc         the object
     * @param metaObject the metaobject to examine
     * @param token      the token that contains LIST_PROPERTY_START or PROPERTY_START events.
     * @return a feature object
     */
    public FeatureType getPropertyMetaObject(BaseObjectType rc, MetaObjectType metaObject, TermToken token) {
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
    public abstract FeatureType getPropertyMetaObject(BaseObjectType rc, MetaObjectType metaObject, String name);

    /**
     * Set start position in object. Default implementation tries to set
     * properties startLine, startColumn, and startOffset with corresponding
     * values.
     *
     * @param rc         the object
     * @param metaObject the meta object
     * @param token      teh start object token
     * @return the value to be passed to
     *         {@link #setObjectEndPos(Object, Object, Object, net.sf.etl.parsers.TermToken)}, the
     *         default implementation returns the start position.
     */
    public Object setObjectStartPos(BaseObjectType rc, MetaObjectType metaObject, TermToken token) {
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
     *                   {@link #setObjectStartPos(Object, Object, net.sf.etl.parsers.TermToken)}
     * @param token      the end object token
     */
    public void setObjectEndPos(BaseObjectType rc, MetaObjectType metaObject, Object startValue, TermToken token) {
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
     * {@link #setObjectStartPos(Object, Object, net.sf.etl.parsers.TermToken)} and
     * {@link #setObjectEndPos(Object, Object, Object, net.sf.etl.parsers.TermToken)}.
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
     * create class. For example BeansObjectFactory uses BeanInfo as meta object.
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
     * {@link ObjectFactory#setObjectStartPos(Object, Object, net.sf.etl.parsers.TermToken)}
     * and
     * {@link ObjectFactory#setObjectEndPos(Object, Object, Object, net.sf.etl.parsers.TermToken)}
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
         * Use fields {@code start} and {@code end} (both are {@link net.sf.etl.parsers.TextPos})
         */
        POSITIONS,
        /**
         * Use the field {@code location} of type {@link net.sf.etl.parsers.SourceLocation}.
         */
        SOURCE_LOCATION,
    }
}
