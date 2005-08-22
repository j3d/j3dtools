#*********************************************************************
#
#                         (C) 2001-02 j3d.org
#                         http://code.j3d.org/
#
# Lowest level common makefile for both native and Java code
# 
# Author: Justin Couch
# Version: $Revision: 1.11 $
#
#*********************************************************************

#
# Directories for standard stuff
#
include $(PROJECT_ROOT)/make/Makefile.inc

JAVA_DEV_ROOT = $(JAVA_DIR)

CLASS_DIR     = $(PROJECT_ROOT)/classes
JAVADOC_DIR   = $(DOCS_DIR)/javadoc
JAR_DIR       = $(PROJECT_ROOT)/jars
JAR_MAKE_DIR  = $(MAKE_DIR)/jar
JAVA_SRC_DIR  = $(JAVA_DEV_ROOT)
DESTINATION   = $(PROJECT_ROOT)/classes
JAR_TMP_DIR   = $(PROJECT_ROOT)/.jar_tmp
MANIFEST_DIR  = $(MAKE_DIR)/manifest

#
# Built up tool information
#
ifdef JAVA_HOME
  JAVAC    = $(JAVA_HOME)/bin/javac
  JAR      = $(JAVA_HOME)/bin/jar
  JAVADOC  = $(JAVA_HOME)/bin/javadoc
  JAVAH    = $(JAVA_HOME)/bin/javah
  JAR_INSTALL_DIR = $(JAVA_HOME)/jre/lib/ext
else
  JAVAC    = javac
  JAR      = jar
  JAVADOC  = javadoc
  JAVAH	   = javah
endif

EMPTY         =
SPACE         = $(EMPTY) $(EMPTY)

OS_NAME=$(shell uname)
ifeq (, $(strip $(findstring CYGWIN, $(OS_NAME))))
  PATH_SEP=':'
else
  PATH_SEP=';'
endif

ifdef JARS
	LOCAL_JARTMP  = $(patsubst %,$(JAR_DIR)/%,$(JARS))
	LOCAL_JARLIST = $(subst $(SPACE),$(PATH_SEP),$(LOCAL_JARTMP))
endif

ifdef JARS_3RDPARTY
	OTHER_JARTMP  = $(patsubst %,$(LIB_DIR)/%,$(JARS_3RDPARTY))
	OTHER_JARLIST = $(subst $(SPACE),$(PATH_SEP),$(OTHER_JARTMP))
endif

SOURCEPATH = $(JAVA_SRC_DIR)

CP = $(CLASS_DIR)

ifdef LOCAL_JARLIST
  ifdef CP
    CP :="$(CP)$(PATH_SEP)$(LOCAL_JARLIST)"
  else
    CP :="$(LOCAL_JARLIST)"
  endif
endif

ifdef OTHER_JARLIST
  ifdef CLASSPATH
    CP1:="$(CP)$(PATH_SEP)$(OTHER_JARLIST)"
  else
    CP1 := "$(OTHER_JARLIST)"
  endif
endif

ifdef CP1
  CLASSPATH="$(CP1)"
else
  CLASSPATH="$(CP)"
endif

JAVADOC_CLASSPATH=$(CLASS_DIR)$(PATH_SEP)$(OTHER_JARLIST)

