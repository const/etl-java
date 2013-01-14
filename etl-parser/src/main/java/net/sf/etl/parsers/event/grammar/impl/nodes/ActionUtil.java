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
import net.sf.etl.parsers.PropertyName;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.event.grammar.impl.flattened.WrapperLink;
import net.sf.etl.parsers.event.impl.term.action.Action;
import net.sf.etl.parsers.event.impl.term.action.StructuralTokenAction;

/**
 * This class contains utilities related to generation of actions.
 *
 * @author const
 */
class ActionUtil {
    /**
     * A private constructor. The class contains only static members.
     */
    private ActionUtil() {
        // Do nothing
    }

    /**
     * Start object states
     *
     * @param source     the source location that defined this action
     * @param bodyStates the body states
     * @param name       the name of object
     * @param wrappers   the wrappers of the object
     * @param atMark     if true, object should be started at mark
     * @return a report object state
     */
    static StructuralTokenAction startObject(SourceLocation source, Action bodyStates,
                                             ObjectName name, WrapperLink wrappers,
                                             boolean atMark) {
        return new StructuralTokenAction(source, startIncludeWrappers(bodyStates, wrappers),
                Terms.OBJECT_START, name, atMark);
    }

    /**
     * Start include wrappers associated with this object
     *
     * @param bodyStates the body states
     * @param wrappers   the wrappers to process
     * @return a new begin state
     */
    private static Action startIncludeWrappers(Action bodyStates, WrapperLink wrappers) {
        if (wrappers != null) {
            final ObjectName wrapperObject = new ObjectName(wrappers.namespace(), wrappers.name());
            final PropertyName wrapperProperty = new PropertyName(wrappers.property());
            return startIncludeWrappers(
                    new StructuralTokenAction(
                            wrappers.propertyLocation(),
                            new StructuralTokenAction(wrappers.objectLocation(), bodyStates, Terms.OBJECT_START, wrapperObject, true),
                            Terms.PROPERTY_START, wrapperProperty, true),
                    wrappers.innerWrapper());
        }
        return bodyStates;
    }

    /**
     * Generate end object states
     *
     * @param source     the source location that defined this action
     * @param normalExit the exit state
     * @param name       the name of object
     * @param wrappers   the wrappers of the object
     * @return the generated state
     */
    static StructuralTokenAction endObject(SourceLocation source, Action normalExit, ObjectName name, WrapperLink wrappers) {
        return new StructuralTokenAction(source, endIncludeWrappers(normalExit, wrappers), Terms.OBJECT_END, name, false);
    }

    /**
     * End include wrappers associated with this object
     *
     * @param next     the next states
     * @param wrappers the wrappers to process
     * @return the new begin state
     */
    private static Action endIncludeWrappers(Action next, WrapperLink wrappers) {
        if (wrappers != null) {
            next = endIncludeWrappers(next, wrappers.innerWrapper());
            final ObjectName wrapperObject = new ObjectName(wrappers.namespace(), wrappers.name());
            final PropertyName wrapperProperty = new PropertyName(wrappers.property());
            return new StructuralTokenAction(
                    wrappers.propertyLocation(),
                    new StructuralTokenAction(wrappers.objectLocation(), next, Terms.OBJECT_END, wrapperObject, false),
                    Terms.PROPERTY_END, wrapperProperty, false);
        }
        return next;
    }
}
