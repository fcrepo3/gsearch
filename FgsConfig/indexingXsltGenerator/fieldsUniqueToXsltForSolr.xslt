<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xslt="http://stylesheet/generation"
		xmlns:foxml="info:fedora/fedora-system:def/foxml#"
		xmlns:audit="info:fedora/fedora-system:def/audit#"
		xmlns:dc="http://purl.org/dc/elements/1.1/">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
	<xsl:strip-space elements="*"/>

	<xsl:namespace-alias result-prefix="xsl"
                       stylesheet-prefix="xslt"/>
       
	<xsl:template match="/">

<xsl:text>	
<!-- $Id: foxmlToSolrGenerated.xslt $ -->
</xsl:text>			
<xslt:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns:foxml="info:fedora/fedora-system:def/foxml#"
		xmlns:audit="info:fedora/fedora-system:def/audit#"
		xmlns:dc="http://purl.org/dc/elements/1.1/"
		xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
		xmlns:dtu_meta="http://www.dtu.dk/dtu_meta/" xmlns:meta="http://www.dtu.dk/dtu_meta/meta/"
    	xmlns:exts="xalan://dk.defxws.fedoragsearch.server.GenericOperationsImpl"
    		exclude-result-prefixes="exts">
	<xslt:output method="xml" indent="yes" encoding="UTF-8"/>
		
	<xslt:param name="REPOSITORYNAME" select="'FgsRepos'"/>
	<xslt:param name="REPOSBASEURL" select="'http://localhost:8080/fedora'"/>
	<xslt:param name="FEDORASOAP" select="'http://localhost:8080/fedora/services'"/>
	<xslt:param name="FEDORAUSER" select="'fedoraAdmin'"/>
	<xslt:param name="FEDORAPASS" select="'fedoraAdmin'"/>
	<xslt:param name="TRUSTSTOREPATH" select="'trustStorePath'"/>
	<xslt:param name="TRUSTSTOREPASS" select="'trustStorePass'"/>

	<xslt:variable name="PID" select="/foxml:digitalObject/@PID"/>
	
	<xslt:template match="/">
		<xsl:comment>The following allows only active FedoraObjects to be indexed.</xsl:comment>
		<xslt:if test="foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']">
			<xslt:if test="not(foxml:digitalObject/foxml:datastream[@ID='METHODMAP'] or foxml:digitalObject/foxml:datastream[@ID='DS-COMPOSITE-MODEL'])">
				<xslt:if test="starts-with($PID,'')">
					<xslt:apply-templates mode="activeFedoraObject"/>
				</xslt:if>
			</xslt:if>
		</xslt:if>
		<xsl:comment>The following allows inactive FedoraObjects to be deleted from the index.</xsl:comment>
		<xslt:if test="foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Inactive']">
			<xslt:if test="not(foxml:digitalObject/foxml:datastream[@ID='METHODMAP'] or foxml:digitalObject/foxml:datastream[@ID='DS-COMPOSITE-MODEL'])">
				<xslt:if test="starts-with($PID,'')">
					<xslt:apply-templates mode="inactiveFedoraObject"/>
				</xslt:if>
			</xslt:if>
		</xslt:if>
	</xslt:template>
	

	<xslt:template match="/foxml:digitalObject" mode="activeFedoraObject">
		<add> 
		<doc> 
			<field name="PID">
				<xslt:value-of select="$PID"/>
			</field>
			<field name="REPOSITORYNAME">
				<xslt:value-of select="$REPOSITORYNAME"/>
			</field>
			<field name="REPOSBASEURL">
				<xslt:value-of select="substring($FEDORASOAP, 1, string-length($FEDORASOAP)-9)"/>
			</field>
			<field name="TITLE_UNTOK">
				<xslt:value-of select="foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/dc:title"/>
			</field>
			<field name="AUTHOR_UNTOK">
				<xslt:value-of select="foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/dc:creator"/>
			</field>
			
			<xsl:comment>indexing foxml property fields</xsl:comment>			
			<xslt:for-each select="foxml:objectProperties/foxml:property">
				<field>
					<xslt:attribute name="name"> 
						<xslt:value-of select="concat('fgs.', substring-after(@NAME,'#'))"/>
					</xslt:attribute>
					<xslt:value-of select="@VALUE"/>
				</field>
			</xslt:for-each>
			
			<xsl:comment>indexing foxml fields</xsl:comment>
			<xsl:for-each select="//IFname">
				<xsl:if test="name(..)='element'">
					<xslt:for-each>
						<xsl:attribute name="select"><xsl:value-of select="preceding-sibling::*/text()"/></xsl:attribute>
						<field>
							<xsl:attribute name="name"><xsl:value-of select="text()"/></xsl:attribute>
							<xslt:value-of select="text()"/>
						</field>
					</xslt:for-each>
				</xsl:if>
				<xsl:if test="name(..)='attribute'">
					<xslt:for-each>
						<xsl:attribute name="select"><xsl:value-of select="preceding-sibling::*/text()"/></xsl:attribute>
						<field>
							<xsl:attribute name="name"><xsl:value-of select="text()"/></xsl:attribute>
							<xslt:value-of select="."/>
						</field>
					</xslt:for-each>
				</xsl:if>
			</xsl:for-each>

			<xsl:comment> a datastream is fetched, if its mimetype 
			     can be handled, the text becomes the value of the field.
			     This is the version using PDFBox,
			     below is the new version using Apache Tika. </xsl:comment>
			<xsl:comment> 
			<xslt:for-each select="foxml:datastream[@CONTROL_GROUP='M' or @CONTROL_GROUP='E' or @CONTROL_GROUP='R']">
				<field>
					<xslt:attribute name="name">
						<xslt:value-of select="concat('ds.', @ID)"/>
					</xslt:attribute>
					<xslt:value-of select="exts:getDatastreamText($PID, $REPOSITORYNAME, @ID, $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
				</field>
			</xslt:for-each>
			 </xsl:comment>

			<xsl:comment> Text and metadata extraction using Apache Tika. </xsl:comment>
			<xslt:for-each select="foxml:datastream[@CONTROL_GROUP='M' or @CONTROL_GROUP='E' or @CONTROL_GROUP='R']">
				<xslt:value-of disable-output-escaping="yes" select="exts:getDatastreamFromTika($PID, $REPOSITORYNAME, @ID, 'field', concat('ds.', @ID), concat('dsmd.', @ID, '.'), '', $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
			</xslt:for-each>
			
			<xsl:comment>creating an index field with all text from the foxml record and its datastreams</xsl:comment>

			<field name="foxml.all.text">
				<xslt:for-each select="//text()">
					<xslt:value-of select="."/>
					<xslt:text>&#160;</xslt:text>
				</xslt:for-each>
				<xslt:for-each select="//foxml:datastream[@CONTROL_GROUP='M' or @CONTROL_GROUP='E' or @CONTROL_GROUP='R']">
					<xslt:value-of select="exts:getDatastreamText($PID, $REPOSITORYNAME, @ID, $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
					<xslt:text>&#160;</xslt:text>
				</xslt:for-each>
			</field>
		</doc>
		</add>
			
	</xslt:template>

	<xslt:template match="/foxml:digitalObject" mode="inactiveFedoraObject">
		<delete> 
			<id><xslt:value-of select="$PID"/></id>
		</delete>
	</xslt:template>
	
	</xslt:stylesheet>
	
	</xsl:template>

</xsl:stylesheet>	
