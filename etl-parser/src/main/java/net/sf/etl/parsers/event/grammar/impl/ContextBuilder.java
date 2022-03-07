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
package net.sf.etl.parsers.event.grammar.impl; // NOPMD

import net.sf.etl.parsers.DefinitionContext;
import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.ExpressionContext;
import net.sf.etl.parsers.PropertyName;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.SyntaxRole;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.TokenKey;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.characters.QuoteClass;
import net.sf.etl.parsers.event.grammar.KeywordContext;
import net.sf.etl.parsers.event.grammar.TermParserStateFactory;
import net.sf.etl.parsers.event.grammar.impl.flattened.ChoiceCaseDefView;
import net.sf.etl.parsers.event.grammar.impl.flattened.ContextImportView;
import net.sf.etl.parsers.event.grammar.impl.flattened.ContextView;
import net.sf.etl.parsers.event.grammar.impl.flattened.DefView;
import net.sf.etl.parsers.event.grammar.impl.flattened.DefinitionView;
import net.sf.etl.parsers.event.grammar.impl.flattened.OpLevel;
import net.sf.etl.parsers.event.grammar.impl.flattened.StatementView;
import net.sf.etl.parsers.event.grammar.impl.flattened.WrapperLink;
import net.sf.etl.parsers.event.grammar.impl.nodes.FallbackObjectNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.FirstChoiceNode;
import net.sf.etl.parsers.event.grammar.impl.nodes.Node;
import net.sf.etl.parsers.event.impl.term.action.buildtime.ActionLinker;
import net.sf.etl.parsers.event.unstable.model.grammar.BlankSyntaxStatement;
import net.sf.etl.parsers.event.unstable.model.grammar.BlockRef;
import net.sf.etl.parsers.event.unstable.model.grammar.ChoiceOp;
import net.sf.etl.parsers.event.unstable.model.grammar.DoclinesOp;
import net.sf.etl.parsers.event.unstable.model.grammar.Element;
import net.sf.etl.parsers.event.unstable.model.grammar.ExpressionRef;
import net.sf.etl.parsers.event.unstable.model.grammar.ExpressionStatement;
import net.sf.etl.parsers.event.unstable.model.grammar.FirstChoiceOp;
import net.sf.etl.parsers.event.unstable.model.grammar.FloatOp;
import net.sf.etl.parsers.event.unstable.model.grammar.GraphicsOp;
import net.sf.etl.parsers.event.unstable.model.grammar.IdentifierOp;
import net.sf.etl.parsers.event.unstable.model.grammar.IntegerOp;
import net.sf.etl.parsers.event.unstable.model.grammar.KeywordStatement;
import net.sf.etl.parsers.event.unstable.model.grammar.Let;
import net.sf.etl.parsers.event.unstable.model.grammar.ListOp;
import net.sf.etl.parsers.event.unstable.model.grammar.ModifierOp;
import net.sf.etl.parsers.event.unstable.model.grammar.ModifiersOp;
import net.sf.etl.parsers.event.unstable.model.grammar.NumberOp;
import net.sf.etl.parsers.event.unstable.model.grammar.ObjectOp;
import net.sf.etl.parsers.event.unstable.model.grammar.OneOrMoreOp;
import net.sf.etl.parsers.event.unstable.model.grammar.OperandOp;
import net.sf.etl.parsers.event.unstable.model.grammar.OperatorDefinition;
import net.sf.etl.parsers.event.unstable.model.grammar.OptionalOp;
import net.sf.etl.parsers.event.unstable.model.grammar.RefOp;
import net.sf.etl.parsers.event.unstable.model.grammar.Sequence;
import net.sf.etl.parsers.event.unstable.model.grammar.StringOp;
import net.sf.etl.parsers.event.unstable.model.grammar.Syntax;
import net.sf.etl.parsers.event.unstable.model.grammar.SyntaxDefinition;
import net.sf.etl.parsers.event.unstable.model.grammar.SyntaxStatement;
import net.sf.etl.parsers.event.unstable.model.grammar.TokenOp;
import net.sf.etl.parsers.event.unstable.model.grammar.Wrapper;
import net.sf.etl.parsers.event.unstable.model.grammar.ZeroOrMoreOp;
import net.sf.etl.parsers.literals.LiteralUtils;
import net.sf.etl.parsers.literals.NumberInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * <p>
 * A builder for context. This class is used to build all
 * {@link net.sf.etl.parsers.event.impl.term.action.ActionStateFactory} objects from flattened {@link ContextView}. The
 * class works in the context of {@link GrammarBuilder}.
 * </p>
 *
 * @author const
 */
