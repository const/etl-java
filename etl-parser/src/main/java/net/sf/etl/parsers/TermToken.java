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

/**
 * This is a term token that corresponds to events that are results of the
 * parsing of the ETL source according to some grammar.
 *
 * @author const
 */
public final class TermToken extends AbstractToken {
    /**
     * Kind of token
     */
    private final Terms kind;
    /**
     * Role of token
     */
    private final SyntaxRole role;
    /**
     * Phrase token associated with this term token.
     */
    private final PhraseToken token;
    /**
     * property name or object name
     */
    private final Object structureId;

    /**
     * An error token. It is used to report syntax and grammar errors.
     *
     * @param kind        the kind of error
     * @param role        the role of the token
     * @param token       the phrase token
     * @param structureId the structure identifier
     * @param start       the start of the token
     * @param end         the end of the token
     * @param errorInfo   the error information error info covered by error
     */
    public TermToken(Terms kind, SyntaxRole role, Object structureId, PhraseToken token, TextPos start, TextPos end, ErrorInfo errorInfo) {
        super(start, end, errorInfo);
        this.kind = kind;
        this.role = role;
        this.token = token;
        this.structureId = structureId;
    }


    /**
     * An term token that wraps some phrase token.
     *
     * @param context a context of event
     * @param kind    a kind of error
     * @param role    role associated with token
     * @param token   a phrase token
     */
    public TermToken(TermContext context, Terms kind, SyntaxRole role,
                     PhraseToken token) {
        // TODO merge with primary constructor
        super(token.start(), token.end(), token.errorInfo());
        switch (kind) {
            case STRUCTURAL:
            case VALUE:
            case VALUE_START:
            case VALUE_PART:
            case VALUE_END:
            case CONTROL:
            case IGNORABLE:
            case EOF:
            case BLOCK_START:
            case BLOCK_END:
            case SEGMENT_START:
            case SEGMENT_END:
            case LEXICAL_ERROR:
            case SEGMENT_ERROR:
                break;
            default:
                throw new IllegalArgumentException("Invalid term kind for "
                        + "this constructor: " + kind);

        }
        if ((role != null) != token.hasToken()) {
            throw new IllegalArgumentException("Role must be specified only "
                    + "for tokens that has lexer token associated. " + token);
        }
        this.kind = kind;
        this.role = role;
        this.token = token;
        this.structureId = context;
    }

    /**
     * An term token that that reports some event associated with grammar
     * context but does not wraps token.
     *
     * @param context a context of event
     * @param kind    a kind of error
     * @param pos     a position of event
     */
    public TermToken(TermContext context, Terms kind, TextPos pos) {
        super(pos, pos);
        // TODO merge with primary constructor
        switch (kind) {
            case GRAMMAR_IS_LOADED:
            case ATTRIBUTES_START:
            case ATTRIBUTES_END:
            case DOC_COMMENT_START:
            case DOC_COMMENT_END:
            case EXPRESSION_START:
            case EXPRESSION_END:
                break;
            default:
                throw new IllegalArgumentException("Invalid term kind for "
                        + "this constructor" + kind);
        }
        this.kind = kind;
        this.role = null;
        this.token = null;
        this.structureId = context;
    }

    /**
     * An term token that that reports some event that does not carry additional
     * information.
     *
     * @param kind a kind of error
     * @param pos  a position of event
     */
    public TermToken(Terms kind, TextPos pos) {
        super(pos, pos);
        // TODO merge with primary constructor
        switch (kind) {
            case MODIFIERS_START:
            case MODIFIERS_END:
                break;
            default:
                throw new IllegalArgumentException("Invalid term kind for "
                        + "this constructor" + kind);
        }
        this.kind = kind;
        this.role = null;
        this.token = null;
        this.structureId = null;
    }

    /**
     * An term token that wraps some phrase token.
     *
     * @param kind  a kind of error
     * @param role  role associated with token
     * @param token a phrase token
     */
    public TermToken(Terms kind, SyntaxRole role, PhraseToken token) {
        // TODO merge with primary constructor
        this(null, kind, role, token);
    }

    /**
     * A constructor for object start/end events
     *
     * @param kind a kind of term token
     * @param name an name of object
     * @param pos  an position
     */
    public TermToken(Terms kind, ObjectName name, TextPos pos) {
        super(pos, pos);
        // TODO merge with primary constructor
        switch (kind) {
            case OBJECT_START:
            case OBJECT_END:
                break;
            default:
                throw new IllegalArgumentException("Invalid term kind for "
                        + "this constructor" + kind);
        }
        this.kind = kind;
        this.structureId = name;
        this.role = null;
        this.token = null;
    }

    /**
     * A constructor for property start/end events
     *
     * @param kind a kind of term token
     * @param name an name of property
     * @param pos  an position
     */
    public TermToken(Terms kind, PropertyName name, TextPos pos) {
        // TODO merge with primary constructor
        super(pos, pos);
        switch (kind) {
            case PROPERTY_START:
            case PROPERTY_END:
            case LIST_PROPERTY_START:
            case LIST_PROPERTY_END:
                break;
            default:
                throw new IllegalArgumentException("Invalid term kind for "
                        + "this constructor" + kind);
        }
        this.kind = kind;
        this.structureId = name;
        this.role = null;
        this.token = null;
    }

    /**
     * @return a context of the term
     */
    public TermContext context() {
        switch (kind()) {
            case BLOCK_START:
            case BLOCK_END:
            case SEGMENT_START:
            case SEGMENT_END:
            case ATTRIBUTES_START:
            case ATTRIBUTES_END:
            case DOC_COMMENT_START:
            case DOC_COMMENT_END:
            case EXPRESSION_START:
            case EXPRESSION_END:
            case GRAMMAR_IS_LOADED:
                return (TermContext) structureId;
            default:
                throw new IllegalStateException(
                        "Term context is not supported for kind: " + kind);
        }
    }

    /**
     * @return kind of the term
     */
    public Terms kind() {
        return kind;
    }

    /**
     * @return role of the token
     */
    public SyntaxRole role() {
        return role;
    }

    /**
     * @return name of property
     */
    public PropertyName propertyName() {
        switch (kind()) {
            case LIST_PROPERTY_START:
            case LIST_PROPERTY_END:
            case PROPERTY_START:
            case PROPERTY_END:
                return (PropertyName) structureId;
            default:
                throw new IllegalStateException(
                        "Property name is not supported for kind: " + kind);
        }
    }

    /**
     * @return name of the object
     */
    public ObjectName objectName() {
        switch (kind()) {
            case OBJECT_START:
            case OBJECT_END:
                return (ObjectName) structureId;
            default:
                throw new IllegalStateException(
                        "Property name is not supported for kind: " + kind);
        }
    }

    /**
     * @return wrapped token
     */
    public PhraseToken token() {
        return token;
    }

    /**
     * @return true if the token has a phase token
     */
    public boolean hasPhraseToken() {
        return token != null;
    }

    /**
     * @return true if the token has a lexical token token
     */
    public boolean hasLexicalToken() {
        return hasPhraseToken() && token.hasToken();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Token[" + kind + " " + start() + "-" + end() + ", role=" + role
                + ", structureId=" + structureId + " ,token = " + token
                + ", error=" + errorInfo() + "]";
    }
}
