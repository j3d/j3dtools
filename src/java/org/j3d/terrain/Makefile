#*********************************************************************
#
#  (C) 2001 J3d.org
#    http://code.j3d.org/
# 
# Package makefile for the org.j3d.terrain directory
#
# Author: Justin Couch
# Version: $Revision: 1.6 $
#
#*********************************************************************

PACKAGE = org.j3d.terrain

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = TextureTileGenerator.java \
	TerrainData.java \
	StaticTerrainData.java \
	TiledTerrainData.java \
	FreeFormTerrainData.java \
	AbstractTerrainData.java \
	AbstractStaticTerrainData.java \
	AbstractTiledTerrainData.java \
	AbstractFreeFormTerrainData.java \
	Landscape.java 

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY =

include ${PROJECT_ROOT}/make/Makefile.jav
