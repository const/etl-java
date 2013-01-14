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

/**
 * This is an object scope node
 *
 * @author const
 */
public class ObjectNode extends TermScopeNode {
    /**
     * The name of the object
     */
    private final ObjectName name;
    /**
     * The wrappers for the node
     */
    private final WrapperLink wrappers;

    /**
     * The constructor
     *
     * @param name     the name of object
     * @param atMark   if true, the node is started at mark
     * @param wrappers wrappers for this node
     */
    public ObjectNode(ObjectName name, boolean atMark, WrapperLink wrappers) {
        super(atMark);
        this.name = name;
        this.wrappers = wrappers;
    }

    /**
     * @return an object name
     */
    public ObjectName name() {
        return name;
    }


    @Override
    protected Action buildStartState(ActionBuilder b, Action bodyStates, Action errorExit, Action errorCloseState) {
        return ActionUtil.startObject(source, bodyStates, name, wrappers, isAtMark());
    }


    @Override
    protected Action buildEndState(ActionBuilder b, Action normalExit, Action errorExit) {
        return ActionUtil.endObject(source, normalExit, name, wrappers);
    }

}
