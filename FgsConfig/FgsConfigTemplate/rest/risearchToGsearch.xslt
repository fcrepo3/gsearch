<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:result="http://www.w3.org/2001/sw/DataAccess/rf1/result"
    	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>

	<xsl:template match="/">
		<result:newQueryPart>
			<xsl:text>(</xsl:text>
			<xsl:for-each select="//result:result"> 
				<xsl:if test="position()>1"> or </xsl:if>
				<xsl:text>PID:</xsl:text>
				<xsl:text>"</xsl:text><xsl:value-of select="substring-after(result:obj1/@uri, '/')"/><xsl:text>"</xsl:text>
			</xsl:for-each>
			<xsl:text>)</xsl:text>
		</result:newQueryPart>
	</xsl:template>
	
    <xsl:template match="text()"/>
	
</xsl:stylesheet>	
