#*********************************************************************
#
#  (C) 2001 J3d.org
#    http://code.j3d.org/
# 
# Package makefile for the org.j3d.geom.particle directory
#
# Author: Justin Couch
# Version: $Revision: 1.17 $
#
#*********************************************************************

PACKAGE = org.j3d.geom

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = GeometryData.java \
  UnsupportedTypeException.java \
  InvalidArraySizeException.java \
  CoordinateUtils.java \
  NormalUtils.java \
  GeometryGenerator.java \
  BoxGenerator.java \
  ConeGenerator.java \
  SphereGenerator.java \
  CylinderGenerator.java \
  TorusGenerator.java \
  SpringGenerator.java \
  SuperShapeGenerator.java \
  SeidelNode.java \
  SeidelTrapezoid.java \
  SeidelSegment.java \
  SeidelVertexChain.java \
  SeidelMonotoneChain.java \
  SeidelTriangulator.java \
  PolyVertex.java \
  EarCutTriangulator.java \
  TriangulationUtils.java \
  IntersectionUtils.java \
  CharacterData.java \
  CharacterCreator.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
# EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
