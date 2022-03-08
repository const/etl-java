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
package net.sf.etl.parsers.event.impl.bootstrap; // NOPMD

import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.PhraseTokens;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.event.impl.util.ListStack;
import net.sf.etl.parsers.event.tree.SimpleObjectFactory.Property;
import net.sf.etl.parsers.event.unstable.model.grammar.Attributes;
import net.sf.etl.parsers.event.unstable.model.grammar.BlockRef;
import net.sf.etl.parsers.event.unstable.model.grammar.ChoiceOp;
import net.sf.etl.parsers.event.unstable.model.grammar.CompositeSyntax;
import net.sf.etl.parsers.event.unstable.model.grammar.Context;
import net.sf.etl.parsers.event.unstable.model.grammar.ContextImport;
import net.sf.etl.parsers.event.unstable.model.grammar.ContextInclude;
import net.sf.etl.parsers.event.unstable.model.grammar.ContextOp;
import net.sf.etl.parsers.event.unstable.model.grammar.ContextRef;
import net.sf.etl.parsers.event.unstable.model.grammar.Def;
import net.sf.etl.parsers.event.unstable.model.grammar.DoclinesOp;
import net.sf.etl.parsers.event.unstable.model.grammar.DocumentationSyntax;
import net.sf.etl.parsers.event.unstable.model.grammar.EObject;
import net.sf.etl.parsers.event.unstable.model.grammar.Element;
import net.sf.etl.parsers.event.unstable.model.grammar.ExpressionRef;
import net.sf.etl.parsers.event.unstable.model.grammar.ExpressionStatement;
import net.sf.etl.parsers.event.unstable.model.grammar.FirstChoiceOp;
import net.sf.etl.parsers.event.unstable.model.grammar.FloatOp;
import net.sf.etl.parsers.event.unstable.model.grammar.Grammar;
import net.sf.etl.parsers.event.unstable.model.grammar.GrammarLiteObjectFactory;
import net.sf.etl.parsers.event.unstable.model.grammar.IdentifierOp;
import net.sf.etl.parsers.event.unstable.model.grammar.IntegerOp;
import net.sf.etl.parsers.event.unstable.model.grammar.KeywordStatement;
import net.sf.etl.parsers.event.unstable.model.grammar.Let;
import net.sf.etl.parsers.event.unstable.model.grammar.ListOp;
import net.sf.etl.parsers.event.unstable.model.grammar.Modifier;
import net.sf.etl.parsers.event.unstable.model.grammar.ModifierOp;
import net.sf.etl.parsers.event.unstable.model.grammar.ModifiersOp;
import net.sf.etl.parsers.event.unstable.model.grammar.Namespace;
import net.sf.etl.parsers.event.unstable.model.grammar.NumberOp;
import net.sf.etl.parsers.event.unstable.model.grammar.ObjectName;
import net.sf.etl.parsers.event.unstable.model.grammar.ObjectOp;
import net.sf.etl.parsers.event.unstable.model.grammar.OneOrMoreOp;
import net.sf.etl.parsers.event.unstable.model.grammar.OperandOp;
import net.sf.etl.parsers.event.unstable.model.grammar.OperatorDefinition;
import net.sf.etl.parsers.event.unstable.model.grammar.OptionalOp;
import net.sf.etl.parsers.event.unstable.model.grammar.RefOp;
import net.sf.etl.parsers.event.unstable.model.grammar.RepeatOp;
import net.sf.etl.parsers.event.unstable.model.grammar.Sequence;
import net.sf.etl.parsers.event.unstable.model.grammar.Statement;
import net.sf.etl.parsers.event.unstable.model.grammar.StringOp;
import net.sf.etl.parsers.event.unstable.model.grammar.SyntaxDefinition;
import net.sf.etl.parsers.event.unstable.model.grammar.TokenOp;
import net.sf.etl.parsers.event.unstable.model.grammar.TokenRefOp;
import net.sf.etl.parsers.event.unstable.model.grammar.Wrapper;
import net.sf.etl.parsers.event.unstable.model.grammar.ZeroOrMoreOp;
import net.sf.etl.parsers.streams.PhraseParserReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
 * <dt>try*
 * <dd>These methods examine stream and if head matches specified, they consume
 * and returns true. Otherwise they do nothing with stream and return false.
 * <dt>start*
 * <dd>These methods unconditionally start parsing what is specified. And if
 * stream content does not match expected tokens, the parser fails.
 * <dt>end*
 * <dd>These methods unconditionally start parsing what is specified. And if
 * stream content does not match expected tokens, the parser fails.
 * <dt>match*
 * <dd>These method match current lexer and if token matches specified, return
 * true of false.
 * </dl>
 *
 * @author const
 */
