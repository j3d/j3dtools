#*********************************************************************
#
#  (C) 2001 J3d.org
# 
#*********************************************************************

# Names of packages to be compiled. These are declared in the order that
# they are to be compiled in. We create one at a time
BUILD_ORDER = org.j3d.util \
	org.j3d.util.device \
	org.j3d.util.interpolator \
	org.j3d.device.input \
	org.j3d.device.input.spaceball.transformation \
	org.j3d.device.input.spaceball.driver \
	org.j3d.device.input.spaceball \
	org.j3d.texture \
	org.j3d.geom \
	org.j3d.geom.terrain \
	org.j3d.geom.overlay \
	org.j3d.geom.particle \
	org.j3d.ui \
	org.j3d.ui.image \
	org.j3d.ui.navigation \
	org.j3d.terrain \
	org.j3d.terrain.roam \
	org.j3d.loaders \
	org.j3d.loaders.ac3d.util \
	org.j3d.loaders.ac3d.models \
	org.j3d.loaders.ac3d.parser.exceptions \
	org.j3d.loaders.ac3d.parser \
	org.j3d.loaders.ac3d.parser.handlers \
	org.j3d.loaders.ac3d \
	org.j3d.loaders.stl \
	org.j3d.loaders.vterrain \
	org.j3d.loaders.dem \



# Names of packages that we want included in the javadoc
JAVADOC_PACKAGES = org.j3d.util \
	org.j3d.util.device \
	org.j3d.util.interpolator \
	org.j3d.device.input.spaceball \
	org.j3d.device.input.spaceball.transformation \
	org.j3d.texture \
	org.j3d.geom \
	org.j3d.geom.terrain \
	org.j3d.geom.overlay \
	org.j3d.geom.particle \
	org.j3d.ui \
	org.j3d.ui.image \
	org.j3d.ui.navigation \
	org.j3d.terrain \
	org.j3d.terrain.roam \
	org.j3d.loaders \
	org.j3d.loaders.ac3d \
	org.j3d.loaders.stl \
	org.j3d.loaders.vterrain \
	org.j3d.loaders.dem \

# Names of JAR files we want to create
JARS = j3d-org.jar

# 3rd party libraries that need including.
JARS_3RDPARTY=

# Definitions of setup information if we are making a self-executable JAR
MAIN_CLASS     = 
MAIN_PACKAGE   = 
MAIN_JAR       = j3d-org.jar

#*********************************************************************
#
# Javadoc
#
#*********************************************************************

OVERVIEW    = overview.html
WINDOWTITLE = 'j3d.org Code Library'
DOCTITLE    = 'j3d.org Code Repository API'
HEADER      = 'j3d.org Code'
BOTTOM      = '<center> \
               Latest Info from <a href="http://code.j3d.org"> \
               http://code.j3d.org/</a><br> \
			   <font size=2>Copyright &copy; 2001 - \
			   <a href="http://www.j3d.org">j3d.org</a></font> \
               </center>'

# A list of the local directories that contain other Javadoc that we want
# to link to. For example we link to the JDK, JAXP, SAI and JUnit Javadocs
# here. Note that these *must* be full URLs and not just local directory 
# references. If your directory path contains a space in it, you may be in
# trouble. We haven't tested it with that. Try using double quotes inside
# the single quotes to get the space treated in the file name
#LINK_URLS   = 'file:///c:/java/jaxp-1.1/docs/api/' \
#              'file:///c:/java/junit3.5/javadoc/' \
#			  'file:///c:/java/jdk1.3/docs/api/' \
#			  'file:///c:/java/j3d/1.2.1/html/'

include $(PROJECT_ROOT)/make/Makefile.jav 
