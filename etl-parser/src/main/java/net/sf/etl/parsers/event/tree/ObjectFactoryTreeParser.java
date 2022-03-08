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

package net.sf.etl.parsers.event.tree; // NOPMD

import net.sf.etl.parsers.ObjectName;
import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.impl.util.ListStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a TreeParser that uses {@link ObjectFactory} to create a tree of AST objects.
 *
 * @param <B> this is a base type for returned objects
 * @param <F> this is a type for feature metatype used by objects
 * @param <M> this is a type for meta object type
 * @param <H> this is a holder type for collection properties
 * @author const
 */
public final class ObjectFactoryTreeParser<B, F, M, H>
        implements TreeParser<B> {
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ObjectFactoryTreeParser.class);
    /**
     * The object factory.
     */
    private final ObjectFactory<B, F, M, H> factory;
    /**
     * The collectors.
     */
    private final ListStack<TokenCollector> collectors = new ListStack<>();
    /**
     * Token listeners for all tokens.
     */
    private final List<TokenCollector> tokenListeners = new ArrayList<>();
    /**
     * The current system identifier.
     */
    private final String systemId;
    /**
     * The currently available object.
     */
    private B current;
    /**
     * flag indicating that parser had errors.
     */
    private boolean hadErrors;
    /**
     * The current state.
     */
    private State state = new SourceState();
    /**
     * Listener for tokens with errors.
     */
    private TokenCollector errorTokenHandler = token -> {
        if (LOG.isErrorEnabled()) {
            LOG.error("ERROR: %s Error detected: %s".formatted(getSystemId(), token));
        }
    };
    /**
     * Listener for unexpected tokens.
     */
    private TokenCollector unexpectedTokenHandler = token -> {
        throw new ParserException("The token is not expected at this position: " + token);
    };


    /**
     * The constructor.
     *
     * @param factory  the object factory
     * @param systemId the system id
     */
    public ObjectFactoryTreeParser(final ObjectFactory<B, F, M, H>
                                           factory, final String systemId) {
        this.factory = factory;
        this.systemId = systemId;
        tokenListeners.add(token -> {
            if (token.hasAnyErrors()) {
                hadErrors = true;
                if (errorTokenHandler != null) {
                    errorTokenHandler.collect(token);
                }
            }
        });
    }

    /**
     * The utility method that helps to live with Java generics. It just invokes a constructor but it is appropriately
     * parameterized.
     *
     * @param factory        the factory
     * @param sourceSystemId the system id for the source
     * @param <B1>           this is a base type for returned objects
     * @param <F1>           this is a type for feature metatype used by objects
     * @param <M1>           this is a type for meta-object type
     * @param <H1>           this is a holder type for collection properties
     * @return the created parser
     */
    public static <B1, F1, M1, H1> ObjectFactoryTreeParser<B1, F1, M1, H1>
    make(final ObjectFactory<B1, F1, M1, H1> factory,
         final String sourceSystemId) {
        return new ObjectFactoryTreeParser<>(
                factory, sourceSystemId);
    }

    @Override
    public B read() {
        if (current == null) {
            throw new ParserException("The result is not available!");
        }
        final B rc = current;
        current = null;
        return rc;
    }

    @Override
    public ParserState parse(final Cell<TermToken> tokenCell) {
        while (true) {
            if (current != null) {
                return ParserState.OUTPUT_AVAILABLE;
            }
            if (state == null) {
                return ParserState.EOF;
            }
            if (tokenCell.isEmpty() || skipIgnorable(tokenCell)) {
                return ParserState.INPUT_NEEDED;
            }
            state.parse(tokenCell);
        }
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    /**
     * Skip tokens that does not affect parsing process.
     *
     * @param token the token
     * @return true if something was skipped
     */
    private boolean skipIgnorable(final Cell<TermToken> token) {
        final TermToken tk = token.peek();
        if (!collectors.isEmpty()) {
            notify(tk, collectors);
        }
        notify(tk, tokenListeners);
        switch (tk.kind()) {
            case EOF:
            case VALUE:
            case PROPERTY_START:
            case PROPERTY_END:
            case LIST_PROPERTY_END:
            case LIST_PROPERTY_START:
            case OBJECT_END:
            case OBJECT_START:
                return false;
            default:
                token.take();
                return true;
        }
    }

    /**
     * Notify listeners.
     *
     * @param tk        the token
     * @param listeners the listener
     */
    private void notify(final TermToken tk, final List<TokenCollector> listeners) {
        for (final TokenCollector c : listeners) {
            try {
                c.collect(tk);
            } catch (Exception ex) { // NOPMD
                LOG.error("Error while notifying listener", ex);
            }
        }
    }

    @Override
    public boolean hadErrors() {
        return hadErrors;
    }

    @Override
    public void setErrorTokenHandler(final TokenCollector errorTokenHandler) {
        this.errorTokenHandler = errorTokenHandler;
    }

    @Override
    public void setUnexpectedTokenHandler(final TokenCollector unexpectedTokenHandler) {
        this.unexpectedTokenHandler = unexpectedTokenHandler;
    }

    @Override
    public void addTokenListener(final TokenCollector listener) {
        this.tokenListeners.add(listener);
    }

    @Override
    public void removeTokenListener(final TokenCollector listener) {
        this.tokenListeners.add(listener);
    }

    /**
     * The parser state.
     */
    private abstract class State {
        /**
         * The previous state in the state stack.
         */
        private final State previous;
        /**
         * The object returned by invoked state.
         */
        private B result;

        /**
         * Create state and install it.
         */
        protected State() {
            previous = state;
            if (previous != null) {
                previous.setResult(null);
            }
            state = this;
        }

        /**
         * Exit from state.
         *
         * @param stateResult the result returned to the previous state
         */
        protected final void exit(final B stateResult) {
            if (previous != null) {
                previous.setResult(stateResult);
            }
            exit();
        }

        /**
         * Exit from state.
         */
        protected final void exit() {
            state = previous;
        }

        /**
         * Do some action with token. Something should change as result of token action.
         *
         * @param token the token
         */
        protected abstract void parse(final Cell<TermToken> token);

        /**
         * @return the object returned by invoked state.
         */
        protected final B getResult() {
            return result;
        }

        /**
         * Set result.
         *
         * @param result the result
         */
        protected final void setResult(final B result) {
            this.result = result;
        }
    }

    /**
     * The source state.
     */
    private final class SourceState extends State {
        @Override
        protected void parse(final Cell<TermToken> tokenCell) {
            if (getResult() != null) {
                current = getResult();
                setResult(null);
                return;
            }
            final TermToken token = tokenCell.peek();
            switch (token.kind()) {
                case OBJECT_START:
                    if (factory.isIgnorable(token, token.objectName())) {
                        new SkipObjectState();
                        return;
                    } else {
                        new ObjectState(tokenCell);
                        return;
                    }
                case PROPERTY_START:
                case LIST_PROPERTY_START:
                    if (unexpectedTokenHandler != null) {
                        unexpectedTokenHandler.collect(token);
                    }
                    tokenCell.take();
                    return;
                case PROPERTY_END:
                case LIST_PROPERTY_END:
                    tokenCell.take();
                    return;
                case EOF:
                    tokenCell.take();
                    exit();
                    return;
                default:
                    throw new IllegalStateException("Unexpected token: " + token);
            }
        }
    }

    /**
     * The state that just skips nested objects from parse.
     */
    private final class SkipObjectState extends State {
        /**
         * the nested object count.
         */
        private int count;

        @Override
        protected void parse(final Cell<TermToken> tokenCell) {
            final TermToken token = tokenCell.take();
            switch (token.kind()) {
                case OBJECT_START:
                    count++;
                    return;
                case OBJECT_END:
                    count--;
                    if (count == 0) {
                        exit();
                    }
                    return;
                case EOF:
                    tokenCell.put(token);
                    throw new IllegalStateException("There are " + count + " of unclosed objects before EOF");
                default:
                    // just skip the token
            }
        }
    }

    /**
     * The object state.
     */
    private final class ObjectState extends State {
        /**
         * the name of object.
         */
        private final ObjectName name;
        /**
         * The meta object.
         */
        private final M metaObject;
        /**
         * The object under construction.
         */
        private final B rc;
        /**
         * The stat position value.
         */
        private final Object startValue;
        /**
         * Extra objects.
         */
        private int extraObjects;

        /**
         * The constructor from token cell that contains object start token.
         *
         * @param tokenCell the token cell
         */
        protected ObjectState(final Cell<TermToken> tokenCell) {
            final TermToken token = tokenCell.take();
            assert token.kind() == Terms.OBJECT_START : "parser is not over object: " + token;
            name = token.objectName();
            metaObject = factory.getMetaObject(name);
            rc = factory.createInstance(metaObject, name);
            startValue = factory.setObjectStartPos(rc, metaObject, token);
            if (rc instanceof TokenCollector c) {
                collectors.push(c);
            }
        }

        @Override
        protected void parse(final Cell<TermToken> tokenCell) {
            final TermToken token = tokenCell.peek();
            switch (token.kind()) {
                case EOF:
                    throw new IllegalStateException("Unexpected eof inside object: " + token);
                case OBJECT_END:
                    tokenCell.take();
                    if (extraObjects > 0) {
                        extraObjects--;
                    } else {
                        assert token.objectName().equals(name) : "type name does not match ";
                        factory.setObjectEndPos(rc, metaObject, startValue, token);
                        if (rc instanceof TokenCollector) {
                            final TokenCollector r = collectors.pop();
                            assert r == rc; // NOPMD
                        }
                        exit(rc);
                        return;
                    }
                case VALUE:
                    if (unexpectedTokenHandler != null) {
                        unexpectedTokenHandler.collect(token);
                    }
                    tokenCell.take();
                    break;
                case OBJECT_START:
                    tokenCell.take();
                    if (unexpectedTokenHandler != null) {
                        unexpectedTokenHandler.collect(token);
                    }
                    extraObjects++;
                    break;
                case PROPERTY_START:
                case LIST_PROPERTY_START:
                    new PropertyState(rc, metaObject, tokenCell);
                    break;
                default:
                    throw new ParserException("Unexpected token kind: " + token);
            }

        }
    }

    /**
     * The property state.
     */
    private final class PropertyState extends State {
        /**
         * The feature of the object.
         */
        private final F feature;
        /**
         * True if the list property.
         */
        private final boolean isList;
        /**
         * The holder type for the property.
         */
        private final H holder;
        /**
         * The object under construction.
         */
        private final B object;
        /**
         * The meta object.
         */
        private final M metaObject;
        /**
         * amount of extra properties.
         */
        private int extraProperties;

        /**
         * The constructor.
         *
         * @param object     the object
         * @param metaObject the meta object
         * @param tokenCell  the token cell
         */
        protected PropertyState(final B object, final M metaObject,
                                final Cell<TermToken> tokenCell) {
            this.object = object;
            this.metaObject = metaObject;
            final TermToken token = tokenCell.take();
            assert token.kind() == Terms.PROPERTY_START || token.kind() == Terms.LIST_PROPERTY_START
                    : "parser is not over property: " + token;
            feature = factory.getPropertyMetaObject(object, metaObject, token);
            isList = token.kind() == Terms.LIST_PROPERTY_START;
            holder = isList ? factory.startListCollection(object, metaObject, feature) : null;
        }

        @Override
        protected void parse(final Cell<TermToken> tokenCell) {
            if (getResult() != null) {
                if (isList) {
                    factory.addToFeature(object, feature, holder, getResult());
                } else {
                    factory.setToFeature(object, feature, getResult());
                }
                setResult(null);
            }
            final TermToken token = tokenCell.peek();
            switch (token.kind()) {
                case PROPERTY_END:
                case LIST_PROPERTY_END:
                    tokenCell.take();
                    if (extraProperties > 0) {
                        extraProperties--;
                    } else {
                        if (isList) {
                            factory.endListCollection(object, metaObject, feature, holder);
                        }
                        exit();
                    }
                    break;
                case PROPERTY_START:
                case LIST_PROPERTY_START:
                    if (unexpectedTokenHandler != null) {
                        unexpectedTokenHandler.collect(token);
                    }
                    extraProperties++;
                    tokenCell.take();
                    break;
                case OBJECT_START:
                    if (factory.isIgnorable(token, token.objectName())) {
                        new SkipObjectState();
                    } else {
                        new ObjectState(tokenCell);
                    }
                    break;
                case VALUE:
                    final Token value = token.token().token();
                    if (isList) {
                        factory.addValueToFeature(object, feature, holder, value);
                    } else {
                        factory.setValueToFeature(object, feature, value);
                    }
                    tokenCell.take();
                    break;
                case EOF:
                    throw new IllegalStateException("Unexpected eof inside property: " + token);
                default:
                    throw new IllegalStateException("Unexpected token inside property: " + token);
            }
        }
    }
}
