<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:output method="html" indent="yes" encoding="UTF-8"/>

	<xsl:param name="ERRORMESSAGE" select="''"/>

	<xsl:include href="adminCommonToHtml.xslt"/>
	
	<xsl:template name="opSpecifics">
		<h2>getIndexDocument</h2>
		<p/>
		<table border="3" cellpadding="5" cellspacing="0" width="784">
				<tr>
					<th>IndexField name</th>			
					<th>contents</th>			
				</tr>
			<xsl:for-each select="//field">
				<tr>
					<td>
						<xsl:value-of select="@name"/>
					</td>			
					<td>
						<xsl:value-of select="."/>
					</td>			
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	
</xsl:stylesheet>	





				



