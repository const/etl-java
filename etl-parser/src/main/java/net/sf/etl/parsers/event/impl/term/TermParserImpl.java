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

package net.sf.etl.parsers.event.impl.term; // NOPMD

import net.sf.etl.parsers.DefinitionContext;
import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.LoadedGrammarInfo;
import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.TermParser;
import net.sf.etl.parsers.event.grammar.BootstrapGrammars;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.grammar.Keyword;
import net.sf.etl.parsers.event.grammar.KeywordContext;
import net.sf.etl.parsers.event.grammar.TermParserContext;
import net.sf.etl.parsers.event.grammar.TermParserState;
import net.sf.etl.parsers.event.grammar.TermParserStateFactory;
import net.sf.etl.parsers.event.impl.util.ListStack;
import net.sf.etl.parsers.event.unstable.model.doctype.Doctype;
import net.sf.etl.parsers.literals.StringInfo;
import net.sf.etl.parsers.literals.StringParser;
import net.sf.etl.parsers.resource.ResolvedObject;
import net.sf.etl.parsers.resource.ResourceReference;
import net.sf.etl.parsers.resource.ResourceRequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Core implementation of term parser that delegates to other term parsers.
 */
public final class TermParserImpl implements TermParser { // NOPMD
    /**
     * The term parser context.
     */
    private final TermParserContext context = new TermParserContextImpl();
    /**
     * The token queue.
     */
    private final MarkedQueue<TermToken> queue = new MarkedQueue<TermToken>();
    /**
     * Stack for soft ends.
     */
    private final ListStack<Integer> softEndStack = new ListStack<Integer>();
    /**
     * The list stack.
     */
    private final ListStack<KeywordContext> keywords = new ListStack<KeywordContext>();
    /**
     * The compiled grammar.
     */
    private CompiledGrammar grammar;
    /**
     * The system id of the source.
     */
    private String systemId;
    /**
     * The initial definition context.
     */
    private DefinitionContext initialContext;
    /**
     * Current token cell (set by parse method).
     */
    private Cell<PhraseToken> tokenCell;
    /**
     * If true, the grammar is in script mode.
     */
    private Boolean scriptMode;
    /**
     * The state stack.
     */
    private TermParserState stateStack;
    /**
     * If true, advance is needed.
     */
    private boolean advanceNeeded;
    /**
     * The count for disabled soft ends.
     */
    private int disabledSoftEndCount;
    /**
     * The classified keyword.
     */
    private Keyword classifiedKeyword;
    /**
     * True if keyword is actually classified.
     */
    private boolean isKeywordClassified;
    /**
     * The token listener to handle some special conditions (currently only parsing and gathering
     * information from document type).
     */
    private TermTokenListener termTokenListener;
    /**
     * The grammar request.
     */
    private ResourceRequest grammarRequest;
    /**
     * Errors detected during construction of grammar request from doctype (assuming that doctype parsed correctly).
     */
    private ErrorInfo grammarRequestErrors;
    /**
     * The initial context name.
     */
    private String initialContextName;
    /**
     * The document type object.
     */
    private Doctype doctype;
    /**
     * The current position.
     */
    private TextPos currentPos;
    /**
     * The default public id.
     */
    private String defaultPublicId;
    /**
     * The default system id.
     */
    private String defaultSystemId;
    /**
     * The default context.
     */
    private String defaultContext;
    /**
     * The default script mode.
     */
    private Boolean defaultScriptMode;

    @Override
    public void forceGrammar(final CompiledGrammar forcedGrammar, final boolean forcedGrammarScriptMode) {
        this.scriptMode = forcedGrammarScriptMode;
        if (this.grammar != null) {
            throw new IllegalStateException("The grammar is already provided");
        }
        this.grammar = forcedGrammar;
    }

    @Override
    public void setDefaultGrammar(final String userPublicId, final String userSystemId,
                                  final String userContextName, final Boolean userScriptMode) {
        this.defaultPublicId = userPublicId;
        this.defaultSystemId = userSystemId;
        defaultContext = userContextName;
        defaultScriptMode = userScriptMode;
    }

