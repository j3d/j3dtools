#*********************************************************************
#
#  (C) 2001 J3d.org
#    http://code.j3d.org/
# 
# Package makefile for the org.j3d.util directory
#
# Author: Justin Couch
# Version: $Revision: 1.10 $
#
#*********************************************************************

PACKAGE = org.j3d.util

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = ErrorHandler.java \
		 HashSet.java \
		 CharHashMap.java \
		 IntHashMap.java \
		 FloatHashMap.java \
		 DoubleHashMap.java \
		 ObjectArray.java \
		 CircularList.java \
		 Queue.java \
         SAXErrorHandler.java \
         SAXEntityResolver.java	\
		 ImageLoader.java \
		 ColorUtils.java \
		 ImageGenerator.java \
		 ImageUtils.java \
		 UserSupplementData.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY =

include ${PROJECT_ROOT}/make/Makefile.jav
