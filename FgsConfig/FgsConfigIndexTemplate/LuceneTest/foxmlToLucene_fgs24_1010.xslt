<?xml version="1.0" encoding="UTF-8"?> 
<!-- $Id: foxmlToLucene_fgs24_1010.xslt for test $ -->
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

	<xsl:variable name="PID" select="/foxml:digitalObject/@PID"/>
	
	<xsl:template match="/">
		<IndexDocument> 
			<xsl:attribute name="PID">
				<xsl:value-of select="$PID"/>
			</xsl:attribute>
		<xsl:if test="foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']">
			<xsl:if test="not(foxml:digitalObject/foxml:datastream[@ID='METHODMAP'] or foxml:digitalObject/foxml:datastream[@ID='DS-COMPOSITE-MODEL'])">
				<xsl:if test="starts-with($PID,'')">
					<xsl:apply-templates mode="activeFedoraObject"/>
				</xsl:if>
			</xsl:if>
		</xsl:if>
		</IndexDocument>
	</xsl:template>

	<xsl:template match="/foxml:digitalObject" mode="activeFedoraObject">
		    <!-- The PID index field lets you search on the PID value -->
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

			<!-- testing of Tika extraction. 
				The three parameters specific to getDatastreamFromTikaWithMetadata are
				- pluginName			: either "Lucene" or "Solr"
				- indexfieldnamePrefix	: optional or empty, prefixed to the metadata indexfield names
				- selectedFieldnames	: comma-separated list of metadata field names with params, if empty then all fields are included with default params
					- params			: metadataFieldName ['/' [index] ['/' [store] ['/' [termVector] ['/' [boost]]]]]
						metadataFieldName can be seen as index field names, when this list is empty
						index			: ['TOKENIZED'|'UN_TOKENIZED']	# first alternative is default
						store			: ['YES'|'NO']					# first alternative is default
						termVector		: ['YES'|'NO']					# first alternative is default
						boost			: <decimal number>				# '1.0' is default
			-->
				<IndexField IFname="ds.testMwordX" index="TOKENIZED" store="YES" termVector="YES">
					<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamFromTika($PID, $REPOSITORYNAME, 'testMwordX', $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
				</IndexField>
				<IndexField IFname="ds.testMword" index="TOKENIZED" store="YES" termVector="YES">
					<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamFromTikaWithMetadata($PID, $REPOSITORYNAME, 'testMword', 'Lucene', 'dsSomeMd.', 'title,Author,Word-Count', $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
				</IndexField>
				<IndexField IFname="ds.testMpdf" index="TOKENIZED" store="YES" termVector="YES">
					<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamFromTikaWithMetadata($PID, $REPOSITORYNAME, 'testMpdf', 'Lucene', 'dsAllMd.', '', $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
				</IndexField>
				<IndexField IFname="ds.testMpdfSomeMd" index="TOKENIZED" store="YES" termVector="YES">
					<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamFromTikaWithMetadata($PID, $REPOSITORYNAME, 'testMpdf', 'Lucene', 'dsUntok.', 'created/UN_TOKENIZED//NO,Content-Type/UN_TOKENIZED/YES/YES/2.5', $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
				</IndexField>

	</xsl:template>
	
</xsl:stylesheet>	
