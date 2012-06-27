<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
		
<!-- This xslt stylesheet presents an end-user search page,
     including hits, if any.
-->
	<xsl:output method="html" indent="yes" encoding="UTF-8"/>

	<xsl:param name="ERRORMESSAGE" select="''"/>	
	<xsl:param name="FGSUSERNAME" select="''"/>
	<xsl:param name="TIMEUSEDMS" select="''"/>
	
	<xsl:param name="BASEROOT">http://localhost:8080/fedoragsearch</xsl:param>
	
	<xsl:variable name="indexfields" select="document('fieldsUnique.xml')" />

	<xsl:template match="/resultPage">
		<html>
			<head>
				<title>Customizable End-user Search Client for Fedora Generic Search Service</title>
				<link rel="stylesheet" type="text/css" href="css/fgseu.css"/>
				<script type="text/javascript" src="js/fgseu.js"></script>
				<script type="text/javascript" src="js/jquery-1.6.4.min.js"></script>
			</head>
			<body>
			  <div id="all">
				<div id="header">
					<a href="" id="logo"></a>
					<div id="title">
						<h1>Customizable End-user Search Client for Fedora Generic Search Service</h1>
					</div>
					<div id="currentUser" >
						<h4>You are logged in as <xsl:value-of select="$FGSUSERNAME"/></h4>
					</div>
				</div>
				<p/>
        		<xsl:apply-templates select="error"/>
				<xsl:call-template name="mainArea"/>
				<div id="footer">
   					<div id="copyright">
						Copyright &#xA9; 2011, 2012 Technical University of Denmark
					</div>
				</div>
			  </div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template name="mainArea">

		<div id="fgseuMainArea">
			<script>baseRoot='<xsl:value-of select="$BASEROOT"/>';query=' ';sentQuery=' ';facetsAvailable=true;latestFacet='foxml.all.text';latestFacetLabel='';termsShownField='';</script>
			<div id="fgseuLeftColumnArea">
			
				<div id="fgseuFormHeaderArea">
					<!-- A custom header can be placed here -->
				</div>

				<div id="fgseuFormDiv">
					<div id="fgseuFormArea">
						<form id="osform" action="javascript:fgseuSearch(document.getElementById('osform'));">
							<table id="fgseuInputControlArea">
								<tr>
									<td colspan="2">
							  			<table class="standardTable">
											<xsl:call-template name="inputFields"/>
										</table>
									</td>
								</tr>
								<tr>
									<td class="firstCellInColumn_1">
										<b>&#160;</b>
									</td>
									<td>
							  			<table class="standardTable">
											<tr>
												<td class="firstCellInColumn_2">
													<xsl:text>&#160;&#160;</xsl:text> 
												 		<input type="button" id="openfullquerybutton" value="Show query" style=""
														onclick="javascript:fgseuOpenFullQuery(document.getElementById('osform'));"/>
														<input type="button" id="closefullquerybutton" value="Hide query" style="display:none;"
														onclick="javascript:fgseuCloseFullQuery();"/>
														
												<xsl:text>&#160;&#160;</xsl:text>
													<input type="button" id="showurlbutton" value="Show the search URL" style=""
													onclick="javascript:showSearchUrl();"/>
													<input type="button" id="hideurlbutton" value="Hide the search URL" style="display:none;"
													onclick="javascript:hideSearchUrl();"/>
												</td>
												<td class="standardTableCell">
													<input type="submit" name="searchbutton" value="SEARCH" title="Search on visible fields"
													onclick="javascript:fgseuSearch(document.getElementById('osform'));return false;"/>
													<input type="hidden" name="hitPageStart" value="1" />
												</td>
											</tr>
										</table>
									</td>
								</tr>
								<tr>
									<td class="standardTableCell">
										<b>&#160;</b>
									</td>
									<td class="standardTableCell">
										sort:
												<select name="sortFields">
													<option value="PID,SCORE">
														relevance
													</option>
													<option value="TITLE_UNTOK,STRING">
														title
													</option>
													<option value="AUTHOR_UNTOK,STRING;TITLE_UNTOK,STRING">
														author
													</option>
												</select>
												<xsl:text>&#160;&#160;&#160;</xsl:text>
												<!-- The format choice is not currently in use
										format:
												<select name="format">
													<option value="short">
														short
													</option>
													<option value="long">
														long
													</option>
												</select>
												<xsl:text>&#160;&#160;&#160;</xsl:text>
												 -->
										hits per page:
												<input type="text"
													name="hitPageSize" size="2" value="10" />
									</td>
								</tr>
								<tr id="fullQueryConstruction" style="display:none;">
									<td class="standardTableCell">
										<b>&#160;</b>
									</td>
									<td class="standardTableCell">
										<p>You may use <a href="http://lucene.apache.org/java/3_5_0/queryparsersyntax.html" target="_blank">
										the full query syntax</a>.
										</p>
												<input type="button" name="refreshfromfacetsbutton" value="Refresh from fields" 
													onclick="javascript:fgseuRefreshFullQuery(document.getElementById('osform'));"/>
												<xsl:text>&#160;&#160;</xsl:text>
												<input type="submit" name="submitfullquerybutton" value="Submit the query" 
													onclick="javascript:fgseuQuery(document.getElementById('osform'), document.getElementById('fullQueryConstructionArea').value);return false;"/>
												<br/>
													<textarea id="fullQueryConstructionArea" rows="8" cols="70">&#160;</textarea>
									</td>
								</tr>
								<tr id="copySearchUrl" style="display:none;">
									<td class="standardTableCell">
										<b>&#160;</b>
									</td>
									<td class="standardTableCell">
										<p>You may copy the search URL to other programs:
										</p>
													<textarea id="urlTextArea" rows="4" cols="70">&#160;</textarea>
									</td>
								</tr>
							</table>
						</form>
					</div>
				</div>
							
				<div id="fgseuResultSetArea">
					<span>&#160;</span>
				</div>

			</div>
							
			<div id="fgseuRightColumnArea">
				
					<div class="fgseuRightColumnBody fgseuRightColumnHelp">&#160;Click a search field group name to open/close it</div>
					<div class="fgseuRightColumnBody fgseuRightColumnHelp">&#160;Click a search field name to open/close it</div>
					<div class="fgseuRightColumnBody fgseuRightColumnHelp">&#160;Click a term to use it in search</div>
				
					<div class="fgseuRightColumnBody">
					<xsl:for-each select="$indexfields//IFname">
						<xsl:variable name="currentIFName" select="text()"/>
						<xsl:variable name="currentGroup" select="substring-before ($currentIFName, '.')"/>
						<xsl:variable name="currentDisplayName"><xsl:value-of select="../DisplayName/text()"/></xsl:variable>
						<xsl:choose>
							<xsl:when test="$currentIFName = 'foxml.all.text'">
								<div class="indexElementWithoutGroup">
								<xsl:call-template name="aSelectField">
									<xsl:with-param name="fieldName" select="$currentIFName" />
									<xsl:with-param name="fieldLabel" select="$currentDisplayName" />
								</xsl:call-template>
									<div id="fgseuRightColumnArea2{$currentIFName}" class="browseBox" style="display:none;">
										<span>&#160;</span>
									</div>
									<div id="fgseuRightColumnArea1{$currentIFName}" class="browseBox" style="display:none;">
										<span>&#160;</span>
									</div>
								</div>
							</xsl:when>
							<xsl:when test="position() != 1">
								<xsl:variable name="precedingIFName" select="preceding::IFname[1]/text()"/><!-- $indexfields//preceding-sibling::IFname[1]/text() -->
								<xsl:variable name="precedingGroup" select="substring-before ($precedingIFName, '.')"/>
								<xsl:choose>
									<xsl:when test="$precedingGroup = $currentGroup">
										<div class="{$currentGroup} indexElement" style="display:none;">
											<xsl:call-template name="aSelectField">
												<xsl:with-param name="fieldName" select="$currentIFName" />
												<xsl:with-param name="fieldLabel" select="$currentDisplayName" />
											</xsl:call-template>
											<div id="fgseuRightColumnArea2{$currentIFName}" class="browseBox" style="display:none;">
												<span>&#160;</span>
											</div>
											<div id="fgseuRightColumnArea1{$currentIFName}" class="browseBox" style="display:none;">
												<span>&#160;</span>
											</div>
										</div>
									</xsl:when>
									<xsl:otherwise>
										<div id="foxmlGroup_{$currentGroup}" class="indexGroup">
											<a onclick="javascript:$('.{$currentGroup}').toggle();$('.right_{$currentGroup}').toggle();$('.down_{$currentGroup}').toggle();"><img class="right_{$currentGroup}" src="images/right_red.gif"/><img class="down_{$currentGroup}" style="display:none;" src="images/down_red.gif"/><span><xsl:value-of select="$currentGroup"/></span></a>
										</div>
										<div class="{$currentGroup} indexElement" style="display:none;">
											<xsl:call-template name="aSelectField">
												<xsl:with-param name="fieldName" select="$currentIFName" />
												<xsl:with-param name="fieldLabel" select="$currentDisplayName" />
											</xsl:call-template>
											<div id="fgseuRightColumnArea2{$currentIFName}" class="browseBox" style="display:none;" >
												<span>&#160;</span>
											</div>
											<div id="fgseuRightColumnArea1{$currentIFName}" class="browseBox" style="display:none;">
												<span>&#160;</span>
											</div>
										</div>
									</xsl:otherwise>
								</xsl:choose> 
							</xsl:when>
							<xsl:otherwise>
								<!-- This is the first item -->
								<div id="foxmlGroup_{$currentGroup}" class="indexGroup">
									<a onclick="javascript:$('.{$currentGroup}').toggle();$('.right_{$currentGroup}').toggle();$('.down_{$currentGroup}').toggle();"><img class="right_{$currentGroup}" src="images/right_red.gif"/><img class="down_{$currentGroup}" style="display:none;" src="images/down_red.gif"/><span><xsl:value-of select="$currentGroup"/></span></a>
								</div>
								<div class="{$currentGroup} indexElement" style="display:none;">
									<xsl:call-template name="aSelectField">
										<xsl:with-param name="fieldName" select="$currentIFName" />
										<xsl:with-param name="fieldLabel" select="$currentDisplayName" />
									</xsl:call-template>
									<div id="fgseuRightColumnArea2{$currentIFName}" class="browseBox" style="display:none;">
										<span>&#160;</span>
									</div>
									<div id="fgseuRightColumnArea1{$currentIFName}" class="browseBox" style="display:none;">
										<span>&#160;</span>
									</div>
								</div>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</div>
			</div>
		</div>
	</xsl:template>

	<xsl:template name="inputFields">
		<tr id="fgseuInputFields"> 
			<td id="fgseuSearchArea">
				<table id="fgseuInputArea" class="standardTable">
					<xsl:for-each select="$indexfields//IFname">
						<xsl:variable name="initValue">
							<xsl:choose>
								<xsl:when test="../DisplayName/@initValue"><xsl:value-of select="@initValue" /></xsl:when>
								<xsl:when test="../DisplayName/@uiUse = 'ORfacet'">---select from list</xsl:when>
								<xsl:when test="../DisplayName/@uiUse = 'ANDfacet'">---enter search terms or select from lists</xsl:when>
								<xsl:otherwise>---enter search terms or select from lists---</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						
						<xsl:choose>
							<xsl:when test="../DisplayName/@browseFrom"> 
								<xsl:call-template name="fieldCriterium">
									<xsl:with-param name="uiUse" select="../DisplayName/@uiUse" />
									<xsl:with-param name="initValue" select="$initValue" />
									<xsl:with-param name="fieldName" select="text()" />
									<xsl:with-param name="fieldLabel" select="../DisplayName/text()" />
									<xsl:with-param name="browseFrom" select="../DisplayName/@browseFrom" />
								</xsl:call-template>
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template name="fieldCriterium">
									<xsl:with-param name="uiUse" select="../DisplayName/@uiUse" />
									<xsl:with-param name="initValue" select="$initValue" />
									<xsl:with-param name="fieldName" select="text()" />
									<xsl:with-param name="fieldLabel" select="../DisplayName/text()" />
								</xsl:call-template>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</table>
			</td>
		</tr>
	</xsl:template>

	<xsl:template name="aSelectField">
		<xsl:param name="fieldName">fieldName</xsl:param>
		<xsl:param name="fieldLabel">fieldLabel</xsl:param>
			<a title="show this field">
				<xsl:attribute name="href">javascript:showFacet('<xsl:value-of select="$fieldLabel"/>','<xsl:value-of select="$fieldName"/>')</xsl:attribute><!-- ;$('.right_<xsl:value-of select="$fieldName"/>').toggle();$('.down_<xsl:value-of select="$fieldName"/>').toggle(); -->
				<img id="right_{$fieldName}" src="images/right_red.gif"/><img id="down_{$fieldName}" style="display:none;" src="images/down_red.gif"/>
				<xsl:value-of select="$fieldName"/>
				<xsl:if test="$fieldLabel and $fieldLabel != $fieldName">
					(<xsl:value-of select="$fieldLabel"/>)
				</xsl:if>
			</a>;
	</xsl:template>

	<xsl:template name="fieldCriterium">
		<xsl:param name="uiUse">uiUse</xsl:param>
		<xsl:param name="initValue">initValue</xsl:param>
		<xsl:param name="fieldName">fieldName</xsl:param>
		<xsl:param name="fieldLabel">fieldLabel</xsl:param>
		<xsl:param name="browseFrom">aa</xsl:param>
		<xsl:variable name="displayIfBasic2">
			<xsl:choose>
				<xsl:when test="$fieldName='foxml.all.text'"></xsl:when>
				<xsl:otherwise>display:none</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="fieldvalue"><xsl:value-of select="$initValue"/></xsl:variable>
		<div id="{$fieldName}uiUse" style="display:none"><xsl:value-of select="$uiUse" /></div>
		<div id="{$fieldName}initValue" style="display:none"><xsl:value-of select="$initValue" /></div>
		<div id="{$fieldName}browseFrom" style="display:none"><xsl:value-of select="$browseFrom" /></div>
											<tr id="{$fieldName}row" style="{$displayIfBasic2}" class="fieldRow">
												<td class="fieldRowLabel">
													<xsl:value-of select="$fieldLabel"/>&#160;
												</td>
												<td class="fieldRowInputCell">
													<input type="text" class="fieldRowInputField">
														<xsl:attribute name="id"><xsl:value-of select="$fieldName"/></xsl:attribute>
														<xsl:attribute name="name"><xsl:value-of select="$fieldName"/></xsl:attribute>
														<xsl:attribute name="value"><xsl:value-of select="$fieldvalue"/></xsl:attribute>
														<xsl:attribute name="onBlur">fieldOnBlur('<xsl:value-of select="$fieldName"/>');</xsl:attribute>
														<xsl:attribute name="onFocus">fieldOnFocus('<xsl:value-of select="$fieldName"/>');</xsl:attribute>
														<xsl:attribute name="onClick">fieldOnClick('<xsl:value-of select="$fieldName"/>');</xsl:attribute>
													</input>
													&#160;
													<xsl:choose>
														<xsl:when test="$fieldName != 'foxml.all.text'">
															<input type="button" value="Hide" onclick="javascript:hideFacet('{$fieldName}');" title="hide this field"/>
														</xsl:when>
														<xsl:otherwise>
															<input type="button" value="Clear" onclick="javascript:clearFacet('{$fieldName}');" title="clear this field"/>
														</xsl:otherwise>
													</xsl:choose>
												</td>
											</tr>
	</xsl:template>
	
	<xsl:template name="error">
		<p class="error">
				<xsl:value-of select="$ERRORMESSAGE"/>
		</p>			
	</xsl:template>
	
	<xsl:template match="message">
		<p class="message">
				<xsl:value-of select="./text()"/>
		</p>			
	</xsl:template>

  <!-- disable all default text node output -->
  <xsl:template match="text()"/>
	
</xsl:stylesheet>	
