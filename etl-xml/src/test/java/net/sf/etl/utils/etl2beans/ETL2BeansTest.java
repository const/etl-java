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

package net.sf.etl.utils.etl2beans;

import net.sf.etl.utils.ConverterTestBase;
import net.sf.etl.utils.ETLProcessor;
import org.junit.Test;

/**
 * The smoke test for ETL2Beans
 */
public class ETL2BeansTest extends ConverterTestBase {
    /**
     * Test simple run.
     */
    @Test
    public void testSimple() {
        final String moduleDir = getModuleBaseDirectory();
        ETLProcessor.main(new String[]{
                "beans",
                "-C", moduleDir + "/../etl-parser/src/test/resources/META-INF/xml/catalog.xml",
                "-m", "http://etl.sf.net/2006/samples/imports/Expression/0.1=net.sf.etl.parsers.term.beans",
                "-m", "http://etl.sf.net/2006/samples/imports/Main/0.1=net.sf.etl.parsers.term.beans",
                "-c", moduleDir + "/../etl-parser/target/test-classes",
                "-i", moduleDir + "/../etl-parser/src/test/resources/net/sf/etl/parsers/term/imports/*.i.etl",
                "--output", moduleDir + "/target/temp/output/beans/*.i.b.xml",
        });
    }
}
