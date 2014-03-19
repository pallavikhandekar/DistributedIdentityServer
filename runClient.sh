#!/bin/bash
MyDir="$( cd "$( dirname "$0" )" && pwd )"


case $# in
1)
querytype=$1;;
*) echo "Usage: "`basename $0`<Query>"; exit 1;;
esac


#######################################################################
#
#######################################################################
function create(){

echo creating user
  java identity/client/IdClient --create <username>
  #java identity/client/IdClient --create <username> <realname>
  #java identity/client/IdClient   --create <username> --password <password>
  #java identity/client/IdClient --create <username> <realname> --password <password>
}
########################################################################
#
########################################################################
function modify()
{
echo modifying user
 java identity/client/IdClient --modify <oldname> <newname>
 #java identity/client/IdClient --modify <oldname> <newname> --password <password>
}
########################################################################
#
########################################################################
function delete()
{
echo deleting user
 java identity/client/IdClient --delete <username> 
 #java identity/client/IdClient --delete <username> --password <password>
}
########################################################################
#
########################################################################
function lookup()
{
echo lookinup user

 java identity/client/IdClient --lookup <username>

}
########################################################################
#
########################################################################
function reverselookup()
{
echo reverse looking up user
 java identity/client/IdClient --reverse-lookup <uuid>
}
########################################################################
#
########################################################################
function getUsers()
{
 java identity/client/IdClient -g users
}
########################################################################
#
########################################################################
function getUuids()
{
 java identity/client/IdClient -g uuids
}
########################################################################
#
########################################################################
function getAll()
{
 java identity/client/IdClient -g all
}
########################################################################
#
########################################################################
function Main(){


case $querytype in
"create") create;;
"modify") modify;;
"delete") delete;;
"lookup") lookup;;
"reverse-lookup") reverselookup;;
"getall") getAll;;
"getusers") getUsers;;
"getuuids") getUuids;;
esac

}

########################################################################
#Begin calling the Main function
########################################################################
Main
exit 0
#!/bin/bash
MyDir="$( cd "$( dirname "$0" )" && pwd )"


case $# in
2) 
hostname=$1;
querytype=$2;;
*) echo "Usage: "`basename $0` " <server hostname> <Query>"; exit 1;;
esac


#######################################################################
#
#######################################################################
function create(){

echo creating user
  java identity/client/IdClient $hostname --create pkhandekar --password password
  #java identity/client/IdClient localhost --create <username> <realname>
  #java identity/client/IdClient localhost --create <username> --password <password>
  #java identity/client/IdClient localhost --create <username> <realname> --password <password>
}
########################################################################
#
########################################################################
function modify()
{
echo modifying user
 java identity/client/IdClient localhost --modify pkhandekar newpkhandekar --password password
 #java identity/client/IdClient localhost --modify <oldname> <newname> --password <password>
}
########################################################################
#
########################################################################
function delete()
{
echo deleting user
 java identity/client/IdClient localhost --delete pkhandekar --password password
 #java identity/client/IdClient localhost --delete <username> --password <password>
}
########################################################################
#
########################################################################
function lookup()
{
echo lookinup user

 #lookup before modification
 #java identity/client/IdClient localhost --lookup pkhandekar

 #lookup after modification
 java identity/client/IdClient localhost --lookup newpkhandekar

}
########################################################################
#
########################################################################
function reverselookup()
{
echo reverse looking up user
 java identity/client/IdClient localhost --reverse-lookup 02c5c94f-7bea-4777-a09c-cc045643a642
}
########################################################################
#
########################################################################
function getUsers()
{
 java identity/client/IdClient localhost -g users
}
########################################################################
#
########################################################################
function getUuids()
{
 java identity/client/IdClient localhost -g uuids
}
########################################################################
#
########################################################################
function getAll()
{
 java identity/client/IdClient localhost -g all
}
########################################################################
#
########################################################################
function Main(){


case $querytype in
"create") create;;
"modify") modify;;
"delete") delete;;
"lookup") lookup;;
"reverse-lookup") reverselookup;;
"getall") getAll;;
"getusers") getUsers;;
"getuuids") getUuids;;
esac

}

########################################################################
#Begin calling the Main function
########################################################################
Main
exit 0
