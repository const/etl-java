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

import net.sf.etl.parsers.PropertyName;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;
import net.sf.etl.parsers.event.impl.term.action.Action;
import net.sf.etl.parsers.event.impl.term.action.StructuralTokenAction;

/**
 * Then property scope node
 *
 * @author const
 */
public class PropertyNode extends TermScopeNode {
    /**
     * The name of property
     */
    private final PropertyName name;
    /**
     * The flag that indicates that it is a list property
     */
    private final boolean isList;

    /**
     * A constructor
     *
     * @param name   a property name for the scope
     * @param isList true if it is a list property
     * @param atMark if true the property should be started at mark
     */
    public PropertyNode(PropertyName name, boolean isList, boolean atMark) {
        super(atMark);
        this.name = name;
        this.isList = isList;
    }

    /**
     * @return a property name
     */
    public PropertyName name() {
        return name;
    }

    /**
     * @return true if it is a list property
     */
    public boolean isList() {
        return isList;
    }


    @Override
    protected Action buildStartState(ActionBuilder b, Action bodyStates, Action errorExit, Action errorCloseState) {
        return new StructuralTokenAction(bodyStates, isList ? Terms.LIST_PROPERTY_START : Terms.PROPERTY_START,
                name, isAtMark());
    }

    @Override
    protected Action buildEndState(ActionBuilder b, Action normalExit, Action errorExit) {
        return new StructuralTokenAction(normalExit, isList ? Terms.LIST_PROPERTY_END
                : Terms.PROPERTY_END, name, false);
    }

}
