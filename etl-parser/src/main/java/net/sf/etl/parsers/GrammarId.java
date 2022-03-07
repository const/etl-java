package net.sf.etl.parsers;

import net.sf.etl.parsers.literals.ParseResult;
import net.sf.etl.parsers.literals.StringParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Grammar identifier.
 *
 * @param name    the name
 * @param version the version
 */
public record GrammarId(String name, String version) {
    /**
     * The resource prefix for grammars in Jar
     */
    public static final String RESOURCE_PREFIX = "META-INF/etl/grammars/";
    private static final Pattern VERSION_PATTERN = Pattern.compile("[a-zA-Z0-9.\\-]*");

    public GrammarId {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(version, "version");
        if (!VERSION_PATTERN.matcher(version).matches()) {
            throw new IllegalArgumentException("Version is in invalid format");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
    }

    /**
     * Create grammar id w/o version.
     *
     * @param name the name
     * @return the grammar id
     */
    public static GrammarId unversioned(String name) {
        return new GrammarId(name, "");
    }

    /**
     * Check if version is valid.
     *
     * @param version the version
     * @return true if version is valid
     */
    public static boolean isVersionValid(String version) {
        return version != null && VERSION_PATTERN.matcher(version).matches();
    }

    /**
     * Parse version from tokens
     *
     * @param name    the qualified name
     * @param version the version
     * @return parsed grammar id
     */
    public static ParseResult<GrammarId> parse(SourceLocation location, List<Token> name, Token version) {
        Objects.requireNonNull(location, "source");
        Objects.requireNonNull(name, "name");
        var errors = new ArrayList<ErrorInfo>();
        var qName = name.stream()
                .map(Token::text)
                .collect(Collectors.joining("."));
        String v;
        if (version == null) {
            v = "";
        } else if (version.hasErrors()) {
            v = "";
            errors.add(version.errorInfo());
        } else {
            var p = new StringParser(version.text(), version.start(), location.systemId());
            var r = p.parse();
            if (r.getErrors() != null) {
                errors.add(r.getErrors());
            }
            v = r.getText();
            if (!GrammarId.isVersionValid(v)) {
                v = "";
                errors.add(new ErrorInfo("syntax.InvalidVersion", List.of(v), new SourceLocation(version.start(), version.end(), location.systemId()), null));
            }
        }
        if (qName.isEmpty()) {
            return new ParseResult<>(null, ErrorInfo.merge(errors));
        } else {
            return new ParseResult<>(new GrammarId(qName, v), ErrorInfo.merge(errors));
        }
    }

    /**
     * @return get relative path for the grammar
     */
    public String getRelativePath() {
        return name.replace('.', '/')
                + (version.isEmpty() ? "" : "-" + version.replace('.', '_'))
                + ".g.etl";
    }

    public String getResourcePath() {
        return RESOURCE_PREFIX + getRelativePath();
    }

    @Override
    public String toString() {
        return name + (version.isEmpty() ? "" : ':' + version);
    }
}
