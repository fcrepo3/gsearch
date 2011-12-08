<?xml version="1.0" encoding="UTF-8"?> 
<!-- $Id: foxmlToSolr_fgs24_1010.xslt for test $ -->
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   
    	xmlns:exts="xalan://dk.defxws.fedoragsearch.server.GenericOperationsImpl"
    		exclude-result-prefixes="exts"
		xmlns:foxml="info:fedora/fedora-system:def/foxml#"
		xmlns:dc="http://purl.org/dc/elements/1.1/"
		xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>

<!--
	 This xslt stylesheet generates the Solr doc element consisting of field elements
     from a FOXML record. 
     You must specify the index field elements in solr's schema.xml file,
     including the uniqueKey element, which in this case is set to "PID".
     Options for tailoring:
       - generation of fields from other XML metadata streams than DC
       - generation of fields from other datastream types than XML
         - from datastream by ID, text fetched, if mimetype can be handled.
-->

	<xsl:param name="REPOSITORYNAME" select="repositoryName"/>
	<xsl:param name="FEDORASOAP" select="repositoryName"/>
	<xsl:param name="FEDORAUSER" select="repositoryName"/>
	<xsl:param name="FEDORAPASS" select="repositoryName"/>
	<xsl:param name="TRUSTSTOREPATH" select="repositoryName"/>
	<xsl:param name="TRUSTSTOREPASS" select="repositoryName"/>
	<xsl:variable name="PID" select="/foxml:digitalObject/@PID"/>
	
	<xsl:template match="/">
		<!-- The following allows only active FedoraObjects to be indexed. -->
		<xsl:if test="foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']">
			<xsl:if test="not(foxml:digitalObject/foxml:datastream[@ID='METHODMAP'] or foxml:digitalObject/foxml:datastream[@ID='DS-COMPOSITE-MODEL'])">
				<xsl:if test="starts-with($PID,'')">
					<xsl:apply-templates mode="activeFedoraObject"/>
				</xsl:if>
			</xsl:if>
		</xsl:if>
		<!-- The following allows inactive FedoraObjects to be deleted from the index. -->
		<xsl:if test="foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Inactive']">
			<xsl:if test="not(foxml:digitalObject/foxml:datastream[@ID='METHODMAP'] or foxml:digitalObject/foxml:datastream[@ID='DS-COMPOSITE-MODEL'])">
				<xsl:if test="starts-with($PID,'')">
					<xsl:apply-templates mode="inactiveFedoraObject"/>
				</xsl:if>
			</xsl:if>
		</xsl:if>
	</xsl:template>

	<xsl:template match="/foxml:digitalObject" mode="activeFedoraObject">
		<add> 
		<doc> 
			<field name="PID">
				<xsl:value-of select="$PID"/>
			</field>
			<field name="REPOSITORYNAME">
				<xsl:value-of select="$REPOSITORYNAME"/>
			</field>
			<field name="REPOSBASEURL">
				<xsl:value-of select="substring($FEDORASOAP, 1, string-length($FEDORASOAP)-9)"/>
			</field>
			<xsl:for-each select="foxml:objectProperties/foxml:property">
				<field>
					<xsl:attribute name="name"> 
						<xsl:value-of select="concat('fgs.', substring-after(@NAME,'#'))"/>
					</xsl:attribute>
					<xsl:value-of select="@VALUE"/>
				</field>
			</xsl:for-each>
			<xsl:for-each select="foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/*">
				<field>
					<xsl:attribute name="name">
						<xsl:value-of select="concat('dc.', substring-after(name(),':'))"/>
					</xsl:attribute>
					<xsl:value-of select="text()"/>
				</field>
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
				<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamTextFromTika(		$PID, $REPOSITORYNAME, 'testMwordX', 'field', 'ds.testMwordX',                                                    $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>

				<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamMetadataFromTika(	$PID, $REPOSITORYNAME, 'testMword',  'field',                      'dsSomeMd.', 'title=TITLE,Author,Word-Count=WordCount',  $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>

				<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamFromTika(			$PID, $REPOSITORYNAME, 'testMpdf',   'field', 'ds.testMpdf',       'dsAllMd.',  '',                               $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>

				<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamFromTika(			$PID, $REPOSITORYNAME, 'testMpdf',   'field', 'ds.testMpdfSomeMd', 'dsUntok.',  'created=IFCreated,Content-Type', $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>

				<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamFromTika(			$PID, $REPOSITORYNAME, 'testMpdf',   'field', null,                'dsUntok.',  'created=IFCreated,Content-Type', $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
			
			<!-- 
			creating an index field with all text from the foxml record and its datastreams
			-->

			<field name="foxml.all.text">
				<xsl:for-each select="//text()">
					<xsl:value-of select="."/>
					<xsl:text>&#160;</xsl:text>
				</xsl:for-each>
				<xsl:for-each select="//foxml:datastream[@CONTROL_GROUP='M' or @CONTROL_GROUP='E' or @CONTROL_GROUP='R']">
					<xsl:value-of select="exts:getDatastreamText($PID, $REPOSITORYNAME, @ID, $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
					<xsl:text>&#160;</xsl:text>
				</xsl:for-each>
			</field>
			
		</doc>
		</add>
	</xsl:template>

	<xsl:template match="/foxml:digitalObject" mode="inactiveFedoraObject">
		<delete> 
			<id><xsl:value-of select="$PID"/></id>
		</delete>
	</xsl:template>
	
</xsl:stylesheet>	
