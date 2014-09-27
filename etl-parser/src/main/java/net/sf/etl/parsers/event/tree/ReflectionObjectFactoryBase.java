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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * This class provide facilities for mapping class names.
 *
 * @param <BaseObjectType> the base type for returned objects
 * @param <FeatureType>    the type for feature metatype used by objects
 * @param <MetaObjectType> the type for meta object type
 * @param <HolderType>     the holder type for collection properties
 * @author const
 */
public abstract class ReflectionObjectFactoryBase<BaseObjectType, FeatureType, MetaObjectType, HolderType>
        extends ObjectFactory<BaseObjectType, FeatureType, MetaObjectType, HolderType> {
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ReflectionObjectFactoryBase.class);
    /**
     * The class loader that should be used to load classes.
     */
    private final ClassLoader classLoader;
    /**
     * The map from namespace to java package.
     */
    private final HashMap<String, String> namespaceMapping = new HashMap<String, String>();
    /**
     * The map from namespace to object to java class.
     */
    private final HashMap<String, HashMap<String, Class<?>>> objectMapping =
            new HashMap<String, HashMap<String, Class<?>>>();

    /**
     * The constructor.
     *
     * @param classLoader the class loader for the parser
     */
    public ReflectionObjectFactoryBase(final ClassLoader classLoader) {
        this.classLoader = classLoader == null ? getClassLoader() : classLoader;
    }

    /**
     * This method tries to detect class loader that should be used by this
     * instance in case when class loader is not provided by creator of the
     * parser.
     * <p/>
     * The method checks contextClassLoader of the thread, and if is still not
     * found, uses class loader of parser class. Note if the class is
     * subclassed, a classloader of the subclass will be used.
     *
     * @return the class loader
     */
    private ClassLoader getClassLoader() {
        ClassLoader factoryClassLoader = null;
        try {
            factoryClassLoader = Thread.currentThread().getContextClassLoader();
        } catch (final Exception ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("There is a security problem with getting classLoader", ex);
            }
        }
        if (factoryClassLoader == null) {
            factoryClassLoader = getClass().getClassLoader();
        }
        return factoryClassLoader;
    }

    /**
     * Add mapping from namespace to java package.
     *
     * @param namespace   the namespace
     * @param javaPackage the java package
     */
    public final void mapNamespaceToPackage(final String namespace, final String javaPackage) {
        namespaceMapping.put(namespace, javaPackage);
    }

    /**
     * Map name to the class.
     *
     * @param namespace the object namespace
     * @param name      the object name in parser
     * @param beanClass the class of java bean
     */
    public final void mapNameToClass(final String namespace, final String name, final Class<?> beanClass) {
        HashMap<String, Class<?>> nameToClass = objectMapping.get(namespace);
        if (nameToClass == null) {
            nameToClass = new HashMap<String, Class<?>>();
            objectMapping.put(namespace, nameToClass);
        }
        nameToClass.put(name, beanClass);
    }

    /**
     * Get object class.
     *
     * @param name the name of class
     * @return class for the object name
     */
    protected final Class<?> getObjectClass(final ObjectName name) {
        // check object name map
        final HashMap<String, Class<?>> nameToObject = objectMapping.get(name.namespace());
        if (nameToObject != null) {
            final Class<?> rc = nameToObject.get(name.name());
            if (rc != null) {
                return rc;
            }
        }
        // check namespace map
        final String packageName = namespaceMapping.get(name.namespace());
        if (packageName != null) {
            final String className = packageName + "." + name.name();
            Class<?> rc;
            try {
                if (classLoader != null) {
                    rc = classLoader.loadClass(className);
                } else {
                    rc = Class.forName(className);
                }
                return rc;
            } catch (final ClassNotFoundException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Class has not been found for name: " + name, ex);
                }
            }
        }
        throw new ParserException("Class not found for object name " + name);
    }
}