public final class ContextBuilder { // NOPMD
    /**
     * The grammar builder for this context builder.
     */
    private final GrammarBuilder grammarBuilder;
    /**
     * The view that is being built.
     */
    private final ContextView contextView;
    /**
     * The builders for operator levels.
     */
    private final NavigableMap<Integer, OperatorLevelBuilder> operatorLevels =
            new TreeMap<Integer, OperatorLevelBuilder>();
    /**
     * The  context name.
     */
    private final DefinitionContext contextName;
    /**
     * The builder for statement sequence in this block.
     */
    private ActionBuilder actionBuilder;
    /**
     * The keyword context for the context, it is yielded by statement production.
     */
    private KeywordContext keywordContext;

    /**
     * The constructor.
     *
     * @param view    the view to compile
     * @param builder the parent builder to use
     */
    public ContextBuilder(final GrammarBuilder builder, final ContextView view) {
        this.grammarBuilder = builder;
        this.contextView = view;
        this.contextName = new DefinitionContext(grammarBuilder().getGrammarInfo(), contextView.name());
        if (view.isDefault()) {
            grammarBuilder.setDefaultContext(contextName);
        }
    }

    /**
     * Check of if token matches the specified text.
     *
     * @param text  the text to check
     * @param token the token
     * @return true if matches
     */
    private static boolean match(final String text, final Token token) {
        return text != null ? text.equals(text(token)) : token == null;
    }

    /**
     * Extract token from the text.
     *
     * @param token the token to check (might be null)
     * @return the token text or null if token is null
     */
    private static String text(final Token token) {
        return token == null ? null : token.text();
    }

    /**
     * The parser for the expression.
     *
     * @return the parser
     */
    public TermParserStateFactory parser() {
        return actionBuilder != null ? actionBuilder.targetFactory() : null;
    }

    /**
     * @return the expression levels
     */
    public NavigableSet<Integer> expressionLevels() {
        return operatorLevels.navigableKeySet();
    }

    /**
     * The expression parser.
     *
     * @param level the level for the parser
     * @return the expression parser
     */
    public TermParserStateFactory expressionParser(final Integer level) {
        final int actualLevel;
        if (level == null) {
            actualLevel = operatorLevels.lastKey();
        } else {
            actualLevel = operatorLevels.floorKey(level);
        }
        return operatorLevels.get(actualLevel).builder().targetFactory();
    }

    /**
     * Prepare context. This method creates instances of action builder.
     */
    public void prepare() {
        if (contextView.statements().size() > 0) {
            actionBuilder = new ActionBuilder(this);
        }
        for (OpLevel l = contextView.allExpressionsLevel(); l != null; l = l.getPreviousLevel()) {
            final OperatorLevelBuilder builder = new OperatorLevelBuilder(this, l); // NOPMD
            operatorLevels.put(l.getPrecedence(), builder);
        }
    }

    /**
     * @return the context view associated with this builder
     */
    public ContextView contextView() {
        return contextView;
    }

    /**
     * @return the name of context
     */
    public String name() {
        return contextView.name();
    }

    /**
     * Compile syntax to nodes.
     */
    public void buildNodes() {
        for (final OperatorLevelBuilder builder : operatorLevels.values()) {
            builder.buildNodes();
        }
        if (actionBuilder != null) {
            buildStatementNodes();
        }
    }

    /**
     * Build look ahead for the statements and expressions.
     */
    public void buildLookAhead() {
        final HashSet<ActionBuilder> visitedBuilders = new HashSet<ActionBuilder>();
        for (final OperatorLevelBuilder builder : operatorLevels.values()) {
            builder.builder().buildLookAhead(visitedBuilders);
        }
        if (actionBuilder != null) {
            actionBuilder.buildLookAhead(visitedBuilders);
        }
    }

    /**
     * Compile activations locally. On this phase no information is obtained
     * from other contexts except information about availability of the
     * activations which is created on the previous prepared stage.
     */
    public void buildStateMachines() {
        for (final OperatorLevelBuilder builder : operatorLevels.values()) {
            builder.builder().buildActions();
        }
        if (actionBuilder != null) {
            actionBuilder.buildActions();
        }
    }

