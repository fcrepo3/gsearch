<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
		
	<xsl:output method="html" indent="yes" encoding="UTF-8"/>

	<xsl:param name="ERRORMESSAGE" select="''"/>

	<xsl:param name="TIMEUSEDMS" select="''" />
	
	<xsl:param name="uilang">en</xsl:param>
	<xsl:param name="fieldLabel">fieldLabel</xsl:param>
	<xsl:param name="FACETLIMIT">5</xsl:param>
		
	<xsl:variable name="FIELDNAME" select="/response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst/@name"/>

	<xsl:variable name="HITTOTAL" select="/response/result/@numFound"/>
	<xsl:variable name="HITPAGESIZE" select="/response/result/@size"/>

	<xsl:template match="/response">

		<div id="fgseuFacetTermsDiv">
		
			<div class="fgseuRightColumnBody">
		
			<div class="fgseuRightColumnSubHeader"><a onclick="javascript:toggleCollapsibleBox('{$FIELDNAME}','Facet');">Most frequent terms in result set<img src="images/minus.png" class="iconFacet_{$FIELDNAME} collapsibleIcon"/><img src="images/plus.png" class="iconFacet_{$FIELDNAME} collapsibleIcon" style="display:none"/></a></div>
			
			<p style="display:none"><xsl:value-of select="$TIMEUSEDMS" /> ms</p>
				
		<div id="facetsTIMEUSEDMS" style="display:none"><xsl:value-of select="$TIMEUSEDMS" /></div>
	 				
			<p class="fgseuRightColumnHelp" style="display:none">Click to use/remove term in search</p>

		<div id="fgseuFacetTermsList" class="collapsibleFacet_{$FIELDNAME}">
		
		<xsl:if test="not(lst[@name='facet_counts']/lst[@name='facet_fields']/lst/int[@name])">
			<p>No facets found!</p>
		</xsl:if>

		<xsl:for-each select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst/int[@name]">
				<xsl:choose>
					<xsl:when test="position() > 5">
					<div class="moreFacet_{$FIELDNAME}" style="display:none">
						#
						<a>
							<xsl:attribute name="href">javascript:searchFor(%22<xsl:value-of select="@name"/>%22, %22<xsl:value-of select="$FIELDNAME"/>%22)</xsl:attribute>
							<xsl:value-of select="@name"/>
							[<xsl:value-of select="text()"/>]
						</a>
						<br/>
					</div>
					</xsl:when>
					<xsl:otherwise>
					#
					<a>
						<xsl:attribute name="href">javascript:searchFor(%22<xsl:value-of select="@name"/>%22, %22<xsl:value-of select="$FIELDNAME"/>%22)</xsl:attribute>
						<xsl:value-of select="@name"/>
						[<xsl:value-of select="text()"/>]
					</a>
					<br/>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:if test="position() > 5 and position() = last()">
					<input type="button" id="moreFacets_{$FIELDNAME}" value="More terms"
						onclick="javascript:toggleMoreFacetTerms('{$FIELDNAME}');"/>
					<input type="button" id="lessFacets_{$FIELDNAME}" value="Less terms"
						onclick="javascript:toggleMoreFacetTerms('{$FIELDNAME}');" style="display:none"/>
				</xsl:if>
		</xsl:for-each>
		
		</div>
		
		</div>
		
		<xsl:for-each select="lst[@name='facet_counts']/str[@name='exception']">
			<div class="error">
				<xsl:copy-of select="." />
			</div>
		</xsl:for-each>
		
		</div>
		
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
