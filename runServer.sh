#!/bin/bash
MyDir="$( cd "$( dirname "$0" )" && pwd )"

#rmiregistry port is set to 5195 by default.

declare -i portnumber
portnumber=5194


#######################################################################
#
#######################################################################
function StartServer(){
echo Success
 killall -9 rmiregistry
 sleep 2

rmiregistry $portnumber &
  
  java  identity/server/IdServer --port $portnumber --verbose
}

########################################################################
#
########################################################################
function Main(){
StartServer
}

