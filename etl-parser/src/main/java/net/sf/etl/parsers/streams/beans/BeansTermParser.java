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
import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.literals.LiteralUtils;
import net.sf.etl.parsers.streams.TermParserReader;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * This class parses event streams and creates java bean object. The parser is
 * intended for small domains where it is feasible to model AST as JavaBeans. In
 * such situation, there is no need to use EMF or something even more complex.
 * For example, the parser is used in test suite for some tests.
 * </p>
 * <p>
 * The parser uses BeanInfo classes quite naively. The performance impact of
 * this is not known and it should be measured later. For example, parser tries
 * to locate property in the list of properties returned by BeanInfo using
 * linear search. It also has unknown impact.
 * </p>
 * <p>
 * The parser recognizes the following types in properties of beans:
 * </p>
 * <dl>
 * <dt>String
 * <dt>
 * <dd>In that case text is supplied as is</dd>
 * <dt>int</dt>
 * <dd>This value is mapped using LiteralUtils</dd>
 * <dt>boolean
 * <dt>
 * <dd>In that case value is mapped using new Boolean(text).booleanValue()</dd>
 * </dl>
 *
 * @author const
 */
public class BeansTermParser extends AbstractReflectionParser<Object, PropertyDescriptor, BeanInfo, List<Object>> {
    /**
     * a logger
     */
    private static final Logger log = Logger.getLogger(BeansTermParser.class.getName());

    /**
     * A constructor for term parser
     *
     * @param parser      the parser
     * @param classLoader the class loader for the parser
     */
    public BeansTermParser(TermParserReader parser, ClassLoader classLoader) {
        super(parser, classLoader);
    }

    @Override
    protected BeanInfo getMetaObject(final ObjectName name) {
        final Class<?> beanClass = getObjectClass(name);
        return getBeanInfo(beanClass);
    }

    /**
     * Create object instance
     *
     * @param metaObject the bean info for the object
     * @param name       the name of the object
     * @return a new instance or null if it cannot be created
     */
    @Override
    protected Object createInstance(BeanInfo metaObject, ObjectName name) {
        final Class<?> beanClass = metaObject
                .getBeanDescriptor().getBeanClass();
        try {
            return beanClass.newInstance();
        } catch (final Exception e) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Object cannot be created: " + beanClass.getName());
            }
            throw new ParserException("Object cannot be created: " + beanClass.getName(), e);
        }
    }

    /**
     * Get bean info for the class. This method may be overridden by subclasses
     * to change a way BeanInfo is found.
     *
     * @param beanClass a bean class
     * @return the corresponding bean info or null if there is a problem
     */
    protected BeanInfo getBeanInfo(Class<?> beanClass) {
        try {
            return Introspector.getBeanInfo(beanClass);
        } catch (final IntrospectionException e) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "BeanInfo has not been found for class: " + beanClass.getName());
            }
            throw new ParserException("BeanInfo not found for object name " + beanClass.getName(), e);
        }
    }

    @Override
    protected void setToFeature(Object rc, PropertyDescriptor f, Object v) {
        final Method m = f.getWriteMethod();
        try {
            m.invoke(rc, v);
        } catch (final Exception e) {
            throw new ParserException("Cannot set to feature " + f.getName()
                    + " a value " + v + " of type " + v.getClass().getName()
                    + " using method " + m, e);
        }
    }

    @Override
    protected void addToFeature(Object rc, PropertyDescriptor f, List<Object> holder, Object v) {
        holder.add(v);
    }

    @Override
    protected List<Object> startListCollection(Object rc, BeanInfo metaObject, PropertyDescriptor f) {
        final Method m = f.getReadMethod();
        try {
            final Object array = m.invoke(rc);
            final ArrayList<Object> list = new ArrayList<Object>();
            if (array != null) {
                final int n = Array.getLength(array);
                for (int i = 0; i < n; i++) {
                    list.add(Array.get(array, i));
                }
            }
            return list;
        } catch (final Exception e) {
            throw new ParserException("Cannot read property " + f.getName(), e);
        }
    }

    @Override
    protected void endListCollection(Object rc, BeanInfo metaObject,
                                     PropertyDescriptor f, List<Object> holder) {
        try {
            final ArrayList<Object> list = (ArrayList<Object>) holder;
            final Method m = f.getWriteMethod();
            final int n = list.size();
            final Object array = Array.newInstance(f.getPropertyType().getComponentType(), n);
            for (int i = 0; i < n; i++) {
                Array.set(array, i, list.get(i));
            }
            m.invoke(rc, array);
        } catch (final Exception e) {
            throw new ParserException("Cannot write feature " + f, e);
        }
    }

    @Override
    protected PropertyDescriptor getPropertyMetaObject(Object rc, BeanInfo metaObject, String name) {
        final PropertyDescriptor[] propertyDescriptors = metaObject.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getName().equals(name)) {
                return propertyDescriptor;
            }
        }
        throw new ParserException("Cannot find feature " + name + " ind class "
                + metaObject.getBeanDescriptor().getBeanClass().getName());
    }

    @Override
    protected Object parseValue(Object rc, PropertyDescriptor f, Token value) {
        Class<?> fc = f.getPropertyType();
        if (fc.isArray()) {
            fc = fc.getComponentType();
        }
        if (fc == int.class || fc == Integer.class) {
            return LiteralUtils.parseInt(value.text());
        }
        return value.text();
    }

}
