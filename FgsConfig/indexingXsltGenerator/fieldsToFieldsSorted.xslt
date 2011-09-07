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
				<xsl:sort select="text()"/>
				<xsl:copy-of select=".."/>
   				<xsl:text>&#xA;</xsl:text>
			</xsl:for-each>
		</root>
	</xsl:template>
<!-- 
		<xsl:for-each select="//orgs/organisation">

			<xsl:sort select="text()"/>

				<organisation p_pos="{@p_pos}" aff_no="{position()}">
					<xsl:value-of select="."/> 
				</organisation> 
	
		</xsl:for-each>

		<xsl:for-each select="//orgs/organisation">

			<xsl:if test="position()=1 or text() != preceding-sibling::*[1]/text()">

				<organisation p_pos="{@p_pos}" aff_no="{position()}">
					<xsl:value-of select="."/> 
				</organisation> 
	
			</xsl:if>
			
		</xsl:for-each>

outputdir=data/$EVENT_ACRONYM-foxmlWithMxd

mkdir $outputdir


for (( i=0 ; i <= $MAXID ; i++ ))
do
  
  if [ -s temp0.xml ]
  then
  
  
    j=$i
    if [ $i -lt 100 ]
    then
      j=0$j
    fi
    if [ $i -lt 10 ]
    then
      j=0$j
    fi
    outfile=$EVENT_ACRONYM-$EVENT_NUMBER-$j
	PDFi=$PDF
	filename=/Users/gertschmeltzpedersen/dev/workspaces/proc2Workspace/proceedings-gsearch/data/Papers/${i}_Paper.pdf
	if [ -f $filename ]
	then
	  PDFi=pdfyes
	fi
    echo "$outputdir/$outfile.xml $PDFi $filename"
    if [ ! -s $outputdir/$outfile.xml ]
    then
      rm $outputdir/$outfile.xml
    fi
  
  fi
done
  <xsl:template match="text()"/>
	

<xsl:template match="text()">
    <field>
    <xsl:text>_</xsl:text><xsl:value-of select="name(parent::node())"/>
    </field>
</xsl:template>
<xsl:template match="/">
    <xsl:text>_</xsl:text><xsl:value-of select="name()"/>
    <xsl:call-template name="down" ><with-param name="next" select="child::node()"/></xsl:call-template>
</xsl:template>
<xsl:template name="down">
	<xsl:param name="next"/>
    <xsl:apply-templates select="next/text()"/>
    <xsl:apply-templates select="next/@*"/>
    <xsl:call-template name="down" ><with-param name="next" select="next/child::node()"/></xsl:call-template>
</xsl:template>
<xsl:template name="up">
    <xsl:call-template name="up" select="parent::node()"/>
    <xsl:text>_</xsl:text><xsl:value-of select="name(parent::node())"/>
</xsl:template>
<xsl:template match="@*">
    <xsl:text>_@</xsl:text><xsl:value-of select="local-name()"/>
</xsl:template>
	 -->
</xsl:stylesheet>	




				




