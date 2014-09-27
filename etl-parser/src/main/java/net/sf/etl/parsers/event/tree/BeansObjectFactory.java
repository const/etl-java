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
import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.PropertyName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
 * The parser recognizes the following types in bean properties:
 * </p>
 * <dl>
 * <dt>String
 * <dd>In that case text is supplied as is
 * <dt>int
 * <dd>This value is mapped using LiteralUtils
 * <dt>boolean
 * <dd>In that case value is mapped using new Boolean(text).booleanValue()
 * </dl>
 *
 * @author const
 */
public class BeansObjectFactory extends ReflectionObjectFactoryBase<Object, PropertyDescriptor,
        BeanInfo, List<Object>> {
    /**
     * a logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BeansObjectFactory.class);

    /**
     * A constructor for term parser.
     *
     * @param classLoader the class loader for the parser
     */
    public BeansObjectFactory(final ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    protected final BeanInfo getMetaObject(final ObjectName name) {
        final Class<?> beanClass = getObjectClass(name);
        return getBeanInfo(beanClass);
    }

    @Override
    protected final Object createInstance(final BeanInfo metaObject, final ObjectName name) {
        final Class<?> beanClass = metaObject.getBeanDescriptor().getBeanClass();
        try {
            return beanClass.newInstance();
        } catch (final Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Object cannot be created: " + beanClass.getName());
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
    protected final BeanInfo getBeanInfo(final Class<?> beanClass) {
        try {
            return Introspector.getBeanInfo(beanClass);
        } catch (final IntrospectionException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("BeanInfo has not been found for class: " + beanClass.getName());
            }
            throw new ParserException("BeanInfo not found for object name " + beanClass.getName(), e);
        }
    }

    @Override
    public final void setToFeature(final Object rc, final PropertyDescriptor f, final Object v) {
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
    public final void addToFeature(final Object rc, final PropertyDescriptor f, final List<Object> holder,
                                   final Object v) {
        holder.add(v);
    }

    @Override
    public final List<Object> startListCollection(final Object rc, final BeanInfo metaObject,
                                                  final PropertyDescriptor f) {
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
    public final void endListCollection(final Object rc, final BeanInfo metaObject,
                                        final PropertyDescriptor f, final List<Object> holder) {
        try {
            final Method m = f.getWriteMethod();
            final int n = holder.size();
            final Object array = Array.newInstance(f.getPropertyType().getComponentType(), n);
            for (int i = 0; i < n; i++) {
                Array.set(array, i, holder.get(i));
            }
            m.invoke(rc, array);
        } catch (final Exception e) {
            throw new ParserException("Cannot write feature " + f, e);
        }
    }

    @Override
    public final PropertyDescriptor getPropertyMetaObject(final Object rc, final BeanInfo metaObject,
                                                          final String name) {
        final String featureName = PropertyName.lowerCaseFeatureName(name);
        final PropertyDescriptor[] propertyDescriptors = metaObject.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getName().equals(featureName)) {
                return propertyDescriptor;
            }
        }
        throw new ParserException("Cannot find feature " + featureName + " in class "
                + metaObject.getBeanDescriptor().getBeanClass().getName());
    }

    @Override
    protected final Class<?> getFeatureType(final PropertyDescriptor feature) {
        Class<?> fc = feature.getPropertyType();
        if (fc.isArray()) {
            fc = fc.getComponentType();
        }
        return fc;
    }
}
