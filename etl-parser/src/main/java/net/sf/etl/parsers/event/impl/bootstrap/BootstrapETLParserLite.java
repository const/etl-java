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
package net.sf.etl.parsers.event.impl.bootstrap;

import net.sf.etl.parsers.PhraseTokens;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.event.unstable.model.grammar.*;
import net.sf.etl.parsers.literals.LiteralUtils;
import net.sf.etl.parsers.streams.PhraseParserReader;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <p>
 * This parser reads ETL grammar expressed in ETL. This is the only grammar that
 * can be reliably read by this parser. Differently from standard parsers it
 * breaks on the first error of failed assumption about ETL grammar because it
 * assumes that ETL grammar itself is correct. All other grammars will be read
 * by compiled ETL grammar for ETL.
 * </p>
 * <p>
 * Basically parser reads some correct grammars correctly. If you are lucky the
 * parser would parse your grammar correctly. If you are unlucky, it will parse
 * your grammar and will not notice bugs in it so compilation process will fail
 * in weird way later.
 * </p>
 * <p>
 * Note that the parser is not optimized and is implemented in simplest possible
 * way that still works. It does not checks correctness of grammar thoroughly so
 * giving invalid grammar to it might produce a mess at later phases of
 * pipeline.
 * </p>
 * <p>
 * There are the following method types in the source:
 * </p>
 * <dl>
 * <dt>try*</dt>
 * <dd>These methods examine stream and if head matches specified, they consume
 * and returns true. Otherwise they do nothing with stream and return false.</dd>
 * <dt>start*</dt>
 * <dd>These methods unconditionally start parsing what is specified. And if
 * stream content does not match expected tokens, the parser fails.</dd>
 * <dt>end*</dt>
 * <dd>These methods unconditionally start parsing what is specified. And if
 * stream content does not match expected tokens, the parser fails.</dd>
 * <dt>match*</dt>
 * <dd>These method match current lexer and if token matches specified, return
 * true of false.</dd>
 * <dt></dt>
 * <dd></dd>
 * </dl>
 *
 * @author const
 */
// NOTE POST 0.3 Implement better error checking and make this parser to reject
// non matching grammars.
public class BootstrapETLParserLite {
    /**
     * a logger used by this class to log the problems
     */
    private static final Logger log = Logger.getLogger(BootstrapETLParserLite.class.getName());

    /**
     * stack of properties
     */
    final private Stack<Field> propertyStack = new Stack<Field>();

    /**
     * stack of objects
     */
    final private Stack<EObject> objectStack = new Stack<EObject>();

    /**
     * a phrase parser used by bootstrap parser
     */
    private final PhraseParserReader parser;

    /**
     * Result of parsing
     */
    private Grammar result;

    /**
     * A constructor from phrase parser
     *
     * @param parser a parser to use
     */
    public BootstrapETLParserLite(PhraseParserReader parser) {
        this.parser = parser;
        parser.advance();
        skipIgnorable();
    }

    /**
     * Parse grammar.
     *
     * @return the first parsed grammar encountered in the file
     */
    public Grammar parse() {
        final long start = System.currentTimeMillis();
        try {
            while (trySegment()) {
                if (tryGrammar()) {
                    return result;
                } else if (tryDoctype()) {
                    // do nothing
                } else if (match(PhraseTokens.STATEMENT_END)) {
                    // do nothing it is a blank statement
                } else {
                    fail();
                }
                endSegment();
            }
            return null;
        } finally {
            final long end = System.currentTimeMillis();
            if (log.isLoggable(Level.FINE)) {
                log.fine("Bootstrap parser finished, worked for "
                        + (end - start) + "ms.");
            }
        }
    }

    /**
     * @return true if doctype has been parsed.
     */
    private boolean tryDoctype() {
        if (!match("doctype")) {
            return false;
        }
        while (!match(PhraseTokens.STATEMENT_END)) {
            advance();
        }
        return true;
    }

    /**
     * stop parsing segment
     */
    private void endSegment() {
        consume(PhraseTokens.STATEMENT_END);
    }

    /**
     * @return true if segment start matched and consumed.
     */
    private boolean trySegment() {
        return match(PhraseTokens.SIGNIFICANT) || match(PhraseTokens.START_BLOCK);
    }

