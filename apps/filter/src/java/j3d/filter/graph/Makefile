#*********************************************************************
#
#  (C) 2001 - 2011 J3d.org
#    http://code.j3d.org/
# 
# Package makefile for the j3d.filter.graph directory
#
# Author: Justin Couch
# Version: $Revision: 1.17 $
#
#*********************************************************************

PACKAGE = j3d.filter.graph

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = Vertex.java \
  AbstractSceneGraphObject.java \
  SceneGraphStructureObject.java \
  SceneGraphDecoratorObject.java \
  BoundedObject.java \
  Leaf.java \
  Geometry.java \
  VertexGeometry.java \
  LineGeometry.java \
  TriangleGeometry.java \
  QuadGeometry.java \
  PhysicalProperties.java \
  VisualProperties.java \
  Mesh.java \
  Group.java \
  TransformGroup.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
# EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=

include ${PROJECT_ROOT}/make/Makefile.jav
