<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
		
<!-- This xslt stylesheet is common to 
     gfindObjectsToHtml, browseIndexToHtml, getIndexInfoToHtml, and updateIndexToHtml.
-->
	
	<xsl:output method="html" indent="yes" encoding="UTF-8"/>
	
	<xsl:param name="FGSUSERNAME" select="''"/>
	<xsl:param name="TIMEUSEDMS" select="''"/>

	<xsl:template match="/resultPage">
		<html>
			<head>
				<title>Admin Client for Fedora Generic Search Service</title>
				<link rel="stylesheet" type="text/css" href="css/basic.css"/>
				<style type="text/css">
					.highlight {
						background: yellow;
					}
				</style>
				<script language="javascript">
				</script>
			</head>
			<body>
				<div id="header">
					<a href="" id="logo"></a>
					<div id="title">
						<h1>Admin Client for Fedora Generic Search Service</h1>
					</div>
					<div align="right">
						<h4>You are logged in as <xsl:value-of select="$FGSUSERNAME"/> 
						using <a href="FgsConfig/FGSCONFIGNAME" title="to see config files set tomcat/conf/web.xml listings to true">FGSCONFIGNAME</a></h4>
					</div>
				</div>
				<table cellspacing="10" cellpadding="10">
					<tr>
					<th><a href="?operation=updateIndex">updateIndex</a></th>
					<th><a href="?operation=gfindObjects">gfindObjects</a></th>
					<th><a href="?operation=browseIndex">browseIndex</a></th>
					<th><a href="?operation=getRepositoryInfo">getRepositoryInfo</a></th>
					<th><a href="?operation=getIndexInfo">getIndexInfo</a></th>
					<th><a href="?operation=gfindObjects&amp;restXslt=fgseuSearchToHtml" target="fgseuSearch">enduserSearch</a></th>
					<td>(<xsl:value-of select="$TIMEUSEDMS"/> milliseconds)</td>
					</tr>
				</table>
				<p/>
				<!-- 
				<xsl:if test="$ERRORMESSAGE">
					<xsl:call-template name="error"/>
	 			</xsl:if>
	 			 -->
        		<xsl:apply-templates select="error"/>
				<xsl:call-template name="opSpecifics"/>
				<div id="footer">
   					<div id="copyright">
						Copyright &#xA9; 2010, 2011, 2012 Technical University of Denmark
					</div>
				</div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template name="error">
		<p>
			<font color="red">
				<xsl:value-of select="$ERRORMESSAGE"/>
			</font>
		</p>			
	</xsl:template>
	
	<xsl:template match="message">
		<p>
			<font color="red">
				<xsl:value-of select="./text()"/>
			</font>
		</p>			
	</xsl:template>
	
</xsl:stylesheet>	





				




