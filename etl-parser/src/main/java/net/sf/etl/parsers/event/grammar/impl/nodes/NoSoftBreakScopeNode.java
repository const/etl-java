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
