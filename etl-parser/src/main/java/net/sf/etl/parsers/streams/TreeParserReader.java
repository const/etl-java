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

package net.sf.etl.parsers.streams;

import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.tree.ObjectFactory;
import net.sf.etl.parsers.event.tree.ObjectFactoryTreeParser;
import net.sf.etl.parsers.event.tree.TreeParser;

/**
 * The tree reader
 */
public class TreeParserReader<Element> extends AbstractReaderImpl<Element> {
    /**
     * The reader
     */
    private final TermParserReader termParserReader;
    /**
     * The tree parser
     */
    private final TreeParser<Element> treeParser;
    /**
     * The object factory
     */
    private final ObjectFactory<Element, ?, ?, ?> objectFactory;
    /**
     * The underlying lexer
     */
    final Cell<TermToken> tokenCell = new Cell<TermToken>();

    /**
     * The constructor
     *
     * @param termParserReader the term parser reader
     * @param objectFactory    the object factory
     */
    public TreeParserReader(TermParserReader termParserReader, ObjectFactory<Element, ?, ?, ?> objectFactory) {
        this.termParserReader = termParserReader;
        this.objectFactory = objectFactory;
        this.treeParser = ObjectFactoryTreeParser.make(objectFactory);
        objectFactory.setSystemId(termParserReader.getSystemId());
    }

    /**
     * @return the object factory
     */
    public ObjectFactory<Element, ?, ?, ?> getObjectFactory() {
        return objectFactory;
    }

    /**
     * Do advancing using underlying resources
     *
     * @return if moved to next token
     */
    @Override
    protected boolean doAdvance() {
        while (true) {
            ParserState state = treeParser.parse(tokenCell);
            switch (state) {
                case INPUT_NEEDED:
                    if (termParserReader.advance()) {
                        tokenCell.put(termParserReader.current());
                    } else {
                        throw new ParserException("Advancing should be possible before EOF: " + termParserReader);
                    }
                    break;
                case EOF:
                    return false;
                case OUTPUT_AVAILABLE:
                    current = treeParser.read();
                    return true;
                default:
                    throw new ParserException("Invalid state from the phrase parser: " + state);
            }
        }
    }


    @Override
    protected void doClose() throws Exception {
        termParserReader.close();
    }

    @Override
    public String getSystemId() {
        return termParserReader.getSystemId();
    }
}
