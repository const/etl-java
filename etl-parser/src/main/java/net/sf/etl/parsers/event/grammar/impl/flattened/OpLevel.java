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
package net.sf.etl.parsers.event.grammar.impl.flattened;

import java.util.HashSet;

/**
 * Level in operator set. All operators are already split in categories.
 *
 * @author const
 */
public class OpLevel {
    /**
     * "xf" operators.
     */
    public final HashSet<OpDefinitionView> xf = new HashSet<OpDefinitionView>();
    /**
     * "yf" operators.
     */
    public final HashSet<OpDefinitionView> yf = new HashSet<OpDefinitionView>();
    /**
     * "yfy" operators.
     */
    public final HashSet<OpDefinitionView> yfy = new HashSet<OpDefinitionView>();
    /**
     * "xfx" operators.
     */
    public final HashSet<OpDefinitionView> xfx = new HashSet<OpDefinitionView>();
    /**
     * "xfy" operators.
     */
    public final HashSet<OpDefinitionView> xfy = new HashSet<OpDefinitionView>();
    /**
     * "yfx" operators.
     */
    public final HashSet<OpDefinitionView> yfx = new HashSet<OpDefinitionView>();
    /**
     * "fx" operators.
     */
    public final HashSet<OpDefinitionView> fx = new HashSet<OpDefinitionView>();
    /**
     * "fy" operators.
     */
    public final HashSet<OpDefinitionView> fy = new HashSet<OpDefinitionView>();
    /**
     * "f" operators.
     */
    public final HashSet<OpDefinitionView> f = new HashSet<OpDefinitionView>();
    /**
     * priority of this level
     */
    public int precedence = -1;
    /**
     * next level with higher priority
     */
    public OpLevel nextLevel;
    /**
     * Previous level with lower priority
     */
    public OpLevel previousLevel;
}
