<?xml version="1.0" encoding="UTF-8"?> 
<!-- $Id: foxmlToLucene.xslt $ -->
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   
    	xmlns:exts="xalan://dk.defxws.fedoragsearch.server.GenericOperationsImpl"
    		exclude-result-prefixes="exts"
		xmlns:foxml="info:fedora/fedora-system:def/foxml#"
		xmlns:dtu_meta="http://www.dtu.dk/dtu_meta/" 
		xmlns:meta="http://www.dtu.dk/dtu_meta/meta/"
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
<!--
	 This xslt stylesheet generates the IndexDocument consisting of IndexFields
     from a FOXML record. The IndexFields are:
       - from the root element = PID
       - from foxml:property   = type, state, contentModel, ...
       - from oai_dc:dc        = title, creator, ...
     The IndexDocument element gets a PID attribute, which is mandatory,
     while the PID IndexField is optional.
     Options for tailoring:
       - IndexField types, see Lucene javadoc for Field.Store, Field.Index, Field.TermVector
       - IndexField boosts, see Lucene documentation for explanation
       - IndexDocument boosts, see Lucene documentation for explanation
       - generation of IndexFields from other XML metadata streams than DC
         - e.g. as for uvalibdesc included above and called below, the XML is inline
         - for not inline XML, the datastream may be fetched with the document() function,
           see the example below (however, none of the demo objects can test this)
       - generation of IndexFields from other datastream types than XML
         - from datastream by ID, text fetched, if mimetype can be handled
         - from datastream by sequence of mimetypes, 
           text fetched from the first mimetype that can be handled,
           default sequence given in properties.
-->

	<xsl:variable name="PID" select="/foxml:digitalObject/@PID"/>
	<xsl:variable name="docBoost" select="1.4*2.5"/> <!-- or any other calculation, default boost is 1.0 -->
	
	<xsl:template match="/">
		<IndexDocument> 
		    <!-- The PID attribute is mandatory for indexing to work -->
			<xsl:attribute name="PID">
				<xsl:value-of select="$PID"/>
			</xsl:attribute>
			<xsl:attribute name="boost"> <!-- example of setting a boost -->
				<xsl:value-of select="$docBoost"/>
			</xsl:attribute>
		<!-- The following allows only active FedoraObjects to be indexed. -->
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
			
			<!-- indexing foxml property fields -->
			
			<xsl:for-each select="foxml:objectProperties/foxml:property">
				<IndexField index="UN_TOKENIZED" store="YES" termVector="NO">
					<xsl:attribute name="IFname"> 
						<xsl:value-of select="concat('fgs.', substring-after(@NAME,'#'))"/>
					</xsl:attribute>
					<xsl:value-of select="@VALUE"/>
				</IndexField>
			</xsl:for-each>
			
			<!-- indexing inline dc fields -->
			
			<xsl:for-each select="foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/*">
				<IndexField index="TOKENIZED" store="YES" termVector="YES">
					<xsl:attribute name="IFname">
						<xsl:value-of select="concat('dc.', substring-after(name(),':'))"/>
					</xsl:attribute>
					<xsl:value-of select="text()"/>
				</IndexField>
			</xsl:for-each>
		
		<!-- testing fedora repository URI -->
			
		<xsl:variable name="testMapplXml" select="document(concat('http://localhost:8080/fedora/objects/',$PID,'/datastreams/testMapplXml/content'))"/>
		
		<IndexField IFname="testMapplXml.meta.title"> 
			<xsl:value-of select="$testMapplXml//meta:title"/>
		</IndexField> 
		
		<!-- testing location URI -->
			
		<xsl:variable name="indexInfo" select="document('tomcat/webapps/fedoragsearch/WEB-INF/classes/configTestOnLuceneFgs242_1083/index/FgsIndex/indexInfo.xml')"/>
		
		<IndexField IFname="indexInfo.AdminInfo"> 
			<xsl:value-of select="$indexInfo//AdminInfo"/>
		</IndexField> 
		
		<!-- testing file URI -->
			
		<xsl:variable name="indexInfoFile" select="document('file:///Users/gertschmeltzpedersen/f36/tomcat/webapps/fedoragsearch/WEB-INF/classes/configTestOnLuceneFgs242_1083/index/FgsIndex/indexInfo.xml')"/>
		
		<IndexField IFname="indexInfoFile.AdminInfo"> 
			<xsl:value-of select="$indexInfoFile//AdminInfo"/>
			<xsl:value-of select="$indexInfoFile//exception/message"/>
		</IndexField> 
		
		<!-- testing non-existing fedora repository URI -->
			
		<xsl:variable name="testMapplXml" select="document(concat('http://localhost:8080/fedora/objects/',$PID,'/datastreams/nonExistingDatastream/content'))"/>
		
		<IndexField IFname="nonExistingDatastream"> 
			<xsl:value-of select="'nonExistingDatastream'"/>
		</IndexField> 
		
		<!-- testing non-existing location URI -->
			
		<xsl:variable name="indexInfo" select="document('non-existing/path/indexInfo.xml')"/>
		
		<IndexField IFname="nonExistingLocation"> 
			<xsl:value-of select="'nonExistingLocation'"/>
		</IndexField> 
		
	</xsl:template>
	
</xsl:stylesheet>	
