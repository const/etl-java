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

package net.sf.etl.parsers.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Generator that generates test for graphics characters basing
 */
public class GraphicsCheckGenerator { // NOPMD
    /**
     * The size of range that is expanded to the set of switch cases, ranges larger than that are checked using
     * range checks.
     */
    private static final int EXPAND_RANGE_LIMIT = 3;

    private static void line(final String line) {
        System.out.println(line); // NOPMD
    }

    public static void main(final String args[]) throws Exception { // NOPMD
        final BufferedReader in = new BufferedReader(new InputStreamReader(GraphicsCheckGenerator.class.getResourceAsStream("/unicode/graphics.txt"), StandardCharsets.UTF_8));
        try {
            int start = -1;
            int current = -1;
            int block = -1;
            in.readLine(); // skip first line
            final TreeMap<Integer, Page> pages = new TreeMap<Integer, Page>();
            // collect blocks
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                final String[] t = line.split("\t");
                final int codepoint = Integer.parseInt(t[0].trim(), 16);
                line(">>> " + Integer.toHexString(codepoint));
                if (current == -1) {
                    start = current = codepoint;
                } else if (current + 1 == codepoint) {
                    current = codepoint;
                } else {
                    block = collectRanges(block, start, current, pages);
                    start = current = codepoint;
                }
            }
            collectRanges(block, start, current, pages);
            line("final int high = codepoint >> 8;");
            line("final int low = codepoint & 0xFF;");
            line("switch(high) {");
            for (final Page page : pages.values()) {
                line("\t" + "case 0x" + Integer.toHexString(page.block) + ":");
                if (page.ranges.size() == 1 && (page.ranges.get(0).start & 0xFF) == 0 && (page.ranges.get(0).end & 0xFF) == 0xFF) {
                    line("\t\t" + "return true;");
                    continue;
                }

                boolean hasSingle = false;
                for (final Range r : page.ranges) {
                    if (r.end - r.start < EXPAND_RANGE_LIMIT) {
                        if (!hasSingle) {
                            line("\t\t" + "switch(low) {");
                            hasSingle = true;
                        }
                        for (int i = r.start; i <= r.end; i++) {
                            line("\t\t\t" + "case 0x" + Integer.toHexString(i & 0xFF) + ":");
                        }
                    }
                }
                final String p;
                if (hasSingle) {
                    line("\t\t\t\t" + "return true;");
                    line("\t\t\t" + "default:");
                    p = "\t\t\t\t";
                } else {
                    p = "\t\t\t";
                }
                for (final Range r : page.ranges) {
                    if (r.end - r.start >= EXPAND_RANGE_LIMIT) {
                        final int lowEnd = r.end & 0xFF;
                        final int lowStart = r.start & 0xFF;
                        if (lowStart == 0) {
                            line(p + "if(low <= 0x" + Integer.toHexString(lowEnd) + ") return true;");
                        } else if (lowEnd == 0xFF) {
                            line(p + "if(0x" + Integer.toHexString(lowStart) + " <= low) return true;");
                        } else {
                            line(p + "if(0x" + Integer.toHexString(lowStart) +
                                    " <= low && low <= 0x" + Integer.toHexString(lowEnd) + ") return true;");
                        }
                    }
                }
                line(p + "return false;");
                if (hasSingle) {
                    line("\t\t\t" + "}");
                }
            }
            line("\t" + "default:");
            line("\t\t" + "return false;");
            line("}");
        } finally {
            in.close();
        }

    }

    private static int collectRanges(final int previousBlock, final int start, final int end, final NavigableMap<Integer, Page> pages) {
        final int startBlock = start >> 8;
        final int endBlock = end >> 8;
        int block = previousBlock;
        if (startBlock != endBlock) {
            final int boundary = (startBlock << 8) + 0xFF; // NOPMD
            block = collectRanges(block, start, boundary, pages);
            return collectRanges(block, boundary + 1, end, pages);
        }
        final Page page;
        if (block != startBlock) {
            block = startBlock;
            page = new Page(block);
            pages.put(block, page);
        } else {
            page = pages.get(block);
        }
        final Range range = new Range(start, end);
        line(">>> " + range);
        page.ranges.add(range);
        return block;
    }

    private static class Page {
        private final List<Range> ranges = new ArrayList<Range>();
        private final int block;

        private Page(final int block) {
            this.block = block;
        }
    }

    private static class Range {
        private final int start;
        private final int end;

        private Range(final int start, final int end) {
            this.end = end;
            this.start = start;
        }

        @Override
        public String toString() {
            return "Range{" +
                    "start=" + Integer.toHexString(start) +
                    ", end=" + Integer.toHexString(end) +
                    '}';
        }
    }
}
