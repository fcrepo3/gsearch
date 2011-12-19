<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="html" indent="yes" encoding="UTF-8" />

	<xsl:param name="ERRORMESSAGE" select="''" />

	<xsl:param name="TIMEUSEDMS" select="''" />
	
	<xsl:param name="FEDORABASEURL">http://localhost:8080//fedora/get/</xsl:param>
	<xsl:param name="GSEARCHBASEURL">http://localhost:8080/fedoragsearch/rest</xsl:param>
	<xsl:param name="sortFields">en</xsl:param>
	<xsl:param name="format">short</xsl:param>
	<xsl:param name="defaultIndexField">ALL</xsl:param>
	<xsl:param name="moduleUri">fgseu/</xsl:param>
	<xsl:param name="indexing-xslt-uri">/data/fedora/gsearch/config/index/ProceedingsIndex/foxmlToLucene.xslt</xsl:param>

	<xsl:variable name="indexfields" select="document($indexing-xslt-uri)" />
	
		<xsl:variable name="INDEXNAME" select="/resultPage/@indexName" />
		<xsl:variable name="QUERY" select="/resultPage/gfindObjects/@query" />
		<xsl:variable name="HITPAGESTART" select="/resultPage/gfindObjects/@hitPageStart" />
		<xsl:variable name="HITPAGESIZE" select="/resultPage/gfindObjects/@hitPageSize" />
		<xsl:variable name="HITTOTAL" select="/resultPage/gfindObjects/@hitTotal" />
		<xsl:variable name="HITPAGEEND">
			<xsl:choose>
				<xsl:when
					test="$HITPAGESTART + $HITPAGESIZE - 1 > $HITTOTAL">
					<xsl:value-of select="$HITTOTAL" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of
						select="$HITPAGESTART + $HITPAGESIZE - 1" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="HITPAGENO" select="ceiling($HITPAGESTART div $HITPAGESIZE)" />
		<xsl:variable name="HITPAGENOLAST" select="ceiling($HITTOTAL div $HITPAGESIZE)" />
		<xsl:variable name="NEWHITPAGESTART" select="(($HITPAGENO - 1) * $HITPAGESIZE + 1)" />

	<xsl:template match="/resultPage">
		
			<div id="fgseuResult">
			
				<script>
					query = '<xsl:value-of select="$QUERY" />'; 
					sortFields = '<xsl:value-of select="$sortFields" />';
				</script>
				
				<div id="searchTIMEUSEDMS" style="display:none"><xsl:value-of select="$TIMEUSEDMS" /> ms</div>

											<xsl:apply-templates select="error" />
								<xsl:if test="$QUERY and $QUERY != '' ">
											<div id="fgseuHideSearchUrl" class="border-bottom" style="display:none;">
												<form action="">
													<textarea id="urltextarea" rows="4" cols="70">
														<xsl:value-of select="$GSEARCHBASEURL"/>?operation=gfindObjects&amp;restXslt=copyXml&amp;hitPageStart=1&amp;hitPageSize=10&amp;fieldMaxLength=500&amp;snippetsMax=0&amp;sortFields=<xsl:value-of select="$sortFields" />&amp;query=<xsl:value-of select="$QUERY" />
													</textarea>
												</form>
											</div>
										<xsl:call-template name="gotootherpages"/>
									<xsl:if test="$HITTOTAL > 0">
										<table id="fgseuResultSetTable">
											<xsl:apply-templates select="gfindObjects/objects" />
										</table>
										<xsl:call-template name="gotootherpages"/>
									</xsl:if>
								</xsl:if>
			</div>
	</xsl:template>

	<xsl:template name="gotootherpages">
									<table class="fgseuPageControl">
										<xsl:if test="$HITTOTAL = 0">
											<tr>
												<td>
													No hits in the result set for the specified search criteria!
												</td>
											</tr>
										</xsl:if>
										<xsl:if test="$HITTOTAL > 0">
											<tr>
											<td>
												Hit no. <b> <xsl:value-of select="$HITPAGESTART" /></b> to <b> <xsl:value-of select="$HITPAGEEND" /></b> of <b> <xsl:value-of select="$HITTOTAL" /></b> hits.
												</td>
												<td class="pageNoLabel">
													Page no. 
												</td>
												<td class="pageControlField">
												  <xsl:choose>
													<xsl:when test="$HITPAGENO > 1">
														<form id="ffgsfpform">
															<input type="hidden" name="hitPageStart" value="1" />
															<input type="hidden" name="hitPageSize" value="{$HITPAGESIZE}" />
															<input type="hidden" name="sortFields" value="{$sortFields}" />
															<input type="hidden" name="format" value="{$format}" />
															<input type="button" name="firstpagebutton" value="1" 
																onclick="javascript:ffgsSearch(document.getElementById('ffgsfpform'));"/>
														</form>
													</xsl:when>
													<xsl:otherwise></xsl:otherwise>
												  </xsl:choose>
												</td>
												<td class="pageControlField"><!-- align="center" valign="middle" -->
												  <xsl:choose>
													<xsl:when test="$HITPAGENO > 2">
														<form id="ffgsppform">
															<input type="hidden" name="hitPageStart" value="{$HITPAGESTART - $HITPAGESIZE}" />
															<input type="hidden" name="hitPageSize" value="{$HITPAGESIZE}" />
															<input type="hidden" name="sortFields" value="{$sortFields}" />
															<input type="hidden" name="format" value="{$format}" />
															<input type="button" name="previouspagebutton" value="{$HITPAGENO - 1}" 
																onclick="javascript:ffgsSearch(document.getElementById('ffgsppform'));"/>
														</form>
													</xsl:when>
													<xsl:otherwise></xsl:otherwise>
												  </xsl:choose>
												</td>
												<td class="pageControlField">
													<b><xsl:value-of select="$HITPAGENO" /></b>
												</td>
												<td class="pageControlField">
												  <xsl:choose>
													<xsl:when test="$HITPAGENOLAST > $HITPAGENO + 1">
														<form id="fgseunpform">
															<input type="hidden" name="hitPageStart" value="{$HITPAGESTART + $HITPAGESIZE}" />
															<input type="hidden" name="hitPageSize" value="{$HITPAGESIZE}" />
															<input type="hidden" name="sortFields" value="{$sortFields}" />
															<input type="hidden" name="format" value="{$format}" />
															<input type="button" name="nextpagebutton" value="{$HITPAGENO + 1}" 
																onclick="javascript:fgseuSearch(document.getElementById('fgseunpform'));"/>
														</form>
													</xsl:when>
													<xsl:otherwise></xsl:otherwise>
												  </xsl:choose>
												</td>
												<td class="pageControlField">
												  <xsl:choose>
													<xsl:when test="$HITPAGENOLAST > $HITPAGENO">
														<form id="fgseulpform">
															<input type="hidden" name="hitPageStart" value="{$HITPAGESIZE * ($HITPAGENOLAST - 1) + 1}" />
															<input type="hidden" name="hitPageSize" value="{$HITPAGESIZE}" />
															<input type="hidden" name="sortFields" value="{$sortFields}" />
															<input type="hidden" name="format" value="{$format}" />
															<input type="button" name="lastpagebutton" value="{$HITPAGENOLAST}" 
																onclick="javascript:fgseuSearch(document.getElementById('fgseulpform'));"/>
														</form>
													</xsl:when>
													<xsl:otherwise></xsl:otherwise>
												  </xsl:choose>
												</td>
											</tr>
										</xsl:if>
									</table>
	</xsl:template>

	<xsl:template match="object">
		<tr><td>
			<table id="fgseuInnerResultSetTable">
				<tr>
					<td class="firstColumn">
						<span class="hitno">
							<xsl:value-of select="@no"/>
							<xsl:value-of select="'. '"/>
						</span>
						<a>
							<xsl:variable name="PIDVALUE">
								<xsl:choose>
									<xsl:when test="@PID">
								 		<xsl:value-of select="@PID"/>
									</xsl:when>
									<xsl:otherwise>
								 		<xsl:value-of select="normalize-space(field[@name='PID'])"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<xsl:attribute name="href"><xsl:value-of select="normalize-space(field[@name='REPOSBASEURL'])"/>/objects/<xsl:value-of select="$PIDVALUE"/>
							</xsl:attribute>
							<xsl:value-of select="$PIDVALUE"/>
						</a>
						<br/>
						<span class="hitscore">
							(<xsl:value-of select="@score"/>)
						</span>
						<a>
							<xsl:variable name="PIDVALUE">
								<xsl:choose>
									<xsl:when test="@PID">
								 		<xsl:value-of select="@PID"/>
									</xsl:when>
									<xsl:otherwise><xsl:value-of select="normalize-space(field[@name='PID'])"/></xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<xsl:attribute name="href">?operation=gfindObjects&amp;query=PID:%22<xsl:value-of select="$PIDVALUE"/>%22&amp;restXslt=adminGetIndexDocumentToHtml&amp;fieldMaxLength=0&amp;snippetsMax=0</xsl:attribute>
							<xsl:value-of select="'getIndexDocument'"/>
						</a>
					</td>
					<td>
						<span class="hittitle">
							<xsl:copy-of select="field[@name='dc.title']/node()"/>
						</span>
					</td>
				</tr>
				<xsl:for-each select="field[@snippet='yes']">
				  <xsl:if test="@name!='dc.title'">
					<tr>
						<td class="firstColumn indexIdicator">
							<span class="hitfield">
								<xsl:value-of select="@name"/>
							</span>
						</td>
						<td>
							<span class="hitsnippet">
								<xsl:copy-of select="node()"/>
							</span>
						</td>
					</tr>
				  </xsl:if>
				</xsl:for-each>
			</table>
		</td></tr>
	</xsl:template>

	<xsl:template name="aMoreField">
		<xsl:param name="fieldLabel">noLabel</xsl:param>
		<xsl:param name="fieldValue"></xsl:param>
		<xsl:if test="$fieldValue">
			<br />
			<span class="ffgsResultSetCellMoreFieldName"><xsl:value-of select="$fieldLabel" /></span>
			: <span class="ffgsResultSetCellMoreFieldText"><xsl:copy-of select="$fieldValue" /></span>
		</xsl:if>
	</xsl:template>

	<xsl:template name="aMultipleField">
		<xsl:param name="fieldLabel">noLabel</xsl:param>
		<xsl:param name="fieldName"></xsl:param>
		<xsl:if test="field[@name=$fieldName]">
			<br />
			<span class="ffgsResultSetCellMoreFieldName"><xsl:value-of select="$fieldLabel" /></span> :
			<xsl:for-each select="field[@name=$fieldName]">
				<span class="ffgsResultSetCellMoreFieldText"><xsl:value-of select="' '" /><xsl:value-of select="." /></span>
				<xsl:if test="position()!=last()"><xsl:value-of select="' ;'" /></xsl:if>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>

	<xsl:template name="aMoreFieldSubstitute">
		<xsl:param name="fieldLabel">noLabel</xsl:param>
		<xsl:param name="fieldName"></xsl:param>
		<xsl:param name="fieldValue"></xsl:param>
		<xsl:variable name="fieldValueText">
			<xsl:choose>
				<xsl:when test="$fieldValue/span">
					<xsl:value-of select="$fieldValue/span" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$fieldValue" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
 		<xsl:variable name="fieldSubstituteValue">
			<xsl:copy-of select="$indexfields//FacetSubstitutes[@IFname=$fieldName]//FacetSubstitute[@indexValue=$fieldValueText]"/>
		</xsl:variable>
		<xsl:if test="$fieldSubstituteValue">
			<br />
			<span class="ffgsResultSetCellMoreFieldName"><xsl:value-of select="$fieldLabel" /></span>
			: <span class="ffgsResultSetCellMoreFieldText"><xsl:copy-of select="$fieldSubstituteValue" /></span>
		</xsl:if>
	</xsl:template>

	<xsl:template name="getUrls">
		<xsl:param name="variant"></xsl:param>
		<xsl:param name="pidValue"></xsl:param>
			<xsl:call-template name="getUrl">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PDF'" />
			</xsl:call-template>
			<xsl:call-template name="getUrl">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PDF-not-indexed'" />
			</xsl:call-template>
			<xsl:call-template name="getUrl">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PDF-poster'" />
			</xsl:call-template>
			<xsl:call-template name="getUrl">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PDF-report-poster'" />
			</xsl:call-template>
			<xsl:call-template name="getUrl">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PDF-presentation'" />
			</xsl:call-template>
			<xsl:call-template name="getUrl">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PDF-udvidet-abstract'" />
			</xsl:call-template>
			<xsl:call-template name="getUrl">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PPT-poster'" />
				<xsl:with-param name="icon" select="'ppt'" />
			</xsl:call-template>
			<xsl:call-template name="getUrl">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PPS-presentation'" />
				<xsl:with-param name="icon" select="'pps'" />
			</xsl:call-template>
	</xsl:template>

	<xsl:template name="getUrl">
		<xsl:param name="variant"></xsl:param>
		<xsl:param name="pidValue"></xsl:param>
		<xsl:param name="docType"></xsl:param>
		<xsl:param name="icon">pdf</xsl:param>
			<xsl:variable name="fieldName">FULLTEXT-<xsl:value-of select="$docType" /></xsl:variable>
							<xsl:if test="field[@name = $fieldName]/text()">
							  <xsl:choose>
							  	<xsl:when test="$variant = 'a'">
								<a class="ffgsLink" title="{$docType} link" target="_blank">
									<xsl:attribute name="href"><xsl:value-of select="$FEDORABASEURL"/>/objects/<xsl:value-of select="$pidValue" />/datastreams/<xsl:value-of select="$docType" />/content </xsl:attribute>
									<img src="{$moduleUri}images/{$icon}.png" alt="{$docType}" title="{$docType} download" />
								</a>
								<br />
							  	</xsl:when>
							  	<xsl:when test="$variant = 'b'"> Retrieved from: <xsl:value-of select="$FEDORABASEURL"/>/objects/<xsl:value-of select="$pidValue" />/datastreams/<xsl:value-of select="$docType" />/content </xsl:when>
							  </xsl:choose>
							</xsl:if>
	</xsl:template>

	<xsl:template name="getDois">
		<xsl:param name="variant"></xsl:param>
		<xsl:param name="pidValue"></xsl:param>
			<xsl:if test="field[@name = 'DOI-METADATA']/text() and $variant = 'citation'"> DOI (Metadata): <xsl:value-of select="field[@name = 'DOI-METADATA']"/> .</xsl:if>
			<xsl:call-template name="getDoi">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PDF'" />
				<xsl:with-param name="docKind" select="'Full text'" />
			</xsl:call-template>
			<xsl:call-template name="getDoi">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PDF-not-indexed'" />
				<xsl:with-param name="docKind" select="'Full text'" />
			</xsl:call-template>
			<xsl:call-template name="getDoi">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PDF-poster'" />
				<xsl:with-param name="docKind" select="'Poster'" />
			</xsl:call-template>
			<xsl:call-template name="getDoi">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PDF-report-poster'" />
				<xsl:with-param name="docKind" select="'Poster'" />
			</xsl:call-template>
			<xsl:call-template name="getDoi">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PDF-presentation'" />
				<xsl:with-param name="docKind" select="'Presentation'" />
			</xsl:call-template>
			<xsl:call-template name="getDoi">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PDF-udvidet-abstract'" />
				<xsl:with-param name="docKind" select="'Extended abstract'" />
			</xsl:call-template>
			<xsl:call-template name="getDoi">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PPT-poster'" />
				<xsl:with-param name="docKind" select="'Poster'" />
				<xsl:with-param name="icon" select="'ppt'" />
			</xsl:call-template>
			<xsl:call-template name="getDoi">
				<xsl:with-param name="variant" select="$variant" />
				<xsl:with-param name="pidValue" select="$pidValue" />
				<xsl:with-param name="docType" select="'PPS-presentation'" />
				<xsl:with-param name="docKind" select="'Presentation'" />
				<xsl:with-param name="icon" select="'pps'" />
			</xsl:call-template>
	</xsl:template>

	<xsl:template name="getDoi">
		<xsl:param name="variant"></xsl:param>
		<xsl:param name="pidValue"></xsl:param>
		<xsl:param name="docType"></xsl:param>
		<xsl:param name="docKind"></xsl:param>
		<xsl:param name="icon">pdf</xsl:param>
			<xsl:variable name="fieldName">FULLTEXT-<xsl:value-of select="$docType" /></xsl:variable>
				<xsl:if test="field[@name = $fieldName]/text()">
					<xsl:variable name="doiFieldName">DOI-<xsl:value-of select="$docType" /></xsl:variable>
					<xsl:choose>
						<xsl:when test="field[@name = $doiFieldName]/text()">
							<xsl:variable name="DOI"><xsl:value-of select="field[@name = $doiFieldName]/text()" /></xsl:variable>
							<xsl:choose>
								<xsl:when test="$variant = 'displayline'">
									<br />
									<span class="ffgsResultSetCellMoreFieldName">DOI</span> :
									<a class="ffgsLink" title="Find via DOI resolver" target="_blank">
										<xsl:attribute name="href">http://dx.doi.org/<xsl:value-of select="$DOI"/></xsl:attribute>
										<xsl:value-of select="$docKind"/> at <xsl:value-of select="$DOI"/>
									</a>
								</xsl:when>
								<xsl:when test="$variant = 'citation'"> DOI (<xsl:value-of select="$docKind"/>): <xsl:value-of select="$DOI"/> . </xsl:when>
							</xsl:choose>
						</xsl:when>
						<xsl:otherwise> 
							<xsl:choose>
								<xsl:when test="$variant = 'displayline'">
									<br />
									<a class="ffgsLink" title="{$docType} download" target="_blank">
										<xsl:attribute name="href"><xsl:value-of select="$FEDORABASEURL"/>/objects/<xsl:value-of select="$pidValue" />/datastreams/<xsl:value-of select="$docType" />/content </xsl:attribute>
										<img src="{$moduleUri}images/{$icon}.png" alt="{$docType}" title="{$docType} download" />
										<xsl:value-of select="' '"/><xsl:value-of select="$docKind"/>
									</a>
								</xsl:when>
								<xsl:when test="$variant = 'citation'"> Retrieved from: <xsl:value-of select="$FEDORABASEURL"/>/objects/<xsl:value-of select="$pidValue" />/datastreams/<xsl:value-of select="$docType" />/content </xsl:when>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
	</xsl:template>

	<xsl:template name="error">
		<p class="error">
			<xsl:value-of select="$ERRORMESSAGE" />
		</p>
	</xsl:template>

	<xsl:template match="message">
		<p class="error">
			Your search input forms an illegal query.
		</p>
		<p class="tip">
			You may be able to see why from the error text below in blue, or 
			<a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html" target="_blank">
			from the full query syntax</a>.
			You may see the generated query from Show Advanced Search and Show query.
		</p>
		<p class="message">
				<xsl:value-of select="./text()" />
		</p>
	</xsl:template>

	<!-- disable all default text node output -->
	<xsl:template match="text()" />

</xsl:stylesheet>
