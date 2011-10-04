#*********************************************************************
#
#  (C) 2011 J3d.org
#    http://code.j3d.org/
# 
# Package makefile for the org.j3d.loaders.ldraw directory
#
# Author: Justin Couch
# Version: $Revision: 1.3 $
#
#*********************************************************************

PACKAGE = org.j3d.loaders.ldraw

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  LDrawColor.java \
  LDrawStdColor.java \
  LDrawOtherColor.java \
  LDrawGlitterColor.java \
  LDrawSpeckleColor.java \
  LDrawMilkyColor.java \
  LDrawFilePart.java \
  LDrawFileReference.java \
  LDrawFileType.java \
  LDrawHeader.java \
  LDrawRenderable.java \
  LDrawLine.java \
  LDrawTriangle.java \
  LDrawQuad.java \
  LDrawParseObserver.java \
  LDrawParser.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = vecmath.jar


include ${PROJECT_ROOT}/make/Makefile.jav
