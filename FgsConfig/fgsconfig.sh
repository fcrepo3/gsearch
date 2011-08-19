#!/bin/sh
#$Id fgsconfig.sh $
 
# <p><b>License and Copyright: </b>The contents of this file will be subject to the
# same open source license as the Fedora Repository System.

# Copyright &copy; 2011 by The Technical University of Denmark.
# All rights reserved.</p>

# @author  gsp@dtic.dtu.dk
# @version 2.3

add_to_prop_file ()
{
propfilename=fgsconfig$addtopropfilename-input.properties
echo "\n$1" >> $propfilename
}

get_input_with_default ()
{
propfilename=fgsconfig$addtopropfilename-input.properties
defaultedinput=$2
while [ true ]
do
 echo "\n# $3\n"
 read -p "$1 [$2]: " input
 if [ "$input" ]
 then
  defaultedinput="$input"
 fi 
 if [ "$defaultedinput" ]
 then
  break
 else
  echo "\nMUST NOT BE EMPTY!"
 fi
done
echo "\n# $3" >> $propfilename
echo "$1=$defaultedinput" >> $propfilename
}



echo "\n*****************************************************"
echo "Basic FedoraGenericSearch (GSearch) 2.3 Configuration"
echo "*****************************************************"

echo "\nAssumption: You have copied the directory FgsConfig from .../webapps/fedoragsearch to a location, where you have write permission, and where you are now."

echo "\nHere is a file list with explanations:\n "

echo "\nfgsconfig.sh                        The shell script that you are running now\n "

echo "\nfgsconfig-default.properties        The default initial property values - for the configuration overall\n "

echo "\nfgsconfig-repos-default.properties                                      - for the repository configuration\n "

echo "\nfgsconfig-index-default.properties                                      - for the index configuration\n "

echo "\nfgsconfig.xml                       The ant script that will insert property values into your final config files\n "

echo "\nTo configure GSearch, please enter the following properties, default value is in []."

echo "\nIn the simplest case, you may choose default values all the way.\n "

if [ -f fgsconfig-input.properties ]
then
  cp -p fgsconfig-input.properties fgsconfig-input.properties.backup
  read -p "Do you want to reuse from your previous input [yes/no]: " previousinputyesno
  if [ $previousinputyesno -a $previousinputyesno = 'no' ]
  then
    . fgsconfig-default.properties
  else
    . fgsconfig-input.properties
  fi
else
  . fgsconfig-default.properties
fi

addtopropfilename=

echo "# file.name=fgsconfig-input.properties\n" > fgsconfig-input.properties

add_to_prop_file "# begin.time=`date`"

get_input_with_default "gsearchBase"            "$gsearchBase"           "gsearchBase is used for SOAP deployment."
get_input_with_default "gsearchAppName"         "$gsearchAppName"        "gsearchAppName is used for SOAP deployment."
get_input_with_default "gsearchUser"            "$gsearchUser"           "gsearchUser is used for SOAP deployment."
get_input_with_default "gsearchPass"            "$gsearchPass"           "gsearchPass is used for SOAP deployment."

finalConfigPath=$CATALINA_HOME/webapps/fedoragsearch/WEB-INF/classes
get_input_with_default "finalConfigPath"            "$finalConfigPath"            "finalConfigPath must be in the classpath of the web server."
finalConfigPath=$defaultedinput
finalConfigName=FgsConfigFinal
templateConfigPath=.
templateConfigName=FgsConfigTemplate
add_to_prop_file "# finalConfigName is the dir name that is to contain the instantiated final config for GSearch, which the GSearch Config.java class looks for in classpath at runtime."
add_to_prop_file "finalConfigName=$finalConfigName"
add_to_prop_file "# templateConfigPath is the path to the template config for GSearch."
add_to_prop_file "templateConfigPath=$templateConfigPath"
add_to_prop_file "# templateConfigName is the dir name that contains the template config for GSearch."
add_to_prop_file "templateConfigName=$templateConfigName"

logFilePath=$FEDORA_HOME/server/logs
get_input_with_default "logFilePath"            "$logFilePath"           "logFilePath is where to find the log file."

get_input_with_default "logLevel"               "$logLevel"              "logLevel can be DEBUG, INFO, WARN, ERROR, FATAL."

get_input_with_default "namesOfRepositories"    "$namesOfRepositories"   "namesOfRepositories separated by space."
namesOfRepositories=$defaultedinput

get_input_with_default "namesOfIndexes"            "$namesOfIndexes"       "namesOfIndexes separated by space."
namesOfIndexes=$defaultedinput

add_to_prop_file "# input.end.time=`date`"
add_to_prop_file "# Now inserting the property values into the config files with ant ..."

echo "\nThe resulting property set is in fgsconfig-input.properties"
echo "\nNow inserting the property values into the config files with ant ..."

ant -f fgsconfig.xml
  
add_to_prop_file "# repos.begin.time=`date`"


