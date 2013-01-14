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

package net.sf.etl.parsers.event.unstable.model.doctype;

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Document type object. Note that this object is just a place holder for the information,
 * and some fields could be missing from it.
 */
public class Doctype {
    /**
     * The list of errors
     */
    public final List<ErrorInfo> errors = new ArrayList<ErrorInfo>();
    /**
     * The location where document type is defined
     */
    public SourceLocation location;
    /**
     * The type token
     */
    public Token type;
    /**
     * The system id string token
     */
    public Token systemId;
    /**
     * The public id token
     */
    public Token publicId;
    /**
     * The context token
     */
    public Token context;
}
