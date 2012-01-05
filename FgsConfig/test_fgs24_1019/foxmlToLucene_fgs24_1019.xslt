<?xml version="1.0" encoding="UTF-8"?> 
<!-- $Id: foxmlToLucene_fgs24_1019.xslt $ -->
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   
    	xmlns:exts="xalan://dk.defxws.fedoragsearch.server.GenericOperationsImpl"
    		exclude-result-prefixes="exts"
		xmlns:foxml="info:fedora/fedora-system:def/foxml#"
		xmlns:fedora-model="info:fedora/fedora-system:def/model#"
		xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
		xmlns:rel="info:fedora/fedora-system:def/relations-external#"
		xmlns:result="http://www.w3.org/2001/sw/DataAccess/rf1/result"
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
			
			<!-- indexing inline RELS-EXT -->
			
			<xsl:for-each select="foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/rdf:RDF/rdf:Description">
				<xsl:for-each select="fedora-model:hasModel">
					<IndexField IFname="RELS-EXT.hasModel" index="UN_TOKENIZED" store="YES" termVector="NO">
						<xsl:value-of select="@rdf:resource"/>
					</IndexField>
				</xsl:for-each>
				<xsl:for-each select="rel:isMemberOf">
					<xsl:variable name="collUri" select="@rdf:resource"/>
					<IndexField IFname="RELS-EXT.isMemberOf" index="UN_TOKENIZED" store="YES" termVector="NO">
						<xsl:value-of select="@rdf:resource"/>
					</IndexField>	
					<xsl:variable name="query">select+$member+from+%3C%23ri%3E+where+$member+%3Cinfo:fedora/fedora-system:def/relations-external%23isMemberOf%3E+%3C<xsl:value-of select="$collUri"/>%3E</xsl:variable>
					<xsl:variable name="sparqlUrl">http://localhost:8080/fedora/risearch?type=tuples&amp;lang=itql&amp;limit=20&amp;format=Sparql&amp;query=<xsl:value-of select="$query"/></xsl:variable>
					<xsl:variable name="sparql" select="document($sparqlUrl)"/>
					<xsl:for-each select="$sparql//result:member">
						<IndexField IFname="RELS-EXT.hasCoMember" index="UN_TOKENIZED" store="YES" termVector="NO">
							<xsl:value-of select="$collUri"/>/<xsl:value-of select="@uri"/>
						</IndexField>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:for-each>
			
			<!-- Text and metadata extraction using Apache Tika. -->
			
			<xsl:for-each select="foxml:datastream[@CONTROL_GROUP='M' or @CONTROL_GROUP='E' or @CONTROL_GROUP='R']">
				<xsl:value-of disable-output-escaping="yes" select="exts:getDatastreamFromTika($PID, $REPOSITORYNAME, @ID, 'IndexField', concat('ds.', @ID), concat('dsmd_', @ID, '.'), '', $FEDORASOAP, $FEDORAUSER, $FEDORAPASS, $TRUSTSTOREPATH, $TRUSTSTOREPASS)"/>
			</xsl:for-each>

	</xsl:template>
	
</xsl:stylesheet>	
