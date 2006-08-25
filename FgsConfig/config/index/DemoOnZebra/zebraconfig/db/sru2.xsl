<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:srw="http://www.loc.gov/zing/srw/" 
                xmlns:dc="http://www.loc.gov/zing/srw/dcschema/v1.0/" 
                xmlns:zr="http://explain.z3950.org/dtd/2.0/" 
                xmlns:diag="http://www.loc.gov/zing/srw/diagnostic/" 
                version="1.0">

  <xsl:output method="html" version="1.0" encoding="UTF-8" indent="yes"/>
  <!--<xsl:include href="dc.xsl"/>--> 

  <xsl:template match="text()"/>

  <xsl:template match="/">
    <xsl:call-template name="html"/>
  </xsl:template>

  <xsl:template name="html">
    <html>
      <head>
        <title>
          <xsl:value-of select="//zr:explain/zr:databaseInfo/zr:title"/>
        </title>      
        <link href="docpath/css.css" rel="stylesheet" 
              type="text/css" media="screen, all"/>
        <script language="JavaScript" src="docpath/cookie.js">/* */</script>
        <script language="JavaScript" src="docpath/sru2.js">/* */</script>
      </head>
      <body onLoad="sru_explain.read_session();">
        <div class="body">
          <xsl:apply-templates/>
        </div>
      </body>
    </html>
  </xsl:template>

  <!-- explain -->
  <xsl:template match="//zr:explain">
    <xsl:call-template name="dbinfo"/>
    <xsl:call-template name="cqlform"/>
    <xsl:call-template name="searchform"/>
    <xsl:call-template name="scanform"/>
  </xsl:template>

  <xsl:template name="dbinfo">
    <div class="dbinfo">
      <h1><xsl:value-of select="//zr:explain/zr:databaseInfo/zr:title"/>
      </h1>
      <h2><xsl:value-of select="//zr:explain/zr:databaseInfo/zr:description"/>
      </h2>
      <h4>
        <xsl:value-of select="//zr:explain/zr:databaseInfo/zr:author"/>
        <br/>
        <xsl:value-of select="//zr:explain/zr:databaseInfo/zr:history"/>
      </h4>
    </div>
  </xsl:template>

  <xsl:template name="cqlform">
    <div class="cqlform">
      <form name="cqlform"  method="get"> <!-- action=".." -->
         <xsl:call-template name="operatorblock"/>
         <!--
         <div class="submit">
           <input type="button" value="Construct CQL" 
                  onClick="return sru_explain.construct_CQL();"/>
         </div>
         -->
       </form>
    </div>
  </xsl:template>

  <xsl:template name="searchform">
    <div class="searchform">
      <form name="searchform"  method="get"> <!-- action=".." -->
        <input type="hidden" name="version" value="1.1"/>
        <input type="hidden" name="operation" value="searchRetrieve"/>
        <div class="query">
          <input type="text" name="query"/>
        </div>
        <div class="parameters">
          <xsl:text>startRecord: </xsl:text>
          <input type="text" name="startRecord" value="1"/>
          <xsl:text> maximumRecords: </xsl:text>
          <input type="text" name="maximumRecords" value="0"/>
          <xsl:text> recordSchema: </xsl:text>
          <select name="recordSchema">
          <xsl:for-each select="//zr:schemaInfo/zr:schema">
            <option value="{@name}">
              <xsl:value-of select="zr:title"/>
            </option>            
          </xsl:for-each>
          </select>
        </div>

        <!--<input type="hidden" name="stylesheet" value="docpath/sru2.xsl"/>--> 
        <input type="hidden" name="stylesheet" value=""/> 

        <div class="submit">
          <input type="submit" value="Send Search Request"
                 onClick="sru_explain.write_session();"/>
        </div>
      </form>
    </div>
  </xsl:template>

  <xsl:template name="scanform">
    <div class="scanform">
      <form name="scanform" method="get"> <!-- action=".." -->
        <input type="hidden" name="version" value="1.1"/>
        <input type="hidden" name="operation" value="scan"/>
        <div class="scanClause">
          <input type="text" name="scanClause"/>
        </div>
        <div class="parameters">
          <xsl:text>responsePosition: </xsl:text>
          <input type="text" name="responsePosition" value="11"/>
          <xsl:text> maximumTerms: </xsl:text>
          <input type="text" name="maximumTerms" value="21"/>
        </div>
        <!--<input type="hidden" name="stylesheet" value="docpath/sru2.xsl"/>--> 
        <input type="hidden" name="stylesheet" value=""/> 
        <div class="submit">
          <input type="submit" value="Send Scan Request"
                 onClick="sru_explain.write_session();"/>
        </div>
      </form>
    </div>
  </xsl:template>

  <xsl:template name="operatorblock">
    <xsl:call-template name="indexquery">
      <xsl:with-param name="nr" select="0"/>
    </xsl:call-template>
    <xsl:call-template name="operator"/>
    <xsl:call-template name="indexquery">
      <xsl:with-param name="nr" select="1"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="operator">
    <xsl:param name="nr"/>
    <div class="operator">
      <select name="operator{$nr}" onChange="return sru_explain.construct_CQL();">
        <option value="and">and</option>  
        <option value="or">or</option>  
        <option value="not">not</option>  
        <option value="prox">prox</option>  
        <option value="prox/distance&lt;3">prox/distance&lt;3</option>  
      </select>
    </div>
  </xsl:template>

  <xsl:template name="indexquery">
    <xsl:param name="nr"/>
    <div class="index">
      <xsl:call-template name="index">
        <xsl:with-param name="nr" select="$nr"/>
      </xsl:call-template>
      <xsl:call-template name="relation">
        <xsl:with-param name="nr" select="$nr"/>
      </xsl:call-template>
      <xsl:text> </xsl:text>
      <xsl:call-template name="term">
        <xsl:with-param name="nr" select="$nr"/>
      </xsl:call-template>
    </div>
  </xsl:template>

  <xsl:template name="index">
    <xsl:param name="nr"/>
    <select name="index{$nr}" onChange="return sru_explain.construct_CQL();">
      <xsl:for-each 
          select="//zr:indexInfo/zr:index[zr:map/zr:name/@set]">
        <xsl:variable name="index">
          <xsl:value-of select="zr:map/zr:name/@set"/>
          <xsl:text>.</xsl:text>
          <xsl:value-of select="zr:map/zr:name/text()"/>
        </xsl:variable>
        <option value="{$index}"><xsl:value-of select="$index"/></option>
      </xsl:for-each>
    </select>
  </xsl:template>

  <xsl:template name="relation">
    <xsl:param name="nr"/>
    <select name="relation{$nr}" onChange="return sru_explain.construct_CQL();">
      <xsl:variable name="defrel" 
                    select="//zr:configInfo/zr:default[@type='relation']"/>
      <option value="{$defrel}"><xsl:value-of select="$defrel"/></option>  
      <xsl:for-each select="//zr:configInfo/zr:supports[@type='relation']">
        <xsl:variable name="rel" select="text()"/>
        <option value="{$rel}"><xsl:value-of select="$rel"/></option>
      </xsl:for-each>
    </select>
  </xsl:template>

  <xsl:template name="term">
    <xsl:param name="nr"/>
    <input name="term{$nr}" type="text" 
           onBlur="return sru_explain.construct_CQL();"
           onKeyUp="return sru_explain.construct_CQL();"/>
    

  </xsl:template>

  <!-- searchRetrieveResponse -->
  <xsl:template match="//srw:searchRetrieveResponse">
    <div class="search">
      searchRetrieveResponse
    </div>
  </xsl:template>

  <!-- scanResponse -->
  <xsl:template match="//srw:scanResponse">
    <div class="scan">
      scanResponse
    </div>
  </xsl:template>

  <!-- diagnostics -->
  <xsl:template match="diag:diagnostic">
    <div class="diagnostic">
      <!-- <xsl:value-of select="diag:uri"/> -->
      <xsl:text> </xsl:text>
      <xsl:value-of select="diag:message"/>
      <xsl:text>: </xsl:text>
      <xsl:value-of select="diag:details"/>
    </div>
  </xsl:template>

</xsl:stylesheet>