    /**
     * @param tk a token kind to match
     * @return true if matched.
     */
    private boolean match(PhraseTokens tk) {
        return parser.current().kind() == tk;
    }

    /**
     * Fail parsing with exception
     */
    private void fail() {
        throw new IllegalStateException("Some problem at token: "
                + parser.current());
    }

    /**
     * Parse grammar
     *
     * @return true if it were indeed an grammar.
     */
    private boolean tryGrammar() {
        if (!match("grammar")) {
            return false;
        }
        startObject(new Grammar());
        advance();
        do {
            value(field(Grammar.class, "name"));
        } while (tryToken("."));
        if (match(Tokens.STRING)) {
            value(field(Grammar.class, "version"));
        }
        startBlock();
        startProperty(field(Grammar.class, "content"));
        while (trySegment()) {
            if (tryNamespace()) {
                // do nothing
            } else if (tryContext()) {
                // do nothing
            } else {
                // note that other constructs are not supported
                // because they are not needed by ETL grammar
                fail();
            }
            endSegment();
        }
        endProperty();
        endBlock();
        result = (Grammar) endObject();

        return true;
    }

    /**
     * @return true if context statement is matched and parsed, false if
     *         statement is not matched.
     */
    private boolean tryContext() {
        if (!match("context")) {
            return false;
        }
        startObject(new Context());
        advance();
        while (true) {
            if (match("default")) {
                modifier(field(Context.class, "defaultModifier"));
            } else if (match("abstract")) {
                modifier(field(Context.class, "abstractModifier"));
            } else {
                break;
            }
        }
        value(field(Context.class, "name"));

        startBlock();
        startProperty(field(Context.class, "content"));
        while (trySegment()) {
            if (tryDef()) {
                // do nothing
            } else if (tryStatement()) {
                // do nothing
            } else if (tryDocumentation()) {
                // do nothing
            } else if (tryContextImport()) {
                // do nothing
            } else if (tryContextInclude()) {
                // do nothing
            } else if (tryOp()) {
                // do nothing
            } else if (tryAttributes()) {
                // do nothing
            } else {
                fail();
            }
            endSegment();
        }
        endProperty();
        endBlock();
        endObject();
        return true;
    }

    /**
     * @return true if simple operator statement is matched and parsed, false if
     *         statement is not matched.
     */
    private boolean tryOp() {
        if (!tryToken("op", OperatorDefinition.class)) {
            return false;
        }
        if (match("composite")) {
            modifier(field(OperatorDefinition.class, "isComposite"));
        }
        value(field(SyntaxDefinition.class, "name"));
        consume("(");
        value(field(OperatorDefinition.class, "associativity"));
        if (tryToken(",")) {
            value(field(OperatorDefinition.class, "precedence"));
        }
        if (tryToken(",")) {
            value(field(OperatorDefinition.class, "text"));
        }
        consume(")");
        parseDefSyntax();
        endObject();
        return true;
    }

    /**
     * @return true if context "import" statement is matched and parsed, false
     *         if statement is not matched.
     */
    private boolean tryContextImport() {
        if (!match("import")) {
            return false;
        }
        startObject(new ContextImport());
        advance();
        value(field(ContextImport.class, "localName"));
        consume("=");
        value(field(ContextRef.class, "contextName"));
        endObject();
        return true;
    }

    /**
     * @return true if context "import" statement is matched and parsed, false
     *         if statement is not matched.
     */
    private boolean tryContextInclude() {
        if (!match("include")) {
            return false;
        }
        startObject(new ContextInclude());
        advance();
        value(field(ContextRef.class, "contextName"));
        if (tryToken("wrapper")) {
            startProperty(field(ContextInclude.class, "wrappers"));
            parseWrapperObject();
            while (tryToken("/")) {
                parseWrapperObject();
            }
            endProperty();
        }
        endObject();
        return true;
    }

    /**
     * @return true if "statement" statement is matched and parsed, false if
     *         statement is not matched.
     */
    private boolean tryStatement() {
        if (!match("statement")) {
            return false;
        }
        startObject(new Statement());
        advance();
        value(field(SyntaxDefinition.class, "name"));
        parseDefSyntax();
        endObject();
        return true;
    }

    /**
     * @return true if documentation statement is matched and parsed, false if
     *         statement is not matched.
     */
    private boolean tryDocumentation() {
        if (!match("documentation")) {
            return false;
        }
        startObject(new DocumentationSyntax());
        advance();
        value(field(SyntaxDefinition.class, "name"));
        parseDefSyntax();
        endObject();
        return true;
    }

