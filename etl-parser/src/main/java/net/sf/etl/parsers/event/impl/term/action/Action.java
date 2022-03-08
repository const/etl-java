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

package net.sf.etl.parsers.event.impl.term.action;

import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.event.grammar.TermParserContext;

/**
 * The base class for the actions that does something with parser.
 * <p/>
 * The actions are generally mutable, but it is a design time interface. In runtime, the actions are not exposed
 * outside of {@link ActionStateFactory} using public API, so their mutability is non-issue.
 */
public abstract class Action {
    /**
     * The source of action.
     */
    private final SourceLocation source;

    /**
     * The action.
     *
     * @param source the source location in the grammar that caused this node creation
     */
    protected Action(final SourceLocation source) {
        this.source = source;
    }

    /**
     * Parse more elements.
     *
     * @param context the context of the parser
     * @param state   the context state
     */
    public abstract void parseMore(TermParserContext context, ActionState state);

    /**
     * @return the source.
     */
    public final SourceLocation getSource() {
        return source;
    }
}
