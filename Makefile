#
# Top Level makefile for the j3d.org code repository.
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
export PROJECT_ROOT=$(PWD)
endif

include $(PROJECT_ROOT)/make/Makefile.inc

help:
	$(PRINT) 
	$(PRINT) "                   The j3d.org Code Repository"
	$(PRINT) 
	$(PRINT) "More information on this project can be found at http://code.j3d.org"
	$(PRINT) 
	$(PRINT) "The following options are offered and will build the codebase but does"
	$(PRINT) "not include any Java3D-specific classes:"
	$(PRINT) 
	$(PRINT) "class:       Compile just the classes. Don't make JAR files."
	$(PRINT) "jar:         Make the java JAR file"
	$(PRINT) "javadoc:     Generate the javadoc information"
	$(PRINT) "config:      Build the configuration files."
	$(PRINT) "images:      Build the images."
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

all: class images config jar javadoc

images:
	make -f $(IMAGES_DIR)/Makefile buildall
	
config:
	make -f $(CONFIG_DIR)/Makefile buildall
	
class:
	make -f $(JAVA_DIR)/Makefile buildall

jar:
	make -f $(JAVA_DIR)/Makefile jar

jni:
	make -f $(JAVA_DIR)/Makefile jni

libs:
	make -f $(JAVA_DIR)/Makefile nativeall
	make -f $(NATIVE_DIR)/Makefile buildall
    
javadoc:
	make -f $(JAVA_DIR)/Makefile javadoc

clean:
	make -f $(JAVA_DIR)/Makefile clean
	make -f $(NATIVE_DIR)/Makefile clean
	
#
# Java3D-specific renderer
#
j3d:
	make -f $(JAVA_DIR)/Makefile-java3d buildall

j3d-javadoc:
	make -f $(JAVA_DIR)/Makefile-java3d javadoc

j3d-jar: j3d
	make -f $(JAVA_DIR)/Makefile-java3d jar
