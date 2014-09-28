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
package net.sf.etl.parsers.event.grammar.impl;

import net.sf.etl.parsers.PropertyName;
import net.sf.etl.parsers.SyntaxRole;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.event.grammar.impl.flattened.DefinitionView;
import net.sf.etl.parsers.event.grammar.impl.flattened.OpDefinitionView;
import net.sf.etl.parsers.event.grammar.impl.flattened.OpDefinitionView.PropertyInfo;
import net.sf.etl.parsers.event.grammar.impl.flattened.OpLevel;
import net.sf.etl.parsers.event.unstable.model.grammar.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * The builder for single operator level.
 *
 * @author const
 */
public final class OperatorLevelBuilder {
    /**
     * The level to compile.
     */
    private final OpLevel level;
    /**
     * The parent context builder.
     */
    private final ContextBuilder contextBuilder;
    /**
     * The state machine builder associated.
     */
    private final ActionBuilder b;

    /**
     * The constructor.
     *
     * @param l       the level to build
     * @param builder the parent builder
     */
    public OperatorLevelBuilder(final ContextBuilder builder, final OpLevel l) {
        super();
        this.level = l;
        this.contextBuilder = builder;
        b = new ActionBuilder(contextBuilder);
    }

    /**
     * Compile operator level locally.
     */
    public void buildNodes() {
        if (level.getPrecedence() == 0) {
            compilePrimary();
        } else {
            compileNormalLevel();
        }
    }

    /**
     * Compile normal level.
     */
    private void compileNormalLevel() { // NOPMD
        final ActionBuilder previousLevel = getPreviousBuilder();
        final HashSet<DefinitionView> visited = new HashSet<DefinitionView>();
        final Element contextElement = contextBuilder.contextView().reportingContext();
        b.startChoice(contextElement);
        // Compile F operations
        for (final OpDefinitionView d : level.getF()) {
            b.startDefinition(d);
            startOpObject(d);
            compileOpText(visited, d);
            b.endObject();
            b.endDefinition();
        }
        // Compile FX operations
        for (final OpDefinitionView d : level.getFX()) {
            b.startDefinition(d);
            startOpObject(d);
            compileOpText(visited, d);
            compileRightArg(d, previousLevel);
            b.endObject();
            b.endDefinition();
        }
        // Compile FY
        for (final OpDefinitionView d : level.getFY()) {
            b.startDefinition(d);
            startOpObject(d);
            compileOpText(visited, d);
            compileRightArg(d, b);
            b.endObject();
            b.endDefinition();
        }
        // Compile XF? and YF? operators
        b.startSequence(contextElement);
        b.call(contextElement, previousLevel);
        if (level.getXF().size() + level.getXFX().size() + level.getXFY().size() > 0) {
            b.startChoice(contextElement);
            // no op case
            b.startSequence(contextElement);
            b.endSequence();
            // Compile XF
            for (final OpDefinitionView d : level.getXF()) {
                b.startDefinition(d);
                b.startSequence(d.operator());
                compileLeftArg(d);
                startOpObjectAtMark(d);
                compileOpText(visited, d);
                b.endObject();
                b.endSequence();
                b.endDefinition();
            }
            // Compile XFX
            for (final OpDefinitionView d : level.getXFX()) {
                b.startDefinition(d);
                b.startSequence(d.operator());
                compileLeftArg(d);
                startOpObjectAtMark(d);
                compileOpText(visited, d);
                compileRightArg(d, previousLevel);
                b.endObject();
                b.endSequence();
                b.endDefinition();
            }
            // Compile XFY
            for (final OpDefinitionView d : level.getXFY()) {
                b.startDefinition(d);
                b.startSequence(d.operator());
                compileLeftArg(d);
                startOpObjectAtMark(d);
                compileOpText(visited, d);
                compileRightArg(d, b);
                b.endObject();
                b.endSequence();
                b.endDefinition();
            }
            b.endChoice();
        }
        // Compile YF, YFY, and YFX. These are tricky ones as they are right
        // recursive. Recursion is converted to Iteration here.
        if (level.getYF().size() + level.getYFX().size() > 0) {
            b.startRepeat(contextElement);
            b.startChoice(contextElement);
            compileYfAndYfx(previousLevel, visited);
            b.endChoice();
            b.endRepeat(); // YF and YFX
        }
        // yfy are at they end
        if (level.getYFY().size() > 0) {
            b.startChoice(contextElement);
            for (final OpDefinitionView d : level.getYFY()) {
                b.startDefinition(d);
                b.startSequence(d.operator());
                compileLeftArg(d);
                startOpObjectAtMark(d);
                compileOpText(visited, d);
                compileRightArg(d, b);
                b.endObject();
                b.endSequence();
                b.endDefinition();
            }
            b.endChoice();
        }
        b.endSequence(); // ?F?
        b.endChoice(); // all ops
    }

