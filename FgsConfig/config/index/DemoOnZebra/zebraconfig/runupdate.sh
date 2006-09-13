#! /bin/sh

# Run the fedoragsearch updateIndex operation on zebra

get_timestamp(){
    echo "`date '+20%y-%m-%d %T'`"
}

usage(){
    echo "Usage:"
    echo "    runupdate createEmpty"
    echo "    runupdate updatePid <pid>"
    echo "    runupdate update"
    echo "    runupdate deletePid <pid>"
}

kill_process(){
    export PID=`ps ax | grep "$1"  | grep -v grep | perl -ne '$_ =~ /^(.{6})/; print $1'`
    echo "kill '$1' PID=$PID"
    kill -9 $PID
}

if [ $# \< 1 ] ; then
    usage
    exit 1
fi;
    
if [ ! -f db/lock/zebrasrv.pid ] ; then

  echo "<`get_timestamp`> zebrasrv-2.0 -f db/yazserver.xml -l server.log &"
  zebrasrv-2.0 -f db/yazserver.xml -l server.log &
    
fi;

case "$1" in

  createEmpty)

#    if [ ! $# == 1 ] ; then
#        usage
#        exit 1
#    fi;
  
    echo "<`get_timestamp`> zebraidx-2.0 -c db/zebra.cfg init"
    zebraidx-2.0 -l indexer.log -c db/zebra.cfg init

    ;;

  updatePid)
  
    export FILENAME=`echo "$2" | sed -e s/:/_/`
    echo "<`get_timestamp`> zebraidx-2.0 -c db/zebra.cfg update temp_records/$FILENAME"
    zebraidx-2.0 -l indexer.log -c db/zebra.cfg update temp_records/$FILENAME
    echo "<`get_timestamp`> zebraidx-2.0 -c db/zebra.cfg commit"
    zebraidx-2.0 -l indexer.log -c db/zebra.cfg commit
    rm temp_records/*
  
    ;;

  update)
  
    echo "<`get_timestamp`> zebraidx-2.0 -c db/zebra.cfg update temp_records"
    zebraidx-2.0 -l indexer.log -c db/zebra.cfg update temp_records
    echo "<`get_timestamp`> zebraidx-2.0 -c db/zebra.cfg commit"
    zebraidx-2.0 -l indexer.log -c db/zebra.cfg commit
    rm temp_records/*
  
    ;;

  deletePid)
  
    export FILENAME=`echo "$2" | sed -e s/:/_/`
    rm temp_records/*
    echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" >> temp_records/$FILENAME
    echo "<IndexDocument PID=\"$2\">" >> temp_records/$FILENAME
    echo "<IndexField IFname=\"PID\">" >> temp_records/$FILENAME
    echo "$2" >> temp_records/$FILENAME
    echo "</IndexField>" >> temp_records/$FILENAME
    echo "</IndexDocument>" >> temp_records/$FILENAME
    echo "<`get_timestamp`> zebraidx-2.0 -c db/zebra.cfg delete temp_records/$FILENAME"
    zebraidx-2.0 -l indexer.log -c db/zebra.cfg delete temp_records/$FILENAME
    echo "<`get_timestamp`> zebraidx-2.0 -c db/zebra.cfg commit"
    zebraidx-2.0 -l indexer.log -c db/zebra.cfg commit
    rm temp_records/*
  
    ;;

  stop)

#    kill_process "runupdate"

    ;;

  test)

#    $0 createEmpty
#    sleep 1
    export XSLTPATH=/home/gsp/fedora-2.1.1/server/jakarta-tomcat-5.0.28/webapps/fedoragsearch/WEB-INF/classes/config/index/DemoOnLucene/demoFoxmlToLucene.xslt
    export OBJECTPATH=/home/gsp/fedora-2.1.1/data/objects/2006/0516/12/24
    xsltproc $XSLTPATH $OBJECTPATH/$2 > temp_records/$2
    $0 updatePid $2

    ;;

  *)
    get_timestamp ;
    usage ;
    exit 1;
esac

exit 0


