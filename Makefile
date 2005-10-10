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
# - clean:    Blow everything away
#

ifndef PROJECT_ROOT
export PROJECT_ROOT=/usr/local/src/projects/j3d.org/code
endif

include $(PROJECT_ROOT)/make/Makefile.inc

VERSION=0.1

help:
	$(PRINT) 
	$(PRINT) "                   The Xj3D Project"
	$(PRINT) 
	$(PRINT) "More information on this project can be found at http://www.xj3d.org"
	$(PRINT) 
	$(PRINT) "The following options are offered and will build the entire codebase:"
	$(PRINT) 
	$(PRINT) "class:       Compile just the classes. Don't make JAR files."
	$(PRINT) "bin:         Build parsers and classes"
	$(PRINT) "jar:         Make the java JAR file"
	$(PRINT) "javadoc:     Generate the javadoc information"
	$(PRINT) "docs:        Generate both parser and javadoc files"
	$(PRINT) "all:         Build everything (including docs)"
	$(PRINT) "clean:       Blow all the library classes away"
	$(PRINT) "nuke:        Blow both lib and example code away"
	$(PRINT) 
	$(PRINT) "The following allow you to build only the files needed by a"
	$(PRINT) "specific renderer, without compiling the others."
	$(PRINT) 
	$(PRINT) "j3d:         Java3D classes"
	$(PRINT) "j3d-jar:     Java3D bin and jar files"
	$(PRINT) "j3d-javadoc: Java3D javadoc"
	$(PRINT) 

all: class jar javadoc

class:
	cd $(JAVA_DIR) && make buildall

jar:
	cd $(JAVA_DIR) && make jar

jni:
	cd $(JAVA_DIR) && make jni

libs:
	cd $(JAVA_DIR) && make nativeall
	cd $(NATIVE_DIR) && make buildall
    
javadoc:
	cd $(JAVA_DIR) && make javadoc

clean:
	cd $(JAVA_DIR) && make clean
	
#
# Java3D-specific renderer
#
j3d:
	cd $(JAVA_DIR) && make -f Makefile-java3d buildall

j3d-javadoc:
	cd $(JAVA_DIR) && make -f Makefile-java3d javadoc

j3d-jar: j3d
	cd $(JAVA_DIR) && make -f Makefile-java3d jar