# has the user defined an external classpath to use here? If so, append
# it to the ordinary classpath.
ifdef PROJECT_CLASSPATH
    CLASSPATH := $(CLASSPATH)$(PATH_SEP)"$(PROJECT_CLASSPATH)"
    JAVADOC_CLASSPATH := $(JAVADOC_CLASSPATH)$(PATH_SEP)"$(PROJECT_CLASSPATH)"

  ifndef IS_WIN32
    CLASSPATH := $(subst ",,$(CLASSPATH))
    JAVADOC_CLASSPATH := $(subst ",,$(JAVADOC_CLASSPATH))
  endif
endif

#
# Build rules.
#
PACKAGE_LOC     = $(subst .,/,$(PACKAGE))
PACKAGE_DIR     = $(DESTINATION)/$(PACKAGE_LOC)
JAVA_FILES      = $(filter  %.java,$(SOURCE))
NONJAVA_FILES   = $(patsubst %.java,,$(SOURCE))
CLASS_FILES     = $(JAVA_FILES:%.java=$(PACKAGE_DIR)/%.class)
OTHER_FILES     = $(EXTRA:%=$(PACKAGE_DIR)/%)

JNI_CLASS_FILES = $(JNI_SOURCE:%.java=$(PACKAGE_DIR)/%.class)
JNI_PKG_PREFIX  = $(subst .,_,$(PACKAGE))
JNI_HEADERS     = $(JNI_SOURCE:%.java=%.h)

JAR_CLASS_FILES = $(patsubst %, %/*.*, $(JAR_CONTENT))

#JAR_EXTRA_FILES = $(EXTRA_FILES:%=$(JAVA_SRC_DIR)/%)

JAR_CONTENT_CMD = -C $(JAR_TMP_DIR) . $(patsubst %, -C $(JAVA_SRC_DIR) %, $(EXTRA_FILES))
LINK_FILES      = $(patsubst %, -link %,$(LINK_URLS))

# Make a list of all packages involved
ifdef PACKAGE
  PACKAGE_LIST  = $(subst .,/,$(PACKAGE))
  NATIVE_LIST  = $(subst .,/,$(PACKAGE))
else
  PACKAGE_LIST  = $(subst .,/,$(BUILD_ORDER))
  NATIVE_LIST  = $(subst .,/,$(NATIVE_PACKAGES))
endif

PLIST_CLEAN     = $(patsubst %,$(JAVA_SRC_DIR)/%/.clean,$(PACKAGE_LIST))
PLIST_BUILD     = $(patsubst %,$(JAVA_SRC_DIR)/%/.build,$(PACKAGE_LIST))
JNI_LIST_BUILD = $(patsubst %,$(JAVA_SRC_DIR)/%/.native,$(NATIVE_LIST))

#
# Option listing for the various commands
#
JAVAC_OPTIONS = -d $(DESTINATION) -classpath $(CLASSPATH) \
                -sourcepath $(SOURCEPATH) $(JAVAC_FLAGS)
JAVAH_OPTIONS = -d $(INCLUDE_DIR) -classpath $(CLASSPATH)

ifdef MANIFEST
  JAR_OPTIONS = -cvmf
  JAR_MANIFEST = $(MANIFEST_DIR)/$(MANIFEST)
else
  JAR_OPTIONS = -cvf
endif

JAVADOC_OPTIONS  = \
     -d $(JAVADOC_DIR) \
     -sourcepath $(JAVA_SRC_DIR) \
     -classpath $(JAVADOC_CLASSPATH) \
     -author \
     -use \
     -version \
     -windowtitle $(WINDOWTITLE) \
     -doctitle $(DOCTITLE) \
     -header $(HEADER) \
     -bottom $(BOTTOM) \
	 $(LINK_FILES)
	 
ifdef OVERVIEW
  JAVADOC_OPTIONS += -overview $(OVERVIEW)
endif

ifdef JAVADOC_FLAGS
  JAVADOC_OPTIONS += $(JAVADOC_FLAGS)
endif

#
# General build rules
#

# Rule 0. Applied when make is called without targets.
all: $(DESTINATION) $(CLASS_FILES) $(OTHER_FILES)

# Rule 1. If the destination dir is missing then create it
$(DESTINATION) :
	$(PRINT) Creating $(DESTINATION)
	@ $(MAKEDIR) $(DESTINATION)

# Rule 3. Change ".build" tag to "Makefile", thus call the package makefile
# which in turn recalls this makefile with target all (rule 0).
%.build :
	$(PRINT) Building directory $(subst .build,' ',$@)
	@ $(MAKE) -k -f $(subst .build,Makefile,$@) all

# Rule 4. Call rule 3 for every package
buildall : $(PLIST_BUILD)
	$(PRINT) Done build.

#
# Specific dependency build rules
#

# Rule 5. Building a .class file from a .java file
$(PACKAGE_DIR)/%.class : $(JAVA_SRC_DIR)/$(PACKAGE_LOC)/%.java
	$(PRINT) Compiling $*.java
	@ $(JAVAC) $(JAVAC_OPTIONS) $<

# Rule 6. Building a .class file from a .java file. Invokes rule 5.
%.class : $(JAVA_SRC_DIR)/$(PACKAGE_LOC)/%.java
	@ $(MAKE) -k $(PACKAGE_DIR)/$@

# Rule 9. Default behaviour within a package: Simply copy the object from src
# to classes. Note that the location of this rule is important. It must be after
# the package specifics.
$(PACKAGE_DIR)/% : $(SRC_DIR)/$(PACKAGE_LOC)/%
	$(MAKEDIR)  $(PACKAGE_DIR)
	$(COPY) $< $@
	$(CHMOD) u+rw $<

# Rule 3. Change ".build" tag to "Makefile", thus call the package makefile
# which in turn recalls this makefile with target all (rule 10).
%.native :
	$(PRINT) Building native $(subst .build,' ',$@)
	@ $(MAKE) -k -f $(subst .native,Makefile,$@) jni

# Rule 5. Call rule 2 for every package
nativeall : $(DESTINATION) $(INCLUDE_DIR) $(LIB_DIR) $(JNI_LIST_BUILD)
	$(PRINT) Done native headers

# Rule 6. If the destination dir is missing then create it
$(INCLUDE_DIR) :
	$(PRINT) Missing include dir. Creating $(INCLUDE_DIR)
	@ $(MAKEDIR) $(INCLUDE_DIR)

# Rule 7. If the destination dir is missing then create it
$(LIB_DIR) :
	$(PRINT) Missing library dir. Creating $(LIB_DIR)
	@ $(MAKEDIR) $(LIB_DIR)

# Rule 2 Build JNI .h files. Invokes rule 7.
jni : $(DESTINATION) $(JNI_CLASS_FILES) $(JNI_HEADERS)

# Rule 7. Building a JNI .h stub file from a .class file
%.h : %.class
	$(PRINT) Creating header for $*
	@ $(JAVAH) $(JAVAH_OPTIONS) $(PACKAGE).$*

#
# Cleanups
#

# Rule 10. Remove all produced files (except javadoc)
cleanall :
	$(DELETE) $(PACKAGE_DIR)/*.class $(OTHER_FILES) $(JNI_HEADERS)


# Rule 11. Change ".clean" tag to "Makefile", thus call the package makefile
# which in turn recalls this makefile with target cleanall (rule 10).
%.clean :
	$(MAKE) -k -f $(subst .clean,Makefile,$@) cleanall


# Rule 12: Call rule 11 for every package directory
clean : $(PLIST_CLEAN)
	$(PRINT) Done clean.

#
# JAR file related stuff
#

# Rule 13. Build a jar file. $* strips the last phony .JAR extension.
# Copy all the required directories to a temp dir and then build the 
# JAR from that. The -C option on the jar command recurses all the
# directories, which we don't want because we want to control the 
# packaging structure. 
%.JAR :
	@ $(MAKEDIR) $(JAR_DIR) $(JAR_TMP_DIR)
	$(PRINT) Deleting the old JAR file
	@ $(DELETE) $(JAR_DIR)/$*
	$(PRINT) Building the new JAR file $*
	@ $(RMDIR) $(JAR_TMP_DIR)/*
	$(CD) $(CLASS_DIR) && $(COPY_PATH) $(JAR_CLASS_FILES) $(JAR_TMP_DIR)
	$(JAR) $(JAR_OPTIONS) $(JAR_MANIFEST) $(JAR_DIR)/$* $(JAR_CONTENT_CMD)

# Rule 13. Create given jar file by invoking its Makefile which triggers
# rule 12
%.jar :
	$(PRINT) Building JAR file $@
	@ $(MAKE) -k -f $(patsubst %,$(JAR_MAKE_DIR)/Makefile.$*,$@) $@.JAR
	$(PRINT) Cleaning up
	@ $(RMDIR) $(JAR_TMP_DIR)


# Rule 14. Create all jar files by invoking rule 13
jar : $(JARS)
	$(PRINT) Done jars.


# Rule 15. Build javadoc for all listed packages
javadoc :
	@ $(MAKEDIR) $(JAVADOC_DIR)
	$(PRINT) Cleaning out old docs
	@ $(RMDIR) $(JAVADOC_DIR)/*
	@ $(PRINT) $(JAVADOC_PACKAGES) > $(JAVA_DEV_ROOT)/packages.tmp
	$(PRINT) Starting Javadoc process
	@ $(JAVADOC) $(JAVADOC_OPTIONS) @$(JAVA_DEV_ROOT)/packages.tmp
	@ $(DELETE) $(JAVA_DEV_ROOT)/packages.tmp
	$(PRINT) Done JavaDoc.

# Rule 18. Install the JAR files after we have created them
install: $(JAR_INSTALL_DIR)
	$(PRINT) Copying JAR files to $(JAR_INSTALL_DIR)
	$(COPY) $(JAR_DIR)/* $(JAR_INSTALL_DIR)

# Rule 18. Copy the properties files to the classes directory
properties: $(OTHER_FILES)

# Rule 16. A combination of steps used for automatic building
complete : clean buildall jar javadoc
