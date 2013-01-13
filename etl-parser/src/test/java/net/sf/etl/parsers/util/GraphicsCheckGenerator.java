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
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Generator that generates test for graphics characters basing
 */
public class GraphicsCheckGenerator {
    /**
     * The size of range that is expanded to the set of switch cases, ranges larger than that are checked using
     * range checks.
     */
    static final int EXPAND_RANGE_LIMIT = 3;


    public static void main(String args[]) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(GraphicsCheckGenerator.class.getResourceAsStream("/unicode/graphics.txt"), "UTF-8"));
        try {
            int start = -1;
            int current = -1;
            int block = -1;
            in.readLine(); // skip first line
            TreeMap<Integer, Page> pages = new TreeMap<Integer, Page>();
            // collect blocks
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String[] t = line.split("\t");
                int codepoint = Integer.parseInt(t[0].trim(), 16);
                System.out.println(">>> " + Integer.toHexString(codepoint));
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
            System.out.println("final int high = codepoint >> 8;");
            System.out.println("final int low = codepoint & 0xFF;");
            System.out.println("switch(high) {");
            for (Page page : pages.values()) {
                System.out.println("\t" + "case 0x" + Integer.toHexString(page.block) + ":");
                if (page.ranges.size() == 1 && (page.ranges.get(0).start & 0xFF) == 0 && (page.ranges.get(0).end & 0xFF) == 0xFF) {
                    System.out.println("\t\t" + "return true;");
                    continue;
                }

                boolean hasSingle = false;
                for (Range r : page.ranges) {
                    if (r.end - r.start < EXPAND_RANGE_LIMIT) {
                        if (!hasSingle) {
                            System.out.println("\t\t" + "switch(low) {");
                            hasSingle = true;
                        }
                        for (int i = r.start; i <= r.end; i++) {
                            System.out.println("\t\t\t" + "case 0x" + Integer.toHexString(i & 0xFF) + ":");
                        }
                    }
                }
                String p;
                if (hasSingle) {
                    System.out.println("\t\t\t\t" + "return true;");
                    System.out.println("\t\t\t" + "default:");
                    p = "\t\t\t\t";
                } else {
                    p = "\t\t\t";
                }
                for (Range r : page.ranges) {
                    if (r.end - r.start >= EXPAND_RANGE_LIMIT) {
                        int lowEnd = r.end & 0xFF;
                        int lowStart = r.start & 0xFF;
                        if (lowStart == 0) {
                            System.out.println(p + "if(low <= 0x" + Integer.toHexString(lowEnd) + ") return true;");
                        } else if (lowEnd == 0xFF) {
                            System.out.println(p + "if(0x" + Integer.toHexString(lowStart) + " <= low) return true;");
                        } else {
                            System.out.println(p + "if(0x" + Integer.toHexString(lowStart) +
                                    " <= low && low <= 0x" + Integer.toHexString(lowEnd) + ") return true;");
                        }
                    }
                }
                System.out.println(p + "return false;");
                if (hasSingle) {
                    System.out.println("\t\t\t" + "}");
                }
            }
            System.out.println("\t" + "default:");
            System.out.println("\t\t" + "return false;");
            System.out.println("}");
        } finally {
            in.close();
        }

    }

    private static int collectRanges(int block, int start, int end, TreeMap<Integer, Page> pages) {
        int startBlock = start >> 8;
        int endBlock = end >> 8;
        if (startBlock != endBlock) {
            int boundary = (startBlock << 8) + 0xFF;
            block = collectRanges(block, start, boundary, pages);
            return collectRanges(block, boundary + 1, end, pages);
        }
        Page page;
        if (block != startBlock) {
            block = startBlock;
            page = new Page(block);
            pages.put(block, page);
        } else {
            page = pages.get(block);
        }
        Range range = new Range(start, end);
        System.out.println(">>> " + range);
        page.ranges.add(range);
        return block;
    }

    static class Page {
        final ArrayList<Range> ranges = new ArrayList<Range>();
        private final int block;

        public Page(int block) {
            this.block = block;
        }
    }

    static class Range {
        final int start;
        final int end;

        Range(int start, int end) {
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
