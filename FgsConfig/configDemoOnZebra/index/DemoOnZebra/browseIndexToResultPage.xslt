<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:zs="http://www.loc.gov/zing/srw/"
	xmlns:zd="http://www.loc.gov/zing/srw/diagnostic/"
	xmlns:foxml="info:fedora/fedora-system:def/foxml#"
	xmlns:dc="http://purl.org/dc/elements/1.1/">

	<!-- This xslt stylesheet generates the resultPage
		from a Zebra browseIndex.
	-->

	<xsl:output method="xml" indent="yes" encoding="UTF-8" />

	<xsl:param name="STARTTERM" select="query" />
	<xsl:param name="INDEXNAME" select="indexName" />
	<xsl:param name="FIELDNAME" select="fieldName" />
	<xsl:param name="TERMPAGESIZE" select="10" />
	<xsl:param name="RESULTPAGEXSLT" select="resultPageXslt" />
	<xsl:param name="DATETIME" select="none" />

    <xsl:template match="text()"/>

	<xsl:template match="/zs:scanResponse">
		<xsl:variable name="TERMTOTAL" select="99999999" />
		<resultPage dateTime="{$DATETIME}" indexName="{$INDEXNAME}">
        	<xsl:apply-templates select="zs:diagnostics"/>
			<browseIndex startTerm="{$STARTTERM}"
				fieldName="{$FIELDNAME}" termPageSize="{$TERMPAGESIZE}"
				resultPageXslt="{$RESULTPAGEXSLT}" termTotal="{$TERMTOTAL}">
				<fields>
					<field>fgs.PID</field>
					<field>dc.creator</field>
					<field>dc.description</field>
					<field>dc.format</field>
					<field>dc.identifier</field>
					<field>dc.publisher</field>
					<field>dc.relation</field>
					<field>dc.rights</field>
					<field>dc.subject</field>
					<field>dc.title</field>
					<field>fgs.contentModel</field>
					<field>fgs.createdDate</field>
					<field>fgs.label</field>
					<field>fgs.lastModifiedDate</field>
					<field>fgs.state</field>
					<field>fgs.type</field>
					<field>fgs.repositoryName</field>
					<field>fgs.DS2</field>
				</fields>
				<terms>
					<xsl:for-each select="zs:terms/zs:term">
						<term>
							<xsl:attribute name="no">
								<xsl:value-of select="' '" />
							</xsl:attribute>
							<xsl:attribute name="fieldtermhittotal">
								<xsl:value-of
									select="zs:numberOfRecords" />
							</xsl:attribute>
							<xsl:value-of select="zs:value" />
						</term>
					</xsl:for-each>
				</terms>
			</browseIndex>
		</resultPage>
	</xsl:template>
	
	<xsl:template match="zd:diagnostic">
		<error>
			<message>
				(<xsl:value-of select="zd:uri"/>)
				<xsl:value-of select="zd:message"/>
			</message>
		</error>
	</xsl:template>

</xsl:stylesheet>









