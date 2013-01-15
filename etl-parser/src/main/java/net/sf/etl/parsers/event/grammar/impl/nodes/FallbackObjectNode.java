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
public class FallbackObjectNode extends ScopeNode {
    /**
     * The name of object to be created.
     */
    ObjectName name;
    /**
     * The include wrappers for this object
     */
    WrapperLink wrappers;

    /**
     * Set object that should be used for fallback
     *
     * @param name     the name of an object to be created
     * @param wrappers the include wrappers for this object
     */
    public void setFallbackObject(ObjectName name, WrapperLink wrappers) {
        this.name = name;
        this.wrappers = wrappers;
    }

    @Override
    public Action buildActions(ActionBuilder b, Action normalExit, Action errorExit, Action recoveryTest) {
        assert name != null : "Fallback scope should have been initialized";
        errorExit = ActionUtil.endObject(source, errorExit, name, wrappers);
        errorExit = new CommitMarkAction(source, errorExit);
        errorExit = ActionUtil.startObject(source, errorExit, name, wrappers, true);
        return innerNode().buildActions(b, normalExit, errorExit, recoveryTest);
    }
}