    /**
     * @return true if attributes statement is matched and parsed, false if
     *         statement is not matched.
     */
    private boolean tryAttributes() {
        if (!match("attributes")) {
            return false;
        }
        startObject(new Attributes());
        advance();
        value(field(SyntaxDefinition.class, "name"));
        parseDefSyntax();
        endObject();
        return true;
    }

    /**
     * @return true if context statement is matched and parsed, false if
     *         statement is not matched.
     */
    private boolean tryDef() {
        if (!match("def")) {
            return false;
        }
        startObject(new Def());
        advance();
        value(field(SyntaxDefinition.class, "name"));
        parseDefSyntax();
        endObject();
        return true;
    }

    /**
     *
     */
    private void parseDefSyntax() {
        startProperty(field(SyntaxDefinition.class, "syntax"));
        parseSyntaxBlock();
        endProperty();
    }

    /**
     * parse block of syntax.
     */
    private void parseSyntaxBlock() {
        startBlock();
        while (trySegment()) {
            if (tryLet()) {
                // do nothing
            } else {
                parseSyntaxExpressionStatement();
            }
            endSegment();
        }
        endBlock();
    }

    /**
     * parse expression syntax statement
     */
    private void parseSyntaxExpressionStatement() {
        startObject(new ExpressionStatement());
        startProperty(field(ExpressionStatement.class, "syntax"));
        syntax();
        endProperty();
        endObject();
    }

    /**
     * @return true if let statement is matched and parsed, false if statement
     *         is not matched.
     */
    private boolean tryLet() {
        if (!match("@")) {
            return false;
        }
        startObject(new Let());
        advance();
        value(field(Let.class, "name"));
        value(field(Let.class, "operator"));
        startProperty(field(Let.class, "expression"));
        syntax();
        endProperty();
        endObject();
        return true;
    }

    /**
     * Parse syntax. Note that it parses all syntax constructs independently of
     * context.
     */
    private void syntax() {
        parseChoiceLevel();
    }


    /**
     * parse choice level operators
     */
    private void parseFirstChoiceLevel() {
        parseRepeatLevel();
        while (tryToken("/")) {
            wrapTopObject(new FirstChoiceOp(), field(FirstChoiceOp.class, "first"));
            startProperty(field(FirstChoiceOp.class, "second"));
            parseRepeatLevel();
            endProperty();
            endObject();
        }
    }

    /**
     * parse choice level operators
     */
    private void parseChoiceLevel() {
        parseFirstChoiceLevel();
        while (tryToken("|")) {
            wrapTopObject(new ChoiceOp(), field(ChoiceOp.class, "options"));
            startProperty(field(ChoiceOp.class, "options"));
            parseFirstChoiceLevel();
            endProperty();
            endObject();
        }
    }

    /**
     * parse operators at repeat level
     */
    private void parseRepeatLevel() {
        parsePrimaryLevel();
        while (true) {
            if (tryToken("?")) {
                wrapTopObject(new OptionalOp(), field(RepeatOp.class, "syntax"));
                endObject();
            } else if (tryToken("*")) {
                wrapTopObject(new ZeroOrMoreOp(), field(RepeatOp.class,
                        "syntax"));
                endObject();
            } else if (tryToken("+")) {
                wrapTopObject(new OneOrMoreOp(),
                        field(RepeatOp.class, "syntax"));
                endObject();
            } else {
                break;
            }
        }
    }

