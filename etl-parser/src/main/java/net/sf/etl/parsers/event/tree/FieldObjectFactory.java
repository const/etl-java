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
package net.sf.etl.parsers.event.tree;

import net.sf.etl.parsers.ObjectName;
import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.PropertyName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This is a builder for simple models that use fields to represent the model
 * properties. Such models are not suitable to be public API because of
 * versioning issues, but they can very useful lightweight internal model API.
 * </p>
 * <p>
 * Fields of type that extends List interface are treated as collection
 * properties. Generic arguments of the type are checked if specific value type
 * can be added to it.
 * </p>
 * <p>
 * Fields of other types are treated as simple properties.
 * </p>
 * <p/>
 * <p>
 * Currently only the following value types are recognized:
 * </p>
 * <ul>
 * <li>{@link String} - text of token is assigned to it "as is".</li>
 * <li>int or {@link Integer} - text of token is converted to int with
 * {@link net.sf.etl.parsers.literals.LiteralUtils#parseInt(String)}.</li>
 * <li>Enumerations - appropriate constant is found using
 * {@link String#equalsIgnoreCase(String)} comparison with {@link Enum#name()}
 * value of the literal.</li>
 * </ul>
 *
 * @param <BaseObject> a type of the base object
 * @author const
 */
public class FieldObjectFactory<BaseObject> extends ReflectionObjectFactoryBase<BaseObject, Field,
        Class<?>, List<Object>> {
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ReflectionObjectFactoryBase.class);

    /**
     * The cache of fields.
     */
    // TODO review usage ConcurrentHashMap in object factories (static vs. per parser instance)
    private final Map<Class<?>, Map<String, Field>> fieldCache = // NOPMD
            new HashMap<Class<?>, Map<String, Field>>();

    /**
     * The constructor from super class.
     *
     * @param classLoader the class loader for the parser
     */
    public FieldObjectFactory(final ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public final void addToFeature(final BaseObject rc, final Field f, final List<Object> holder, final Object v) {
        holder.add(v);
        valueEnlisted(rc, f, v);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final BaseObject createInstance(final Class<?> metaObject, final ObjectName name) {
        try {
            return (BaseObject) metaObject.newInstance();
        } catch (Exception e) { // NOPMD
            if (LOG.isDebugEnabled()) {
                LOG.debug("Instance of " + metaObject.getCanonicalName() + " cannot be created.", e);
            }
            throw new ParserException("Instance of " + metaObject.getCanonicalName() + " cannot be created.", e);
        }
    }

    @Override
    public final void endListCollection(final BaseObject rc, final Class<?> metaObject, final Field f,
                                        final List<Object> holder) {
        // do nothing
    }

    @Override
    protected final Class<?> getMetaObject(final ObjectName name) {
        return getObjectClass(name);
    }

    @Override
    public final Field getPropertyMetaObject(final BaseObject rc, final Class<?> metaObject, final String name) {
        return field(metaObject, name);
    }

    @Override
    public final void setToFeature(final BaseObject rc, final Field f, final Object v) {
        try {
            f.set(rc, v);
        } catch (IllegalAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("The field " + f + " cannot be accessed.", e);
            }
            throw new ParserException("The field " + f + " cannot be accessed.", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final List<Object> startListCollection(final BaseObject rc, final Class<?> metaObject, final Field f) {
        try {
            final List<Object> list = (List<Object>) f.get(rc);
            if (list == null) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("The field " + f + " must have a collection value.");
                }
                throw new IllegalStateException("The field " + f + " must have a collection value.");
            }
            return list;
        } catch (ClassCastException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("The field " + f + " is not of List type.", e);
            }
            throw e;
        } catch (IllegalAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("The field " + f + " cannot be accessed.", e);
            }
            throw new ParserException("The field " + f + " cannot be accessed.", e);
        }
    }

    @Override
    protected final Class<?> getFeatureType(final Field f) {
        Class<?> elementType = f.getType();
        if (List.class.isAssignableFrom(elementType)) {
            final Type rawType = f.getGenericType();
            if (rawType instanceof ParameterizedType) {
                final ParameterizedType pt = (ParameterizedType) rawType;
                final Type arg = pt.getActualTypeArguments()[0];
                if (arg instanceof Class) {
                    elementType = (Class<?>) arg;
                } else {
                    throw new ParserException("The field " + f + " has unsupported collection type.");
                }
            } else {
                throw new ParserException("The field " + f + " has unsupported collection type: "
                        + f.getGenericType().getClass().getCanonicalName());
            }
        }
        return elementType;
    }

    /**
     * This method is called when value is added to feature of object by either
     * set or by using add method. This method gives a chance to implementers
     * set owner for the added object.
     *
     * @param rc an object to process
     * @param f  a filed of the object
     * @param v  a value added to field
     */
    protected void valueEnlisted(final BaseObject rc, final Field f, final Object v) {
        // by default, do nothing
    }

    /**
     * Get a field from class. The method maintains internal cache. Note that
     * cache is private to this parser instance.
     *
     * @param c    class to example
     * @param name a name to get
     * @return the field
     */
    private Field field(final Class<?> c, final String name) {
        try {
            final String featureName = PropertyName.lowerCaseFeatureName(name);
            Map<String, Field> classFields = fieldCache.get(c);
            if (classFields == null) {
                classFields = new HashMap<String, Field>();
                fieldCache.put(c, classFields);
            }
            Field rc = classFields.get(featureName);
            if (rc == null) {
                rc = c.getField(featureName);
                classFields.put(featureName, rc);
            }
            return rc;
        } catch (Exception e) { // NOPMD
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to find field " + name + " in class " + c.getCanonicalName(), e);
            }
            throw new ParserException("Unable to find field " + name + " in class " + c.getCanonicalName(), e);
        }
    }
}
