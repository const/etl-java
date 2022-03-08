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
package net.sf.etl.parsers.event.tree; // NOPMD

import net.sf.etl.parsers.ObjectName;
import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.literals.LiteralUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This is an abstract factory for objects created by tree parser, it provides some default behavior
 * to support parsing process, but all that behavior could be customized.
 *
 * @param <B> this is a base type for returned objects
 * @param <F> this is a type for feature metatype used by objects
 * @param <M> this is a type for meta-object type
 * @param <H> this is a holder type for collection properties
 * @author const
 */
public abstract class ObjectFactory<B, F, M, H> {
    /**
     * This set contains namespaces ignored by parser.
     */
    private final Set<String> ignoredNamespaces = new HashSet<>();
    /**
     * This is map from ignored object names to set of namespaces.
     */
    private final Map<String, Set<String>> ignoredObjects = new HashMap<>(); // NOPMD
    /**
     * The value parsers.
     */
    private final List<ValueParser> valueParsers = new ArrayList<>();

    /**
     * If this flag is true, when default statement is encountered during
     * hasNext(), hasNext returns false (meaning that no more objects are
     * expected here).
     */
    private boolean abortOnDefault;
    /**
     * The current position policy.
     */
    private PositionPolicy positionPolicy = PositionPolicyExpanded.get();

