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
package net.sf.etl.parsers.streams.beans;

import net.sf.etl.parsers.ObjectName;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.literals.LiteralUtils;
import net.sf.etl.parsers.streams.TermParserReader;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * {@link LiteralUtils#parseInt(String)}.</li>
 * <li>Enumerations - appropriate constant is found using
 * {@link String#equalsIgnoreCase(String)} comparison with {@link Enum#name()}
 * value of the literal.</li>
 * </ul>
 *
 * @param <BaseObject> a type of the base object
 * @author const
 */
public class FieldTermParser<BaseObject> extends AbstractReflectionParser<BaseObject, Field, Class<?>, List<Object>> {
    /**
     * The logger
     */
    private static final Logger log = Logger.getLogger(AbstractReflectionParser.class.getName());

    /**
     * The cache of fields.
     */
    private final HashMap<Class<?>, HashMap<String, Field>> fieldCache = new HashMap<Class<?>, HashMap<String, Field>>();

    /**
     * The constructor from super class
     *
     * @param parser      the term parser
     * @param classLoader the class loader for the parser
     */
    public FieldTermParser(TermParserReader parser, ClassLoader classLoader) {
        super(parser, classLoader);
    }

    @Override
    protected void addToFeature(BaseObject rc, Field f, List<Object> holder, Object v) {
        holder.add(v);
        valueEnlisted(rc, f, v);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected BaseObject createInstance(Class<?> metaObject, ObjectName name) {
        try {
            return (BaseObject) metaObject.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            if (log.isLoggable(Level.SEVERE)) {
                log.log(Level.SEVERE, "Instance of " + metaObject.getCanonicalName() + " cannot be created.", e);
            }
            throw new RuntimeException("Instance of " + metaObject.getCanonicalName() + " cannot be created.", e);
        }
    }

    @Override
    protected void endListCollection(BaseObject rc, Class<?> metaObject, Field f, List<Object> holder) {
        // do nothing
    }

    @Override
    protected Class<?> getMetaObject(ObjectName name) {
        return getObjectClass(name);
    }

    @Override
    protected Field getPropertyMetaObject(BaseObject rc, Class<?> metaObject, String name) {
        return field(metaObject, name);
    }

    @Override
    protected void setToFeature(BaseObject rc, Field f, Object v) {
        try {
            f.set(rc, v);
        } catch (IllegalAccessException e) {
            if (log.isLoggable(Level.SEVERE)) {
                log.log(Level.SEVERE, "The field " + f + " cannot be accessed.", e);
            }
            throw new RuntimeException("The field " + f + " cannot be accessed.", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Object> startListCollection(BaseObject rc, Class<?> metaObject, Field f) {
        try {
            List<Object> list = (List<Object>) f.get(rc);
            if (list == null) {
                if (log.isLoggable(Level.SEVERE)) {
                    log.log(Level.SEVERE, "The field " + f + " must have a collection value.");
                }
                throw new IllegalStateException("The field " + f + " must have a collection value.");
            }
            return list;
        } catch (ClassCastException e) {
            if (log.isLoggable(Level.SEVERE)) {
                log.log(Level.SEVERE, "The field " + f + " is not of List type.", e);
            }
            throw e;
        } catch (IllegalAccessException e) {
            if (log.isLoggable(Level.SEVERE)) {
                log.log(Level.SEVERE, "The field " + f + " cannot be accessed.", e);
            }
            throw new RuntimeException("The field " + f + " cannot be accessed.", e);
        }
    }

    @Override
    protected Object parseValue(BaseObject rc, Field f, Token value) {
        Class<?> elementType = f.getType();
        if (List.class.isAssignableFrom(elementType)) {
            Type rawType = f.getGenericType();
            if (rawType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) rawType;
                Type arg = pt.getActualTypeArguments()[0];
                if (arg instanceof Class) {
                    elementType = (Class<?>) arg;
                } else {
                    throw new RuntimeException("The field " + f + " has unsupported collection type.");
                }
            } else {
                throw new RuntimeException("The field " + f + " has unsupported collection type: "
                        + f.getGenericType().getClass().getCanonicalName());
            }
        }
        if (elementType == Token.class) {
            return value;
        }
        if (elementType == String.class) {
            return value.text();
        } else if (elementType == int.class || elementType == Integer.class) {
            return LiteralUtils.parseInt(value.text());
        } else if (elementType.isEnum()) {
            final String text = value.text();
            for (final Object o : elementType.getEnumConstants()) {
                final Enum<?> e = (Enum<?>) o;
                // note that line below works only for single-word enums
                if (text.equalsIgnoreCase(e.name())) {
                    return e;
                }
            }
            throw new RuntimeException("No constant with name " + value.text()
                    + " in enum " + elementType.getCanonicalName());
        }
        throw new RuntimeException("Unsupported field type " + f);
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
    protected void valueEnlisted(BaseObject rc, Field f, Object v) {
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
    private Field field(Class<?> c, String name) {
        try {
            name = lowerCaseFeatureName(name);
            HashMap<String, Field> classFields = fieldCache.get(c);
            if (classFields == null) {
                classFields = new HashMap<String, Field>();
                fieldCache.put(c, classFields);
            }
            Field rc = classFields.get(name);
            if (rc == null) {
                rc = c.getField(name);
                classFields.put(name, rc);
            }
            return rc;
        } catch (Exception e) {
            if (log.isLoggable(Level.SEVERE)) {
                log.log(Level.SEVERE, "Unable to find field " + name + " in class " + c.getCanonicalName(), e);
            }
            throw new RuntimeException("Unable to find field " + name + " in class " + c.getCanonicalName(), e);
        }
    }
}
