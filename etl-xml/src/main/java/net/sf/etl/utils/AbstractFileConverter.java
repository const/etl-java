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
package net.sf.etl.utils;

import net.sf.etl.parsers.DefaultTermParserConfiguration;
import net.sf.etl.parsers.TermParserConfiguration;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.streams.LexerReader;
import net.sf.etl.parsers.streams.PhraseParserReader;
import net.sf.etl.parsers.streams.TermParserReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

/**
 * Abstract class for utilities that convert one set of sources to another
 *
 * @author const
 */
public abstract class AbstractFileConverter {
    /**
     * base directory
     */
    protected String inFiles;
    /**
     * output directory
     */
    protected String outFiles;
    /**
     * Map from source to destinations. Null key means stdin. Null value means
     * stdout.
     */
    protected TreeMap<String, String> sourceFiles = new TreeMap<String, String>();
    /**
     * The configuration
     */
    protected TermParserConfiguration configuration = DefaultTermParserConfiguration.INSTANCE;

    /**
     * Parse custom option in argument list
     *
     * @param args argument list
     * @param i    offset in args array
     * @return new offset in args array
     * @throws Exception if there is a problem with handling option
     */
    protected int handleCustomOption(String[] args, int i) throws Exception {
        System.err.println("unknown option at " + i + ": " + args[i]);
        return i;
    }

    /**
     * Process content and write it on output stream
     *
     * @param outStream a output stream
     * @param p         a term parser
     * @throws Exception an exception that is thrown in case of problem
     */
    protected abstract void processContent(OutputStream outStream, TermParserReader p) throws Exception;

    /**
     * parser program arguments
     *
     * @param args arguments to parse
     * @throws Exception if there is a problem
     */
    protected void parseArgs(String[] args) throws Exception {
        // TODO handle catalog options as well
        for (int i = 0; i < args.length; i++) {
            if ("-in".equals(args[i])) {
                if (this.inFiles == null) {
                    this.inFiles = args[i + 1];
                    i++;
                } else {
                    System.err.println(args[i] + " option ignored because there is already input file " + inFiles);
                    i++;
                }
            } else if ("-out".equals(args[i])) {
                if (this.outFiles == null) {
                    this.outFiles = args[i + 1];
                    i++;
                } else {
                    System.err.println(args[i] + " option ignored because there is already output file " + outFiles);
                    i++;
                }
            } else {
                i = handleCustomOption(args, i);
            }
        }
        prepareFileMapping();

    }

    /**
     * Prepare file mapping
     *
     * @throws MalformedURLException if there file name error
     */
    protected void prepareFileMapping() throws MalformedURLException {
        if (inFiles == null) {
            if (outFiles != null && outFiles.indexOf('*') != -1) {
                System.err.println("wildcard is allowed only if there is input file specified: " + outFiles);
                System.exit(1);
            }
            sourceFiles.put(null, null);
        }
        if (outFiles == null) {
            if (inFiles != null) {
                if (inFiles.indexOf('*') != -1) {
                    System.err.println("wildcard is allowed only if there is output file specified: " + inFiles);
                    System.exit(1);
                }
                sourceFiles.put(new File(inFiles).getAbsoluteFile().toURI().toString(), null);
            }
        } else {
            if (inFiles.indexOf('*') != -1) {
                // wildcard
                final File in = new File(inFiles).getAbsoluteFile();
                final File out = new File(outFiles).getAbsoluteFile();
                final int starPos = in.getName().indexOf('*');
                if (starPos == -1) {
                    System.err.println("wildcard is specified in wrong place: " + inFiles);
                    System.exit(1);
                }
                final String inPrefix = in.getName().substring(0, starPos);
                final String inSuffix = in.getName().substring(starPos + 1);
                if (inSuffix.indexOf('*') != -1) {
                    System.err.println("Only single wildcard is allowed in input: " + inFiles);
                    System.exit(1);
                }
                final int outStarPos = in.getName().indexOf('*');
                if (outStarPos == -1) {
                    System.err.println("wildcard is required in output: " + outFiles);
                    System.exit(1);
                }
                final String outPrefix = out.getName().substring(0, outStarPos);
                final String outSuffix = out.getName()
                        .substring(outStarPos + 1);
                if (outSuffix.indexOf('*') != -1) {
                    System.err.println("Only single wildcard is allowed in output: " + outFiles);
                    System.exit(1);
                }
                final File inDir = in.getParentFile();
                final File outDir = out.getParentFile();
                //noinspection ResultOfMethodCallIgnored
                outDir.mkdirs();
                final String files[] = inDir.list();
                final int inPrefixSize = inPrefix.length();
                final int inSuffixSize = inSuffix.length();
                final int outPrefixSize = outPrefix.length();
                final int outSuffixSize = outSuffix.length();
                for (final String file : files) {
                    if (file.startsWith(inPrefix) && file.endsWith(inSuffix)) {
                        final int fileNameLength = file.length();
                        final StringBuilder outName = new StringBuilder(
                                fileNameLength - inPrefixSize - inSuffixSize + outPrefixSize + outSuffixSize);
                        outName.append(outPrefix);
                        outName.append(file.substring(inPrefixSize, fileNameLength - inSuffixSize));
                        outName.append(outSuffix);
                        sourceFiles.put(new File(inDir, file).getAbsoluteFile()
                                .toURI().toString(), new File(outDir, outName
                                .toString()).toString());
                    }
                }
            } else {
                // normal
                if (outFiles.indexOf('*') != -1) {
                    System.err.println("wildcard only allowed for output if it is in input: " + outFiles);
                    System.exit(1);
                }
                inFiles = new File(inFiles).getAbsoluteFile().toURI().toString();
                sourceFiles.put(inFiles, outFiles);

            }
        }
    }

    /**
     * Application start method
     *
     * @param args application arguments
     * @throws Exception in case of IO problem
     */
    public void start(String[] args) throws Exception {
        parseArgs(args);
        for (final Map.Entry<String, String> me : sourceFiles.entrySet()) {
            FileOutputStream fout = null;
            if (me.getValue() != null) {
                fout = new FileOutputStream(me.getValue());
            }
            final OutputStream outStream = fout != null ? fout : System.out;
            try {
                TermParserReader p = null;
                try {
                    if (me.getKey() == null) {
                        p = new TermParserReader(configuration, new PhraseParserReader(
                                new LexerReader(configuration, new InputStreamReader(System.in),
                                        "urn:system:in", TextPos.START)));
                    } else {
                        p = new TermParserReader(configuration, new URL(me.getKey()));
                    }
                    p.advance();
                    processContent(outStream, p);
                } finally {
                    if (p != null) {
                        p.close();
                    }
                }
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (final Exception ex) {
                        // ignore exception. The stream is likely has been
                        // closed.
                    }
                } else {
                    System.out.flush();
                }
            }
        }
    }
}
