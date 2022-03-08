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

package net.sf.etl.parsers.event.grammar.impl.nodes;

import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;
import net.sf.etl.parsers.event.impl.term.action.Action;
import net.sf.etl.parsers.event.impl.term.action.DisableSoftEndAction;
import net.sf.etl.parsers.event.impl.term.action.EnableSoftEndAction;

/**
 * Soft-break-disabled scope node. Within scope of this node soft-breaks cannot happen.
 */
public class NoSoftBreakScopeNode extends CleanupScopeNode {
    /**
     * The location of soft breaks.
     */
    private final SourceLocation location;

    /**
     * The constructor from locations
     *
     * @param location the location
     */
    public NoSoftBreakScopeNode(SourceLocation location) {
        this.location = location;
    }

    @Override
    protected Action buildStartState(ActionBuilder b, Action bodyStates, Action errorExit, Action errorCloseState) {
        return new DisableSoftEndAction(location, bodyStates);
    }

    @Override
    protected Action buildEndState(ActionBuilder b, Action normalExit, Action errorExit) {
        return new EnableSoftEndAction(location, normalExit);
    }
}
