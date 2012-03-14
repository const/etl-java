package net.sf.etl.parsers.characters;

import net.sf.etl.parsers.Tokens;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for character classifiers
 */
public class CharacterClassifiersTest {
    
    @Test
    public void testBrackets() {
        assertTrue(Brackets.isBracket('('));
        assertTrue(Brackets.isBracket('('));
        assertTrue(Brackets.isBracket(']'));
        assertTrue(Brackets.isBracket(']'));
        assertFalse(Brackets.isBracket('{'));
        assertFalse(Brackets.isBracket('}'));
    }

    @Test
    public void testGraphics() {
        for(char c : "*+$-\\/%<:=?>.!^~&|`@".toCharArray()) {
            assertTrue("char: "+c, Graphics.isGraphics(c));
        }
        assertFalse(Graphics.isGraphics(','));
        assertFalse(Graphics.isGraphics(';'));
    }
}
