#---------------------------------------------------------------
# Using this Makefile
#
#	To compile your java source (and generate documentation)
#
#	make 
#
#	To clean up your directory (e.g. before submission)
#
#	make clean
#
#---------------------------------------------------------------

JFLAGS=

# Recognize files with .class and .java extensions
.SUFFIXES: .class .java

# This is a rule to convert a file with .java extension
# into a file with a .class extension. The macro $< just
# supplies the name of the file (without the extension) 
# that invoked this rule.

.java.class:
	javac $(JFLAGS) $<

# To satisfy the rule named compile, we must have the  following 
# class files (with date no later than the source .java files).
# We also must have satisfied the rule named doc.

all: compile rmi

compile: identity/server/TimerListener.class identity/server/ServerOperations.class identity/server/ShutdownListener.class identity/server/UserInfo.class identity/server/Database.class identity/server/IdServer.class identity/client/IdClient.class election_algorithm/Message.class 	election_algorithm/Election.class election_algorithm/MessageHandlerThread.class election_algorithm/MessageReceiverThread.class 

rmi: compile
#	rmic identity.server.IdServer
#	cp identity/server/IdServer_Stub.class identity/client


# Run javadoc on all java source files in this directory.
# This rule depends upon the rule named html, which makes the
# html directory if does not already exist.

doc: html
	javadoc -private -author -version -d html/ *.java

# Make the html subdirectory.
html:
	mkdir html

clean:
		rm --force  identity/server/*.class
	        rm --force  identity/client/*.class
	        rm --force  election_algorithm/*.class
	
