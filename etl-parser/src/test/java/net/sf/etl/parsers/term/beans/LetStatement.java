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
package net.sf.etl.parsers.term.beans;

import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.streams.beans.TokenCollector;

/**
 * Let statement
 *
 * @author const
 */
public class LetStatement extends Statement implements TokenCollector {
    /**
     * serial version id
     */
    private static final long serialVersionUID = -8827443002800538040L;
    /**
     * name of value
     */
    String name;
    /**
     * value
     */
    Expression value;
    /**
     * the text of the statement
     */
    private StringBuilder text = new StringBuilder();

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the value.
     */
    public Expression getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(Expression value) {
        this.value = value;
    }

    /**
     * @return the text of the statement
     */
    public String statementText() {
        return text.toString();
    }

    public void collect(TermToken token) {
        if (token.hasLexicalToken()) {
            text.append(token.token().token().text());
        }
    }
}
