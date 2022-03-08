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
package net.sf.etl.parsers.event.grammar.impl.flattened;

import java.util.HashSet;
import java.util.Set;

/**
 * Level in operator set. All operators are already split in categories.
 *
 * @author const
 */
public final class OpLevel {
    /**
     * "xf" operators.
     */
    private final Set<OpDefinitionView> xf = new HashSet<>();
    /**
     * "yf" operators.
     */
    private final Set<OpDefinitionView> yf = new HashSet<>();
    /**
     * "yfy" operators.
     */
    private final Set<OpDefinitionView> yfy = new HashSet<>();
    /**
     * "xfx" operators.
     */
    private final Set<OpDefinitionView> xfx = new HashSet<>();
    /**
     * "xfy" operators.
     */
    private final Set<OpDefinitionView> xfy = new HashSet<>();
    /**
     * "yfx" operators.
     */
    private final Set<OpDefinitionView> yfx = new HashSet<>();
    /**
     * "fx" operators.
     */
    private final Set<OpDefinitionView> fx = new HashSet<>();
    /**
     * "fy" operators.
     */
    private final Set<OpDefinitionView> fy = new HashSet<>();
    /**
     * "f" operators.
     */
    private final Set<OpDefinitionView> f = new HashSet<>();
    /**
     * priority of this level.
     */
    private int precedence = -1;
    /**
     * next level with higher priority.
     */
    private OpLevel nextLevel;
    /**
     * Previous level with lower priority.
     */
    private OpLevel previousLevel;

    /**
     * @return xf operators
     */
    public Set<OpDefinitionView> getXF() {
        return xf;
    }

    /**
     * @return yf operators
     */
    public Set<OpDefinitionView> getYF() {
        return yf;
    }

    /**
     * @return yfy operators
     */
    public Set<OpDefinitionView> getYFY() {
        return yfy;
    }

    /**
     * @return xfx operators
     */
    public Set<OpDefinitionView> getXFX() {
        return xfx;
    }

    /**
     * @return xfy operators
     */
    public Set<OpDefinitionView> getXFY() {
        return xfy;
    }

    /**
     * @return yfx operators
     */
    public Set<OpDefinitionView> getYFX() {
        return yfx;
    }

    /**
     * @return fx operators
     */
    public Set<OpDefinitionView> getFX() {
        return fx;
    }

    /**
     * @return fy operators
     */
    public Set<OpDefinitionView> getFY() {
        return fy;
    }

    /**
     * @return f operators
     */
    public Set<OpDefinitionView> getF() {
        return f;
    }

    /**
     * @return the level precedence
     */
    public int getPrecedence() {
        return precedence;
    }

    /**
     * Set precedence.
     *
     * @param precedence the value
     */
    public void setPrecedence(final int precedence) {
        this.precedence = precedence;
    }

    /**
     * @return the next level
     */
    public OpLevel getNextLevel() {
        return nextLevel;
    }

    /**
     * Set the next level.
     *
     * @param nextLevel the value
     */
    public void setNextLevel(final OpLevel nextLevel) {
        this.nextLevel = nextLevel;
    }

    /**
     * @return the previous level
     */
    public OpLevel getPreviousLevel() {
        return previousLevel;
    }

    /**
     * Set previous level.
     *
     * @param previousLevel the previous level.
     */
    public void setPreviousLevel(final OpLevel previousLevel) {
        this.previousLevel = previousLevel;
    }
}
