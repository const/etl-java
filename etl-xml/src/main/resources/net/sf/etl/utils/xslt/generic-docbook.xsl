<!--
  ~ Reference ETL Parser for Java
  ~ Copyright (c) 2000-2022 Konstantin Plotnikov
  ~
  ~ Permission is hereby granted, free of charge, to any person
  ~ obtaining a copy of this software and associated documentation
  ~ files (the "Software"), to deal in the Software without restriction,
  ~ including without limitation the rights to use, copy, modify, merge,
  ~ publish, distribute, sublicense, and/or sell copies of the Software,
  ~ and to permit persons to whom the Software is furnished to do so,
  ~ subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be
  ~ included in all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  ~ EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  ~ MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  ~ NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
  ~ BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  ~ ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  ~ CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  ~ SOFTWARE.
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:p="http://etl.sf.net/2006/etl/presentation"
                version="1.0">
    <xsl:import href="generic.xsl"/>

    <!-- This is a generic XSLT that syntax-highlights ETL code -->
    <xsl:template match="p:source">
        <html>
            <pre>
                <span class="etl_source">
                    <span class="etl_linenum">1:</span>
                    <xsl:apply-templates/>
                </span>
            </pre>
            <ul>
                <xsl:apply-templates mode="errors"/>
            </ul>
        </html>
    </xsl:template>
</xsl:stylesheet>