    /**
     * Compile a sequence of context statements. This sequence might happen in
     * block or at top level of the source.
     */
    private void buildStatementNodes() { // NOPMD
        final ActionBuilder b = actionBuilder;
        final Element contextElement = contextView().reportingContext();
        b.startMarked(contextElement);
        b.startKeywords(contextElement);
        // Start fallback scope. This scope causes creation of object at mark.
        // This is used to prevent the case of dangling properties.
        final FallbackObjectNode fallbackNode = b.startFallbackScope(contextElement);
        b.startDisableSoftEnds(contextElement);
        // parse documentation comments in the context.
        if (contextView.documentation() != null) {
            // if documentation statement present in the context, comments
            // are parsed according to it.
            b.startDefinition(contextView.documentation());
            final SyntaxDefinition definition = contextView.documentation().definition();
            b.startChoice(definition);
            b.startDocComment(definition, contextView.documentation().definitionInfo());
            compileSyntax(new HashSet<>(), b, contextView.documentation().statements());
            b.endDocComment();
            b.startSequence(definition);
            b.endSequence();
            b.endChoice();
            b.endDefinition();
        } else {
            // otherwise comments are treated as ignorable
            b.startRepeat(contextElement);
            b.tokenText(contextElement, Terms.IGNORABLE, SyntaxRole.DOCUMENTATION, TokenKey.simple(Tokens.DOC_COMMENT));
            b.endRepeat();
        }
        // Compile attributes if they are available
        if (contextView.attributes() != null) {
            b.startDefinition(contextView.attributes());
            final SyntaxDefinition definition = contextView.attributes().definition();
            b.startRepeat(definition);
            b.startAttributes(definition, contextView().attributes().definitionInfo());
            compileSyntax(new HashSet<>(), b, contextView.attributes().statements());
            b.endAttributes();
            b.endRepeat();
            b.endDefinition();
        }
        b.endDisableSoftEnds();
        b.endFallbackScope();
        // Finally compile statement choice.
        // NOTE POST 0.2: add alternative that reports error better.
        b.startChoice(contextElement);
        boolean wasEmpty = false;
        StatementView fallback = null;
        for (final StatementView s : contextView.statements()) {
            final DefinitionView def = s.topObjectDefinition(contextView);
            final SyntaxDefinition definition = s.definition();
            if (def == null) {
                contextView.error(definition,
                        "grammar.ObjectDefinition.missingTopObject", name(),
                        grammarBuilder.grammarView().getSystemId());
            } else {
                final WrapperLink wrappers = s.wrappers(contextView);
                b.startStatement(definition, s);
                b.startSequence(definition);
                // handle the case when def/ref has been used
                final ObjectOp o = s.topObject(contextView);
                if (def != s) { // NOPMD
                    b.startDefinition(def);
                }
                b.startObjectAtMark(o, def.convertName(o.getName()), wrappers);
                b.commitMark(definition); // this statement tries to commit mark
                // now body of root object is compiled
                compileSyntax(new HashSet<DefinitionView>(), b, o.getSyntax()); // NOPMD
                // object ends here
                final Node object = b.endObject();
                if (object.matchesEmpty()) {
                    wasEmpty = true;
                    if (fallback == null) {
                        fallback = s;
                    }
                }
                if (def != s) { // NOPMD
                    b.endDefinition(); // def
                }
                b.endSequence();
                b.endStatement(); // s
            }
        }
        if (fallback == null) {
            // select any statement as fallback
            fallback = contextView.statements().iterator().next();
        }
        fallbackNode.setFallbackObject(fallback.topObjectName(contextView), fallback.wrappers(contextView));
        if (!wasEmpty) {
            // If there were no empty node, an empty variant of fallback
            // statement is compiled.
            // NOTE POST 0.2: that this code messes up with syntax error
            // reporting. If statement starts with unknown token, fallback
            // will be executed and error "end of segment expected" reported.
            // better reaction would have been list of statements.
            b.startObjectAtMark(contextElement, fallback.topObjectName(contextView), fallback.wrappers(contextView));
            b.commitMark(contextElement);
            b.error(contextElement, "syntax.UnexpectedToken.expectingStatementFromContext", contextName);
            b.endObject();
        }
        b.endChoice();
        b.endKeywords();
        b.endMarked();
    }

    // CHECKSTYLE:OFF

