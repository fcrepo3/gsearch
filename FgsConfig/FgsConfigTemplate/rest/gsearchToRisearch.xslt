<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:result="http://www.w3.org/2001/sw/DataAccess/rf1/result"
    	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>

	<xsl:template match="/">
		<result:newQueryPart>
			<xsl:text>(</xsl:text>
			<xsl:for-each select="//object"> 
				<xsl:if test="position()>1"> or </xsl:if>
				<xsl:text>$obj1 %3Cmulgara:is%3E </xsl:text> 
				<xsl:text>%3Cinfo:fedora/</xsl:text><xsl:value-of select="field[@name='PID']"/><xsl:text>%3E</xsl:text>
			</xsl:for-each>
			<xsl:text>)</xsl:text>
		</result:newQueryPart>
	</xsl:template>
	
    <xsl:template match="text()"/>
	
</xsl:stylesheet>	
