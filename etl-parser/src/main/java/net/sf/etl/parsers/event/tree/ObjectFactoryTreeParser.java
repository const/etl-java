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

package net.sf.etl.parsers.event.tree;

import net.sf.etl.parsers.*;
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.ParserState;

/**
 * This is a TreeParser that uses {@link ObjectFactory} to create a tree of AST objects.
 *
 * @param <BaseObjectType> this is a base type for returned objects
 * @param <FeatureType>    this is a type for feature metatype used by objects
 * @param <MetaObjectType> this is a type for meta object type
 * @param <HolderType>     this is a holder type for collection properties
 * @author const
 */
public class ObjectFactoryTreeParser<BaseObjectType, FeatureType, MetaObjectType, HolderType> implements TreeParser<BaseObjectType> {
    /**
     * The currently available object
     */
    private BaseObjectType current;
    /**
     * The current state
     */
    private State state = new SourceState();
    /**
     * The object factory
     */
    private final ObjectFactory<BaseObjectType, FeatureType, MetaObjectType, HolderType> factory;

    /**
     * The constructor
     *
     * @param factory the object factory
     */
    public ObjectFactoryTreeParser(ObjectFactory<BaseObjectType, FeatureType, MetaObjectType, HolderType> factory) {
        this.factory = factory;
    }

    /**
     * The utility method that helps to live with Java generics. It just invokes a constructor but it is appropriately
     * parameterized.
     *
     * @param factory          the factory
     * @param <BaseObjectType> this is a base type for returned objects
     * @param <FeatureType>    this is a type for feature metatype used by objects
     * @param <MetaObjectType> this is a type for meta object type
     * @param <HolderType>     this is a holder type for collection properties
     * @return the created parser
     */
    public static <BaseObjectType, FeatureType, MetaObjectType, HolderType>
    ObjectFactoryTreeParser<BaseObjectType, FeatureType, MetaObjectType, HolderType>
    make(ObjectFactory<BaseObjectType, FeatureType, MetaObjectType, HolderType> factory) {
        return new ObjectFactoryTreeParser<BaseObjectType, FeatureType, MetaObjectType, HolderType>(factory);
    }

    @Override
    public BaseObjectType read() {
        if (current == null) {
            throw new ParserException("The result is not available!");
        }
        final BaseObjectType rc = current;
        current = null;
        return rc;
    }

    /**
     * Parse tokenCell,
     *
     * @param tokenCell the cell with the tokenCell. The element is removed if it is consumed and more date is needed.
     * @return the parsed state
     */
    @Override
    public ParserState parse(Cell<TermToken> tokenCell) {
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

    /**
     * Skip tokens that does not affect parsing process
     *
     * @param token the token
     * @return true if something was skipped
     */
    private boolean skipIgnorable(Cell<TermToken> token) {
        final TermToken tk = token.peek();
        factory.handleToken(tk);
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
            case GRAMMAR_IS_LOADED:
                factory.handleLoadedGrammar(tk, tk.loadedGrammar());
                token.take();
                return true;
            default:
                token.take();
                return true;
        }
    }

    @Override
    public String getSystemId() {
        return factory.getSystemId();
    }

    /**
     * The parser state
     */
    abstract class State {
        /**
         * The previous state in the state stack
         */
        private State previous;
        /**
         * The object returned by invoked state
         */
        protected BaseObjectType result;

        /**
         * Create state and install it
         */
        State() {
            previous = state;
            if (previous != null) {
                previous.result = null;
            }
            state = this;
        }

        /**
         * Exit from state
         */
        void exit(BaseObjectType result) {
            if (previous != null) {
                previous.result = result;
            }
            exit();
        }

        /**
         * Exit from state
         */
        void exit() {
            state = previous;
        }

        /**
         * Do some action with token. Something should change as result of token action.
         *
         * @param token the token
         */
        abstract void parse(Cell<TermToken> token);
    }

