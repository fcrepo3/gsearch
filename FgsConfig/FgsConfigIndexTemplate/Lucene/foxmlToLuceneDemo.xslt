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

			<!-- a datastream is fetched, if its mimetype 
			     can be handled, the text becomes the value of the field. -->
			<xsl:for-each select="foxml:datastream[@CONTROL_GROUP='M' or @CONTROL_GROUP='E' or @CONTROL_GROUP='R']">
				<IndexField index="TOKENIZED" store="YES" termVector="NO">
					<xsl:attribute name="IFname">
						<xsl:value-of select="concat('ds.', @ID)"/>
					</xsl:attribute>
					<xsl:value-of select="exts:getDatastreamText($PID, $REPOSITORYNAME, @ID, $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
				</IndexField>
			</xsl:for-each>

			<!-- example of a dissemination identified in bDefPid, methodName, parameters, asOfDateTime is fetched,  
			     if its mimetype can be handled, the text becomes the value of the IndexField. 
			     parameters format is 'name=value name2=value2'-->
			<!-- 
			<IndexField IFname="fgs.Diss.text" index="TOKENIZED" store="YES" termVector="NO"
						bDefPid="demo:19" methodName="getPDF" parameters="" asOfDateTime="" >
			</IndexField>
			-->

			<!-- for not inline XML, the datastream may be fetched with the document() function -->
			<!--  
			<xsl:call-template name="example-of-xml-not-inline"/>
			-->

			<!-- This is an example of calling an extension function, see Apache Xalan, may be used for filters.
			<IndexField IFname="fgs.DS" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="exts:someMethod($PID)"/>
			</IndexField>
			-->
			     
			<!-- 
			creating an index field with all text from the foxml record and its datastreams
			-->

			<IndexField IFname="foxml.all.text" index="TOKENIZED" store="YES" termVector="YES">
				<xsl:for-each select="//text()">
					<xsl:value-of select="."/>
				</xsl:for-each>
				<xsl:for-each select="//foxml:datastream[@CONTROL_GROUP='M' or @CONTROL_GROUP='E' or @CONTROL_GROUP='R']">
					<xsl:value-of select="exts:getDatastreamText($PID, $REPOSITORYNAME, @ID, $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
				</xsl:for-each>
			</IndexField>

	</xsl:template>
	

	<xsl:template name="example-of-xml-not-inline">
	
		<!-- due to Simon Lamb and Steve Bayliss -->
		<!-- using the test object test:fgs23 -->
		<!-- namespaces to be included in the stylesheet element -->
			
		<xsl:variable name="testMapplXml" select="document('http://localhost:8080/fedora/objects/test:fgs23/datastreams/testMapplXml/content')"/>
		
		<IndexField IFname="testMapplXml.meta.title"> 
			<xsl:value-of select="$testMapplXml//meta:title"/>
		</IndexField> 
		
	</xsl:template>
	
</xsl:stylesheet>	
