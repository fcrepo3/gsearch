-------------------------------------------------------------------
              FedoraGSearch Version 2.3
-------------------------------------------------------------------

 * License and Copyright: FedoraGSearch is subject to the same open source 
 * license as the Fedora Commons Repository System at www.fedora-commons.org
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 by The Technical University of Denmark.
 * All rights reserved.
 
 The FedoraGSearch development is originally funded by 
 
     DEFF, Denmark's Electronic Research Library, http://www.deff.dk .
     
The developer is Gert Schmeltz Pedersen, gsp@dtic.dtu.dk, at DTU Library,
the Technical Information Center of Denmark at the Technical University of Denmark.

Contact in the Fedora Commons core developer team is Chris Wilper, cwilper@fedora-commons.org .

Feedback, requests, etc. may also be sent to 
  fedora-commons-developers@lists.sourceforge.net or to
  fedora-commons-users@lists.sourceforge.net .

A prototype was released in March 2006, working with Fedora version 2.1b.

Version 1.1 was released in January 2007, working with Fedora version 2.2.

Version 1.1.1 was released in May 2007 with a bug fix concerning snippets 
for search results.

Version 2.0 was released in February 2008, with new features requested by users.
The main and overall aim is to exploit more features of Lucene.
It works with Fedora version 2.2.1.

Version 2.1 was released in April 2008, updating GSearch to work with Fedora 3.0
and taking advantage of Fedora's new messaging capability for index updates.

Version 2.2 was released in December 2008, updating GSearch to work with Fedora 3.1+.

Version 2.3 was released in July 2011. See feature list below.

The Fedora Commons Documentation for FedoraGSearch is at 
http://fedora-commons.org/confluence/display/FCSVCS/Generic+Search+Service+2.3.
 
The same information is available in the source download
at src/html/search-service.html, and after installation at
http://localhost:8080/fedoragsearch .

The new features in Version 2.3:

- Lucene 2.9 compatibility
- Solr 1.4 compatibility
- Simplified configuration, see the new configFgs23/ and configvalues23.xml
  (resolving FCREPO-464, thanks to Renaud Waldura)
- log4j.xml set to daily rolling file
- ...

The new features in Version 2.2:

- Fedora 3.1 compatibility
- Lucene 2.4.0 compatibility
- Solr 1.3.0 compatibility
- For the lucene plugin: Search result filtering by access constraints, as defined by XACML policies,
  in order to show only those search hits that the user is actually permitted to read.

The new features in Version 2.1:

- Fedora 3.0 compatibility

- Update listener which uses the Fedora messaging client to listen for 
  updates being performed through API-M. These update messages contain the 
  information needed to perform index updates, thereby keeping GSearch
  up-to-date with the Fedora repository.

- Enhanced the sortFields parameter to gfindObjects for Lucene,
  sorting search results by a custom Comparator class,
  see the index.properties file in configTestOnLucene and
  the test class dk.defxws.fedoragsearch.test.ComparatorSourceTest.

- Enhanced the fromFoxmlFiles action of updateIndex for Lucene,
  so that all files are attempted to be indexed,
  even though one or more may fail,
  in which case log messages are given.
  Before, one failure would cause abortion.

The new features in Version 2.0:

- Added a plugin for the Apache Solr search server.

- Updated to Lucene version 2.3.0.

- Added easier configuration, so that you need only edit one file
  with property values, then run it with ant.
				    
- Added a sortFields parameter to gfindObjects for Lucene, sorting
  search results as specified, exploiting Lucene classes for sorting.
  
- Added optimize options for Lucene indexing. The mergeFactor and maxBufferedDocs
  properties will affect performance as explained in the Lucene documentation.
  The optimize action of the updateIndex operation will perform the Lucene method 
  call IndexWriter.optimize(), which merges all segments together into a single segment, 
  optimizing an index for search. The optimize() is no longer called after each updateIndex.

- Added parameters to the indexDocXslt parameter of the updateIndex operation,
  enabling the transfer of param values into the indexing stylesheet.
				    
- Added untokenizedFields property to Lucene index.properties files.
  Adding the property with a list of all untokenized fields will
  ensure that they all select the appropriate analyzer.
				    
- Added properties snippetBegin and snippetEnd, making highlight code configurable.
				    
- Added property for custom URIResolver used by xslt transformers
  for basic authorization and SSL,
  see the example dk.defxws.fedoragsearch.server.URIResolverImpl class.
					
- Removed encoding of special characters in indexFields.
  Snippets now show special characters without modification.
  Indexes should be reindexed.

For examples, see the property files of the example configurations.

-------------------------------------------------------------------
