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
import net.sf.etl.parsers.TermToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
     * The logger
     */
    private static final Logger log = Logger.getLogger(ReflectionObjectFactoryBase.class.getName());
    /**
     * The class loader that should be used to load classes
     */
    protected final ClassLoader classLoader;
    /**
     * the active token collectors
     */
    protected final ArrayList<TokenCollector> collectors = new ArrayList<TokenCollector>();
    /**
     * The map from namespace to java package
     */
    protected final HashMap<String, String> namespaceMapping = new HashMap<String, String>();
    /**
     * The map from namespace to object to java class
     */
    protected final HashMap<String, HashMap<String, Class<?>>> objectMapping = new HashMap<String, HashMap<String, Class<?>>>();

    /**
     * The constructor
     *
     * @param classLoader the class loader for the parser
     */
    public ReflectionObjectFactoryBase(ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = getClassLoader();
        }
        this.classLoader = classLoader;
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
    protected ClassLoader getClassLoader() {
        ClassLoader classLoader = null;
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (final Exception ex) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "There is a security problem with getting classLoader", ex);
            }
        }
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        return classLoader;
    }

    /**
     * Add mapping from namespace to java package
     *
     * @param namespace   the namespace
     * @param javaPackage the java package
     */
    public void mapNamespaceToPackage(String namespace, String javaPackage) {
        namespaceMapping.put(namespace, javaPackage);
    }

    /**
     * Map name to the class
     *
     * @param namespace the object namespace
     * @param name      the object name in parser
     * @param beanClass the class of java bean
     */
    public void mapNameToClass(String namespace, String name, Class<?> beanClass) {
        HashMap<String, Class<?>> nameToClass = objectMapping.get(namespace);
        if (nameToClass == null) {
            nameToClass = new HashMap<String, Class<?>>();
            objectMapping.put(namespace, nameToClass);
        }
        nameToClass.put(name, beanClass);
    }

    /**
     * Get object class
     *
     * @param name the name of class
     * @return class for the object name
     */
    protected Class<?> getObjectClass(ObjectName name) {
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
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Class has not been found for name: " + name);
                }
            }
        }
        throw new ParserException("Class not found for object name " + name);
    }

    /**
     * Handle any token
     *
     * @param token the token
     */
    @Override
    public void handleToken(TermToken token) {
        super.handleToken(token);
        if (!collectors.isEmpty()) {
            for (TokenCollector c : collectors) {
                c.collect(token);
            }
        }
    }

    @Override
    public void objectEnded(BaseObjectType object, TermToken token) {
        if (object instanceof TokenCollector) {
            TokenCollector r = collectors.remove(collectors.size() - 1);
            assert r == object;
        }
        super.objectEnded(object, token);
    }

    @Override
    public void objectStarted(BaseObjectType object, TermToken token) {
        if (object instanceof TokenCollector) {
            collectors.add((TokenCollector) object);
        }
        super.objectStarted(object, token);
    }
}