    /**
     * parse primary level
     */
    private void parsePrimaryLevel() {
        if (tryToken("^", ObjectOp.class)) {
            startProperty(field(ObjectOp.class, "name"));
            parseObjectName();
            endProperty();
            startProperty(field(CompositeSyntax.class, "syntax"));
            parseSequence();
            endProperty();
            endObject();
        } else if (match("left") || match("right")) {
            startObject(new OperandOp());
            value(field(OperandOp.class, "position"));
            endObject();
        } else if (tryToken("identifier", IdentifierOp.class)) {
            parseTokenWrapper();
            endObject();
        } else if (tryToken("modifier", ModifierOp.class)) {
            value(field(ModifierOp.class, "value"));
            parseTokenWrapper();
            endObject();
        } else if (tryToken("modifiers", ModifiersOp.class)) {
            parseWrapper(field(ModifiersOp.class, "wrapper"));
            startProperty(field(ModifiersOp.class, "modifiers"));
            parseSyntaxBlock();
            endProperty();
            endObject();
        } else if (tryToken("token", TokenOp.class)) {
            if (tryToken("(")) {
                value(field(TokenOp.class, "value"));
                consume(")");
            }
            parseTokenWrapper();
            endObject();
        } else if (tryToken("float", FloatOp.class)) {
            parseNumber();
            endObject();
        } else if (tryToken("integer", IntegerOp.class)) {
            parseNumber();
            endObject();
        } else if (tryToken("string", StringOp.class)) {
            consume("(");
            consume("quote");
            consume("=");
            value(field(StringOp.class, "quote"));
            consume(")");
            parseTokenWrapper();
            endObject();
        } else if (tryToken("doclines", DoclinesOp.class)) {
            parseTokenWrapper();
            endObject();
        } else if (tryToken("ref", RefOp.class)) {
            if (tryToken("(")) {
                value(field(RefOp.class, "name"));
                consume(")");
            }
            endObject();
        } else if (tryToken("block", BlockRef.class)) {
            if (tryToken("(")) {
                value(field(ContextOp.class, "context"));
                consume(")");
            }
            endObject();
        } else if (tryToken("expression", ExpressionRef.class)) {
            if (tryToken("(")) {
                value(field(ContextOp.class, "context"));
                if (tryToken(",")) {
                    consume("precedence");
                    consume("=");
                    value(field(ExpressionRef.class, "precedence"));
                }
                consume(")");
            }
            endObject();
        } else if (tryToken("list", ListOp.class)) {
            if (!match(PhraseTokens.START_BLOCK)) {
                value(field(ListOp.class, "separator"));
            }
            startProperty(field(CompositeSyntax.class, "syntax"));
            parseSequence();
            endProperty();
            endObject();
        } else if (match(PhraseTokens.START_BLOCK) || match("%")) {
            startObject(new Sequence());
            startProperty(field(Sequence.class, "syntax"));
            while (true) {
                if (tryToken("%", KeywordStatement.class)) {
                    value(field(KeywordStatement.class, "text"));
                    endObject();
                } else if (match(PhraseTokens.START_BLOCK)) {
                    parseSyntaxBlock();
                } else {
                    break;
                }
            }
            endProperty();
            endObject();
        } else {
            fail();
        }
    }

    /**
     * parse number declaration
     */
    private void parseNumber() {
        if (tryToken("(")) {
            consume("suffix");
            consume("=");
            value(field(NumberOp.class, "suffix"));
            consume(")");
        }
        parseTokenWrapper();
    }

    /**
     * parse token wrapper
     */
    private void parseTokenWrapper() {
        parseWrapper(field(TokenRefOp.class, "wrapper"));
    }

    /**
     * parser wrapper part
     *
     * @param property a property to assign the wrapper
     */
    private void parseWrapper(Field property) {
        if (tryToken("wrapper")) {
            startProperty(property);
            parseWrapperObject();
            endProperty();
        }
    }

    /**
     * parse wrapper object
     */
    private void parseWrapperObject() {
        startObject(new Wrapper());
        startProperty(field(Wrapper.class, "object"));
        parseObjectName();
        endProperty();
        consume(".");
        value(field(Wrapper.class, "property"));
        endObject();
    }

    /**
     * parse sequence object using next block
     */
    private void parseSequence() {
        startObject(new Sequence());
        startProperty(field(Sequence.class, "syntax"));
        parseSyntaxBlock();
        endProperty();
        endObject();
    }

    /**
     * parse object name.
     */
    private void parseObjectName() {
        startObject(new ObjectName());
        value(field(ObjectName.class, "prefix"));
        consume(":");
        value(field(ObjectName.class, "name"));
        endObject();
    }

