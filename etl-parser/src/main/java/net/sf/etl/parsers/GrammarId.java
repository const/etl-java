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
