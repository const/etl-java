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

package net.sf.etl.parsers.event.impl.term.action;

import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.SyntaxRole;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.event.grammar.TermParserContext;

/**
 * The action that reports current phrase token to the parser with the specified kind and syntax role
 */
public class ReportTokenAction extends SimpleAction {
    /**
     * Kind used for reporting
     */
    Terms kind;
    /**
     * The syntax role
     */
    SyntaxRole role;

    /**
     * The constructor
     *
     * @param next the next action
     * @param kind the kind of token
     * @param role the role of token
     */
    public ReportTokenAction(Action next, Terms kind, SyntaxRole role) {
        super(next);
        this.kind = kind;
        this.role = role;
    }

    @Override
    public void parseMore(TermParserContext context, ActionState state) {
        PhraseToken in = context.current();
        TermToken out = new TermToken(kind, role, null, in, in.start(), in.end(), null);
        context.produce(out);
        context.consumePhraseToken();
        state.nextAction(next);
    }
}
