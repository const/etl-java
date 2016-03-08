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

package net.sf.etl.xml_catalog.impl.util;

import net.sf.etl.xml_catalog.util.PublicId;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * The test for public id utility class.
 */
public class PublicIdTest {
    /**
     * Public id column
     */
    private static final int DATA_PUBLIC_ID = 0;
    /**
     * URN column in test data
     */
    private static final int DATA_URN = 1;
    /**
     * The test data directly taken from RFC 3151
     */
    private final String[][] DATA = {
            {"ISO/IEC 10179:1996//DTD DSSSL Architecture//EN",
                    "urn:publicid:ISO%2FIEC+10179%3A1996:DTD+DSSSL+Architecture:EN"},
            {"ISO 8879:1986//ENTITIES Added Latin 1//EN",
                    "urn:publicid:ISO+8879%3A1986:ENTITIES+Added+Latin+1:EN"},
            {"-//OASIS//DTD DocBook XML V4.1.2//EN",
                    "urn:publicid:-:OASIS:DTD+DocBook+XML+V4.1.2:EN"},
            {"+//IDN example.org//DTD XML Bookmarks 1.0//EN//XML",
                    "urn:publicid:%2B:IDN+example.org:DTD+XML+Bookmarks+1.0:EN:XML"},
            {"-//ArborText::prod//DTD Help Document::19970708//EN",
                    "urn:publicid:-:ArborText;prod:DTD+Help+Document;19970708:EN"},
            {"foo", "urn:publicid:foo"},
            {"3+3=6", "urn:publicid:3%2B3=6"},
            {"-//Acme, Inc.//DTD Book Version 1.0", "urn:publicid:-:Acme,+Inc.:DTD+Book+Version+1.0"},
            {"-//OASIS//DTD XML Catalogs V1.1//EN", "urn:publicid:-:OASIS:DTD+XML+Catalogs+V1.1:EN"}
    };

    @Test
    public void normalizeTest() {
        // check that DATA publicids stay the same.
        for (final String[] e : DATA) {
            final String p = e[DATA_PUBLIC_ID];
            assertEquals(p, PublicId.normalize(p));
        }
        assertEquals("a b c d e f", PublicId.normalize(" a  b\nc\rd\te \t\r\nf\n"));
    }

    @Test
    public void encodeTest() {
        for (final String[] e : DATA) {
            assertEquals(e[DATA_URN], PublicId.encodeURN(e[DATA_PUBLIC_ID]));
        }
    }

    @Test
    public void decodeTest() {
        for (final String[] e : DATA) {
            assertEquals(e[DATA_PUBLIC_ID], PublicId.decodeURN(e[DATA_URN]));
        }
    }

    @Test
    public void decodeEncodeTest() {
        // check that DATA public ids stay the same.
        for (final String[] e : DATA) {
            final String v = e[DATA_PUBLIC_ID];
            assertEquals(v, PublicId.decodeURN(PublicId.encodeURN(v)));
        }
    }

    @Test
    public void encodeDecodeTest() {
        // check that DATA urns stay the same.
        for (final String[] e : DATA) {
            final String v = e[DATA_URN];
            assertEquals(v, PublicId.encodeURN(PublicId.decodeURN(v)));
        }
    }
}
