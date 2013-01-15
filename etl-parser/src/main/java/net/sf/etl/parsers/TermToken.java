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
     * The location of definition for the term token
     */
    private final SourceLocation definedAt;

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
        this(kind, role, structureId, token, start, end, null, errorInfo);
    }

    /**
     * An error token. It is used to report syntax and grammar errors.
     *
     * @param kind        the kind of error
     * @param role        the role of the token
     * @param structureId the structure identifier
     * @param token       the phrase token
     * @param start       the start of the token
     * @param end         the end of the token
     * @param definedAt   the grammar location there the token is defined
     * @param errorInfo   the error information error info covered by error
     */
    public TermToken(Terms kind, SyntaxRole role, Object structureId, PhraseToken token, TextPos start, TextPos end, SourceLocation definedAt, ErrorInfo errorInfo) {
        super(start, end, errorInfo);
        this.kind = kind;
        this.role = role;
        this.token = token;
        this.structureId = structureId;
        this.definedAt = definedAt;
        // TODO validation and helper methods
    }


    /**
     * @return a context of the term
     */
    public DefinitionContext statementContext() {
        switch (kind()) {
            case BLOCK_START:
            case BLOCK_END:
            case STATEMENT_START:
            case STATEMENT_END:
            case ATTRIBUTES_START:
            case ATTRIBUTES_END:
            case DOC_COMMENT_START:
            case DOC_COMMENT_END:
            case GRAMMAR_IS_LOADED:
                return (DefinitionContext) structureId;
            default:
                throw new IllegalStateException(
                        "Term context is not supported for kind: " + kind);
        }
    }

    /**
     * @return a context of the term
     */
    public ExpressionContext expressionContext() {
        switch (kind()) {
            case EXPRESSION_START:
            case EXPRESSION_END:
                return (ExpressionContext) structureId;
            default:
                throw new IllegalStateException(
                        "Term context is not supported for kind: " + kind);
        }
    }

    /**
     * @return the grammar where this token is defined. The value could be null, if there is no definition.
     */
    public SourceLocation definedAt() {
        return definedAt;
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
     * @return true if there are errors associated on any level of the token
     */
    public boolean hasAnyErrors() {
        return hasErrors() || hasPhraseToken() && token().hasErrors() || hasLexicalToken() && token().token().hasErrors();
    }

    @Override
    public String toString() {
        return "Token[" + kind + " " + start() + "-" + end() + ", role=" + role
                + ", structureId=" + structureId + " ,token = " + token
                + ", error=" + errorInfo() + "]";
    }
}
