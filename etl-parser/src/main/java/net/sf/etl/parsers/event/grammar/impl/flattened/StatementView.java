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

import net.sf.etl.parsers.event.unstable.model.grammar.Statement;

/**
 * A view of the statement. This class offers some operations for statement
 * definition.
 *
 * @author const
 */
public final class StatementView extends ObjectDefinitionView {

    /**
     * A constructor from fields.
     *
     * @param context    the defining context
     * @param definition the definition
     */
    public StatementView(final ContextView context, final Statement definition) {
        super(context, definition);
    }

    /**
     * A constructor for using in implementing grammar include.
     *
     * @param context    the including context
     * @param definition the definition to copy
     */
    public StatementView(final ContextView context, final DefinitionView definition) {
        super(context, definition);
    }

    /**
     * Get wrappers for this statement that are required in this context.
     *
     * @param contextView the context view in context of which the statement is examined
     * @return wrapper objects or null if there are none
     */
    public WrapperLink wrappers(final ContextView contextView) {
        final ContextView ci = includingContext();
        return contextView.includeWrappers(ci);
    }
}
