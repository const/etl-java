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
package net.sf.etl.parsers.event.grammar.impl.nodes;

import net.sf.etl.parsers.ObjectName;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;
import net.sf.etl.parsers.event.grammar.impl.flattened.WrapperLink;
import net.sf.etl.parsers.event.impl.term.action.Action;
import net.sf.etl.parsers.event.impl.term.action.CommitMarkAction;

/**
 * This this a special error recovery node. If syntax error happens during
 * execution of its inner nodes, it creates object of the specified kind that
 * starts at mark and ends at location of error. The object itself is empty.
 *
 * @author const
 */
public final class FallbackObjectNode extends ScopeNode {
    /**
     * The name of object to be created.
     */
    private ObjectName name;
    /**
     * The include wrappers for this object.
     */
    private WrapperLink wrappers;

    /**
     * Set object that should be used for fallback.
     *
     * @param newName     the name of an object to be created
     * @param newWrappers the include-wrappers for this object
     */
    public void setFallbackObject(final ObjectName newName, final WrapperLink newWrappers) {
        this.name = newName;
        this.wrappers = newWrappers;
    }

    @Override
    public Action buildActions(final ActionBuilder b, final Action normalExit, final Action errorExit,
                               final Action recoveryTest) {
        assert name != null : "Fallback scope should have been initialized";
        final SourceLocation source = getSource();
        Action last = errorExit;
        last = ActionUtil.endObject(source, last, name, wrappers);
        last = new CommitMarkAction(source, last);
        last = ActionUtil.startObject(source, last, name, wrappers, true);
        return innerNode().buildActions(b, normalExit, last, recoveryTest);
    }
}
