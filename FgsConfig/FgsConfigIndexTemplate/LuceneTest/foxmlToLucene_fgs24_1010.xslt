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
				Parameters for getDatastreamFromTika, getDatastreamTextFromTika, and getDatastreamMetadataFromTika:
				- indexFieldTagName		: either "IndexField" (with the Lucene plugin) or "field" (with the Solr plugin)
				- textIndexField		: fieldSpec for the text index field, null or empty if not to be generated								 (not used with getDatastreamMetadataFromTika)
				- indexfieldnamePrefix	: optional or empty, prefixed to the metadata indexfield names											 (not used with getDatastreamTextFromTika)
				- selectedFields		: comma-separated list of metadata fieldSpecs, if empty then all fields are included with default params (not used with getDatastreamTextFromTika)
				- fieldSpec				: metadataFieldName ['=' indexFieldName] ['/' [index] ['/' [store] ['/' [termVector] ['/' [boost]]]]]
						metadataFieldName must be exactly as extracted by Tika from the document. 
										  You may see the available names if you log in debug mode, 
										  look for "METADATA name=" under "fullDsId=" in the log, when "getFromTika" was called during updateIndex
						indexFieldName is used as the generated index field name,
										  if not given, GSearch uses metadataFieldName after replacement of the characters ' ', ':', '/', '=', '(', ')' with '_'
						the following parameters are used with Lucene (with Solr these values are specified in schema.xml)
						index			: ['TOKENIZED'|'UN_TOKENIZED']	# first alternative is default
						store			: ['YES'|'NO']					# first alternative is default
						termVector		: ['YES'|'NO']					# first alternative is default
						boost			: <decimal number>				# '1.0' is default
			-->
				<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamTextFromTika(		$PID, $REPOSITORYNAME, 'testMwordX', 'IndexField', 'ds.testMwordX',                                                                                              $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>

				<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamMetadataFromTika(	$PID, $REPOSITORYNAME, 'testMword',  'IndexField',                      'dsSomeMd.', 'title=TITLE,Author,Word-Count',                                            $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>

				<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamFromTika(			$PID, $REPOSITORYNAME, 'testMpdf',   'IndexField', 'ds.testMpdf//NO',   'dsAllMd.',  '',                                                                         $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>

				<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamFromTika(			$PID, $REPOSITORYNAME, 'testMpdf',   'IndexField', 'ds.testMpdfSomeMd', 'dsUntok.',  'created=IFCreated/UN_TOKENIZED//NO,Content-Type/UN_TOKENIZED/YES/YES/2.5', $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>

				<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamFromTika(			$PID, $REPOSITORYNAME, 'testMpdf',   'IndexField', null,                'dsUntok.',  'created=IFCreated/UN_TOKENIZED//NO,Content-Type/UN_TOKENIZED/YES/YES/2.5', $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>


	</xsl:template>
	
</xsl:stylesheet>	