    /**
     * This object start current object in a way that wraps current top object
     * on the stack into new object. The method used by expression parsers.
     *
     * @param object   new object
     * @param property a property of new object to which current top will be put.
     */
    @SuppressWarnings("unchecked")
    private void wrapTopObject(Element object, Field property) {
        try {
            final Element po = topObject();
            final Field pp = topProperty();
            Element v;
            if (pp.getType() == ArrayList.class) {
                final java.util.ArrayList<?> l = (java.util.ArrayList<?>) pp
                        .get(po);
                v = (Element) l.remove(l.size() - 1);
            } else {
                v = (Element) pp.get(po);
                pp.set(po, null);
            }
            if (property.getType() == ArrayList.class) {
                ((java.util.ArrayList<Object>) property.get(object)).add(v);
            } else {
                property.set(object, v);
            }
            object.ownerObject = v.ownerObject;
            object.ownerFeature = v.ownerFeature;
            v.ownerObject = object;
            v.ownerFeature = property;
            object.start = v.start;
            pushObject(object);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Field cannot be updated", e);
        }
    }

    /**
     * @param c      object of this class will be started if token matches
     * @param string a string to match.
     * @return true if token was detected and consumed, false if token did not
     *         match.
     */
    private boolean tryToken(String string, Class<?> c) {
        if (match(string)) {
            try {
                startObject((Element) c.newInstance());
            } catch (Exception e) {
                throw new RuntimeException("Bootstrap parser cannot "
                        + "create an instance: " + c.getCanonicalName());
            }
            advance();
            return true;
        }
        return false;
    }

    /**
     * @param string a string to match.
     * @return true if token was detected and consumed, false if token did not
     *         match.
     */
    private boolean tryToken(String string) {
        if (match(string)) {
            advance();
            return true;
        }
        return false;
    }

    /**
     * Read modifier
     *
     * @param modifierProperty a property where modifier should be put
     */
    private void modifier(Field modifierProperty) {
        startProperty(modifierProperty);
        startObject(new Modifier());
        value(field(Modifier.class, "value"));
        endObject();
        endProperty();
    }

    /**
     * @return true if namespace statement is matched, false if statement is not
     *         matched.
     */
    private boolean tryNamespace() {
        if (!match("namespace")) {
            return false;
        }
        startObject(new Namespace());
        advance();
        if (match("default")) {
            modifier(field(Namespace.class, "defaultModifier"));
        }
        value(field(Namespace.class, "prefix"));
        consume("=");
        value(field(Namespace.class, "uri"));
        endObject();
        return true;
    }

    /**
     * Consume token that matches string or fail parsing
     *
     * @param string a string to match
     */
    private void consume(String string) {
        if (match(string)) {
            advance();
        } else {
            fail();
        }
    }

    /**
     * End property scope
     */
    private void endProperty() {
        propertyStack.pop();
    }

    /**
     * Start property. All objects created before property ends will be
     * considered belonging to this property
     *
     * @param property a property to start
     */
    private void startProperty(Field property) {
        propertyStack.push(property);
    }

    /**
     * end block
     */
    private void endBlock() {
        consume(PhraseTokens.END_BLOCK);
    }

    /**
     * start parsing block unconditionally
     */
    private void startBlock() {
        consume(PhraseTokens.START_BLOCK);
    }

    /**
     * consume phrase token of specified type and advance
     *
     * @param token a token to consume
     */
    private void consume(PhraseTokens token) {
        if (match(token)) {
            advance();
        } else {
            fail();
        }
    }

    /**
     * @return pop object
     */
    private Element endObject() {
        final Element object = (Element) objectStack.pop();
        object.end = parser.current().start();
        object.systemId = parser.getSystemId();
        return object;
    }