    /**
     * Compile syntax expression.
     *
     * @param visited the set of visited definition nodes. It is used to detect definition cycles.
     * @param b       the builder used for compilation.
     * @param body    the syntax to compile
     */
    public void compileSyntax(final Set<DefinitionView> visited, final ActionBuilder b, // NOPMD
                              final Syntax body) {
        if (body instanceof BlockRef) {
            final BlockRef s = (BlockRef) body;
            final DefinitionView v = b.topDefinition().originalDefinition();
            final ActionBuilder f = getStatementSequenceBuilder(v, s, text(s.getContext()));
            if (f != null) {
                b.block(s, f.context());
            } else {
                error(b, s, "grammar.Block.referringContextWithoutStatements", s.getContext());
            }
        } else if (body instanceof ExpressionRef) {
            final ExpressionRef s = (ExpressionRef) body;
            final DefinitionView v = b.topDefinition().originalDefinition();
            final Integer precedence = parseInteger(s.getPrecedence(), s.getLocation().systemId());
            final ActionBuilder f = getExpressionBuilder(v, s, text(s.getContext()), precedence);
            if (f != null) {
                // TODO register context with the statement, so compiled grammar will contain needed activations.
                final ExpressionContext c = new ExpressionContext(contextView.getDefinitionContext(),
                        f.context(), precedence);
                b.startExpression(s, c);
                b.startMarked(s);
                b.call(s, f);
                b.endMarked();
                b.endExpression();
            }
        } else if (body instanceof ModifiersOp) {
            final ModifiersOp s = (ModifiersOp) body;
            final Wrapper defaultWrapper = s.getWrapper();
            b.startModifiers(s);
            b.startRepeat(s);
            b.startChoice(s);
            // modifiers block contains only let instructions
            for (final Object sso : s.getModifiers()) {
                final SyntaxStatement ss = (SyntaxStatement) sso;
                if (ss instanceof Let) {
                    final Let let = (Let) ss;
                    final ModifierOp m = (ModifierOp) let.getExpression();
                    Wrapper w = m.getWrapper();
                    w = w == null ? defaultWrapper : w;
                    final boolean isList = match("+=", let.getOperator());
                    b.startProperty(let, new PropertyName(text(let.getName())), isList); // NOPMD
                    b.startWrapper(w);
                    b.tokenText(m, Terms.VALUE, SyntaxRole.MODIFIER, m.getValue());
                    b.endWrapper(w);
                    b.endProperty();
                } else if (!(ss instanceof BlankSyntaxStatement)) {
                    error(b, ss, "grammar.Modifiers.invalidStatement");
                }
            }
            b.endChoice();
            b.endRepeat();
            b.endModifiers();
        } else if (body instanceof ListOp) {
            final ListOp s = (ListOp) body;
            b.startSequence(s);
            compileSyntax(visited, b, s.getSyntax());
            b.startRepeat(s);
            final SourceLocation location = s.getLocation();
            final Token sep = s.getSeparator() == null
                    ? new Token(TokenKey.simple(Tokens.COMMA), ",", location.start(), location.end(), null)
                    : s.getSeparator();
            b.tokenText(s, Terms.STRUCTURAL, SyntaxRole.SEPARATOR, sep);
            compileSyntax(visited, b, s.getSyntax());
            b.endRepeat();
            b.endSequence();
        } else if (body instanceof OptionalOp) {
            final OptionalOp s = (OptionalOp) body;
            b.startChoice(s);
            compileSyntax(visited, b, s.getSyntax());
            b.startSequence(s);
            b.endSequence();
            b.endChoice();
        } else if (body instanceof ZeroOrMoreOp) {
            final ZeroOrMoreOp s = (ZeroOrMoreOp) body;
            b.startRepeat(s);
            compileSyntax(visited, b, s.getSyntax());
            b.endRepeat();
        } else if (body instanceof OneOrMoreOp) {
            final OneOrMoreOp s = (OneOrMoreOp) body;
            b.startSequence(s);
            compileSyntax(visited, b, s.getSyntax());
            b.startRepeat(s);
            compileSyntax(visited, b, s.getSyntax());
            b.endRepeat();
            b.endSequence();
        } else if (body instanceof FirstChoiceOp) {
            final FirstChoiceOp s = (FirstChoiceOp) body;
            b.startFirstChoice(s);
            compileSyntax(visited, b, s.getFirst());
            compileSyntax(visited, b, s.getSecond());
            final FirstChoiceNode n = b.endFirstChoice();
            final ListIterator<Node> i = n.nodes().listIterator(n.nodes().size() - 1);
            while (i.hasPrevious()) {
                final Node a = i.previous();
                if (a.matchesEmpty()) {
                    error(b, s.getFirst(), "grammar.Modifiers.firstChoiceEmptyFirst");
                }
            }
        } else if (body instanceof ChoiceOp) {
            final ChoiceOp s = (ChoiceOp) body;
            b.startChoice(s);
            for (final Syntax option : s.getOptions()) {
                compileSyntax(visited, b, option);
            }
            b.endChoice();
        } else if (body instanceof IdentifierOp) {
            final IdentifierOp s = (IdentifierOp) body;
            b.startWrapper(s.getWrapper());
            b.tokenText(s, Terms.VALUE, SyntaxRole.PRIMARY, TokenKey.simple(Tokens.IDENTIFIER));
            b.endWrapper(s.getWrapper());
        } else if (body instanceof GraphicsOp) {
            final GraphicsOp s = (GraphicsOp) body;
            b.startWrapper(s.getWrapper());
            b.tokenText(s, Terms.VALUE, SyntaxRole.PRIMARY, TokenKey.simple(Tokens.GRAPHICS));
            b.endWrapper(s.getWrapper());
        } else if (body instanceof TokenOp) {
            final TokenOp s = (TokenOp) body;
            b.startWrapper(s.getWrapper());
            if (s.getValue() != null) {
                b.tokenText(s, Terms.VALUE, SyntaxRole.KEYWORD, s.getValue());
            } else {
                b.anyToken(s, Terms.VALUE, SyntaxRole.PRIMARY_ANY);
            }
            b.endWrapper(s.getWrapper());
        } else if (body instanceof StringOp) {
            final StringOp s = (StringOp) body;
            String quote;
            try {
                quote = LiteralUtils.parseString(text(s.getQuote()));
            } catch (final Exception ex) { // NOPMD
                // do nothing, quote will stay as null
                quote = null;
            }
            if (quote == null || quote.isEmpty() || QuoteClass.classify(quote.codePointAt(0)) == null) {
                error(b, s, "grammar.String.invalidQuote", s.getQuote());
            } else {
                switch (s.getPrefix().size()) {
                    case 0:
                        compileString(b, s, quote, null);
                        break;
                    case 1:
                        compileString(b, s, quote, text(s.getPrefix().get(0)));
                        break;
                    default:
                        b.startChoice(s);
                        for (final Token prefix : s.getPrefix()) {
                            compileString(b, s, quote, text(prefix));
                        }
                        b.endChoice();
                        break;
                }
            }
        } else if (body instanceof IntegerOp) {
            compileNumber(b, (IntegerOp) body, Tokens.INTEGER, Tokens.INTEGER_WITH_SUFFIX);
        } else if (body instanceof FloatOp) {
            compileNumber(b, (FloatOp) body, Tokens.FLOAT, Tokens.FLOAT_WITH_SUFFIX);
        } else if (body instanceof ObjectOp) {
            final ObjectOp s = (ObjectOp) body;
            b.startObject(s, b.topDefinition().convertName(s.getName()));
            compileSyntax(visited, b, s.getSyntax());
            b.endObject();
        } else if (body instanceof RefOp) {
            final RefOp s = (RefOp) body;
            final DefView d = contextView.def(b.topDefinition(), s);
            if (d != null) {
                if (visited.contains(d)) {
                    error(b, body, "grammar.Ref.cyclicRef", s.getName(), contextView.name(),
                            contextView.grammar().getSystemId());
                } else {
                    visited.add(d);
                    b.startDefinition(d);
                    b.startSequence(s);
                    compileSyntax(visited, b, d.statements());
                    b.endSequence();
                    visited.remove(d);
                    b.endDefinition();
                }
            } else {
                // TODO beter error if not choice
                final List<ChoiceCaseDefView> choices = contextView.choices(b.topDefinition(), s);
                b.startChoice(s);
                for (final ChoiceCaseDefView choiceCaseDefView : choices) {
                    if (visited.contains(choiceCaseDefView)) {
                        error(b, body, "grammar.Ref.cyclicRef", s.getName(), contextView.name(),
                                contextView.grammar().getSystemId());
                    } else {
                        visited.add(choiceCaseDefView);
                        b.startDefinition(choiceCaseDefView);
                        b.startSequence(s);
                        compileSyntax(visited, b, choiceCaseDefView.statements());
                        b.endSequence();
                        visited.remove(choiceCaseDefView);
                        b.endDefinition();
                    }
                }
                b.endChoice();
            }
        } else if (body instanceof DoclinesOp) {
            final DoclinesOp s = (DoclinesOp) body;
            b.startWrapper(s.getWrapper());
            b.tokenText(s, Terms.VALUE, SyntaxRole.DOCUMENTATION, TokenKey.simple(Tokens.DOC_COMMENT));
            b.endWrapper(s.getWrapper());
            b.startRepeat(s);
            b.startWrapper(s.getWrapper());
            b.tokenText(s, Terms.VALUE, SyntaxRole.DOCUMENTATION, TokenKey.simple(Tokens.DOC_COMMENT));
            b.endWrapper(s.getWrapper());
            b.endRepeat();
        } else if (body instanceof OperandOp) {
            error(b, body, "grammar.Operand.misplacedOperand");
        } else if (body instanceof Sequence) {
            final Sequence s = (Sequence) body;
            b.startSequence(s);
            compileSyntax(visited, b, s.getSyntax());
            b.endSequence();
        } else {
            throw new IllegalStateException("[BUG]Unknown syntax element: " + body);
        }
    }
    // CHECKSTYLE:ON

