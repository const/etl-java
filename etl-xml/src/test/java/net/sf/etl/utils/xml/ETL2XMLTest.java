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
package net.sf.etl.utils.xml;

import net.sf.etl.utils.ConverterTestBase;
import net.sf.etl.utils.ETLProcessor;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The smoke test for ETL2XML.
 */
public class ETL2XMLTest extends ConverterTestBase {

    @Test
    public void smokeTestHtml() {
        final String moduleDir = getModuleBaseDirectory();
        ETL2XML.main(new String[]{
                "-f", "html",
                "-i", moduleDir + "/../etl-parser/src/main/resources/META-INF/etl/grammars/ETL/*.g.etl",
                "--output", moduleDir + "/target/temp/output/formatted-grammars/*.g.etl.html",
        });
        var dir = new File(moduleDir + "/target/temp/output/formatted-grammars");
        assertTrue(dir.exists());
        var list = dir.list();
        assertNotNull(list);
        assertTrue(list.length > 0);
    }

    @Test
    public void smokeTestHtmlWithCatalog() {
        final String moduleDir = getModuleBaseDirectory();
        ETL2XML.main(new String[]{
                "-f", "html",
                "-c", moduleDir + "/../etl-parser/target/test-classes",
                "-i", moduleDir + "/../etl-parser/src/test/resources/net/sf/etl/parsers/term/imports/*.i.etl",
                "--output", moduleDir + "/target/temp/output/beans/*.i.b.html",
        });
    }


    @Test
    public void smokeTestXmi() {
        final String moduleDir = getModuleBaseDirectory();
        ETL2XML.main(new String[]{
                "-f", "xmi",
                "-i", moduleDir + "/../etl-parser/src/main/resources/META-INF/etl/grammars/ETL/*.g.etl",
                "--output", moduleDir + "/target/temp/output/formatted-grammars/*.g.etl.xmi",
        });
    }

    @Test
    public void smokeTestPresentation() {
        final String moduleDir = getModuleBaseDirectory();
        ETL2XML.main(new String[]{
                "-f", "presentation",
                "-i", moduleDir + "/../etl-parser/src/main/resources/META-INF/etl/grammars/ETL/*.g.etl",
                "--output", moduleDir + "/target/temp/output/formatted-grammars/*.g.etl.p.xml",
        });
    }

    @Test
    public void smokeTestTree() {
        final String moduleDir = getModuleBaseDirectory();
        ETLProcessor.main(new String[]{
                "xml",
                "--file-encoding", "ASCII",
                "--tab-size", "4",
                "-f", "tree",
                "-i", moduleDir + "/../etl-parser/src/main/resources/META-INF/etl/grammars/ETL/*.g.etl",
                "--output", moduleDir + "/target/temp/output/formatted-grammars/*.g.etl.t.xml",
        });
    }
}
