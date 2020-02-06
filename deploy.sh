#!/usr/bin/env bash

set +e

TOMCAT_HOME=/home/dslaveykov/Downloads/apache-tomcat-8.5.37
DEBUG=true
DEBUG_PORT=8000
ATS_HTTP_DB_LOGGER_BASEDIR=/home/dslaveykov/Projects/github/master/ats-httpdblogger

function is_tomcat_started {
	TOMCAT_PID=`pgrep -f $TOMCAT_HOME/bin`
	return `$TOMCAT_PID | wc -l`
}

function stop_tomcat {
	TOMCAT_PID=`pgrep -f $TOMCAT_HOME/bin`
	if [ ! -z "$TOMCAT_PID" ];
	then
		kill -9 "$TOMCAT_PID"
	fi
	
}

function start_tomcat {
	TOMCAT_PID=`pgrep -f $TOMCAT_HOME/bin`
	if [ -z "$TOMCAT_PID" ];
	then
		if [ $DEBUG == true ];
		then
			export JPDA_ADDRESS=$DEBUG_PORT
			export JPDA_TRANSPORT=dt_socket
			$TOMCAT_HOME/bin/catalina.sh jpda start
		else
			$TOMCAT_HOME/bin/startup.sh
		fi
	fi
}

function restart_tomcat {
	stop_tomcat
	start_tomcat
}

function build_ats_httpdblogger {
	CURR_DIR=$PWD
	cd $ATS_HTTP_DB_LOGGER_BASEDIR
	mvn clean install
	cd $CURR_DIR
}

function deploy_ats_httpdblogger {
	WAR_FILE="$ATS_HTTP_DB_LOGGER_BASEDIR/target/ats-httpdblogger-4.0.7-SNAPSHOT.war"
	build_ats_httpdblogger
	cp $WAR_FILE $TOMCAT_HOME/webapps
}

function delete_tomcat_logs {
	rm -rf $TOMCAT_HOME/logs/*.*
	rm -rf $TOMCAT_HOME/logs/*
}

stop_tomcat
delete_tomcat_logs
deploy_ats_httpdblogger
start_tomcat