    /**
     * Parse token as parseInteger and report errors.
     *
     * @param token    the token to parse
     * @param systemId the system id
     * @return the parsed value
     */
    private Integer parseInteger(final Token token, final String systemId) {
        final String t = text(token);
        if (t == null) {
            return null;
        }
        final NumberInfo numberInfo = LiteralUtils.parseNumber(t, token.start(), systemId);
        if (numberInfo.getKind() == Tokens.INTEGER || numberInfo.getKind() == Tokens.INTEGER_WITH_SUFFIX) {
            if (numberInfo.getErrors() != null) {
                error(numberInfo.getErrors());
                return null;
            } else {
                try {
                    return numberInfo.parseInt();
                } catch (Exception e) { // NOPMD
                    // unable to parse error
                    error(new ErrorInfo("grammar.Number.tooBig", Collections.<Object>singletonList(token.text()),
                            new SourceLocation(token.start(), token.end(), systemId), null));
                    return null;
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid token: " + token);
        }
    }

    /**
     * Compile string for the specified prefix.
     *
     * @param b      the builder to use
     * @param s      the operator to compile
     * @param quote  the quote for string
     * @param prefix the prefix instance
     */
    private void compileString(final ActionBuilder b, final StringOp s, final String quote, final String prefix) {
        compileString(b, s, quote, prefix, s.getMultiline() != null);
    }

    /**
     * Compile string token.
     *
     * @param b         the builder
     * @param s         the operator to compile
     * @param quote     the quote to use
     * @param prefix    the prefix to use
     * @param multiline the flag indicating if the string is multiline
     */
    private void compileString(final ActionBuilder b, final StringOp s, final String quote, final String prefix,
                               final boolean multiline) {
        b.startWrapper(s.getWrapper());
        Tokens kind;
        Terms termKind;
        if (multiline) {
            kind = prefix != null ? Tokens.PREFIXED_MULTILINE_STRING : Tokens.MULTILINE_STRING;
        } else {
            kind = prefix != null ? Tokens.PREFIXED_STRING : Tokens.STRING;
        }
        termKind = Terms.VALUE;
        final TokenKey key = TokenKey.quoted(kind, prefix, quote.codePointAt(0), quote.codePointAt(0));
        b.tokenText(s, termKind, SyntaxRole.PRIMARY, key);
        b.endWrapper(s.getWrapper());
    }

    /**
     * Compile number operation.
     *
     * @param b          a builder for state machine
     * @param s          an operator to compile
     * @param simpleKind a kind for simple token
     * @param suffixKind a kind for suffixed token
     */
    private void compileNumber(final ActionBuilder b, final NumberOp s,
                               final Tokens simpleKind, final Tokens suffixKind) {
        if (s.getSuffix().size() == 0) {
            b.startWrapper(s.getWrapper());
            b.tokenText(s, Terms.VALUE, SyntaxRole.PRIMARY, TokenKey.simple(simpleKind));
            b.endWrapper(s.getWrapper());
        } else if (s.getSuffix().size() == 1) {
            compileNumberWithSuffix(b, s, suffixKind, text(s.getSuffix().get(0)));
        } else {
            b.startChoice(s);
            for (final Token suffix : s.getSuffix()) {
                compileNumberWithSuffix(b, s, suffixKind, text(suffix));
            }
            b.endChoice();
        }
    }

    /**
     * Compile number with suffix.
     *
     * @param b          the builder
     * @param s          the syntax element
     * @param suffixKind the suffix kind
     * @param suffix     the suffix
     */
    private void compileNumberWithSuffix(final ActionBuilder b, final NumberOp s,
                                         final Tokens suffixKind, final String suffix) {
        final char ch = suffix.charAt(0);
        if (ch == 'E' || ch == 'e' || ch == '_') {
            error(b, s, "grammar.NumberOp.invalidSuffix", s.getSuffix());
        }
        b.startWrapper(s.getWrapper());
        b.tokenText(s, Terms.VALUE, SyntaxRole.PRIMARY, TokenKey.modified(suffixKind, suffix));
        b.endWrapper(s.getWrapper());
    }

    /**
     * Report error.
     *
     * @param b       the builder
     * @param e       the element that contains position of the statement
     * @param errorId identifier of the error
     */
    public void error(final ActionBuilder b, final Element e, final String errorId) {
        error(b.topDefinition(), e, errorId);
    }

    /**
     * Report error.
     *
     * @param error the error to report
     */
    public void error(final ErrorInfo error) {
        contextView().grammar().error(error);
    }

    /**
     * Report error.
     *
     * @param b         a builder
     * @param e         a element that contains position of the statement
     * @param errorId   identifier of the error
     * @param errorArgs error argument
     */
    public void error(final ActionBuilder b, final Element e, final String errorId, final Object... errorArgs) {
        final DefinitionView d = b.topDefinition();
        error(d, e, errorId, errorArgs);
    }

    /**
     * Report error.
     *
     * @param d        a context definition
     * @param e        a element that contains position of the statement
     * @param errorId  identifier of the error
     * @param errorArg error argument
     */
    public void error(final DefinitionView d, final Element e, final String errorId, final Object... errorArg) {
        final ContextView ctx = d.originalDefinition().definingContext();
        ctx.error(e, errorId, errorArg);
    }

    /**
     * Get expression builder for the specified context import.
     *
     * @param contextDefinition the definition that contains expression elements
     * @param e                 the element that makes a reference. It is used for error reporting.
     * @param context           the context import name that have to be resolved
     * @param precedence        the precedence of expression
     * @return an factory that correspond to expression.
     */
    private ActionBuilder getExpressionBuilder(final DefinitionView contextDefinition, final Element e,
                                               final String context, final Integer precedence) {
        final ContextBuilder referencedContext = getReferencedContext(
                contextDefinition, e, context);
        if (referencedContext == null) {
            // if context builder is not found, than it is dangling reference
            // that has been reported in method above
            return null;
        }
        // get allowed levels according to precedence
        final NavigableMap<Integer, OperatorLevelBuilder> allowedLevels;
        if (precedence == null) {
            allowedLevels = referencedContext.operatorLevels;
        } else {
            // check if precedence value is valid.
            if (precedence < 0) {
                error(contextDefinition, e, "grammar.Expression.invalidPrecedence", precedence);
                return null;
            }
            // note that precedence+1 is used because headMap does not include
            // the key used to create it.
            allowedLevels = referencedContext.operatorLevels.headMap(precedence, true);
        }
        if (referencedContext.operatorLevels.isEmpty()) {
            // this means that there are no expressions in the referenced
            // context or expression definition has serious errors (for example
            // no primary level). If it were errors, the compiler will not get
            // here.
            error(contextDefinition, e, "grammar.Expression.noValidExpression",
                    referencedContext.name(), contextView.name(), contextView.grammar().getSystemId());
            return null;
        }
        // now allowed levels are got and we just extract last production
        final Integer key = allowedLevels.lastKey();
        return referencedContext.operatorLevels.get(key).builder();
    }

    /**
     * Get builder for statement.
     *
     * @param contextDefinition the definition that contains block elements
     * @param e                 the block element
     * @param context           the context import name for context
     * @return an builder from this grammar
     */
    private ActionBuilder getStatementSequenceBuilder(final DefinitionView contextDefinition, final Element e,
                                                      final String context) {
        final ContextBuilder referencedContext = getReferencedContext(
                contextDefinition, e, context);
        if (referencedContext == null) {
            // if context builder is not found, than it is dangling reference
            // that has been reported in method above
            return null;
        }
        return referencedContext.actionBuilder;
    }

    /**
     * Get referenced context.
     *
     * @param contextDefinition the definition that contains element
     * @param e                 the an element that references other context
     * @param context           the context import name
     * @return a context builder for specified parameters
     */
    private ContextBuilder getReferencedContext(final DefinitionView contextDefinition, final Element e,
                                                final String context) {
        // if context is not specified, it is this context
        if (context == null) {
            return this;
        }
        final ContextView referencedContext;
        // otherwise locate context import that correspond to context
        final ContextImportView ci = contextView.contextImport(context);
        if (ci == null) {
            referencedContext = grammarBuilder().grammarView().context(context);
            if (referencedContext == null) {
                error(contextDefinition, e,
                        "grammar.ContextOp.invalidImportName", context,
                        contextView.name(), contextView.grammar().getSystemId());
                return null;
            }
        } else {
            assert ci.referencedContext() != null;
            referencedContext = ci.referencedContext();
        }
        final ContextBuilder rc = grammarBuilder.contextBuilder(referencedContext);
        assert rc != null;
        return rc;
    }

    /**
     * Compile syntax statement.
     *
     * @param visited   the set of visited "def"-s
     * @param b         the state machine builder used by compiler
     * @param statement the statement to compile
     */
    private void compileSyntaxStatement(final Set<DefinitionView> visited,
                                        final ActionBuilder b, final SyntaxStatement statement) {
        if (statement instanceof BlankSyntaxStatement) {
            // compile empty sequence. This sequence is to be optimized out.
            b.startSequence(statement);
            b.endSequence();
        } else if (statement instanceof Let) {
            final Let s = (Let) statement;
            if (s.getExpression() instanceof OperandOp) {
                if (!(s.getOwnerObject() instanceof ObjectOp)
                        || !(s.getOwnerObject().getOwnerObject() instanceof OperatorDefinition)) {
                    error(b, s.getExpression(), "grammar.Operand.misplacedOperand");
                    // Otherwise operand statement is placed correctly, ignore the
                    // statement
                }
            } else {
                final String op = text(s.getOperator());
                final boolean isList = "+=".equals(op);
                if (isList || "=".equals(op)) {
                    // simple property
                    b.startProperty(s, new PropertyName(text(s.getName())), isList);
                    compileSyntax(visited, b, s.getExpression());
                    b.endProperty();
                } else {
                    throw new IllegalStateException("[BUG]Unknown operator: " + op);
                }
            }
        } else if (statement instanceof KeywordStatement) {
            final KeywordStatement s = (KeywordStatement) statement;
            b.tokenText(s, Terms.STRUCTURAL, SyntaxRole.KEYWORD, s.getText());
        } else if (statement instanceof ExpressionStatement) {
            final ExpressionStatement s = (ExpressionStatement) statement;
            b.startSequence(s);
            compileSyntax(visited, b, s.getSyntax());
            b.endSequence();
        }
    }

    /**
     * Compile a list of syntax constructs (statements or syntax expressions).
     *
     * @param visited the set of visited definition nodes. It is used to detect definition cycles.
     * @param b       the builder to use
     * @param list    the list of statements to compile
     */
    public void compileSyntax(final Set<DefinitionView> visited, final ActionBuilder b, final List<?> list) {
        for (final Object o : list) {
            if (o instanceof Syntax s) {
                compileSyntax(visited, b, s);
            } else if (o instanceof SyntaxStatement s) {
                compileSyntaxStatement(visited, b, s);
            }
        }
    }

    /**
     * @return the definition context for this context builder
     */
    public DefinitionContext termContext() {
        return contextName;
    }

    /**
     * Get level builder by precedence.
     *
     * @param precedence the precedence
     * @return the level builder
     */
    public OperatorLevelBuilder levelBuilder(final int precedence) {
        return operatorLevels.get(precedence);
    }

    /**
     * @return grammar builder for this context builder
     */
    public GrammarBuilder grammarBuilder() {
        return grammarBuilder;
    }

    /**
     * @return the action linker
     */
    public ActionLinker getLinker() {
        return grammarBuilder().getLinker();
    }

    /**
     * @return the keyword context or null if it was not set yet
     */
    public KeywordContext getKeywordContext() {
        return keywordContext;
    }

    /**
     * Set keyword context for the production.
     *
     * @param keywordContext the keyword context
     */
    public void setKeywordContext(final KeywordContext keywordContext) {
        if (this.keywordContext != null) {
            // this is an assumption that keyword context is set only once for the context builder.
            // If it is not, something needs to be changed.
            throw new IllegalStateException("Keyword context is already set!");
        }
        this.keywordContext = keywordContext;
    }
}
