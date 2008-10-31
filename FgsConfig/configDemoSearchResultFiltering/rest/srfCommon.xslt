<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
		
<!-- This xslt stylesheet is common to 
     basicGfindObjectsToHtml, basicBrowseIndexToHtml, basicGetIndexInfoToHtml, and basicUpdateIndexToHtml.
-->
	
	<xsl:output method="html" indent="yes" encoding="UTF-8"/>
	
	<xsl:param name="TIMEUSEDMS" select="''"/>
	<xsl:param name="FGSUSERNAME" select="''"/>

	<xsl:template match="/resultPage">
		<html>
			<head>
				<title>Search Result Filtering Demo Client for GSearch</title>
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
						<h1>Search Result Filtering Demo Client for GSearch</h1>
					</div>
				</div>
				<table cellspacing="10" cellpadding="10">
					<tr>
					<th><a href="?operation=updateIndex">updateIndex</a></th>
					<th><a href="?operation=gfindObjects">gfindObjects</a></th>
					<th><a href="?operation=browseIndex">browseIndex</a></th>
					<th><a href="?operation=getRepositoryInfo">getRepositoryInfo</a></th>
					<th><a href="?operation=getIndexInfo">getIndexInfo</a></th>
					<td>(<xsl:value-of select="$TIMEUSEDMS"/> milliseconds)</td>
					<td>(fgsUserName=<xsl:value-of select="$FGSUSERNAME"/>)</td>
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
						Copyright &#xA9; 2008 Technical University of Denmark, DEFF Fedora Project
					</div>
					<div id="lastModified">
						Last Modified
						<script type="text/javascript">
							//<![CDATA[
							var cvsDate = "$Date: 2008-08-29 $";
							var parts = cvsDate.split(" ");
							var modifiedDate = parts[1];
							document.write(modifiedDate);
							//]]>
						</script>
						by Gert Schmeltz Pedersen 
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





				




