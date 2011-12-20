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
<!-- $Id: foxmlToLuceneGenerated.xslt $ -->
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
		<IndexDocument boost="1.0"> 
			<xslt:attribute name="PID">
				<xslt:value-of select="$PID"/>
			</xslt:attribute>
		    <xsl:comment>The PID attribute is mandatory for indexing to work</xsl:comment>
		<xsl:comment>The following allows only active FedoraObjects to be indexed.</xsl:comment>
		<xslt:if test="foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']">
			<xslt:if test="not(foxml:digitalObject/foxml:datastream[@ID='METHODMAP'] or foxml:digitalObject/foxml:datastream[@ID='DS-COMPOSITE-MODEL'])">
				<xslt:if test="starts-with($PID,'')">
					<xslt:apply-templates mode="activeFedoraObject"/>
				</xslt:if>
			</xslt:if>
		</xslt:if>
		</IndexDocument>
	</xslt:template>
	

	<xslt:template match="/foxml:digitalObject" mode="activeFedoraObject">
		    <xsl:comment>The PID index field lets you search on the PID value</xsl:comment>
			<IndexField IFname="PID" index="UN_TOKENIZED" store="YES" termVector="NO" boost="1.0">
				<xslt:value-of select="$PID"/>
			</IndexField>
			<IndexField IFname="REPOSITORYNAME" index="UN_TOKENIZED" store="YES" termVector="NO" boost="1.0">
				<xslt:value-of select="$REPOSITORYNAME"/>
			</IndexField>
			<IndexField IFname="REPOSBASEURL" index="UN_TOKENIZED" store="YES" termVector="NO" boost="1.0">
				<xslt:value-of select="substring($FEDORASOAP, 1, string-length($FEDORASOAP)-9)"/>
			</IndexField>
			<IndexField IFname="TITLE_UNTOK" index="UN_TOKENIZED" store="YES" termVector="NO" boost="1.0">
				<xslt:value-of select="foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/dc:title"/>
			</IndexField>
			<IndexField IFname="AUTHOR_UNTOK" index="UN_TOKENIZED" store="YES" termVector="NO" boost="1.0">
				<xslt:value-of select="foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/dc:creator"/>
			</IndexField>
			
			<xsl:comment>indexing foxml property fields</xsl:comment>			
			<xslt:for-each select="foxml:objectProperties/foxml:property">
				<IndexField index="UN_TOKENIZED" store="YES" termVector="NO">
					<xslt:attribute name="IFname"> 
						<xslt:value-of select="concat('fgs.', substring-after(@NAME,'#'))"/>
					</xslt:attribute>
					<xslt:value-of select="@VALUE"/>
				</IndexField>
			</xslt:for-each>
			
			<xsl:comment>indexing foxml fields</xsl:comment>
			<xsl:for-each select="//IFname">
				<xsl:if test="name(..)='element'">
					<xslt:for-each>
						<xsl:attribute name="select"><xsl:value-of select="preceding-sibling::*/text()"/></xsl:attribute>
						<IndexField index="TOKENIZED" store="YES" termVector="YES" boost="1.0">
							<xsl:attribute name="IFname"><xsl:value-of select="text()"/></xsl:attribute>
							<xsl:attribute name="displayName"><xsl:value-of select="text()"/></xsl:attribute>
							<xslt:value-of select="text()"/>
						</IndexField>
					</xslt:for-each>
				</xsl:if>
				<xsl:if test="name(..)='attribute'">
					<xslt:for-each>
						<xsl:attribute name="select"><xsl:value-of select="preceding-sibling::*/text()"/></xsl:attribute>
						<IndexField index="UN_TOKENIZED" store="YES" termVector="NO" boost="1.0">
							<xsl:attribute name="IFname"><xsl:value-of select="text()"/></xsl:attribute>
							<xsl:attribute name="displayName"><xsl:value-of select="text()"/></xsl:attribute>
							<xslt:value-of select="."/>
						</IndexField>
					</xslt:for-each>
				</xsl:if>
			</xsl:for-each>

			<xsl:comment> a datastream is fetched, if its mimetype 
			     can be handled, the text becomes the value of the field. 
			     This is the version using PDFBox,
			     below is the new version using Apache Tika. </xsl:comment>
			<xsl:comment> 
			<xslt:for-each select="foxml:datastream[@CONTROL_GROUP='M' or @CONTROL_GROUP='E' or @CONTROL_GROUP='R']">
				<IndexField index="TOKENIZED" store="YES" termVector="NO">
					<xslt:attribute name="IFname">
						<xslt:value-of select="concat('ds.', @ID)"/>
					</xslt:attribute>
					<xslt:value-of select="exts:getDatastreamText($PID, $REPOSITORYNAME, @ID, $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
				</IndexField>
			</xslt:for-each>
			 </xsl:comment>

			<xsl:comment> Text and metadata extraction using Apache Tika. </xsl:comment>
			<xslt:for-each select="foxml:datastream[@CONTROL_GROUP='M' or @CONTROL_GROUP='E' or @CONTROL_GROUP='R']">
				<xslt:value-of disable-output-escaping="yes" select="exts:getDatastreamFromTika($PID, $REPOSITORYNAME, @ID, 'IndexField', concat('ds.', @ID), concat('dsmd.', @ID, '.'), '', $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
			</xslt:for-each>
			
			<xsl:comment>creating an index field with all text from the foxml record and its datastreams</xsl:comment>

			<IndexField IFname="foxml.all.text" index="TOKENIZED" store="YES" termVector="YES">
				<xslt:for-each select="//text()">
					<xslt:value-of select="."/>
					<xslt:text>&#160;</xslt:text>
				</xslt:for-each>
				<xslt:for-each select="//foxml:datastream[@CONTROL_GROUP='M' or @CONTROL_GROUP='E' or @CONTROL_GROUP='R']">
					<xslt:value-of select="exts:getDatastreamText($PID, $REPOSITORYNAME, @ID, $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
					<xslt:text>&#160;</xslt:text>
				</xslt:for-each>
			</IndexField>
			
	</xslt:template>
	
	</xslt:stylesheet>
	
	</xsl:template>

</xsl:stylesheet>	
