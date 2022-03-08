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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

/**
 * Class for generic parse error information. This instances of this class are
 * immutable provided that error arguments are immutable if it is error token.
 * Note that the ETL parser infrastructure always returns immutable error
 * information objects.
 *
 * @author const
 */
public final class ErrorInfo implements Iterable<ErrorInfo>, Serializable {
    // FIXME add related locations support
    /**
     * The no arguments constant.
     */
    public static final Object[] NO_ARGS = new Object[0];
    /**
     * The bundle with lexical error messages.
     */
    private static final ResourceBundle LEXICAL_ERRORS = ResourceBundle
            .getBundle("net.sf.etl.parsers.errors.LexicalErrors");
    /**
     * The bundle with phrase error messages.
     */
    private static final ResourceBundle PHRASE_ERRORS = ResourceBundle
            .getBundle("net.sf.etl.parsers.errors.PhraseErrors");
    /**
     * the bundle with syntax error messages.
     */
    private static final ResourceBundle SYNTAX_ERRORS = ResourceBundle
            .getBundle("net.sf.etl.parsers.errors.TermErrors");
    /**
     * The bundle with grammar error messages.
     */
    private static final ResourceBundle GRAMMAR_ERRORS = ResourceBundle
            .getBundle("net.sf.etl.parsers.errors.GrammarErrors");
    /**
     * The id of error.
     */
    private final String errorId;
    /**
     * The arguments of error.
     */
    private final List<Object> errorArgs;
    /**
     * The source location for the error.
     */
    private final SourceLocation location;
    /**
     * the next error for this error info.
     */
    private final ErrorInfo nextError;

    /**
     * The constructor for error info.
     *
     * @param id       the identifier of the error
     * @param args     additional information associated with the error (usually used
     *                 for localized reporting). Note that array must contain
     *                 immutable objects that are preferably of primitive types.
     * @param start    the start of the error scope
     * @param end      the end of the error scope
     * @param systemId the system identifier for error
     */
    public ErrorInfo(final String id, final Object[] args,
                     final TextPos start, final TextPos end, final String systemId) {
        this(id, args, start, end, systemId, null);
    }

    /**
     * The constructor for error info.
     *
     * @param errorId   the identifier of the error
     * @param errorArgs additional information associated with the error (usually used
     *                  for localized reporting). Note that array must contain
     *                  immutable objects that are preferably of primitive types.
     * @param location  the location of the error
     * @param nextError the next error
     */
    public ErrorInfo(final String errorId, final List<Object> errorArgs,
                     final SourceLocation location, final ErrorInfo nextError) {
        this.errorId = errorId;
        this.errorArgs = errorArgs;
        this.location = location;
        this.nextError = nextError;
    }

    /**
     * A constructor for error info.
     *
     * @param id        the identifier of the error
     * @param args      additional information associated with the error (usually used
     *                  for localized reporting). Note that array must contain
     *                  immutable objects that are preferably of primitive types.
     * @param start     the start of the error scope
     * @param end       the end of the error scope
     * @param systemId  the system identifier for error
     * @param nextError the nextError of this error
     */
    public ErrorInfo(final String id, final Object[] args, final TextPos start, final TextPos end,
                     final String systemId, final ErrorInfo nextError) {
        location = new SourceLocation(start, end, systemId);
        this.errorArgs = args == null || args.length == 0
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<Object>(Arrays.asList(args)));
        this.errorId = id;
        this.nextError = nextError;
    }

    /**
     * Merge collection of error to the single ErrorInfo.
     *
     * @param errors the errors to merge
     * @return the error list
     */
    public static ErrorInfo merge(final Collection<ErrorInfo> errors) {
        final ArrayList<ErrorInfo> list = new ArrayList<ErrorInfo>();
        for (final ErrorInfo error : errors) {
            if (error != null) {
                for (final ErrorInfo e : error) {
                    list.add(e);
                }
            }
        }
        Collections.reverse(list);
        ErrorInfo current = null;
        for (final ErrorInfo e : list) {
            current = new ErrorInfo(e.errorId, e.errorArgs, e.location, current); // NOPMD
        }
        return current;
    }

    /**
     * Merge errors.
     *
     * @param errors the errors to merge
     * @return merged errors
     */
    public static ErrorInfo merge(final ErrorInfo... errors) {
        return merge(Arrays.asList(errors));
    }

    /**
     * @return the location information for the error
     */
    public SourceLocation location() {
        return location;
    }

    /**
     * @return the end of the error scope
     */
    public TextPos end() {
        return location.end();
    }

    /**
     * @return the arguments of the error
     */
    public List<Object> errorArgs() {
        return errorArgs;
    }

    /**
     * @return the identifier of the error
     */
    public String errorId() {
        return errorId;
    }

    /**
     * @return the next error for this error info (used when there are several
     * locations to report, or when multiple errors are associated with the current element)
     */
    public ErrorInfo cause() {
        return nextError;
    }

    /**
     * @return the start of the error scope
     */
    public TextPos start() {
        return location.start();
    }

    @Override
    public String toString() {
        return "ErrorInfo{"
                + "errorId='" + errorId + '\''
                + ", errorArgs=" + errorArgs
                + ", location=" + location
                + ", nextError=" + nextError
                + '}';
    }

    /**
     * @return the error message text
     */
    public String message() {
        final ResourceBundle b;
        if (errorId.startsWith("lexical.")) {
            b = LEXICAL_ERRORS;
        } else if (errorId.startsWith("phrase.")) {
            b = PHRASE_ERRORS;
        } else if (errorId.startsWith("syntax.")) {
            b = SYNTAX_ERRORS;
        } else if (errorId.startsWith("grammar.")) {
            b = GRAMMAR_ERRORS;
        } else {
            throw new IllegalStateException("Unknown message prefix: "
                    + errorId);
        }
        return MessageFormat.format(b.getString(errorId), errorArgs.toArray());
    }

    @Override
    public Iterator<ErrorInfo> iterator() {
        return new Iterator<ErrorInfo>() {
            private ErrorInfo current = ErrorInfo.this;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public ErrorInfo next() {
                final ErrorInfo rc = current;
                if (rc == null) {
                    throw new NoSuchElementException();
                }
                current = current.nextError;
                return rc;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Removing elements is not supported");
            }
        };
    }
}