    /**
     * Compile Yf and Yfx operators int heo the context.
     *
     * @param previousLevel the previous level
     * @param visited       the visited builders
     */
    private void compileYfAndYfx(final ActionBuilder previousLevel, final Set<DefinitionView> visited) {
        for (final OpDefinitionView d : level.getYFX()) {
            b.startDefinition(d);
            b.startSequence(d.operator());
            compileLeftArg(d);
            startOpObjectAtMark(d);
            compileOpText(visited, d);
            compileRightArg(d, previousLevel);
            b.endObject();
            b.endSequence();
            b.endDefinition();
        }
        for (final OpDefinitionView d : level.getYF()) {
            b.startDefinition(d);
            b.startSequence(d.operator());
            compileLeftArg(d);
            startOpObjectAtMark(d);
            compileOpText(visited, d);
            b.endObject();
            b.endSequence();
            b.endDefinition();
        }
    }

    /**
     * @param d an operator definition
     */
    private void compileLeftArg(final OpDefinitionView d) {
        PropertyInfo leftArgProperty = d.getLeftArgProperty(contextBuilder.contextView());
        if (leftArgProperty == null) {
            contextBuilder.error(d, d.operator(), "grammar.ObjectDefinition.missingRightProperty");
            leftArgProperty = new PropertyInfo(d.operator(), "fake", false);
        }
        b.startPropertyAtMark(leftArgProperty.element(), getPropertyName(leftArgProperty), leftArgProperty.isList());
        b.endProperty();
    }

    /**
     * Compile marked expression call for the specified level.
     *
     * @param d        a context definition
     * @param argLevel an operator level to be used for right argument
     */
    private void compileRightArg(final OpDefinitionView d, final ActionBuilder argLevel) {
        PropertyInfo rightArgProperty = d.getRightArgProperty(contextBuilder.contextView());
        if (rightArgProperty == null) {
            contextBuilder.error(d, d.operator(), "grammar.ObjectDefinition.missingRightProperty");
            rightArgProperty = new PropertyInfo(d.operator(), "fake", false);
        }
        b.startProperty(rightArgProperty.element(), getPropertyName(rightArgProperty), rightArgProperty.isList());
        b.startMarked(rightArgProperty.element());
        b.call(rightArgProperty.element(), argLevel);
        b.endMarked();
        b.endProperty();
    }

    /**
     * Get name of property from property info.
     *
     * @param propertyInfo a property info
     * @return a property name
     */
    private PropertyName getPropertyName(final PropertyInfo propertyInfo) {
        // NOTE POST 0.2: optimize it later to use some cache, but make
        // performance check to ensure that it actually helps.
        return new PropertyName(propertyInfo.getName());
    }

    /**
     * Compile operator text.
     *
     * @param visited visited definitions
     * @param d       definition view for operator
     */
    private void compileOpText(final Set<DefinitionView> visited, final OpDefinitionView d) {
        if (d.isComposite()) {
            contextBuilder.compileSyntax(visited, b, d.operatorStatements(contextBuilder.contextView()));
        } else {
            b.startChoice(d.operator());
            for (final Token token : d.operator().getText()) {
                b.tokenText(d.operator(), Terms.STRUCTURAL, SyntaxRole.OPERATOR, token);
            }
            b.endChoice();
        }
    }

    /**
     * Start object associated with the operator.
     *
     * @param d an operator to start
     */
    private void startOpObjectAtMark(final OpDefinitionView d) {
        b.startObjectAtMark(d.operator(), d.convertName(d.rootObject(contextBuilder.contextView()).getName()));
    }

    /**
     * Start object associated with the operator.
     *
     * @param d an operator to start
     */
    private void startOpObject(final OpDefinitionView d) {
        b.startObject(d.operator(), d.convertName(d.rootObject(contextBuilder.contextView()).getName()));
    }

    /**
     * @return get builder for the previous level
     */
    private ActionBuilder getPreviousBuilder() {
        final int previousPrecedence = level.getPreviousLevel().getPrecedence();
        return contextBuilder.levelBuilder(previousPrecedence).b;
    }

    /**
     * Compile primary level.
     */
    private void compilePrimary() {
        // There are only composite f-class operators at this level.
        // So we do not have to consider anything else, erroneous
        // entries have been filtered out at previous processing levels.
        b.startChoice(contextBuilder.contextView().reportingContext());
        for (final OpDefinitionView op : level.getF()) {
            b.startDefinition(op.topObjectDefinition(contextBuilder.contextView()));
            b.startSequence(op.operator());
            contextBuilder.compileSyntax(new HashSet<DefinitionView>(), //NOPMD
                    b, op.topObject(contextBuilder.contextView()));
            b.endSequence();
            b.endDefinition();
        }
        b.endChoice();
    }

    /**
     * @return builder for this operator level
     */
    public ActionBuilder builder() {
        return b;
    }
}
