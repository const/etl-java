<!--
  ~ Reference ETL Parser for Java
  ~ Copyright (c) 2000-2013 Constantine A Plotnikov
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

    <!-- This is a generic XSLT that copies text as is and appends list of errors at the end as comments -->
    <xsl:output method="text" xmlns=""/>

    <xsl:template match="p:source">
        <xsl:apply-templates/>
        <xsl:apply-templates mode="errors"/>
    </xsl:template>

    <xsl:template match="*" priority="-1">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="*[@token]" priority="-1">
        <xsl:value-of select="./text()"/>
    </xsl:template>

    <xsl:template match="*" mode="errors">
        <xsl:apply-templates mode="errors"/>
    </xsl:template>

    <xsl:template match="p:error" mode="errors">
        <xsl:text>// !! </xsl:text>
        <xsl:value-of select="@kind"/>
        <xsl:text>: </xsl:text>
        <xsl:value-of select="@shortLocation"/>
        <xsl:text>: </xsl:text>
        <xsl:value-of select="@message"/>
    </xsl:template>

    <xsl:template match="text()" mode="errors"/>

</xsl:stylesheet>
