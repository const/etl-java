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
package net.sf.etl.parsers.event.unstable.model.grammar;

import net.sf.etl.parsers.Token;

import java.util.LinkedList;
import java.util.List;

/**
 * The OperatorDefinition node class. This class is a part of the lightweight
 * grammar model.
 *
 * @author const
 */
public final class OperatorDefinition extends SyntaxDefinition {
    /**
     * the text for operators.
     */
    private final List<Token> text = new LinkedList<Token>();
    /**
     * the associativity.
     */
    private Token associativity;
    /**
     * the precedence.
     */
    private Token precedence;
    /**
     * the composite modifier.
     */
    private Modifier compositeModifier;

    /**
     * @return the text for operators.
     */
    public List<Token> getText() {
        return text;
    }

    /**
     * @return the associativity.
     */
    public Token getAssociativity() {
        return associativity;
    }

    /**
     * Set the associativity.
     *
     * @param associativity the associativity
     */
    public void setAssociativity(final Token associativity) {
        this.associativity = associativity;
    }

    /**
     * @return the precedence.
     */
    public Token getPrecedence() {
        return precedence;
    }

    /**
     * Set the precedence.
     *
     * @param precedence the precedence
     */
    public void setPrecedence(final Token precedence) {
        this.precedence = precedence;
    }

    /**
     * @return the composite modifier.
     */
    public Modifier getCompositeModifier() {
        return compositeModifier;
    }

    /**
     * Set composite modifier.
     *
     * @param compositeModifier the modifier
     */
    public void setCompositeModifier(final Modifier compositeModifier) {
        this.compositeModifier = compositeModifier;
    }
}
