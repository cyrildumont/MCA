#!/bin/sh
# Autor: Cyril Dumont


PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`

MCA_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

PIDFILE=$MCA_HOME/mcaserver.pid 
NAME=$PRG 
MCA_USER=`whoami`

MCA_MAIN="org.mca.startup.Bootstrap"

OPTIONS=" -wait 10 -user $MCA_USER -Dmca.home=$MCA_HOME -Djava.security.policy=$MCA_HOME/conf/security/server.policy -Dcom.sun.management.jmxremote.port=9099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.util.logging.config.file=$MCA_HOME/conf/logging.properties -Djavax.net.ssl.trustStore=$MCA_HOME/conf/security/keystore.server -Djava.security.auth.login.config=$MCA_HOME/conf/security/server.login -cp ./commons-daemon.jar:./bootstrap.jar:./commons-logging-1.0.4.jar:./log4j-1.2.14.jar:../conf/ -outfile $MCA_HOME/logs/mcaserver.out -errfile $MCA_HOME/logs/mcaserver.err -pidfile $PIDFILE"
  	
start(){ 
	echo -n "Starting MCASercer daemon: " 
	#modprobe capability 
	cd $MCA_HOME/bin
	./jsvc $OPTIONS $MCA_MAIN $MCA_HOME/conf/mca-server.xml
	RETVAL=$? 
	[ $RETVAL = 0 ] && echo "success" || echo "failure" 
	echo
	return $RETVAL 
} 

stop(){ 
	echo -n "Stopping MCAServer daemon: " 	
	cd $MCA_HOME/bin
	./jsvc -stop -pidfile $PIDFILE $MCA_MAIN
	RETVAL=$? 
	[ $RETVAL = 0 ] && echo "success" || echo "failure" 
	echo
	return $RETVAL 
} 

case "$1" in 
	start) 
		start 
	;; 
	stop) 
		stop 
	;; 
	restart|reload) 
		stop 
		sleep 10 
		start 
	;; 
	
	*) 
		echo "Usage: $NAME {start|stop|restart}" 
		exit 1 
	;; 
esac 

exit 0
