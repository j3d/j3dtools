#
# Top Level makefile for the j3d.org code repository.
#
# To use this, make sure that you have the PROJECT_ROOT environment variable
# set 
#
# This makefile is designed to build the entire library from scratch. It is
# not desigend as a hacking system. It is recommended that you use the normal
# javac/CLASSPATH setup for that. 
#
# The following commands are offered:
#
# - class:  Compile just the classes. Don't make JAR file 
# - jar:      Make the java JAR file 
# - javadoc:  Generate the javadoc information
# - all:      Build everything (including docs)
# - clean:    Blow away the .class files
#

ifndef PROJECT_ROOT
export PROJECT_ROOT=/usr/local/src/projects/j3d.org/code
endif

include $(PROJECT_ROOT)/make/Makefile.inc

VERSION=1.1

all: class libs jar javadoc

class:
	cd $(JAVA_DIR) && make buildall

jar:
	cd $(JAVA_DIR) && make jar

javadoc:
	cd $(JAVA_DIR) && make javadoc

libs:
	cd $(JAVA_DIR) && make nativeall
	cd $(NATIVE_DIR) && make buildall
    
clean:
	cd $(JAVA_DIR) && make clean
	cd $(NATIVE_DIR) && make clean	
