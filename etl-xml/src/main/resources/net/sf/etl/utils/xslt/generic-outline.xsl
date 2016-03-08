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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://etl.sf.net/2006/etl/presentation" version="1.0">
    <xsl:import href="generic.xsl"/>
    <!--
     This is a generic XSLT that syntax-highlights ETL code and displays
     outline for it.
    -->
    <xsl:template match="p:source">
        <html>
            <head>
                <style xml:space="preserve">
    .etl_pre {position:relative; top:0; left:0; margin-top:0;}
    .etl_source {font-size: normal;}
    .etl_definition {font-style: italic;}
    .etl_docs {color: #003f7f;}
    .etl_comment {color: green; font-style: italic;}
    .etl_keyword {font-weight: bold; }
    .etl_modifier {font-weight: bold; color: purple;}
    .etl_literal {color: blue;}
    .etl_literal2 {color: #006f6f;}
    .etl_linenum {color: #8f8f8f;}
    .etl_error {color: red;}
    .etl_error_message {color: #7f0000; padding-top: 0.5em}
    .etl_errors {margin: 1em; padding: 1em; }
    .etl_selected {background-color: #CfffCf;}
    .etl_outline {margin-top: 0; margin-bottom: 0; font-size: small; margin-left: 0.75em; padding-left: 0.75em;}
    .etl_outline_list {margin-top: 0; marker-offset: 2em; margin-left: 0.25em; padding-left: 0.25em;}
    .etl_outline_item {margin-top: 0;}
    .etl_outline_object_name {font-weight: bold; }
    .etl_outline_object_ns { color: #7f7f7f; }
    .etl_outline_property {}
    .etl_outline_value {color: #00007f; }
    * {padding:0; margin:0;}

    body {height:100%; width:100%; overflow:hidden; left:0; top:0;}
    #left {width:35%; height:100%; position:absolute; left:0; top:0;}
    #right {width:65%;height:100%; position:absolute; left:35%; top:0;}
    .inner_div {position:relative; letf:0; top:0; height:100%; width:100%; overflow:auto};

    </style>
                <script type="text/javascript">
                    var currentSelection = null;
                    function selectNode(e, id, fromText)
                    {
                    if(!e) e = window.event;
                    if(e) {
                    if (e.cancelBubble != null) e.cancelBubble = true;
                    if (e.stopPropagation) e.stopPropagation();
                    if (e.preventDefault) e.preventDefault();
                    if (window.event) e.returnValue = false;
                    if (e.cancel != null) e.cancel = true;
                    }
                    if(currentSelection != null) {
                    document.getElementById(currentSelection).className = '';
                    document.getElementById(currentSelection+'_text').className = '';
                    }
                    if(currentSelection != id) {
                    currentSelection = id;
                    var ast = document.getElementById(currentSelection);
                    var text = document.getElementById(currentSelection+'_text');
                    ast.className = 'etl_selected';
                    text.className = 'etl_selected';
                    (fromText?ast:text).scrollIntoView();
                    document.getElementById("left").scrollIntoView();
                    } else {
                    currentSelection = null;
                    }
                    return false;
                    }
                </script>

            </head>
            <body>
                <div id="left">
                    <div class="inner_div" id="left_inner">
                        <ul class="etl_outline">
                            <xsl:apply-templates mode="outline"/>
                        </ul>
                    </div>
                </div>
                <div id="right">
                    <div class="inner_div" id="right_inner">
                        <pre xml:space="preserve" class="etl_pre"><span class="etl_source"><span
        class="etl_linenum">     1: </span><xsl:apply-templates
        /></span></pre>
                        <ul class="etl_errors">
                            <xsl:apply-templates mode="errors"/>
                        </ul>
                    </div>
                </div>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="*[@type='object']|*[@type='property']|*[@type='list-property']">
        <span id="{generate-id()}_text" onclick="selectNode(event, '{generate-id()}', true)">
            <xsl:apply-templates/>
        </span>
    </xsl:template>
    <xsl:template match="*[@type='object']" mode="outline">
        <li class="etl_outline_item" id="{generate-id()}" onclick="selectNode(event, '{generate-id()}', false)">
            <nobr>
                <span class="etl_outline_object_name">
                    <xsl:value-of select="local-name(.)"/>
                </span>
                <span class="etl_outline_object_ns">
                    <xsl:text>{</xsl:text>
                    <xsl:value-of select="namespace-uri(.)"/>
                    <xsl:text>}</xsl:text>
                </span>
                <xsl:text>:</xsl:text>
            </nobr>
            <ul class="etl_outline_list">
                <xsl:apply-templates mode="outline"/>
            </ul>
        </li>
    </xsl:template>
    <xsl:template match="*[@type='property']|*[@type='list-property']" mode="outline">
        <xsl:choose>
            <xsl:when test="count(.//*[@type='object']) &gt; 0 or count(.//p:value) &gt; 1">
                <li class="etl_outline_item" id="{generate-id()}" onclick="selectNode(event, '{generate-id()}', false)">
                    <span class="etl_outline_property">
                        <xsl:value-of select="name(.)"/>
                        <xsl:text>:</xsl:text>
                    </span>
                    <ul class="etl_outline_list">
                        <xsl:apply-templates mode="outline"/>
                    </ul>
                </li>
            </xsl:when>
            <xsl:when test="count(.//p:value) = 1">
                <li class="etl_outline_item" id="{generate-id()}" onclick="selectNode(event, '{generate-id()}', false)">
                    <span class="etl_outline_property">
                        <xsl:value-of select="name(.)"/>
                        <xsl:text>: </xsl:text>
                    </span>
                    <span class="etl_outline_value">
                        <xsl:value-of select=".//p:value/text()"/>
                    </span>
                </li>
            </xsl:when>
            <xsl:otherwise/>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="p:value" mode="outline" priority="1">
        <li class="etl_outline_item" id="{generate-id()}">
            <span class="etl_outline_value">
                <xsl:value-of select="./text()"/>
            </span>
        </li>
    </xsl:template>
    <xsl:template match="*[@token]" mode="outline">
    </xsl:template>
    <xsl:template match="*" mode="outline">
        <xsl:apply-templates mode="outline"/>
    </xsl:template>
</xsl:stylesheet>