    /**
     * Consume value and put it to property
     *
     * @param featureId a filed to put consumed value
     */
    @SuppressWarnings("unchecked")
    private void value(Field featureId) {
        try {
            final Class type = featureId.getType();
            final EObject top = topObject();
            if (type.isEnum()) {
                final String text = text();
                Enum value = null;
                for (final Object o : type.getEnumConstants()) {
                    final Enum e = (Enum) o;
                    // note that line below works only for single-word enums
                    if (text.equalsIgnoreCase(e.name())) {
                        value = e;
                        break;
                    }
                }
                if (value == null) {
                    throw new RuntimeException("No constant with name "
                            + text() + " in enum " + type.getCanonicalName());
                }
                featureId.set(top, value);
            } else if (type == String.class) {
                featureId.set(top, text());
            } else if (type == Token.class) {
                featureId.set(top, token());
            } else if (type == Integer.class || type == int.class) {
                featureId.set(top, LiteralUtils.parseInt(text()));
            } else if (type == ArrayList.class) {
                Type gType = featureId.getGenericType();
                if (gType instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) gType;
                    if (pt.getActualTypeArguments()[0] == Token.class) {
                        ((ArrayList<Token>) featureId.get(top)).add(token());
                    } else if (pt.getActualTypeArguments()[0] == String.class) {
                        ((ArrayList<String>) featureId.get(top)).add(text());
                    } else {
                        throw new RuntimeException("Unsupported argument type:"
                                + featureId);
                    }
                } else {
                    throw new RuntimeException("Unsupported argument type:"
                            + featureId);
                }
            } else {
                throw new RuntimeException("Unsupported field type:"
                        + featureId);
            }
            advance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Field cannot be accessed: " + featureId, e);
        }
    }

    /**
     * @return text of current token
     */
    private String text() {
        return token().text();
    }

    private Token token() {
        if (!parser.current().hasToken()) {
            fail();
        }
        return parser.current().token();
    }

    /**
     * @return object at top of the stack
     */
    private Element topObject() {
        return (Element) objectStack.peek();
    }

    /**
     * @param object object to start
     */
    private void startObject(Element object) {
        pushObject(object);
        object.start = parser.current().start();
    }

    /**
     * Push object
     *
     * @param object the object to push
     */
    @SuppressWarnings("unchecked")
    private void pushObject(Element object) {
        final Field sf = topProperty();
        try {
            if (sf != null) {
                final Element top = topObject();
                object.ownerObject = top;
                object.ownerFeature = sf;
                Class<?> type = sf.getType();
                if (type == ArrayList.class || type == LinkedList.class) {
                    ((List<EObject>) sf.get(top)).add(object);
                } else {
                    sf.set(top, object);
                }
            }
            objectStack.push(object);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Field cannot be accessed: " + sf, e);
        }
    }

    /**
     * @return currently top property on the stack or null if stack is empty
     */
    private Field topProperty() {
        return propertyStack.size() > 0 ? propertyStack.peek() : null;
    }

    /**
     * Try to match text
     *
     * @param text a text to match
     * @return true if text matches
     */
    private boolean match(String text) {
        return parser.current().hasToken()
                && text.equals(parser.current().token().text());
    }

    /**
     * Move to new phrase token in the stream.
     *
     * @return true if parser actually moved.
     */
    private boolean advance() {
        if (phraseKind() == PhraseTokens.EOF) {
            return false;
        }
        parser.advance();
        skipIgnorable();
        return true;
    }

    /**
     * skip ignorable and control tokens
     */
    private void skipIgnorable() {
        ignoreLoop:
        while (true) {
            switch (phraseKind()) {
                case IGNORABLE:
                case CONTROL:
                case SOFT_STATEMENT_END:
                    break;
                case SIGNIFICANT:
                    if (match(Tokens.DOC_COMMENT)) {
                        break;
                    } else {
                        break ignoreLoop;
                    }
                default:
                    break ignoreLoop;
            }
            parser.advance();
        }
    }

    /**
     * Tries to match token to specific kind
     *
     * @param kind a kind to match
     * @return true if matched.
     */
    private boolean match(Tokens kind) {
        return parser.current().hasToken()
                && parser.current().token().kind() == kind;
    }

    /**
     * @return phrase kind for current token
     */
    private PhraseTokens phraseKind() {
        return parser.current().kind();
    }

    /**
     * a cache of fields. note that strings are interned in this class, so it
     * does not make sense to use plain hash map here.
     */
    HashMap<Class<?>, IdentityHashMap<String, Field>> fieldCache = new HashMap<Class<?>, IdentityHashMap<String, Field>>();

    /**
     * Get a field from class
     *
     * @param c    class to example
     * @param name a name to get
     * @return the field
     */
    private Field field(Class<?> c, String name) {
        try {
            IdentityHashMap<String, Field> classFields = fieldCache.get(c);
            if (classFields == null) {
                classFields = new IdentityHashMap<String, Field>();
                fieldCache.put(c, classFields);
            }
            Field rc = classFields.get(name);
            if (rc == null) {
                rc = c.getField(name);
                classFields.put(name, rc);
            }
            return rc;
        } catch (Exception e) {
            throw new RuntimeException("Unable to find field " + name
                    + " in class " + c.getCanonicalName(), e);
        }
    }

}
