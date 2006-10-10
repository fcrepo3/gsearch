#!/bin/sh
#$Id$

# <p><b>License and Copyright: </b>The contents of this file will be subject to the
# same open source license as the Fedora Repository System at www.fedora.info
# It is expected to be released with Fedora version 2.2.

# Copyright &copy; 2006 by The Technical University of Denmark.
# All rights reserved.</p>

# @author  gsp@dtv.dk
# @version 

sh runRESTClient.sh localhost:8080 configure configDemoOnLucene
sh runRESTClient.sh localhost:8080 getRepositoryInfo
sh runRESTClient.sh localhost:8080 getIndexInfo
sh runRESTClient.sh localhost:8080 updateIndex createEmpty
sh runRESTClient.sh localhost:8080 updateIndex fromPid pid
sh runRESTClient.sh localhost:8080 updateIndex fromFoxmlFiles SindapOnLucene filePath
sh runRESTClient.sh localhost:8080 browseIndex a dc.title
sh runRESTClient.sh localhost:8080 gfindObjects metal
