@echo off

set LIB=..\WEB-INF\lib
set JARS=%LIB%\fedora-3.0-client.jar;%LIB%\log4j-1.2.14.jar;%LIB%\activation-1.0.2.jar;%LIB%\axis.jar;%LIB%\commons-discovery.jar;%LIB%\commons-logging.jar;%LIB%\jaxrpc.jar;%LIB%\mail.jar;%LIB%\saaj.jar;%LIB%\lucene-core-1.9.1.jar;%LIB%\wsdl4j-1.5.1.jar;%LIB%\PDFBox-0.7.2.jar;%LIB%\xml-apis.jar

java -cp ..\WEB-INF\classes;%JARS% dk.defxws.fedoragsearch.client.RESTClient %1 %2 %3 %4 %5 %6 %7 %8