    @Override
    public boolean isGrammarDetermined() {
        return grammar != null;
    }

    @Override
    public CompiledGrammar grammar() {
        return grammar;
    }

    @Override
    public DefinitionContext initialContext() {
        return initialContext != null ? initialContext : grammar == null ? null : grammar().getDefaultContext();
    }

    @Override
    public void start(final String grammarSystemId) {
        this.systemId = grammarSystemId;
        this.stateStack = SourceStateFactory.INSTANCE.start(context, null);
    }

    @Override
    public ResourceRequest grammarRequest() {
        return grammarRequest;
    }

    @Override
    public void provideGrammar(final ResolvedObject<CompiledGrammar> resolvedGrammar, // NOPMD
                               final ErrorInfo resolutionErrors) {
        if (this.grammar != null) {
            throw new IllegalStateException("Grammar is already provided");
        }
        if (!resolvedGrammar.getRequest().equals(grammarRequest)) {
            throw new IllegalStateException("The grammar request " + resolvedGrammar.getRequest()
                    + " do not match original " + grammarRequest);
        }
        final DefinitionContext initialContextCandidate = resolvedGrammar.getObject().getDefaultContext();
        final TextPos contextStart = doctype == null || doctype.getContext() == null
                ? currentPos : doctype.getContext().start();
        final TextPos contextEnd = doctype == null || doctype.getContext() == null
                ? currentPos : doctype.getContext().end();
        if (initialContextName != null) {
            for (final DefinitionContext definitionContext : resolvedGrammar.getObject().getStatementContexts()) {
                if (definitionContext.context().equals(initialContextName)) {
                    initialContext = definitionContext;
                    break;
                }
            }
            if (initialContext == null) {
                if (initialContextCandidate != null) {
                    initialContext = initialContextCandidate;
                    grammarRequestErrors = new ErrorInfo("syntax.InitialContextMissingDefault", new Object[]{
                            initialContextName, initialContextCandidate.context()
                    }, contextStart, contextEnd, systemId, grammarRequestErrors);
                } else {
                    grammarRequestErrors = new ErrorInfo("syntax.InitialContextMissing", new Object[]{
                            initialContextName
                    }, contextStart, contextEnd, systemId, grammarRequestErrors);
                }
            }
        } else {
            initialContext = initialContextCandidate;
            if (initialContext == null) {
                grammarRequestErrors = new ErrorInfo("syntax.NoDefaultContext", ErrorInfo.NO_ARGS,
                        contextStart, contextEnd, systemId, grammarRequestErrors);
            }
        }
        if (initialContext == null) {
            this.grammar = BootstrapGrammars.defaultGrammar();
            initialContext = grammar.getDefaultContext();
        } else {
            this.grammar = resolvedGrammar.getObject();
        }
        final ErrorInfo merged = ErrorInfo.merge(grammarRequestErrors, resolvedGrammar.getObject().getErrors(),
                resolutionErrors);
        if (scriptMode == null) {
            scriptMode = grammar.isScript();
        }
        queue.append(new TermToken(Terms.GRAMMAR_IS_LOADED, null,
                new LoadedGrammarInfo(resolvedGrammar, grammar, initialContext),
                null, currentPos, currentPos, merged));
    }

    @Override
    public TermToken read() {
        if (queue.hasMark() || queue.isEmpty()) {
            throw new ParserException("Unable to get element");
        }
        final TermToken termToken = queue.get();
        if (termTokenListener != null) {
            termTokenListener.observe(termToken);
        }
        return termToken;
    }

