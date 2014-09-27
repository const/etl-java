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

import net.sf.etl.parsers.ObjectName;
import net.sf.etl.parsers.PropertyName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

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
public class SimpleObjectFactory<BaseObject>
        extends ReflectionObjectFactoryBase<BaseObject, SimpleObjectFactory.Property, Class<?>, List<Object>> {
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ReflectionObjectFactoryBase.class);

    /**
     * The cache of fields.
     */
    private final HashMap<Class<?>, HashMap<String, Property>> propertyCache =
            new HashMap<Class<?>, HashMap<String, Property>>();

    /**
     * The constructor from super class.
     *
     * @param classLoader the class loader for the parser
     */
    public SimpleObjectFactory(final ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public final void addToFeature(final BaseObject rc, final Property f, final List<Object> holder, final Object v) {
        holder.add(v);
        valueEnlisted(rc, f, v);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final BaseObject createInstance(final Class<?> metaObject, final ObjectName name) {
        try {
            return (BaseObject) metaObject.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Instance of " + metaObject.getCanonicalName() + " cannot be created.", e);
            }
            throw new RuntimeException("Instance of " + metaObject.getCanonicalName() + " cannot be created.", e);
        }
    }

    @Override
    public final void endListCollection(final BaseObject rc, final Class<?> metaObject, final Property f,
                                        final List<Object> holder) {
        // do nothing
    }

    @Override
    protected final Class<?> getMetaObject(final ObjectName name) {
        return getObjectClass(name);
    }

    @Override
    public final Property getPropertyMetaObject(final BaseObject rc, final Class<?> metaObject, final String name) {
        return property(metaObject, name);
    }

    @Override
    public final void setToFeature(final BaseObject rc, final Property f, final Object v) {
        try {
            f.set(rc, v);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("The property " + f + " cannot be accessed.", e);
            }
            throw new RuntimeException("The property " + f + " cannot be accessed.", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final List<Object> startListCollection(final BaseObject rc, final Class<?> metaObject, final Property f) {
        try {
            final List<Object> list = (List<Object>) f.get(rc);
            if (list == null) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("The property " + f + " must have a collection value.");
                }
                throw new IllegalStateException("The property " + f + " must have a collection value.");
            }
            return list;
        } catch (ClassCastException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("The property " + f + " is not of List type.", e);
            }
            throw e;
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("The property " + f + " cannot be accessed.", e);
            }
            throw new RuntimeException("The property " + f + " cannot be accessed.", e);
        }
    }

    @Override
    protected final Class<?> getFeatureType(final Property f) {
        return f.getType();
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
    protected void valueEnlisted(final BaseObject rc, final Property f, final Object v) {
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
    private Property property(final Class<?> c, final String name) {
        try {
            final String featureName = PropertyName.lowerCaseFeatureName(name);
            HashMap<String, Property> classFields = propertyCache.get(c);
            if (classFields == null) {
                classFields = new HashMap<String, Property>();
                propertyCache.put(c, classFields);
            }
            Property rc = classFields.get(featureName);
            if (rc == null) {
                rc = Property.find(c, name);
                classFields.put(featureName, rc);
            }
            return rc;
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to find property " + name + " in class " + c.getCanonicalName(), e);
            }
            throw new RuntimeException("Unable to find property " + name + " in class " + c.getCanonicalName(), e);
        }
    }

    /**
     * The property object. Note that either setter or getter is defined.
     */
    public static final class Property {
        /**
         * The name of property.
         */
        private final String name;
        /**
         * The getter.
         */
        private final Method getter;
        /**
         * The setter.
         */
        private final Method setter;
        /**
         * The value type.
         */
        private final Class<?> type;

        /**
         * The constructor.
         *
         * @param name   the name
         * @param getter the getter
         * @param setter the setter
         * @param type   the value type
         */
        private Property(final String name, final Method getter, final Method setter, final Class<?> type) {
            this.name = name;
            this.getter = getter;
            this.setter = setter;
            this.type = type;
        }


        /**
         * Create property instance.
         *
         * @param objectClass the object class
         * @param name        the name
         * @return the found instance
         */
        public static Property find(final Class<?> objectClass, final String name) {
            final String uName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            final String setterName = "set" + uName;
            for (Method method : objectClass.getMethods()) {
                if (method.getName().equals(setterName)) {
                    final Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 1) {
                        final String getterName = (parameterTypes[0] == boolean.class ? "is" : "get") + uName;
                        try {
                            return new Property(name, objectClass.getMethod(getterName), method, parameterTypes[0]);
                        } catch (NoSuchMethodException e) {
                            throw new IllegalStateException("Getter is not defined "
                                    + objectClass.getName() + '.' + name, e);
                        }
                    }
                }
            }
            final Method getterMethod;
            try {
                final String getterName = "get" + uName;
                getterMethod = objectClass.getMethod(getterName);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Neither getter or setter is defined for "
                        + objectClass.getName() + '.' + name, e);
            }
            if (!List.class.isAssignableFrom(getterMethod.getReturnType())) {
                throw new IllegalStateException("Non-list getter is detected " + objectClass.getName() + '.' + name);
            }
            final Type genericReturnType = getterMethod.getGenericReturnType();
            if (!(genericReturnType instanceof ParameterizedType)) {
                throw new IllegalStateException("Getter is does not specify parameterized type "
                        + objectClass.getName() + '.' + name + " -> " + genericReturnType);
            }
            ParameterizedType type = (ParameterizedType) genericReturnType;
            final Type[] actualTypeArguments = type.getActualTypeArguments();
            if (actualTypeArguments.length != 1) {
                throw new IllegalStateException("Too much of type arguments "
                        + objectClass.getName() + '.' + name + " -> " + genericReturnType);
            }
            if (actualTypeArguments[0] instanceof Class<?>) {
                return new Property(name, getterMethod, null, (Class<?>) actualTypeArguments[0]);
            } else {
                throw new IllegalStateException("Unrecognized type argument "
                        + objectClass.getName() + '.' + name + " -> " + genericReturnType);
            }
        }

        /**
         * @return the type
         */
        public Class<?> getType() {
            return type;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Set value to object.
         *
         * @param object the object
         * @param value  the value
         * @throws InvocationTargetException in case of invocation problem
         * @throws IllegalAccessException    in case of invocation problem
         */
        public void set(final Object object, final Object value)
                throws InvocationTargetException, IllegalAccessException {
            if (setter != null) {
                setter.invoke(object, value);
            } else {
                throw new IllegalStateException("This is a list property " + getter);
            }
        }

        /**
         * Get value from object.
         *
         * @param object the object
         * @return the value
         * @throws InvocationTargetException in case of invocation problem
         * @throws IllegalAccessException    in case of invocation problem
         */
        public Object get(final Object object) throws InvocationTargetException, IllegalAccessException {
            if (getter != null) {
                return getter.invoke(object);
            } else {
                throw new IllegalStateException("This is a single value property " + setter);
            }
        }

        /**
         * @return true if list property
         */
        public boolean isList() {
            return setter == null;
        }
    }
}
