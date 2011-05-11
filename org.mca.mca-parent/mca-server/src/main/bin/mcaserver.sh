#!/bin/sh
# Autor: Cyril Dumont

MCA_HOME="${mca.home}"
PIDFILE=$MCA_HOME/mcacore.pid 
NAME=mcaserver 
MCA_USER=${jsvc.user}

OPTIONS="-user $MCA_USER -Dmca.home=$MCA_HOME -Djava.security.policy=$MCA_HOME/conf/server.policy -Dcom.sun.management.jmxremote.port=9099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -cp ./commons-daemon.jar:./bootstrap.jar:./commons-logging-1.0.4.jar:./log4j-1.2.14.jar:../conf/ -outfile $MCA_HOME/logs/mcaserver.out -errfile $MCA_HOME/logs/mcaserver.err -pidfile $PIDFILE"
  	
start(){ 
	echo -n "Starting MCASercer daemon: " 
	#modprobe capability 
	cd $MCA_HOME/bin
	./jsvc $OPTIONS org.mca.startup.Bootstrap $MCA_HOME/conf/mca-server.xml
	RETVAL=$? 
	[ $RETVAL = 0 ] && touch /var/lock/$NAME 
	[ $RETVAL = 0 ] && echo "success" || echo "failure" 
	echo 
	return $RETVAL 
} 

stop(){ 
	echo -n "Stopping MCAServer daemon: " 
	PID=`cat $PIDFILE` 
	kill -9 $PID 
	RETVAL=$? 
	[ $RETVAL = 0 ] && rm /var/lock/$NAME 
	[ $RETVAL = 0 ] && rm $PIDFILE 
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
		echo "Usage: /etc/init.d/$NAME {start|stop|restart}" 
		exit 1 
	;; 
esac 

exit 0