    /**
     * The constructor.
     */
    protected ObjectFactory() {
        valueParsers.add(new PrimitiveParser());
        valueParsers.add(new TokenParser());
        valueParsers.add(new EnumParser());
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
    protected final void setAbortOnDefaultGrammar(final boolean value) {
        abortOnDefault = value;
    }


    /**
     * Check if object with specified object name should be ignored.
     *
     * @param token the token name
     * @param name  the name to check
     * @return true if object should be ignored
     */
    public final boolean isIgnorable(final TermToken token, final ObjectName name) {
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
    public final void ignoreNamespace(final String ns) {
        ignoredNamespaces.add(ns);
    }


    /**
     * Ignore specific object kind. Primary candidates for such ignoring are
     * doctype and blank statements.
     *
     * @param namespace the namespace
     * @param name      the name in namespace
     */
    public final void ignoreObjects(final String namespace, final String name) {
        Set<String> namespaces = ignoredObjects.computeIfAbsent(name, k -> new HashSet<>());
        namespaces.add(namespace);
    }

    /**
     * Parse value to fit to feature.
     *
     * @param f     the feature that will be used to set or add this value
     * @param value the value to parse
     * @return parsed value
     */
    public final Object parseValue(final F f, final Token value) {
        final Class<?> type = getFeatureType(f);
        for (final ValueParser parser : valueParsers) {
            final Object o = parser.parse(type, value);
            if (o != null) {
                return o;
            }
        }
        return value.text();
    }

    /**
     * Get feature class. For collection features element type is returned.
     *
     * @param feature the feature.
     * @return the feature type
     */
    protected abstract Class<?> getFeatureType(F feature);

    /**
     * Set value to feature.
     *
     * @param rc    the object
     * @param f     the feature to update
     * @param value the value to set
     */
    public final void setValueToFeature(final B rc, final F f, final Token value) {
        setToFeature(rc, f, parseValue(f, value));
    }

    /**
     * Add value to feature.
     *
     * @param rc     the object
     * @param f      the feature to update
     * @param holder the collection
     * @param value  the value to add
     */
    public final void addValueToFeature(final B rc, final F f, final H holder,
                                        final Token value) {
        addToFeature(rc, f, holder, parseValue(f, value));
    }

    /**
     * Set object to feature.
     *
     * @param rc an object
     * @param f  a feature to update
     * @param v  a value to set
     */
    public abstract void setToFeature(final B rc, final F f, final Object v);

    /**
     * Add object to feature.
     *
     * @param rc     the object
     * @param f      the feature to update
     * @param holder the collection objects
     * @param v      the value to add
     */
    public abstract void addToFeature(final B rc, final F f, final H holder,
                                      final Object v);

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
    public abstract H startListCollection(final B rc, final M metaObject,
                                          final F f);

    /**
     * Finish list collection.
     *
     * @param rc         the object
     * @param metaObject the type of object
     * @param f          the feature to update
     * @param holder     the holder of values
     */
    public abstract void endListCollection(final B rc, final M metaObject,
                                           final F f, final H holder);

    /**
     * get feature meta object.
     *
     * @param rc         the object
     * @param metaObject the metaobject to examine
     * @param token      the token that contains LIST_PROPERTY_START or PROPERTY_START events.
     * @return a feature object
     */
    public final F getPropertyMetaObject(final B rc, final M metaObject,
                                         final TermToken token) {
        return getPropertyMetaObject(rc, metaObject, token.propertyName().name());
    }

    /**
     * get feature meta object.
     *
     * @param rc         the object
     * @param metaObject the meta-object to examine
     * @param name       the name of property.
     * @return a feature object
     */
    public abstract F getPropertyMetaObject(final B rc, final M metaObject,
                                            final String name);

    /**
     * Set start position in object. Default implementation tries to set
     * properties startLine, startColumn, and startOffset with corresponding
     * values.
     *
     * @param rc         the object
     * @param metaObject the meta object
     * @param token      teh start object token
     * @return the value to be passed to
     * {@link #setObjectEndPos(Object, Object, Object, net.sf.etl.parsers.TermToken)}, the
     * default implementation returns the start position.
     */
    public final Object setObjectStartPos(final B rc, final M metaObject,
                                          final TermToken token) {
        return positionPolicy.setObjectStartPos(this, rc, metaObject, token);
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
    public final void setObjectEndPos(final B rc, final M metaObject,
                                      final Object startValue, final TermToken token) {
        positionPolicy.setObjectEndPos(this, null, rc, metaObject, startValue, token);
    }

    /**
     * Set policy on how text position is reported to AST.
     * {@link #setObjectStartPos(Object, Object, net.sf.etl.parsers.TermToken)} and
     * {@link #setObjectEndPos(Object, Object, Object, net.sf.etl.parsers.TermToken)}.
     *
     * @param policy new value of policy
     */
    public final void setPosPolicy(final PositionPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("The null policy is not allowed");
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
    protected abstract M getMetaObject(final ObjectName name);

    /**
     * Create instance of object from meta object.
     *
     * @param metaObject the meta object
     * @param name       the name of object
     * @return new instance
     */
    protected abstract B createInstance(final M metaObject, final ObjectName name);

    /**
     * The value converter interface.
     */
    public interface ValueParser {
        /**
         * Convert token to value.
         *
         * @param valueType  the value type
         * @param valueToken the value token
         * @return the value or null if value cannot be converted
         */
        Object parse(Class<?> valueType, Token valueToken);
    }

    /**
     * The position policy interface.
     */
    public interface PositionPolicy {
        /**
         * Set start position in object.
         *
         * @param factory    the factory instance
         * @param rc         the object
         * @param metaObject the meta-object
         * @param token      teh start object token
         * @param <B1>       this is a base type for returned objects
         * @param <F1>       this is a type for feature metatype used by objects
         * @param <M1>       this is a type for meta-object type
         * @param <H1>       this is a holder type for collection properties
         * @return the value to be passed to
         * {@link #setObjectEndPos(Object, Object, Object, net.sf.etl.parsers.TermToken)}, the
         * default implementation returns the start position.
         */
        <B1, F1, M1, H1>
        Object setObjectStartPos(ObjectFactory<B1, F1, M1, H1> factory, B1 rc, M1 metaObject, TermToken token);

        /**
         * Set end position in object. Default implementation tries to set
         * properties endLine, endColumn, and endOffset with corresponding values.
         *
         * @param factory    the factory instance
         * @param systemId   the system id
         * @param rc         the object
         * @param metaObject the meta object
         * @param startValue the value returned from
         *                   {@link #setObjectStartPos(Object, Object, net.sf.etl.parsers.TermToken)}
         * @param <B1>       this is a base type for returned objects
         * @param <F1>       this is a type for feature metatype used by objects
         * @param <M1>       this is a type for meta-object type
         * @param <H1>       this is a holder type for collection properties
         * @param token      the end object token
         */
        <B1, F1, M1, H1>
        void setObjectEndPos(ObjectFactory<B1, F1, M1, H1> factory,
                             final String systemId, final B1 rc, final M1 metaObject,
                             final Object startValue, final TermToken token);
    }

    /**
     * The converter for the token value.
     */
    public static final class TokenParser implements ValueParser {
        @Override
        public Object parse(final Class<?> valueType, final Token valueToken) {
            if (Token.class == valueType) {
                return valueToken;
            }
            return null;
        }
    }

    /**
     * The primitive value converter.
     */
    public static final class PrimitiveParser implements ValueParser {
        @Override
        public Object parse(final Class<?> valueType, final Token valueToken) {
            if (valueType == int.class || valueType == Integer.class) {
                return LiteralUtils.parseInt(valueToken.text());
            }
            if (valueType == double.class || valueType == Double.class) {
                return LiteralUtils.parseDouble(valueToken.text());
            }
            if (valueType == String.class) {
                return valueToken.text();
            }
            return null;
        }
    }

    /**
     * Parse to enum value.
     */
    public static final class EnumParser implements ValueParser {
        @Override
        public Object parse(final Class<?> valueType, final Token valueToken) {
            if (!valueType.isEnum()) {
                return null;
            }
            final String text = valueToken.text();
            for (final Object o : valueType.getEnumConstants()) {
                final Enum<?> e = (Enum<?>) o;
                // note that line below works only for single-word enums
                if (text.equalsIgnoreCase(e.name())) {
                    return e;
                }
            }
            throw new ParserException("No constant with name " + valueToken.text()
                    + " in enum " + valueType.getCanonicalName());
        }
    }

    /**
     * The star/end position policy. The implementation tries to set
     * property location with corresponding {@link SourceLocation} value.
     */
    public static final class PositionPolicyLocation implements PositionPolicy {

        /**
         * Get instance of the position policy.
         *
         * @return the instance.
         */
        public static PositionPolicy get() {
            return new PositionPolicyLocation();
        }

        @Override
        public <B1, F1, M1, H1> Object setObjectStartPos(
                final ObjectFactory<B1, F1, M1, H1> factory,
                final B1 rc, final M1 metaObject, final TermToken token) {
            return token.start();
        }

        @Override
        public <B1, F1, M1, H1> void setObjectEndPos(
                final ObjectFactory<B1, F1, M1, H1> factory,
                final String systemId, final B1 rc, final M1 metaObject,
                final Object startValue, final TermToken token) {
            final TextPos pos = token.start();
            final F1 locationFeature = factory.getPropertyMetaObject(rc, metaObject, "location");
            factory.setToFeature(rc, locationFeature,
                    new SourceLocation((TextPos) startValue, pos, systemId));
        }
    }

    /**
     * The star/end position policy. The implementation tries to set
     * properties start and end with corresponding {@link TextPos} values.
     */
    public static final class PositionPolicyPositions implements PositionPolicy {

        /**
         * Get instance of the position policy.
         *
         * @return the instance.
         */
        public static PositionPolicy get() {
            return new PositionPolicyPositions();
        }

        @Override
        public <B1, F1, M1, H1> Object setObjectStartPos(
                final ObjectFactory<B1, F1, M1, H1> factory,
                final B1 rc, final M1 metaObject, final TermToken token) {
            final TextPos pos = token.start();
            final F1 startFeature = factory.getPropertyMetaObject(rc, metaObject, "start");
            factory.setToFeature(rc, startFeature, pos);
            return null;
        }

        @Override
        public <B1, F1, M1, H1> void setObjectEndPos(
                final ObjectFactory<B1, F1, M1, H1> factory,
                final String systemId, final B1 rc, final M1 metaObject,
                final Object startValue, final TermToken token) {
            final TextPos pos = token.start();
            final F1 endFeature = factory.getPropertyMetaObject(rc, metaObject, "end");
            factory.setToFeature(rc, endFeature, pos);
        }
    }

    /**
     * The expanded position policy. The implementation tries to set
     * properties startLine, startColumn, and startOffset with corresponding
     * values.
     */
    public static final class PositionPolicyExpanded implements PositionPolicy {

        /**
         * Get instance of the position policy.
         *
         * @return the instance.
         */
        public static PositionPolicy get() {
            return new PositionPolicyExpanded();
        }

        @Override
        public <B1, F1, M1, H1> Object setObjectStartPos(
                final ObjectFactory<B1, F1, M1, H1> factory,
                final B1 rc, final M1 metaObject, final TermToken token) {
            final TextPos pos = token.start();
            final F1 startLineFeature = factory.getPropertyMetaObject(rc, metaObject, "startLine");
            factory.setToFeature(rc, startLineFeature, pos.line());
            final F1 startColumnFeature = factory.getPropertyMetaObject(rc, metaObject, "startColumn");
            factory.setToFeature(rc, startColumnFeature, pos.column());
            final F1 startOffsetFeature = factory.getPropertyMetaObject(rc, metaObject, "startOffset");
            factory.setToFeature(rc, startOffsetFeature, pos.offset());
            return null;
        }

        @Override
        public <B1, F1, M1, H1> void setObjectEndPos(
                final ObjectFactory<B1, F1, M1, H1> factory,
                final String systemId, final B1 rc, final M1 metaObject,
                final Object startValue, final TermToken token) {
            final TextPos pos = token.start();
            final F1 endLineFeature = factory.getPropertyMetaObject(rc, metaObject, "endLine");
            factory.setToFeature(rc, endLineFeature, pos.line());
            final F1 endColumnFeature = factory.getPropertyMetaObject(rc, metaObject, "endColumn");
            factory.setToFeature(rc, endColumnFeature, pos.column());
            final F1 endOffsetFeature = factory.getPropertyMetaObject(rc, metaObject, "endOffset");
            factory.setToFeature(rc, endOffsetFeature, pos.offset());
        }
    }

    /**
     * The "none" position policy. The implementation does nothing.
     */
    public static final class PositionPolicyNone implements PositionPolicy {

        /**
         * Get instance of the position policy.
         *
         * @return the instance.
         */
        public static PositionPolicy get() {
            return new PositionPolicyNone();
        }

        @Override
        public <B1, F1, M1, H1> Object setObjectStartPos(
                final ObjectFactory<B1, F1, M1, H1> factory,
                final B1 rc, final M1 metaObject, final TermToken token) {
            return null;
        }

        @Override
        public <B1, F1, M1, H1> void setObjectEndPos(
                final ObjectFactory<B1, F1, M1, H1> factory,
                final String systemId, final B1 rc, final M1 metaObject,
                final Object startValue, final TermToken token) {
            // do nothing
        }
    }
}
