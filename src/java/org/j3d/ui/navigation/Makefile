#*********************************************************************
#
#  (C) 2001 J3d.org
#    http://code.j3d.org/
# 
# Package makefile for the org.j3d.ui.navigation directory
#
# Author: Justin Couch
# Version: $Revision: 1.11 $
#
#*********************************************************************

PACKAGE = org.j3d.ui.navigation

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = HeightDataSource.java \
    HeightMapGeometry.java \
    NavigationState.java \
    FrameUpdateListener.java \
    NavigationStateListener.java \
    ViewpointSelectionListener.java \
    CollisionListener.java \
    ViewpointData.java \
    ViewpointTransition.java \
    NavigationToolbar.java \
    ViewpointCellRenderer.java \
    ViewpointToolbar.java \
    NavigationProcessor.java \
    NavigationHandler.java \
    MouseViewHandler.java \
    MouseViewBehavior.java \
    NavigationStateManager.java	\
    ViewpointManager.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY =

include ${PROJECT_ROOT}/make/Makefile.jav