// NOTE POST 0.3 Implement better error checking and make this parser to reject
// non matching grammars.
public final class BootstrapETLParserLite { // NOPMD
    /**
     * a logger used by this class to log the problems.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapETLParserLite.class);

    /**
     * stack of properties.
     */
    private final ListStack<Property> propertyStack = new ListStack<Property>();
    /**
     * stack of objects.
     */
    private final ListStack<Element> objectStack = new ListStack<Element>();
    /**
     * stack of objects.
     */
    private final ListStack<TextPos> objectStartStack = new ListStack<TextPos>();
    /**
     * a phrase parser used by bootstrap parser.
     */
    private final PhraseParserReader parser;
    /**
     * The object factory.
     */
    private final GrammarLiteObjectFactory factory = new GrammarLiteObjectFactory();

    /**
     * Result of parsing.
     */
    private Grammar result;

    /**
     * A constructor from phrase parser.
     *
     * @param parser a parser to use
     */
    public BootstrapETLParserLite(final PhraseParserReader parser) {
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
                } else if (!tryDoctype() && !match(PhraseTokens.STATEMENT_END)) {
                    fail();
                }
                endSegment();
            }
            return null;
        } finally {
            final long end = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Bootstrap parser finished, worked for " + (end - start) + "ms.");
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
     * stop parsing segment.
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
     * Check if phrase token kind matches.
     *
     * @param tk a token kind to match
     * @return true if matched.
     */
    private boolean match(final PhraseTokens tk) {
        return parser.current().kind() == tk;
    }

    /**
     * Fail parsing with exception.
     */
    private void fail() {
        throw new IllegalStateException("Some problem at token: "
                + parser.current());
    }

    /**
     * Parse grammar.
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
            value(property(Grammar.class, "name"));
        } while (tryToken("."));
        if (match(Tokens.STRING)) {
            value(property(Grammar.class, "version"));
        }
        startBlock();
        startProperty(property(Grammar.class, "content"));
        while (trySegment()) {
            if (!tryNamespace() && !tryContext()) {
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
     * statement is not matched.
     */
    private boolean tryContext() {
        if (!match("context")) {
            return false;
        }
        startObject(new Context());
        advance();
        while (true) {
            if (match("default")) {
                modifier(property(Context.class, "defaultModifier"));
            } else if (match("abstract")) {
                modifier(property(Context.class, "abstractModifier"));
            } else {
                break;
            }
        }
        value(property(Context.class, "name"));

        startBlock();
        startProperty(property(Context.class, "content"));
        while (trySegment()) {
            if (!tryDef()
                    && !tryStatement()
                    && !tryDocumentation()
                    && !tryContextImport()
                    && !tryContextInclude()
                    && !tryOp()
                    && !tryAttributes()) {
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
     * statement is not matched.
     */
    private boolean tryOp() {
        if (!tryToken("op", OperatorDefinition.class)) {
            return false;
        }
        if (match("composite")) {
            modifier(property(OperatorDefinition.class, "compositeModifier"));
        }
        value(property(SyntaxDefinition.class, "name"));
        consume("(");
        value(property(OperatorDefinition.class, "associativity"));
        if (tryToken(",")) {
            value(property(OperatorDefinition.class, "precedence"));
        }
        if (tryToken(",")) {
            final Property textProperty = property(OperatorDefinition.class, "text");
            value(textProperty);
            while (tryToken("|")) {
                value(textProperty);
            }
        }
        consume(")");
        parseDefSyntax();
        endObject();
        return true;
    }

    /**
     * @return true if context "import" statement is matched and parsed, false
     * if statement is not matched.
     */
    private boolean tryContextImport() {
        if (!match("import")) {
            return false;
        }
        startObject(new ContextImport());
        advance();
        value(property(ContextImport.class, "localName"));
        consume("=");
        value(property(ContextRef.class, "contextName"));
        endObject();
        return true;
    }

    /**
     * @return true if context "import" statement is matched and parsed, false
     * if statement is not matched.
     */
    private boolean tryContextInclude() {
        if (!match("include")) {
            return false;
        }
        startObject(new ContextInclude());
        advance();
        value(property(ContextRef.class, "contextName"));
        if (tryToken("wrapper")) {
            startProperty(property(ContextInclude.class, "wrappers"));
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
     * statement is not matched.
     */
    private boolean tryStatement() {
        if (!match("statement")) {
            return false;
        }
        startObject(new Statement());
        advance();
        value(property(SyntaxDefinition.class, "name"));
        parseDefSyntax();
        endObject();
        return true;
    }

    /**
     * @return true if documentation statement is matched and parsed, false if
     * statement is not matched.
     */
    private boolean tryDocumentation() {
        if (!match("documentation")) {
            return false;
        }
        startObject(new DocumentationSyntax());
        advance();
        value(property(SyntaxDefinition.class, "name"));
        parseDefSyntax();
        endObject();
        return true;
    }

    /**
     * @return true if attributes statement is matched and parsed, false if
     * statement is not matched.
     */
    private boolean tryAttributes() {
        if (!match("attributes")) {
            return false;
        }
        startObject(new Attributes());
        advance();
        value(property(SyntaxDefinition.class, "name"));
        parseDefSyntax();
        endObject();
        return true;
    }

    /**
     * @return true if context statement is matched and parsed, false if
     * statement is not matched.
     */
    private boolean tryDef() {
        if (!match("def")) {
            return false;
        }
        startObject(new Def());
        advance();
        value(property(SyntaxDefinition.class, "name"));
        parseDefSyntax();
        endObject();
        return true;
    }

    /**
     *
     */
    private void parseDefSyntax() {
        startProperty(property(SyntaxDefinition.class, "syntax"));
        parseSyntaxBlock();
        endProperty();
    }

    /**
     * parse block of syntax.
     */
    private void parseSyntaxBlock() {
        startBlock();
        while (trySegment()) {
            if (!tryLet()) {
                parseSyntaxExpressionStatement();
            }
            endSegment();
        }
        endBlock();
    }

    /**
     * parse expression syntax statement.
     */
    private void parseSyntaxExpressionStatement() {
        startObject(new ExpressionStatement());
        startProperty(property(ExpressionStatement.class, "syntax"));
        syntax();
        endProperty();
        endObject();
    }

    /**
     * @return true if let statement is matched and parsed, false if statement
     * is not matched.
     */
    private boolean tryLet() {
        if (!match("@")) {
            return false;
        }
        startObject(new Let());
        advance();
        value(property(Let.class, "name"));
        value(property(Let.class, "operator"));
        startProperty(property(Let.class, "expression"));
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
     * parse choice level operators.
     */
    private void parseFirstChoiceLevel() {
        parseRepeatLevel();
        while (tryToken("/")) {
            wrapTopObject(new FirstChoiceOp(), property(FirstChoiceOp.class, "first")); // NOPMD
            startProperty(property(FirstChoiceOp.class, "second"));
            parseRepeatLevel();
            endProperty();
            endObject();
        }
    }

    /**
     * parse choice level operators.
     */
    private void parseChoiceLevel() {
        parseFirstChoiceLevel();
        while (tryToken("|")) {
            wrapTopObject(new ChoiceOp(), property(ChoiceOp.class, "options")); // NOPMD
            startProperty(property(ChoiceOp.class, "options"));
            parseFirstChoiceLevel();
            endProperty();
            endObject();
        }
    }

    /**
     * parse operators at repeat level.
     */
    private void parseRepeatLevel() {
        parsePrimaryLevel();
        while (true) {
            if (tryToken("?")) {
                wrapTopObject(new OptionalOp(), property(RepeatOp.class, "syntax")); // NOPMD
                endObject();
            } else if (tryToken("*")) {
                wrapTopObject(new ZeroOrMoreOp(), property(RepeatOp.class, // NOPMD
                        "syntax"));
                endObject();
            } else if (tryToken("+")) {
                wrapTopObject(new OneOrMoreOp(), // NOPMD
                        property(RepeatOp.class, "syntax"));
                endObject();
            } else {
                break;
            }
        }
    }

    /**
     * parse primary level.
     */
    private void parsePrimaryLevel() { // NOPMD
        if (tryToken("^", ObjectOp.class)) {
            startProperty(property(ObjectOp.class, "name"));
            parseObjectName();
            endProperty();
            startProperty(property(CompositeSyntax.class, "syntax"));
            parseSequence();
            endProperty();
            endObject();
        } else if (match("left") || match("right")) {
            startObject(new OperandOp());
            value(property(OperandOp.class, "position"));
            endObject();
        } else if (tryToken("identifier", IdentifierOp.class)) {
            parseTokenWrapper();
            endObject();
        } else if (tryToken("modifier", ModifierOp.class)) {
            value(property(ModifierOp.class, "value"));
            parseTokenWrapper();
            endObject();
        } else if (tryToken("modifiers", ModifiersOp.class)) {
            parseWrapper(property(ModifiersOp.class, "wrapper"));
            startProperty(property(ModifiersOp.class, "modifiers"));
            parseSyntaxBlock();
            endProperty();
            endObject();
        } else if (tryToken("token", TokenOp.class)) {
            if (tryToken("(")) {
                value(property(TokenOp.class, "value"));
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
            value(property(StringOp.class, "quote"));
            consume(")");
            parseTokenWrapper();
            endObject();
        } else if (tryToken("doclines", DoclinesOp.class)) {
            parseTokenWrapper();
            endObject();
        } else if (tryToken("ref", RefOp.class)) {
            if (tryToken("(")) {
                value(property(RefOp.class, "name"));
                consume(")");
            }
            endObject();
        } else if (tryToken("block", BlockRef.class)) {
            if (tryToken("(")) {
                value(property(ContextOp.class, "context"));
                consume(")");
            }
            endObject();
        } else if (tryToken("expression", ExpressionRef.class)) {
            if (tryToken("(")) {
                value(property(ContextOp.class, "context"));
                if (tryToken(",")) {
                    consume("precedence");
                    consume("=");
                    value(property(ExpressionRef.class, "precedence"));
                }
                consume(")");
            }
            endObject();
        } else if (tryToken("list", ListOp.class)) {
            if (!match(PhraseTokens.START_BLOCK)) {
                value(property(ListOp.class, "separator"));
            }
            startProperty(property(CompositeSyntax.class, "syntax"));
            parseSequence();
            endProperty();
            endObject();
        } else if (match(PhraseTokens.START_BLOCK) || match("%")) {
            startObject(new Sequence());
            startProperty(property(Sequence.class, "syntax"));
            while (true) {
                if (tryToken("%", KeywordStatement.class)) {
                    value(property(KeywordStatement.class, "text"));
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
     * parse number declaration.
     */
    private void parseNumber() {
        if (tryToken("(")) {
            consume("suffix");
            consume("=");
            value(property(NumberOp.class, "suffix"));
            consume(")");
        }
        parseTokenWrapper();
    }

    /**
     * parse token wrapper.
     */
    private void parseTokenWrapper() {
        parseWrapper(property(TokenRefOp.class, "wrapper"));
    }

    /**
     * parser wrapper part.
     *
     * @param property a property to assign the wrapper
     */
    private void parseWrapper(final Property property) {
        if (tryToken("wrapper")) {
            startProperty(property);
            parseWrapperObject();
            endProperty();
        }
    }

    /**
     * parse wrapper object.
     */
    private void parseWrapperObject() {
        startObject(new Wrapper());
        startProperty(property(Wrapper.class, "object"));
        parseObjectName();
        endProperty();
        consume(".");
        value(property(Wrapper.class, "property"));
        endObject();
    }

    /**
     * parse sequence object using next block.
     */
    private void parseSequence() {
        startObject(new Sequence());
        startProperty(property(Sequence.class, "syntax"));
        parseSyntaxBlock();
        endProperty();
        endObject();
    }

    /**
     * parse object name.
     */
    private void parseObjectName() {
        startObject(new ObjectName());
        value(property(ObjectName.class, "prefix"));
        consume(":");
        value(property(ObjectName.class, "name"));
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
    private void wrapTopObject(final Element object, final Property property) {
        try {
            final Element po = topObject();
            final Property pp = topProperty();
            final Element v;
            if (pp.isList()) {
                final List<?> l = (List<?>) pp.get(po);
                v = (Element) l.remove(l.size() - 1);
            } else {
                v = (Element) pp.get(po);
                pp.set(po, null);
            }
            if (property.isList()) {
                ((List<Object>) property.get(object)).add(v);
            } else {
                property.set(object, v);
            }
            object.setOwnerObject(v.getOwnerObject());
            object.setOwnerFeature(v.getOwnerFeature());
            v.setOwnerObject(object);
            v.setOwnerFeature(property.getName());
            objectStartStack.push(objectStartStack.peek());
            pushObject(object);
        } catch (Exception e) { // NOPMD
            throw new ParserException("Property cannot be updated", e);
        }
    }

    /**
     * Try parsing the specific token.
     *
     * @param c      object of this class will be started if token matches
     * @param string a string to match.
     * @return true if token was detected and consumed, false if token did not
     * match.
     */
    private boolean tryToken(final String string, final Class<?> c) {
        if (match(string)) {
            try {
                startObject((Element) c.newInstance());
            } catch (Exception e) { // NOPMD
                throw new ParserException("Bootstrap parser cannot "
                        + "create an instance: " + c.getCanonicalName(), e);
            }
            advance();
            return true;
        }
        return false;
    }

    /**
     * Try token.
     *
     * @param string a string to match.
     * @return true if token was detected and consumed, false if token did not
     * match.
     */
    private boolean tryToken(final String string) {
        if (match(string)) {
            advance();
            return true;
        }
        return false;
    }

    /**
     * Read modifier.
     *
     * @param modifierProperty a property where modifier should be put
     */
    private void modifier(final Property modifierProperty) {
        startProperty(modifierProperty);
        startObject(new Modifier());
        value(property(Modifier.class, "value"));
        endObject();
        endProperty();
    }

    /**
     * @return true if namespace statement is matched, false if statement is not
     * matched.
     */
    private boolean tryNamespace() {
        if (!match("namespace")) {
            return false;
        }
        startObject(new Namespace());
        advance();
        if (match("default")) {
            modifier(property(Namespace.class, "defaultModifier"));
        }
        value(property(Namespace.class, "prefix"));
        consume("=");
        value(property(Namespace.class, "uri"));
        endObject();
        return true;
    }

    /**
     * Consume token that matches string or fail parsing.
     *
     * @param string a string to match
     */
    private void consume(final String string) {
        if (match(string)) {
            advance();
        } else {
            fail();
        }
    }

    /**
     * End property scope.
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
    private void startProperty(final Property property) {
        propertyStack.push(property);
    }

    /**
     * end block.
     */
    private void endBlock() {
        consume(PhraseTokens.END_BLOCK);
    }

    /**
     * start parsing block unconditionally.
     */
    private void startBlock() {
        consume(PhraseTokens.START_BLOCK);
    }

    /**
     * consume phrase token of specified type and advance.
     *
     * @param token a token to consume
     */
    private void consume(final PhraseTokens token) {
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
        final Element object = objectStack.pop();
        final TextPos start = objectStartStack.pop();
        object.setLocation(new SourceLocation(start, parser.current().start(), parser.getSystemId()));
        return object;
    }

    /**
     * Consume value and put it to property.
     *
     * @param featureId a filed to put consumed value
     */
    @SuppressWarnings("unchecked")
    private void value(final Property featureId) {
        try {
            final Element top = topObject();
            if (featureId.isList()) {
                factory.addValueToFeature(top, featureId, (List<Object>) featureId.get(top), token());
            } else {
                factory.setValueToFeature(top, featureId, token());
            }
            advance();
        } catch (Exception e) { // NOPMD
            throw new ParserException("Property cannot be accessed: " + featureId, e);
        }
    }

    /**
     * @return the current token (expects it to exist)
     */
    private Token token() {
        final PhraseToken current = parser.current();
        if (!current.hasToken()) {
            fail();
        }
        return current.token();
    }

    /**
     * @return object at top of the stack
     */
    private Element topObject() {
        return objectStack.peek();
    }

    /**
     * @param object object to start
     */
    private void startObject(final Element object) {
        pushObject(object);
        objectStartStack.push(parser.current().start());
    }

    /**
     * Push object.
     *
     * @param object the object to push
     */
    @SuppressWarnings("unchecked")
    private void pushObject(final Element object) {
        final Property sf = topProperty();
        try {
            if (sf != null) {
                final Element top = topObject();
                object.setOwnerObject(top);
                object.setOwnerFeature(sf.getName());
                if (sf.isList()) {
                    ((List<EObject>) sf.get(top)).add(object);
                } else {
                    sf.set(top, object);
                }
            }
            objectStack.push(object);
        } catch (Exception e) { // NOPMD
            throw new ParserException("Property cannot be accessed: " + sf, e);
        }
    }

    /**
     * @return currently top property on the stack or null if stack is empty
     */
    private Property topProperty() {
        return propertyStack.size() > 0 ? propertyStack.peek() : null;
    }

    /**
     * Try to match text.
     *
     * @param text a text to match
     * @return true if text matches
     */
    private boolean match(final String text) {
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
     * skip ignorable and control tokens.
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
     * Tries to match token to specific kind.
     *
     * @param kind a kind to match
     * @return true if matched.
     */
    private boolean match(final Tokens kind) {
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
     * Get a Property from class.
     *
     * @param c    class to example
     * @param name a name to get
     * @return the Property
     */
    private Property property(final Class<?> c, final String name) {
        return factory.getPropertyMetaObject(null, c, name);
    }
}
