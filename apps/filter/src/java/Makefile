#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Author: Alan Hudson
# Version: $Revision: 1.12 $
#
#*********************************************************************

# Names of packages to be compiled. These are declared in the order that
# they are to be compiled in. We create one at a time
BUILD_ORDER = \
  j3d.filter \
  j3d.filter.graph \
  j3d.filter.exporter \
  j3d.filter.importer \
  j3d.filter.filters \
  j3d.filter.db \
  j3d.filter.db.inmem \

# Names of packages that we want included in the javadoc
JAVADOC_PACKAGES = \
  j3d.filter \
  j3d.filter.db \
  j3d.filter.importer \
  j3d.filter.importer.collada \
  j3d.filter.filters \
  j3d.filter.ui \

# Names of JAR files we want to create
JARS = j3d.filter.jar

# A version number we tack onto the JAR file name for eclipse versioning
# purposes.
JAR_VERSION=2.0.0

# 3rd party libraries that need including for JAVADOC generation
JARS_JAVADOC=uri.jar

# 3rd party libraries that need including.
JARS_3RDPARTY=

# Definitions of setup information if we are making a self-executable JAR
MAIN_CLASS     = j3d.filter.Filter
MAIN_PACKAGE   = j3d.filter
MAIN_JAR       =

#*********************************************************************
#
# Javadoc
#
#*********************************************************************

OVERVIEW    =
WINDOWTITLE = 'j3d.org Filter'
DOCTITLE    = 'j3d.org Filter'
HEADER      = 'j3d.org Filter'
BOTTOM      = '<font size=2>Copyright &copy; 2000 - 2011\
               <a href=\"http://www.j3d.org\">j3d.org</a> \
               </font>'

# A list of the local directories that contain other Javadoc that we want
# to link to. For example we link to the JDK, JAXP, SAI and JUnit Javadocs
# here. Note that these *must* be full URLs and not just local directory
# references. If your directory path contains a space in it, you may be in
# trouble. We haven't tested it with that. Try using double quotes inside
# the single quotes to get the space treated in the file name
LINK_URLS   =

include $(PROJECT_ROOT)/make/Makefile.jav
