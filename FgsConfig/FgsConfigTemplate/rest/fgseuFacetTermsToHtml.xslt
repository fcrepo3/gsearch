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
	<xsl:param name="FACETLIMIT">5</xsl:param>
	<xsl:param name="moduleUri">ffgs/</xsl:param>
	<xsl:param name="indexing-xslt-uri">/data/fedora/gsearch/config/index/ProceedingsIndex/foxmlToLucene.xslt</xsl:param>
		
	<xsl:variable name="FIELDNAME" select="/response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst/@name"/>
	
	<xsl:variable name="indexfields" select="document($indexing-xslt-uri)" />
	<xsl:variable name="FACETSUBSTITUTES" select="$indexfields//FacetSubstitutes[@IFname=$FIELDNAME]"/>

	<xsl:variable name="HITTOTAL" select="/response/result/@numFound"/>
	<xsl:variable name="HITPAGESIZE" select="/response/result/@size"/>

	<xsl:template match="/response">

		<div id="ffgsFacetTermsDiv">
		
			<div class="ffgsRightColumnBody">
		
			<div class="ffgsRightColumnSubHeader">Most frequent terms in result set</div>
			
			<p style="display:none"><xsl:value-of select="$TIMEUSEDMS" /> ms</p>
				
		<div id="facetsTIMEUSEDMS" style="display:none"><xsl:value-of select="$TIMEUSEDMS" /></div>
	 				
			<p class="ffgsRightColumnHelp" style="display:none">Click to use/remove term in search</p>

		<div id="ffgsFacetTermsList">

		<xsl:for-each select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst/int">

					<xsl:variable name="TERMVALUE" select="@name"/>
					<xsl:variable name="VALUELABEL" select="$FACETSUBSTITUTES/FacetSubstitute[@indexValue=$TERMVALUE]"/>
					<xsl:variable name="TERM">
						<xsl:choose>
							<xsl:when test="$VALUELABEL"><xsl:value-of select="$VALUELABEL"/></xsl:when>
							<xsl:otherwise><xsl:value-of select="$TERMVALUE"/></xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
			<xsl:if test="string-length($TERM) > 0">
				#
				<a>
					<xsl:attribute name="href">javascript:searchFor(%22<xsl:value-of select="$TERMVALUE"/>%22, %22<xsl:value-of select="$FIELDNAME"/>%22)</xsl:attribute>
					<xsl:value-of select="$TERM"/>
					[<xsl:value-of select="text()"/>]
				</a>
				<br/>
			</xsl:if>
			
		</xsl:for-each>
		
		</div>
		
		</div>
		
		<xsl:for-each select="lst[@name='facet_counts']/str[@name='exception']">
			<div id="facetError">
				<xsl:copy-of select="." />
			</div>
		</xsl:for-each>
		
		</div>
		
	</xsl:template>

	<xsl:template name="error">
		<p>
			<font color="red">
				<xsl:value-of select="$ERRORMESSAGE" />
			</font>
		</p>
	</xsl:template>

	<xsl:template match="message">
		<p>
			<font color="red">
				<xsl:value-of select="./text()" />
			</font>
		</p>
	</xsl:template>

	<!-- disable all default text node output -->
	<xsl:template match="text()" />
	
</xsl:stylesheet>	





				