    /**
     * The source state
     */
    class SourceState extends State {
        @Override
        void parse(Cell<TermToken> tokenCell) {
            if (result != null) {
                current = result;
                result = null;
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
                    factory.handleUnexpectedPropertyStart(token);
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
     * The state that just skips nested objects from parse
     */
    class SkipObjectState extends State {
        /**
         * the nested object count
         */
        int count;

        @Override
        void parse(Cell<TermToken> tokenCell) {
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
     * The object state
     */
    class ObjectState extends State {
        /**
         * Extra objects
         */
        int extraObjects = 0;
        /**
         * the name of object
         */
        final ObjectName name;
        /**
         * The meta object
         */
        final MetaObjectType metaObject;
        /**
         * The object under construction
         */
        final BaseObjectType rc;
        /**
         * The stat position value
         */
        final Object startValue;

        /**
         * The constructor from token cell that contains object start token
         *
         * @param tokenCell the token cell
         */
        ObjectState(Cell<TermToken> tokenCell) {
            TermToken token = tokenCell.take();
            assert token.kind() == Terms.OBJECT_START : "parser is not over object: " + token;
            name = token.objectName();
            metaObject = factory.getMetaObject(name);
            rc = factory.createInstance(metaObject, name);
            startValue = factory.setObjectStartPos(rc, metaObject, token);
            factory.objectStarted(rc, token);
        }

        @Override
        void parse(Cell<TermToken> tokenCell) {
            final TermToken current = tokenCell.peek();
            switch (current.kind()) {
                case EOF:
                    throw new IllegalStateException("Unexpected eof inside object: " + current);
                case OBJECT_END:
                    tokenCell.take();
                    if (extraObjects > 0) {
                        extraObjects--;
                    } else {
                        assert current.objectName().equals(name) : "type name does not match ";
                        factory.setObjectEndPos(rc, metaObject, startValue, current);
                        factory.objectEnded(rc, current);
                        exit(rc);
                        return;
                    }
                case VALUE:
                    factory.handleUnexpectedValue(current);
                    tokenCell.take();
                    break;
                case OBJECT_START:
                    tokenCell.take();
                    factory.handleUnexpectedObjectStart(current);
                    extraObjects++;
                    break;
                case PROPERTY_START:
                case LIST_PROPERTY_START:
                    new PropertyState(rc, metaObject, tokenCell);
                    break;
                default:
                    throw new ParserException("Unexpected token kind: " + current);
            }

        }
    }

    /**
     * The property state
     */
    class PropertyState extends State {
        /**
         * The object under construction
         */
        private final BaseObjectType object;
        /**
         * The meta object
         */
        private final MetaObjectType metaObject;
        /**
         * amount of extra properties
         */
        int extraProperties = 0;
        /**
         * The feature of the object
         */
        final FeatureType feature;
        /**
         * True if the list property
         */
        final boolean isList;
        /**
         * The holder type for the property
         */
        final HolderType holder;

        PropertyState(BaseObjectType object, MetaObjectType metaObject, Cell<TermToken> tokenCell) {
            this.object = object;
            this.metaObject = metaObject;
            final TermToken token = tokenCell.take();
            assert token.kind() == Terms.PROPERTY_START || token.kind() == Terms.LIST_PROPERTY_START :
                    "parser is not over property: " + token;
            feature = factory.getPropertyMetaObject(object, metaObject, token);
            isList = token.kind() == Terms.LIST_PROPERTY_START;
            holder = isList ? factory.startListCollection(object, metaObject, feature) : null;
        }

        @Override
        void parse(Cell<TermToken> tokenCell) {
            if (result != null) {
                if (isList) {
                    factory.addToFeature(object, feature, holder, result);
                } else {
                    factory.setToFeature(object, feature, result);
                }
                result = null;
            }
            final TermToken current = tokenCell.peek();
            switch (current.kind()) {
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
                    factory.handleUnexpectedPropertyStart(current);
                    extraProperties++;
                    tokenCell.take();
                    break;
                case OBJECT_START: {
                    if (factory.isIgnorable(current, current.objectName())) {
                        new SkipObjectState();
                    } else {
                        new ObjectState(tokenCell);
                    }
                    break;
                }
                case VALUE: {
                    final Token value = current.token().token();
                    if (isList) {
                        factory.addValueToFeature(object, feature, holder, value);
                    } else {
                        factory.setValueToFeature(object, feature, value);
                    }
                    tokenCell.take();
                    break;
                }
                case EOF:
                    throw new IllegalStateException("Unexpected eof inside property: " + current);
                default:
                    throw new IllegalStateException("Unexpected token inside property: " + current);
            }

        }
    }
}
