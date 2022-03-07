package net.sf.etl.parsers.streams;

import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.resource.ResolvedObject;
import net.sf.etl.parsers.resource.ResourceDescriptor;
import net.sf.etl.parsers.resource.ResourceRequest;

import java.net.URL;
import java.util.List;

/**
 * The locator based on classpath.
 */
public class GrammarClasspathLocator implements GrammarLocator {
    /**
     * The used class loader.
     */
    private final ClassLoader classLoader;

    /**
     * The constructor based over classloader.
     *
     * @param classLoader the classloader
     */
    public GrammarClasspathLocator(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ResolvedObject<String> resolve(ResourceRequest request) {
        URL url = StandardGrammars.getGrammarResource(classLoader, request.grammarId());
        return new ResolvedObject<>(request, List.of(), new ResourceDescriptor(url.toString()), url.toString());
    }
}
