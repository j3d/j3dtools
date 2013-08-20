#*********************************************************************
#
#                         (C) 2009 j3d.org
#                         http://code.j3d.org/
#
# Package makefile for the org.j3d.geom.triangulation directory
#
# Author: Chris Shankland
# Version: $Revision: 1.1 $
#
#*********************************************************************

PACKAGE = org.j3d.geom.triangulation

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
    Edge.java \
    EdgeTable.java \
    Basic.java \
    BBox.java \
    Distance.java \
    HeapNode.java \
    Left.java \
    ListNode.java \
    PntNode.java \
    Triangle.java \
    Triangulator.java \
    Simple.java \
    Project.java \
    Orientation.java \
    Heap.java \
    GeometryInfo.java \
    Numerics.java \
    NoHash.java \
    EarClip.java \
    Desperate.java \
    Clean.java \
    Degenerate.java \
    BottleNeck.java \
    Bridge.java \  

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = vecmath.jar

# Define IGNORE_CYCLES here because there is a known cyclic dependency
IGNORE_CYCLES=1

include ${PROJECT_ROOT}/make/Makefile.jav
