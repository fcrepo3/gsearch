# file.name=fgsconfig-basic-README.txt

**********************************************************************
Basic FedoraGenericSearch (GSearch) 2.3 Installation and Configuration
**********************************************************************

Installation was simply dumping fedoragsearch.war into tomcat webapps.

After tomcat unpacking copy the directory FgsConfig from 
.../webapps/fedoragsearch to a location outside of tomcat, 
where you have write permission, and where you are now.

To configure GSearch, please edit the file fgsconfig-basic.properties,
it has default property values and further instructions.

Then create the final config files by running

  >ant -f fgsconfig-basic.xml
  
This will use the property values in fgsconfig-basic.properties 
and insert them into the copies of the template config files,
that will make up the final config files.
  
The final config files must be located in tomcat classpath,
in order that GSearch can find them at startup.
By default webapps/fedoragsearch/WEB-INF/classes is in tomcat classpath.
Alternatively, you may add another classpath location to tomcat
in catalina.properties .

You should read through the final config files.
You may edit all the properties of the final config files.
If you do edit them, be sure to keep a copy outside of the tomcat.
