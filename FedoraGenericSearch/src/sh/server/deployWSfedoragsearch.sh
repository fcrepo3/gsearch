#!/bin/sh
# $Id$
# ----------------------------------------------------------------------
# Fedora Generic Search Web Service deployment script.
# ----------------------------------------------------------------------

# ----------------------------------------------------------------------
# Environment setup

# Reset the input field separator to its default value
IFS=

# Reset the execution path 
#PATH=/bin:/usr/bin:/usr/local/bin:/opt/bin

# Cannot proceed if FEDORA_HOME is not set
#if [ -z "$FEDORA_HOME" ]; then
#	echo "ERROR: The FEDORA_HOME environment variable is not defined."
#	exit 1
#fi

#FEDORAGSEARCH_HOME="$FEDORA_HOME"/services/fedoragsearch

# FEDORA_JAVA_HOME or JAVA_HOME
if [ -z "${FEDORA_JAVA_HOME:=$JAVA_HOME}" ]; then
	echo "ERROR: neither FEDORA_JAVA_HOME nor JAVA_HOME is defined."
    exit 1
fi

if [ ! -x "$FEDORA_JAVA_HOME"/bin/java -o ! -x "$FEDORA_JAVA_HOME"/bin/orbd ]; then
    echo "Neither FEDORA_JAVA_HOME nor JAVA_HOME appears to be set correcty."
    echo "Make sure FEDORA_JAVA_HOME or JAVA_HOME points to a 1.4JRE/JDK base."
	exit 1
fi

# If set, JAVA_HOME will be restored at the end of the script
JAVA_HOME_BACKUP="$JAVA_HOME"
JAVA_HOME="$FEDORA_JAVA_HOME"
export JAVA_HOME
JAVA="$JAVA_HOME"/bin/java

# Restore JAVA_HOME to its original prior, if any
restoreJavaHome() {
	if [ -z "$JAVA_HOME_BACKUP" ]; then
		export JAVA_HOME="$JAVA_HOME_BACKUP"
	fi
}

TC_BASENAME="jakarta-tomcat-5.0.28"
TC="$FEDORA_HOME"/server/"$TC_BASENAME"
echo "TC=$TC"
TC_COMMON="$TC"/common/lib
TC_ENDORSED="$TC"/common/endorsed
AXIS_UTILITY_LIBS=$TC_COMMON/axis.jar:$TC_COMMON/commons-discovery.jar:$TC_COMMON/commons-logging.jar:$TC_COMMON/jaxrpc.jar:$TC_COMMON/saaj.jar:$TC_COMMON/tt-bytecode.jar:$TC_COMMON/wsdl4j.jar:$TC_COMMON/xercesImpl.jar:$TC_COMMON/xml-apis.jar

LOPTION="-lhttp://alvis.cvt.dk:$2/fedoragsearch/soap"
echo "LOPTION=$LOPTION"
UWOPTIONS="-uFedoraAdmin -wwe4Yahgo"
WSDDPATH="$TC"/webapps/fedoragsearch/src/wsdd

# ----------------------------------------------------------------------
# Functions

start() {

	# Tomcat must be running, started by fedora-start

	echo "Deploying FedoraGSearchAPI..."
	(exec "$JAVA" -cp "$AXIS_UTILITY_LIBS":"$TC"/webapps/fedoragsearch/WEB-INF/classes:"$TC"/webapps/fedoragsearch/WEB-INF/lib/fedora-server-2.0.jar \
	            -Dfedora.home="$FEDORA_HOME" \
	            -Djavax.xml.parsers.DocumentBuilderFactory=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl \
	            -Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl \
	            org.apache.axis.client.AdminClient "$LOPTION" "$WSDDPATH"/deploy.wsdd "$UWOPTIONS")
	trap "Error deploying (see above)... to stop the server, use deployWSfedoragsearch stop." 1 2 15

#	(exec "$JAVA" -cp "$AXIS_UTILITY_LIBS":"$TC"/webapps/fedoragsearch/WEB-INF/classes:"$TC"/webapps/fedoragsearch/WEB-INF/lib/fedora-server-2.0.jar \
#	            -Dfedora.home="$FEDORA_HOME" \
#	            -Djavax.xml.parsers.DocumentBuilderFactory=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl \
#	            -Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl \
#	            org.apache.axis.client.AdminClient list)
#	trap "Error deploying (see above)... to stop the server, use deployWSfedoragsearch stop." 1 2 15
	
	restoreJavaHome
}

stop() {
	echo "Undeploying FedoraGSearchAPI..."
	(exec "$JAVA" -cp "$AXIS_UTILITY_LIBS":"$TC"/webapps/fedoragsearch/WEB-INF/classes:"$TC"/webapps/fedoragsearch/WEB-INF/lib/fedora-server-2.0.jar \
	            -Dfedora.home="$FEDORA_HOME" \
	            -Djavax.xml.parsers.DocumentBuilderFactory=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl \
	            -Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl \
	            org.apache.axis.client.AdminClient "$LOPTION" "$WSDDPATH"/undeploy.wsdd "$UWOPTIONS")
	trap "Error undeploying (see above)... to stop the server, use deployWSfedoragsearch stop." 1 2 15

	restoreJavaHome
}

restart() {
	stop
	start "$@"
	restoreJavaHome
}

# ----------------------------------------------------------------------
# 

case "$1" in
	start)
		shift
		start "$@"
		;;
	stop)
		shift
		stop "$@"
		;;
	restart)
		shift
		restart "$@"
		;;
	*)
		echo "Usage: $0 {start|stop|restart|status}"
		;;
esac