for reposName in $namesOfRepositories ; do 

  echo "\n***** Now repository $reposName *****"

  if [ -f fgsconfig-repos-$reposName-input.properties ]
  then
    cp -p fgsconfig-repos-$reposName-input.properties fgsconfig-repos-$reposName-input.properties.backup
    if [ $previousinputyesno -a $previousinputyesno = 'no' ]
    then
      . fgsconfig-repos-default.properties
    else
      . fgsconfig-repos-$reposName-input.properties
    fi
  else
    . fgsconfig-repos-default.properties
  fi
  
  addtopropfilename=-repos-$reposName

  echo "# file.name=fgsconfig-repos-$reposName-input.properties\n" > fgsconfig-repos-$reposName-input.properties
  
  add_to_prop_file "# repos.begin.time=`date`"
  
  add_to_prop_file "# templateConfigReposPath is the dir name that contains the template repos config for GSearch."
  add_to_prop_file "templateConfigReposPath=$templateConfigPath/FgsConfigReposTemplate"
  
  add_to_prop_file "finalConfigReposPath=$finalConfigPath/$finalConfigName/repository/$reposName"

  get_input_with_default "fedoraBase"              "$fedoraBase"         "$reposName: fedoraBase is base url of this repository."
  get_input_with_default "fedoraAppName"           "$fedoraAppName"      "$reposName: fedoraAppName is Fedora app name of this repository."
  get_input_with_default "fedoraUser"              "$fedoraUser"         "$reposName: fedoraUser is the user name to access this repository."
  get_input_with_default "fedoraPass"              "$fedoraPass"         "$reposName: fedoraPass is the password to access this repository."
  get_input_with_default "fedoraVersion"           "$fedoraVersion"      "$reposName: fedoraVersion is the Fedora version of this repository."
  get_input_with_default "objectStoreBase"         "$objectStoreBase"    "$reposName: objectStoreBase must be the \"object_store_base\" value from fedora.fcfg."

  add_to_prop_file "# input.end.time=`date`"
  add_to_prop_file "# Now inserting the property values into the config files with ant ..."
  
  echo "\nThe resulting property set is in fgsconfig-repos-$reposName-input.properties"
  echo "\nNow inserting the property values into the config files with ant ..."

  ant -f fgsconfig-repos.xml -DreposName=$reposName
					
  add_to_prop_file "# repos.end.time=`date`"

done



addtopropfilename=

add_to_prop_file "# repos.end.time=\"`date`\""
  
add_to_prop_file "# index.begin.time=`date`"

for indexName in $namesOfIndexes ; do 

  echo "\n***** Now index $indexName *****"

  if [ -f fgsconfig-index-$indexName-input.properties ]
  then
    cp -p fgsconfig-index-$indexName-input.properties fgsconfig-index-$indexName-input.properties.backup
    if [ $previousinputyesno -a $previousinputyesno = 'no' ]
    then
      . fgsconfig-index-default.properties
    else
      . fgsconfig-index-$indexName-input.properties
    fi
  else
    . fgsconfig-index-default.properties
  fi
  
  addtopropfilename=-index-$indexName

  echo "# file.name=fgsconfig-index-$indexName-input.properties\n" > fgsconfig-index-$indexName-input.properties
  
  add_to_prop_file "# index.begin.time=`date`"
  
  add_to_prop_file "# templateConfigIndexPath is the dir name that contains the template index config for GSearch."
  add_to_prop_file "templateConfigIndexPath=$templateConfigPath/FgsConfigIndexTemplate"
  
  add_to_prop_file "finalConfigIndexPath=$finalConfigPath/$finalConfigName/index/$indexName"

  indexPath=$FEDORA_HOME/gsearch/$indexName
  get_input_with_default "indexPath"               "$indexPath"            "$indexName: indexPath is the path to the index."

  add_to_prop_file "# input.end.time=`date`"
  add_to_prop_file "# Now inserting the property values into the config files with ant ..."
  
  echo "\nThe resulting property set is in fgsconfig-index-$indexName-input.properties"
  echo "\nNow inserting the property values into the config files with ant ..."

  ant -f fgsconfig-index.xml -DindexName=$indexName
					
  add_to_prop_file "# index.end.time=`date`"

done

addtopropfilename=

add_to_prop_file "# index.end.time=\"`date`\""

echo "\nYou should read the final config files."

echo "\nYou may run this script again, if you want to change some of these property values."

echo "\nThere are many more property values in the final config files."

echo "\nYou may edit the final config files, but such changes will be overwritten, if you run this script again."

echo "\nChanges to the final config files take effect after restart.\n"

#... Prompt for a foxml file, used to generate the indexing stylesheet (foxmlToLucene.xslt or foxmlToSolr.xslt) ...!
#... which may be used next to generate REST xslt for end-user interface ...!

add_to_prop_file "# end.time=\"`date`\""

#get_input_with_default "fedoragsearchVersion"  "$fedoragsearchVersion"  "fedoragsearch.version is the GSearch version that you configure now."

fedoraHome=$FEDORA_HOME
#get_input_with_default "fedora.home"            "$fedoraHome"            "fedora.home is per default set to \$FEDORA_HOME."
fedoraHome=$defaultedinput

webserverHome=$CATALINA_HOME
#get_input_with_default "webserver.home"         "$webserverHome"         "webserver.home is per default set to \$CATALINA_HOME."

fedoragsearchHome=$fedoraHome/gsearch
#get_input_with_default  "fedoragsearch.home"    "$fedoragsearchHome"     "fedoragsearch.home is per default under \$fedora.home."
fedoragsearchHome=$defaultedinput
