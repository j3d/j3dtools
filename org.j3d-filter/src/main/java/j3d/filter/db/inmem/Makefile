#*********************************************************************
#
#  (C) 2001 - 2011 J3d.org
#    http://code.j3d.org/
# 
# Package makefile for the j3d.filter.db.inmem directory
#
# Author: Justin Couch
# Version: $Revision: 1.17 $
#
#*********************************************************************

PACKAGE = j3d.filter.db.inmem

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  AbstractVertexGeometry.java \
  AbstractMemoryGroup.java \
  MemLineGeometry.java \
  MemTriangleGeometry.java \
  MemQuadGeometry.java \
  MemGroup.java \
  MemTransformGroup.java \
  MemPhysicalProperties.java \
  MemVisualProperties.java \
  MemMesh.java \
  InMemoryNodeFactory.java \
  InMemoryDatabase.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
# EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=

include ${PROJECT_ROOT}/make/Makefile.jav
