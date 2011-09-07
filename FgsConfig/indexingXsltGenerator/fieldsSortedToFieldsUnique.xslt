<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:foxml="info:fedora/fedora-system:def/foxml#"
		xmlns:dc="http://purl.org/dc/elements/1.1/">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
	
	<xsl:template match="/">
		<root>
			<xsl:text>&#xA;</xsl:text>
			<xsl:for-each select="//IFname">
				<xsl:if test="position()=1 or text() != ../preceding-sibling::*[1]/IFname/text()">
					<xsl:copy-of select=".."/>
					<xsl:text>&#xA;</xsl:text>
				</xsl:if>
			</xsl:for-each>
		</root>
	</xsl:template>

</xsl:stylesheet>	
