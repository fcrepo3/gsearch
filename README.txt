This is the source tree for the Fedora Generic Search service.
It currently works with Fedora 3.1.

To build the downloadable war, which includes
support for Lucene, Solr, and Zebra plugins:

  cd FedoraGenericSearch
  ant builddownload

After building the downloadable war, documentation
can be viewed locally from FgsBuild/webapp/index.html

To run tests:

  startup an empty Fedora 3.1 with GSearch 2.2 on localhost:8080 with MessagingModule enabled
  fedora-ingest-demos.sh
  cd FedoraGenericSearch
  
  basic operations on the lucene plugin:
  
    ant junit-lucene
  
  sortFields and updateIndex operations on the lucene plugin:
  
    ant junit-testsonlucene
    
  sort result filtering operations on the lucene plugin:
  
    see FedoraGenericSearch/src/test/junit/gsearch/test/searchresultfiltering/TestSearchResultFiltering.java
  
    ant junit-searchresultfiltering
    
  basic operations on the solr plugin:
  
    startup the solr server:
      cd $FEDORA_HOME/gsearch/DemoOnSolr/example
      java -jar start.jar
      
    ant junit-solr
    
  basic operations on the zebra plugin:
  
    install, configure and startup the zebra server:
    
      see $FEDORA_HOME/tomcat/webapps/fedoragsearch/WEB-INF/classes/configDemoOnZebra/index/DemoOnZebra/zebraconfig/README

    ant junit-zebra
    
  all tests:
  
    ant junit-all

To clean up all build-generated files:

  cd FedoraGenericSearch
  ant clean

For more information, see the following web page:

http://fedora-commons.org/confluence/display/FCSVCS/Generic+Search+Service+2.2
