#!/bin/sh
#$Id$

# <p><b>License and Copyright: </b>The contents of this file will be subject to the
# same open source license as the Fedora Repository System at www.fedora.info
# It is expected to be released with Fedora version 2.2.

# Copyright &copy; 2006 by The Technical University of Denmark.
# All rights reserved.</p>

# @author  gsp@dtv.dk
# @version 

        if [ -z "$fgsUserName" ] ; then 
            echo -n "GSearch user name: "
            read fgsUserName
            echo -n "Password: "
            stty -echo
            read fgsPassword
            stty echo
            if [ -z "$fgsPassword" ] ; then 
                fgsPassword=$fgsUserName
            fi;
            echo ""
        fi;
    
LIB=../WEB-INF/lib
JARS=$LIB/log4j-1.2.15.jar:$LIB/activation-1.1.1.jar:$LIB/axis.jar:$LIB/commons-discovery.jar:$LIB/commons-logging.jar:$LIB/jaxrpc-api-1.1.jar:$LIB/mail.jar:$LIB/saaj-api-1.3.jar:$LIB/lucene-core-2.4.0.jar:$LIB/wsdl4j-1.5.1.jar:$LIB/PDFBox-0.7.2.jar:$LIB/xml-apis.jar

java -Xms64m -Xmx96m -Dfedoragsearch.fgsUserName=$fgsUserName -Dfedoragsearch.fgsPassword=$fgsPassword -cp ../WEB-INF/classes:$JARS dk.defxws.fedoragsearch.client.RESTClient "$1" "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"