    @Override
    public ParserState parse(final Cell<PhraseToken> token) {
        tokenCell = token;
        if (tokenCell.hasElement()) {
            currentPos = tokenCell.peek().start();
        }
        try {
            while (true) {
                if (grammarRequest != null && grammar == null) {
                    return ParserState.RESOURCE_NEEDED;
                }
                if (queue.hasElement()) {
                    return ParserState.OUTPUT_AVAILABLE;
                }
                if (stateStack == null) {
                    return ParserState.EOF;
                }
                if (token.isEmpty()) {
                    return ParserState.INPUT_NEEDED;
                }
                stateStack.parseMore();
            }
        } finally {
            tokenCell = null;
        }
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    /**
     * Add listener for tokens.
     *
     * @param listener the listener
     */
    public void addListener(final TermTokenListener listener) {
        if (this.termTokenListener != null) {
            throw new IllegalStateException("The listener is already installed");
        }
        this.termTokenListener = listener;
    }

    /**
     * Remove listener for term tokens.
     *
     * @param listener the listener
     */
    public void removeListener(final TermTokenListener listener) {
        if (this.termTokenListener == listener) {
            this.termTokenListener = null;
        }
    }

    /**
     * Set parsed doctype to the sequence.
     *
     * @param doctype the parsed doctype
     */
    public void setDoctype(final Doctype doctype) { // NOPMD
        this.doctype = doctype;
        if (grammar != null) {
            throw new IllegalStateException("Grammar is already available");
        }
        if (grammarRequest != null) {
            throw new IllegalStateException("The grammar request is already set");
        }
        final String refPublicId;
        String refSystemId;
        if (doctype == null) {
            refPublicId = defaultPublicId;
            refSystemId = defaultSystemId;
            initialContextName = defaultContext;
            scriptMode = defaultScriptMode;
        } else {
            refPublicId = parseDoctypeString(doctype.getPublicId(), "syntax.MalformedDoctypePublicId");
            refSystemId = parseDoctypeString(doctype.getSystemId(), "syntax.MalformedDoctypeSystemId");
            initialContextName = doctype.getContext() == null ? null : doctype.getContext().text();
            if (doctype.getType() != null) {
                scriptMode = "script".equals(doctype.getType().text());
            }
        }
        // attempt to parse system id relatively
        if (refSystemId != null) {
            try {
                refSystemId = URI.create(systemId).resolve(refSystemId).toString();
            } catch (final Throwable t) {
                grammarRequestErrors = new ErrorInfo("syntax.MalformedDoctypeSystemIdURI",
                        new Object[]{
                                refSystemId, t.toString()
                        },
                        doctype == null ? currentPos : doctype.getSystemId().start(),
                        doctype == null ? currentPos : doctype.getSystemId().end(), systemId, grammarRequestErrors);
            }
        }
        grammarRequest = new ResourceRequest(new ResourceReference(refSystemId, refPublicId),
                StandardGrammars.GRAMMAR_REQUEST_TYPE);
    }

    /**
     * Parse doctype token.
     *
     * @param stringToken the token
     * @param errorId     the error if string is invalid.
     * @return the string
     */
    private String parseDoctypeString(final Token stringToken, final String errorId) {
        final String rc;
        if (stringToken == null) {
            rc = null;
        } else {
            final StringInfo parsed = new StringParser(stringToken.text(), stringToken.start(), systemId).parse();
            if (parsed.getText() == null || parsed.getText().length() == 0 || parsed.getErrors() != null) {
                grammarRequestErrors = new ErrorInfo(errorId,
                        Collections.<Object>singletonList(stringToken.text()),
                        new SourceLocation(stringToken.start(), stringToken.end(), systemId),
                        grammarRequestErrors);
                rc = null;
            } else {
                rc = parsed.getText();
            }
        }
        return rc;
    }

    /**
     * This class represents a queue of tokens that have a possibility of
     * position and inserting new tokens just after mark. The functionality is
     * separated into own class just for convenience.
     */
    private static final class MarkedQueue<T> {
        // NOTE POST 0.2: introduce single item optimization.
        /**
         * A stack of marks.
         */
        private final List<Link<T>> markStack = new ArrayList<Link<T>>();
        /**
         * amount of committed marks.
         */
        private int committedMarks;

        /**
         * the first link or null if queue is empty.
         */
        private Link<T> first;

        /**
         * the last link or null if queue is empty.
         */
        private Link<T> last;

        /**
         * Create new mark at the end of queue.
         */
        public void pushMark() {
            markStack.add(last);
        }

        /**
         * commit mark.
         *
         * @return true if some tokens become available and control should be
         * returned to the parser
         */
        public boolean commitMark() {
            if (committedMarks == markStack.size() - 1) {
                committedMarks++;
                return first != null;
            }
            return false;
        }

        /**
         * Pop the mark.
         *
         * @return true if there are no more marks and queue is not empty
         */
        public boolean popMark() {
            assert !markStack.isEmpty() : "[BUG] Mark stack is empty";
            final int size = markStack.size();
            markStack.remove(size - 1);
            if (size == committedMarks) {
                committedMarks--;
            }
            return !hasMark() && first != null;
        }

        /**
         * @return true if there is at least one mark on the stack
         */
        public boolean hasMark() {
            return markStack.size() > committedMarks;
        }

        /**
         * Insert object after mark.
         *
         * @param value a value to insert
         */
        public void insertAtMark(final T value) {
            assert hasMark() : "[BUG] Mark stack is empty";
            final Link<T> mark = peekMark();
            final Link<T> l = new Link<T>(value);
            l.previous = mark;
            if (mark == null) {
                l.next = first;
                first = l;
            } else {
                l.next = mark.next;
                mark.next = l;
            }
            if (l.next == null) {
                last = l;
            } else {
                l.next.previous = l;
            }
        }

        /**
         * @return a current mark
         */
        private Link<T> peekMark() {
            assert hasMark() : "[BUG] Mark stack is empty";
            return markStack.get(markStack.size() - 1);
        }

        /**
         * Append value at end of the queue.
         *
         * @param value a value
         */
        public void append(final T value) {
            final Link<T> l = new Link<T>(value);
            if (last == null) {
                first = l;
                last = l;
            } else {
                last.next = l;
                l.previous = last;
                last = l;
            }
        }

        /**
         * @return peek object after mark or null if there are no objects after
         * mark.
         */
        public T peekObjectAfterMark() {
            final Link<T> mark = peekMark();
            final Link<T> afterMark = mark == null ? first : mark.next;
            return afterMark == null ? null : afterMark.value;
        }


        /**
         * @return check if queue has element to return to the user
         */
        public boolean hasElement() {
            return !hasMark() && !isEmpty();
        }

        /**
         * Get and remove item from queue.
         *
         * @return first item of queue or null.
         */
        public T get() {
            assert !hasMark() : "[BUG]Clients are not supposed to poll "
                    + "the queue while marks are active.";
            if (first == null) {
                return null;
            } else {
                final T rc = first.value;
                if (first.next == null) {
                    last = null;
                    first = null;
                } else {
                    first = first.next;
                    first.previous = null;
                }
                return rc;
            }
        }

        /**
         * @return true if the queue is empty
         */
        public boolean isEmpty() {
            return first == null;
        }

        /**
         * Insert value before mark. This used to report statement start.
         *
         * @param value a value to insert.
         */
        public void insertBeforeMark(final T value) {
            final Link<T> link = new Link<T>(value);
            markStack.set(0, link);
            link.next = first;
            if (first != null) {
                first.previous = link;
            } else {
                last = link;
            }
            first = link;
        }

        @Override
        public String toString() {
            final StringBuilder rc = new StringBuilder();
            rc.append('[');
            Link c = first;
            while (c != null) {
                if (c.previous != null) {
                    rc.append(", ");
                }
                rc.append(c.value);
                c = c.next;
            }
            rc.append(']');
            return rc.toString();
        }

        /**
         * Queue link.
         */
        private static final class Link<T> {
            /**
             * value.
             */
            private final T value;
            /**
             * next link.
             */
            private Link<T> next;
            /**
             * previous link.
             */
            private Link<T> previous;

            /**
             * A constructor.
             *
             * @param value a value that is held by link
             */
            public Link(final T value) {
                if (value == null) {
                    // This is an artificial limitation. However get() interface
                    // should be changed to lift it.
                    throw new IllegalArgumentException("Value cannot be null");
                }
                this.value = value;
            }
        }
    }

    /**
     * The parser context.
     */
    private final class TermParserContextImpl implements TermParserContext {


        @Override
        public boolean isScriptMode() {
            return scriptMode != null && scriptMode;
        }

        @Override
        public PhraseToken current() {
            ensureTokenCellNonEmpty();
            return tokenCell.peek();
        }

        /**
         * Ensure that token cell is not empty.
         */
        private void ensureTokenCellNonEmpty() {
            if (tokenCell == null || tokenCell.isEmpty()) {
                throw new IllegalStateException("The token cell is empty");
            }
        }

        @Override
        public void consumePhraseToken() {
            ensureTokenCellNonEmpty();
            tokenCell.take();
            advanceNeeded = true;
            isKeywordClassified = false;
        }

        @Override
        public boolean produce(final TermToken token) {
            queue.append(token);
            return queue.hasMark();
        }

        @Override
        public boolean produceAfterMark(final TermToken token) {
            queue.insertAtMark(token);
            return queue.hasMark();
        }

        @Override
        public void produceBeforeMark(final TermToken termToken) {
            queue.insertBeforeMark(termToken);
        }

        @Override
        public void pushMark() {
            queue.pushMark();
        }

        @Override
        public void commitMark() {
            queue.commitMark();
        }

        @Override
        public void popMark() {
            queue.popMark();
        }

        @Override
        public void pushKeywordContext(final KeywordContext keywordContext) {
            keywords.push(keywordContext);
            isKeywordClassified = false;
        }

        @Override
        public Keyword classify() {
            if (!isKeywordClassified) {
                isKeywordClassified = true;
                final PhraseToken current = current();
                if (keywords.isEmpty() || !current.hasToken()) {
                    classifiedKeyword = null;
                } else {
                    classifiedKeyword = keywords.peek().get(current.token().text());
                }
            }
            return classifiedKeyword;
        }

        @Override
        public void popKeywordContext(final KeywordContext keywordContext) {
            isKeywordClassified = false;
            keywords.pop();
        }

        @Override
        public void call(final TermParserStateFactory stateFactory) {
            stateStack = stateFactory.start(this, stateStack);
        }

        @Override
        public void exit(final TermParserState state, final boolean success) {
            if (state != stateStack) { // NOPMD
                throw new IllegalArgumentException("Exiting wrong state");
            }
            stateStack = state.getPreviousState();
            if (stateStack != null) {
                stateStack.setCallStatus(success);
            }
        }

        @Override
        public void advanced() {
            advanceNeeded = false;
        }

        @Override
        public boolean isAdvanceNeeded() {
            return advanceNeeded;
        }

        @Override
        public boolean canSoftEndStatement() {
            return disabledSoftEndCount == 0;
        }

        @Override
        public void startSoftEndContext() {
            softEndStack.push(disabledSoftEndCount);
            disabledSoftEndCount = 0;
        }

        @Override
        public void disableSoftEnd() {
            disabledSoftEndCount++;
        }

        @Override
        public boolean enableSoftEnd() {
            return --disabledSoftEndCount == 0;
        }

        @Override
        public void endSoftEndContext() {
            assert disabledSoftEndCount == 0 : "Disabled soft end count should be zero at the end of the context"
                    + disabledSoftEndCount;
            disabledSoftEndCount = softEndStack.pop();
        }

        @Override
        public TermParser parser() {
            return TermParserImpl.this;
        }

        @Override
        public TermToken peekObjectAtMark() {
            return queue.peekObjectAfterMark();
        }
    }
}
