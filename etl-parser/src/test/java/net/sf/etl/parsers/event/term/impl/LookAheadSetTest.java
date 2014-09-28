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
package net.sf.etl.parsers.event.term.impl;

import net.sf.etl.parsers.PhraseTokens;
import net.sf.etl.parsers.TokenKey;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.event.grammar.LookAheadSet;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * This class tests LookAheadSet class. Note that empty, phrase, and tokens are
 * tested independently.
 *
 * @author const
 */
public class LookAheadSetTest {
    /**
     * Test empty tokens
     */
    @Test
    public void testTestEmpty() {
        final LookAheadSet a = LookAheadSet.getWithEmpty(null);
        assertTrue(a.containsEmpty());
        final LookAheadSet b = new LookAheadSet();
        b.add(null, PhraseTokens.START_BLOCK);
        assertFalse(b.containsEmpty());
        b.addEmpty(null);
        assertTrue(b.containsEmpty());
        final LookAheadSet c = new LookAheadSet(b);
        assertTrue(c.containsEmpty());
        assertTrue(c.containsEmpty());
        assertNotNull(a.conflictsWith(b));
        assertNotNull(a.conflictsWith(c));
        assertNotNull(a.conflictsWith(c));
        b.removeEmpty();
        assertNull(a.conflictsWith(b));
    }

    /**
     * Test phrase tokens
     */
    @Test
    public void testPhraseTokens() {
        final LookAheadSet a = LookAheadSet.get(null, PhraseTokens.START_BLOCK);
        assertTrue(a.contains(PhraseTokens.START_BLOCK));
        assertFalse(a.contains(PhraseTokens.END_BLOCK));
        final LookAheadSet b = new LookAheadSet();
        b.add(null, PhraseTokens.END_BLOCK);
        assertNull(b.conflictsWith(a));
        b.add(null, PhraseTokens.START_BLOCK);
        assertNotNull(b.conflictsWith(a));
        final LookAheadSet c = new LookAheadSet(b);
        assertTrue(c.contains(PhraseTokens.START_BLOCK));
        assertTrue(c.contains(PhraseTokens.END_BLOCK));
    }

    /**
     * Test tokens
     */
    @Test
    public void testTokens() {
        final LookAheadSet a = LookAheadSet.get(null, TokenKey.string(Tokens.STRING,
                '\"'));
        final LookAheadSet b = LookAheadSet.getWithText(null, TokenKey.string(Tokens.STRING, '\"'), "\"a\"");
        final LookAheadSet c = LookAheadSet.getWithText(null, TokenKey.simple(Tokens.INTEGER), "1");
        assertNull(a.conflictsWith(b));
        assertNull(b.conflictsWith(c));
        assertNull(a.conflictsWith(c));
        final LookAheadSet d = new LookAheadSet(a);
        assertNotNull(d.conflictsWith(a));
        assertNull(d.conflictsWith(b));
        assertNull(d.conflictsWith(c));
        d.addAll(b);
        assertNotNull(d.conflictsWith(a));
        assertNotNull(d.conflictsWith(b));
        assertNull(d.conflictsWith(c));
        d.addAll(c);
        assertNotNull(d.conflictsWith(a));
        assertNotNull(d.conflictsWith(b));
        assertNotNull(d.conflictsWith(c));
    }
}
