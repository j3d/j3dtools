#*********************************************************************
#
#  (C) 2001 J3d.org
#    http://code.j3d.org/
# 
# Package makefile for the org.j3d.device.input.spaceball.driver directory
#
# Author: Justin Couch
# Version: $Revision: 1.1 $
#
#*********************************************************************

PACKAGE = org.j3d.device.input.spaceball.driver

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = SpaceballPacket.java \
	SpaceballDriver.java \
	Spaceball3003CallbackInterface.java \
	Spaceball3003Packet.java \
	Spaceball3003Driver.java \
	Spaceball4000CallbackInterface.java \
	Spaceball4000Packet.java \
	Spaceball4000Driver.java \
	SpaceballTypeDetector.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY =

include ${PROJECT_ROOT}/make/Makefile.jav
