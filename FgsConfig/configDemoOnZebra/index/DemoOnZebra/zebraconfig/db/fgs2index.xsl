<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:z="http://indexdata.dk/zebra/xslt/1"
  xmlns:foxml="info:fedora/fedora-system:def/foxml#" 
  xmlns:fedoraAudit="http://fedora.comm.nsdlib.org/audit" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:METS="http://www.loc.gov/METS/"
  xmlns:audit="info:fedora/fedora-system:def/audit#"
  xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
  xmlns:dc="http://purl.org/dc/elements/1.1/"

  version="1.0">

  <xsl:output indent="yes" method="xml" version="1.0" encoding="UTF-8"/>

  <!-- <xsl:include href="xpath.xsl"/> -->
  

  <!-- disable all default text node output -->
  <xsl:template match="text()"/>

  <!-- match on alvis xml record -->
  <xsl:template match="/IndexDocument">

    <z:record id="{@PID}" 
              rank="{@rank}" 
              type="update">

    <xsl:apply-templates/>

    </z:record>

  </xsl:template>

  <xsl:template match="IndexField">

    <z:index name="{@IFname}">
        <xsl:value-of select="text()"/>
        <xsl:apply-templates/>
    </z:index>

  </xsl:template>


</xsl:stylesheet>
