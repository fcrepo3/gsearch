<?xml version="1.0" encoding="UTF-8"?> 
<!-- $Id: foxmlToLuceneForXML_SOURCE.xslt $ -->
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   
    	xmlns:exts="xalan://dk.defxws.fedoragsearch.server.GenericOperationsImpl"
    		exclude-result-prefixes="exts"
		xmlns:foxml="info:fedora/fedora-system:def/foxml#"
		xmlns:dc="http://purl.org/dc/elements/1.1/"
		xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
		
	<xsl:param name="REPOSITORYNAME" select="'FgsRepos'"/>
	<xsl:param name="REPOSBASEURL" select="'http://localhost:8080/fedora'"/>
	<xsl:param name="FEDORASOAP" select="'http://localhost:8080/fedora/services'"/>
	<xsl:param name="FEDORAUSER" select="'fedoraAdmin'"/>
	<xsl:param name="FEDORAPASS" select="'fedoraAdmin'"/>
	<xsl:param name="TRUSTSTOREPATH" select="'trustStorePath'"/>
	<xsl:param name="TRUSTSTOREPASS" select="'trustStorePass'"/>
	
	<xsl:variable name="PID"><xsl:value-of select="/foxml:digitalObject/@PID"/>$XML_SOURCE</xsl:variable>
	
	<xsl:template match="/">
		<IndexDocument> 
			<xsl:attribute name="PID">
				<xsl:value-of select="$PID"/>
			</xsl:attribute>
		<xsl:if test="foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']">
			<xsl:if test="not(foxml:digitalObject/foxml:datastream[@ID='METHODMAP'] or foxml:digitalObject/foxml:datastream[@ID='DS-COMPOSITE-MODEL'])">
				<xsl:if test="foxml:digitalObject/foxml:datastream[@ID='XML_SOURCE']">
					<xsl:apply-templates mode="activeFedoraObject"/>
				</xsl:if>
			</xsl:if>
		</xsl:if>
		</IndexDocument>
	</xsl:template>

	<xsl:template match="/foxml:digitalObject" mode="activeFedoraObject">
			<IndexField IFname="PID" index="UN_TOKENIZED" store="YES" termVector="NO" boost="1.0">
				<xsl:value-of select="$PID"/>
			</IndexField>
			<IndexField IFname="REPOSITORYNAME" index="UN_TOKENIZED" store="YES" termVector="NO" boost="1.0">
				<xsl:value-of select="$REPOSITORYNAME"/>
			</IndexField>
			<IndexField IFname="REPOSBASEURL" index="UN_TOKENIZED" store="YES" termVector="NO" boost="1.0">
				<xsl:value-of select="substring($FEDORASOAP, 1, string-length($FEDORASOAP)-9)"/>
			</IndexField>
			
			<!-- indexing inline dc fields -->
			
			<xsl:for-each select="foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/*">
				<IndexField index="TOKENIZED" store="YES" termVector="YES">
					<xsl:attribute name="IFname">
						<xsl:value-of select="concat('dc.', substring-after(name(),':'))"/>
					</xsl:attribute>
					<xsl:value-of select="text()"/>
				</IndexField>
			</xsl:for-each>
			
			<!-- indexing XML_SOURCE datastreams, text and metadata extraction using Apache Tika. -->
			
			<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamFromTika($PID, $REPOSITORYNAME, 'XML_SOURCE', 'IndexField', concat('ds.', 'XML_SOURCE'), concat('dsmd_', 'XML_SOURCE', '.'), '', $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
			
	</xsl:template>
	
</xsl:stylesheet>	
