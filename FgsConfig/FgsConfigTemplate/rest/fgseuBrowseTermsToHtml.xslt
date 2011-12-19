<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
		
<!-- This xslt stylesheet presents a browseIndex page.
-->
	<xsl:output method="html" indent="yes" encoding="UTF-8"/>

	<xsl:param name="ERRORMESSAGE" select="''"/>

	<xsl:param name="TIMEUSEDMS" select="''" />
	
	<xsl:param name="uilang">en</xsl:param>
	<xsl:param name="fieldLabel">fieldLabel</xsl:param>
	<xsl:param name="moduleUri">fgseu/</xsl:param>
	<xsl:param name="indexing-xslt-uri">/data/fedora/gsearch/config/index/ProceedingsIndex/foxmlToLucene.xslt</xsl:param>
	
	<xsl:variable name="FIELDNAME" select="/resultPage/browseIndex/@fieldName"/>
	<xsl:variable name="INDEXNAME" select="/resultPage/@indexName"/>
	<xsl:variable name="STARTTERM" select="/resultPage/browseIndex/@startTerm"/>
	<xsl:variable name="TERMPAGESIZE" select="/resultPage/browseIndex/@termPageSize"/>
	<xsl:variable name="TERMTOTAL" select="/resultPage/browseIndex/@termTotal"/>
	<xsl:variable name="PAGELASTNO" select="/resultPage/browseIndex/terms/term[position()=last()]/@no"/>
	<xsl:variable name="PAGELASTTERM" select="/resultPage/browseIndex/terms/term[position()=last()]/text()"/>
	
	<xsl:variable name="indexfields" select="document($indexing-xslt-uri)" />
	<xsl:variable name="INDEXFIELD" select="$indexfields//IndexField[@IFname=$FIELDNAME]"/>
	<xsl:variable name="FACETSUBSTITUTES" select="$indexfields//FacetSubstitutes[@IFname=$FIELDNAME]"/>
	<xsl:variable name="BROWSEMINIMUM">
						<xsl:choose>
							<xsl:when test="$INDEXFIELD/@browseMinimum"><xsl:value-of select="$INDEXFIELD/@browseMinimum"/></xsl:when>
							<xsl:otherwise>1</xsl:otherwise>
						</xsl:choose>
	</xsl:variable>

	<xsl:template match="/resultPage">

		<div id="fgseuBrowseTermsDiv">
							
			<div><!-- class="fgseuRightColumnBody" -->
		
				<div class="fgseuRightColumnSubHeader">All terms in this field alphabetically</div>
			
				<p style="display:none"><xsl:value-of select="$TIMEUSEDMS" /> ms</p>
				
				<div id="browseTIMEUSEDMS" style="display:none"><xsl:value-of select="$TIMEUSEDMS" /></div>

				<xsl:apply-templates select="error" />
				
				<form method="get" action="javascript:fgseuBrowseForm(document.getElementById('fgseuBrowseForm'));" id="fgseuBrowseForm">
							<input type="hidden" name="fieldName" value="{$FIELDNAME}" />
							<input type="hidden" name="fieldLabel" value="{$fieldLabel}"/>
							&#160;<input type="submit" name="browsebutton" value="Show terms" 
								onclick="javascript:fgseuBrowseForm(document.getElementById('fgseuBrowseForm'));return false;"/>
							from&#160;<input type="text" name="startTerm" size="15" value="{$STARTTERM}"/>
<!-- 							&#160;by&#160;<input type="text" name="termPageSize" size="2" value="{$TERMPAGESIZE}"/>
 -->
 							<input type="hidden" name="termPageSize" size="2" value="{$TERMPAGESIZE}"/>
				</form>
				<xsl:if test="$TERMTOTAL = 0 and $STARTTERM and $STARTTERM != '' ">
					<p>No terms!</p>
	 			</xsl:if>
			
				<div id="fgseuBrowseTermsList">
				
					<xsl:apply-templates select="browseIndex/terms"/>
	 		
	 			</div>
	 			
				<xsl:if test="$TERMTOTAL > 0">
					<xsl:if test="$PAGELASTNO='' or $PAGELASTNO=' ' or $TERMTOTAL > $PAGELASTNO">
						<form id="fgseuNextBrowseForm">
							<input type="hidden" name="fieldName" value="{$FIELDNAME}"/>
							<input type="hidden" name="fieldLabel" value="{$fieldLabel}"/>
							<input type="hidden" name="startTerm" value="{$PAGELASTTERM}!"/>
							<input type="hidden" name="termPageSize" value="{$TERMPAGESIZE}"/>
							<input type="button" value="Next terms"
								onclick="javascript:fgseuBrowseForm(document.getElementById('fgseuNextBrowseForm'));"/>
						</form>
	 				</xsl:if>
	 			
	 			</xsl:if>
	 		</div>
	 	</div>
	</xsl:template>

	<xsl:template match="term">
		<xsl:if test="number(@fieldtermhittotal) >= $BROWSEMINIMUM">
				<!-- <xsl:value-of select="@no"/>. -->
					<xsl:variable name="TERMVALUE" select="text()"/>
					<xsl:variable name="VALUELABEL" select="$FACETSUBSTITUTES/FacetSubstitute[@indexValue=$TERMVALUE]"/>
					<xsl:variable name="TERM">
						<xsl:choose>
							<xsl:when test="$VALUELABEL"><xsl:value-of select="$VALUELABEL"/></xsl:when>
							<xsl:otherwise><xsl:value-of select="text()"/></xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
				#
				<a>
					<xsl:attribute name="href">javascript:searchFor(%22<xsl:value-of select="text()"/>%22, %22<xsl:value-of select="$FIELDNAME"/>%22)</xsl:attribute>
					<xsl:value-of select="$TERM"/>
					[<xsl:value-of select="@fieldtermhittotal"/>]
				</a>
			<br/>
		</xsl:if>
	</xsl:template>

	<xsl:template name="error">
		<p class="error">
			<xsl:value-of select="$ERRORMESSAGE" />
		</p>
	</xsl:template>

	<xsl:template match="message">
		<p class="error">
			<xsl:value-of select="./text()" />
		</p>
	</xsl:template>

	<!-- disable all default text node output -->
	<xsl:template match="text()" />
	
</xsl:stylesheet>	





				




