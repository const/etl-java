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

    <!-- This is a generic XSLT that syntax-highlights ETL code -->
    <xsl:output method="html" xmlns=""/>
    <xsl:variable name="endLineNum">:</xsl:variable>
    <xsl:template match="p:source">
        <html>
            <head>
                <style>
                    .etl_definition {font-style: italic;}
                    .etl_docs {color: #003f7f;}
                    .etl_comment {color: green; font-style: italic;}
                    .etl_keyword {font-weight: bold; }
                    .etl_modifier {font-weight: bold; color: purple;}
                    .etl_literal {color: blue;}
                    .etl_literal2 {color: #006f6f;}
                    .etl_linenum {color: #8f8f8f;}
                    .etl_error {color: red;}
                    .etl_error_message {color: #7f0000;}
                </style>
            </head>
            <body>
                <pre>
                    <span class="etl_source">
                        <span class="etl_linenum">1:</span>
                        <xsl:apply-templates/>
                    </span>
                </pre>
                <ul>
                    <xsl:apply-templates mode="errors"/>
                </ul>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="*">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="p:control|p:structural">
        <span class="etl_keyword">
            <xsl:apply-templates/>
        </span>
    </xsl:template>

    <xsl:template match="*[@role='UNKNOWN']">
        <span class="etl_error">
            <xsl:apply-templates/>
        </span>
    </xsl:template>

    <xsl:template match="*[@role='PRIMARY_ANY']">
        <span class="etl_literal2">
            <xsl:apply-templates/>
        </span>
    </xsl:template>

    <xsl:template match="*[@token]" priority="-1">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="p:newline">
        <xsl:value-of select="./text()"/>
        <xsl:variable name="line1">
            <xsl:number level="any" count="p:newline"/>
        </xsl:variable>
        <xsl:variable name="line" select="$line1+1"/>
        <span class="etl_linenum">
            <xsl:value-of select="substring('       ',1,6 - string-length($line))"/><xsl:value-of select="$line"/><xsl:value-of
                select="$endLineNum"/>
        </span>
    </xsl:template>

    <xsl:template match="p:value[@token='DOC_COMMENT']">
        <span class="etl_docs">
            <xsl:apply-templates/>
        </span>
    </xsl:template>

    <xsl:template match="p:ignorable[@token='LINE_COMMENT'] |
                       p:ignorable[@token='BLOCK_COMMENT']">
        <span class="etl_comment">
            <xsl:apply-templates/>
        </span>
    </xsl:template>


    <xsl:template match="p:value[@role='MODIFIER']">
        <span class="etl_modifier">
            <xsl:apply-templates/>
        </span>
    </xsl:template>

    <xsl:template match="p:value[@token='STRING'] |
                       p:value[@token='MULTILINE_STRING'] |
                       p:value[@token='PREFIXED_STRING'] |
                       p:value[@token='PREFIXED_MULTILINE_STRING'] |
                       p:value[@token='INTEGER'] |
                       p:value[@token='INTEGER_WITH_SUFFIX'] |
                       p:value[@token='INTEGER'] |
                       p:value[@token='FLOAT'] |
                       p:value[@token='FLOAT_WITH_SUFFIX']">
        <span class="etl_literal">
            <xsl:apply-templates/>
        </span>
    </xsl:template>

    <xsl:template match="p:value[@role='KEYWORD']">
        <span class="etl_keyword">
            <xsl:value-of select="./text()"/>
        </span>
    </xsl:template>

    <xsl:template match="*" mode="errors">
        <xsl:apply-templates mode="errors"/>
    </xsl:template>

    <xsl:template match="p:error" mode="errors">
        <li class="etl_error_message">
            <xsl:value-of select="@kind"/>
            <xsl:text>: </xsl:text>
            <xsl:value-of select="@shortLocation"/>
            <xsl:text>: </xsl:text>
            <xsl:value-of select="@message"/>
        </li>
    </xsl:template>
    <xsl:template match="text()" mode="errors">
    </xsl:template>
</xsl:stylesheet>
