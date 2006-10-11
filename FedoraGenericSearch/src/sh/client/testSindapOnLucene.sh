#!/bin/sh
#$Id$

# <p><b>License and Copyright: </b>The contents of this file will be subject to the
# same open source license as the Fedora Repository System at www.fedora.info
# It is expected to be released with Fedora version 2.2.

# Copyright &copy; 2006 by The Technical University of Denmark.
# All rights reserved.</p>

# @author  gsp@dtv.dk
# @version 

# The script assumes that a Fedora repository is running on localhost:8080,
# and that the Fedora demo objects have been ingested into the repository.

get_timestamp(){
    echo "`date '+20%y-%m-%d %T'`"
}

usage(){
    echo "Usage:"
    echo "    ./testDemoOnLucene [expected|test]"
}

run_test(){
    echo "<`get_timestamp`>$CLIENT $1 $2 $3 $4 $5 $6 $7 $8 $9 "
    TESTCOUNT=`expr $TESTCOUNT "+" 1`
    FILENAME=$CLIENT.$TESTCOUNT
    if [ $MODE == "expected" ] ; then
        sh $CLIENT $HOSTPORT $1 $2 $3 $4 $5 $6 $7 $8 $9 > $SUITE/expected/$FILENAME
    else
        sh $CLIENT $HOSTPORT $1 $2 $3 $4 $5 $6 $7 $8 $9 > $SUITE/result/$FILENAME
        diff -I.*milliseconds.* -I.*resultPage.* $SUITE/expected/$FILENAME $SUITE/result/$FILENAME > $SUITE/diff/$FILENAME
        if [ ! `cat testDemoOnLucene/diff/$FILENAME | wc -c` = 0 ] ; then 
            cat testDemoOnLucene/diff/$FILENAME; 
            DIFFCOUNT=`expr $DIFFCOUNT "+" 1`
        fi;
    fi;
}

if [ ! $# == 1 ] ; then
    usage
    exit 1
fi;

PWDDIR=`pwd`

HOSTPORT=localhost:8080
SUITE=testDemoOnLucene
CLIENT=runRESTClient.sh
MODE=$1
TESTCOUNT=0
DIFFCOUNT=0

if [ ! -d $SUITE ] ; then
    mkdir $SUITE
fi;

if [ $MODE == "expected" ] ; then
    rm -r $SUITE/expected
    mkdir $SUITE/expected
fi;

rm -r $SUITE/result
rm -r $SUITE/diff
mkdir $SUITE/result
mkdir $SUITE/diff

run_test configure configDemoOnLucene
run_test getRepositoryInfo
run_test getIndexInfo
run_test updateIndex createEmpty
run_test updateIndex fromPid demo:10
run_test updateIndex fromFoxmlFiles
run_test browseIndex a dc.title
run_test gfindObjects fedora

echo "<`get_timestamp`>TESTCOUNT=$TESTCOUNT"
echo "<`get_timestamp`>DIFFCOUNT=$DIFFCOUNT"

CLIENT=runSOAPClient.sh

TESTCOUNT=0
DIFFCOUNT=0

run_test getRepositoryInfo
run_test getIndexInfo
run_test updateIndex createEmpty
run_test updateIndex fromPid demo:11
run_test updateIndex fromFoxmlFiles
run_test browseIndex a dc.title
run_test gfindObjects fedora

echo "<`get_timestamp`>TESTCOUNT=$TESTCOUNT"
echo "<`get_timestamp`>DIFFCOUNT=$DIFFCOUNT"
