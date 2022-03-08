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

import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.event.unstable.model.grammar.ChoiceCaseDef;

/**
 * This view wraps def construct.
 *
 * @author const
 */
public final class ChoiceCaseDefView extends DefinitionView {
    /**
     * The constructor.
     *
     * @param context    the original context
     * @param definition the definition
     */
    public ChoiceCaseDefView(final ContextView context, final ChoiceCaseDef definition) {
        super(context, definition);
    }

    /**
     * The constructor.
     *
     * @param context    the including context
     * @param definition the included view
     */
    public ChoiceCaseDefView(final ContextView context, final ChoiceCaseDefView definition) {
        super(context, definition);
    }

    /**
     * @return the name of choice this case contributes to
     */
    public String choiceName() {
        final Token choiceName = ((ChoiceCaseDef) definition()).getChoiceName();
        if (choiceName == null) {
            return null;
        } else {
            return choiceName.text();
        }
    }
}
