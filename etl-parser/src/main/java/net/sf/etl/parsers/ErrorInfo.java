/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2009 Constantine A Plotnikov
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

import java.text.MessageFormat;
import java.util.*;

/**
 * Class for generic parse error information. This instances of this class are
 * immutable provided that error arguments are immutable if it is error token.
 * Note that the ETL parser infrastructure always returns immutable error
 * information objects.
 *
 * @author const
 */
public final class ErrorInfo {
    // FIXME add related locations support
    /**
     * a bundle with lexical error messages
     */
    private static final ResourceBundle LEXICAL_ERRORS = ResourceBundle
            .getBundle("net.sf.etl.parsers.errors.LexicalErrors");
    /**
     * a bundle with phrase error messages
     */
    private static final ResourceBundle PHRASE_ERRORS = ResourceBundle
            .getBundle("net.sf.etl.parsers.errors.PhraseErrors");
    /**
     * a bundle with syntax error messages
     */
    private static final ResourceBundle SYNTAX_ERRORS = ResourceBundle
            .getBundle("net.sf.etl.parsers.errors.TermErrors");
    /**
     * a bundle with grammar error messages
     */
    private static final ResourceBundle GRAMMAR_ERRORS = ResourceBundle
            .getBundle("net.sf.etl.parsers.errors.GrammarErrors");
    /**
     * no arguments constant
     */
    public static final Object NO_ARGS[] = new Object[0];
    /**
     * id of error
     */
    private final String errorId;
    /**
     * arguments of error
     */
    private final List<Object> errorArgs;
    /**
     * source location for the error
     */
    private final SourceLocation location;
    /**
     * the nextError for this error info
     */
    private final ErrorInfo nextError;

    /**
     * A constructor for error info
     *
     * @param id       a identifier of the error
     * @param args     additional information associated with the error (usually used
     *                 for localized reporting). Note that array must contain
     *                 immutable objects that are preferably of primitive types.
     * @param start    the start of the error scope
     * @param end      the end of the error scope
     * @param systemId a system identifier for error
     */
    public ErrorInfo(String id, Object args[], TextPos start, TextPos end,
                     String systemId) {
        this(id, args, start, end, systemId, null);
    }

    /**
     * A constructor for error info
     *
     * @param id        a identifier of the error
     * @param args      additional information associated with the error (usually used
     *                  for localized reporting). Note that array must contain
     *                  immutable objects that are preferably of primitive types.
     * @param start     the start of the error scope
     * @param end       the end of the error scope
     * @param systemId  a system identifier for error
     * @param nextError a nextError of this error
     */
    public ErrorInfo(String id, Object args[], TextPos start, TextPos end,
                     String systemId, ErrorInfo nextError) {
        location = new SourceLocation(start, end, systemId);
        this.errorArgs = args.length == 0 ?
                Collections.emptyList() :
                Collections.unmodifiableList(new ArrayList<Object>(Arrays.asList(args)));
        this.errorId = id;
        this.nextError = nextError;
    }

    /**
     * @return location information for the error
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
     *         locations to report, or when multiple errors are associated with the current element)
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
        return "ErrorInfo{" +
                "errorId='" + errorId + '\'' +
                ", errorArgs=" + errorArgs +
                ", location=" + location +
                ", nextError=" + nextError +
                '}';
    }

    /**
     * @return the error message text
     */
    public String message() {
        ResourceBundle b;
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
}
