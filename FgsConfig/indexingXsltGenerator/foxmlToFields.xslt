<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:foxml="info:fedora/fedora-system:def/foxml#"
		xmlns:dc="http://purl.org/dc/elements/1.1/">
	<xsl:output method="xml" indent="no" encoding="UTF-8"/>
	
	<xsl:template match="/">
		<root>
   			<xsl:text>&#xA;</xsl:text>
			<xsl:apply-templates select="//*[not(*)]|//@*"/>
		</root>
	</xsl:template>

	<xsl:template match="*">
		<element>
			<XPath>//<xsl:value-of select="name(.)"/></XPath>
			<IFname><xsl:value-of select="translate(name(.),':','.')"/></IFname>
			<DisplayName uiUse="ANDfacet" initValue="" browseFrom="aa"><xsl:value-of select="translate(name(.),':','.')"/></DisplayName>
		</element>
		<xsl:text>&#xA;</xsl:text>
	</xsl:template>

	<xsl:template match="@*">
		<attribute>
			<XPath>//<xsl:value-of select="name(..)"/>/@<xsl:value-of select="name(.)"/></XPath>
			<IFname><xsl:value-of select="translate(name(..),':','.')"/>_<xsl:value-of select="name(.)"/></IFname>
			<DisplayName uiUse="ORfacet" initValue="" browseFrom="!"><xsl:value-of select="translate(name(..),':','.')"/>_<xsl:value-of select="name(.)"/></DisplayName>
		</attribute>
		<xsl:text>&#xA;</xsl:text>
	</xsl:template>
	
</xsl:stylesheet>	
