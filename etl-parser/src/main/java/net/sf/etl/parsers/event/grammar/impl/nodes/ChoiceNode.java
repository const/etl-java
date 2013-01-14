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

import net.sf.etl.parsers.event.grammar.LookAheadSet;
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;
import net.sf.etl.parsers.event.grammar.impl.flattened.ContextView;
import net.sf.etl.parsers.event.grammar.impl.flattened.DefinitionView;
import net.sf.etl.parsers.event.impl.term.action.Action;
import net.sf.etl.parsers.event.impl.term.action.ReportErrorAction;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A choice node. This node selects between one or more alternatives.
 *
 * @author const
 */
public class ChoiceNode extends GroupNode {

    @Override
    public Action buildActions(ActionBuilder b, Action normalExit, Action errorExit) {
        final HashSet<ActionBuilder> visitedSet = new HashSet<ActionBuilder>();
        final LookAheadSet choiceLa = buildLookAhead(new HashSet<ActionBuilder>());
        ChoiceBuilder builder = new ChoiceBuilder(source);
        builder.setFallback(new ReportErrorAction(source, errorExit,
                "syntax.UnexpectedToken.expectingTokens",
                choiceLa.toString()));
        for (Node node : nodes()) {
            builder.add(node.buildLookAhead(visitedSet), node.buildActions(b, normalExit, errorExit));
        }
        return builder.build(b);
    }

    @Override
    protected boolean calcMatchesEmpty() {
        assert !nodes().isEmpty() : "Choice node must have at least one alternative";
        for (final Node node : nodes()) {
            if (node.matchesEmpty()) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected LookAheadSet createLookAhead(Set<ActionBuilder> visitedBuilders) {
        // TODO handle case of empty node
        assert !nodes().isEmpty() : "Choice node must have at least one alternative";
        final Iterator<Node> i = nodes().iterator();
        final LookAheadSet rc = new LookAheadSet();
        rc.addAll((i.next()).buildLookAhead(visitedBuilders));
        while (i.hasNext()) {
            final Node node = i.next();
            final LookAheadSet nodeLA = node.buildLookAhead(visitedBuilders);
            if (rc.conflictsWith(nodeLA) != null) {
                // NOTE POST 0.2: better diagnostic. Possibly add start/end
                // element to FSM builder
                final DefinitionView d = getDefinition();
                if (getDefinition() != null) {
                    // report that error is inside definition
                    getBuilder().contextBuilder().error(d, d.definition(),
                            "grammar.SyntaxDefinition.choiceConflict", rc.toString(), nodeLA.toString());
                } else {
                    // report that error is inside context
                    final ContextView v = getBuilder().contextBuilder().contextView();
                    v.error("grammar.Context.definitionConflict", rc.toString(), nodeLA.toString());
                }
            }
            rc.addAll(nodeLA);
        }
        return rc;
    }
}
