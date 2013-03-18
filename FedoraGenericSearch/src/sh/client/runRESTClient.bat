@echo off

set LIB=..\WEB-INF\lib
set JARS=%LIB%\fedora-client-3.1.jar;%LIB%\log4j-1.2.15.jar;%LIB%\activation-1.1.1.jar;%LIB%\axis.jar;%LIB%\commons-discovery.jar;%LIB%\commons-logging.jar;%LIB%\jaxrpc-api-1.1.jar;%LIB%\mail.jar;%LIB%\saaj-api-1.3.jar;%LIB%\lucene-core-2.4.0.jar;%LIB%\wsdl4j-1.5.1.jar;%LIB%\PDFBox-0.7.2.jar;%LIB%\xml-apis.jar

java -cp ..\WEB-INF\classes;%JARS% dk.defxws.fedoragsearch.client.RESTClient %1 %2 %3 %4 %5 %6 %7 %8