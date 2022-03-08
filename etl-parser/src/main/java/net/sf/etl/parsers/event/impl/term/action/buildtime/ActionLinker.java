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

package net.sf.etl.parsers.event.impl.term.action.buildtime;

import net.sf.etl.parsers.DefinitionContext;
import net.sf.etl.parsers.event.grammar.TermParserStateFactory;
import net.sf.etl.parsers.event.impl.term.action.CallAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The linker for the productions in the grammar. The linker is scoped to grammar compilation session.
 * It allows linking both statement and expression productions.
 */
public final class ActionLinker {
    /**
     * Scheduled links for the block.
     */
    private final Map<DefinitionContext, List<CallAction>> blocks = // NOPMD
            new HashMap<>();

    /**
     * Resolve block.
     *
     * @param context the context
     * @param factory the state factory for the block
     */
    public void resolveBlock(final DefinitionContext context, final TermParserStateFactory factory) {
        final List<CallAction> calls = blocks.get(context);
        if (calls != null) {
            for (final CallAction call : calls) {
                call.setStateFactory(factory);
            }
        }
    }

    /**
     * Schedule linking for the block, this is done is order to implement recursion for the grammars.
     *
     * @param action  the call action that needs reference to other block
     * @param context the context of the invoked block
     */
    public void linkBlock(final CallAction action, final DefinitionContext context) {
        blocks.computeIfAbsent(context, k -> new ArrayList<>()).add(action);
    }
}
