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
package net.sf.etl.parsers.event.unstable.model.grammar;

import net.sf.etl.parsers.Token;

import java.util.List;

/**
 * The Grammar node class. This class is a part of the lightweight grammar
 * model.
 *
 * @author const
 */
public final class Grammar extends AbstractStatement {
    /**
     * the content.
     */
    private final List<GrammarMember> content = new java.util.ArrayList<>();
    /**
     * The name.
     */
    private final List<Token> name = new java.util.ArrayList<>();
    /**
     * the abstract modifier.
     */
    private Modifier abstractModifier;
    /**
     * the script modifier.
     */
    private Modifier scriptModifier;
    /**
     * the version of the grammar.
     */
    private Token version;

    /**
     * @return the content.
     */
    public List<GrammarMember> getContent() {
        return content;
    }

    /**
     * @return The name.
     */
    public List<Token> getName() {
        return name;
    }

    /**
     * @return the abstract modifier.
     */
    public Modifier getAbstractModifier() {
        return abstractModifier;
    }

    /**
     * Set modifier.
     *
     * @param abstractModifier the modifier
     */
    public void setAbstractModifier(final Modifier abstractModifier) {
        this.abstractModifier = abstractModifier;
    }

    /**
     * @return the script modifier.
     */
    public Modifier getScriptModifier() {
        return scriptModifier;
    }

    /**
     * Set modifier.
     *
     * @param scriptModifier the modifier
     */
    public void setScriptModifier(final Modifier scriptModifier) {
        this.scriptModifier = scriptModifier;
    }

    /**
     * @return the version of the grammar.
     */
    public Token getVersion() {
        return version;
    }

    /**
     * Set modifier.
     *
     * @param version the modifier
     */
    public void setVersion(final Token version) {
        this.version = version;
    }
}
