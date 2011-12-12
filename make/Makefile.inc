#*********************************************************************
#
#                         (C) 2001-07 j3d.org
#                         http://code.j3d.org/
#
# Lowest level common makefile for both native and Java code
# 
# Author: Justin Couch
# Version: $Revision: 1.11 $
#
#*********************************************************************

ifndef APP_ROOT
  BUILD_ROOT_DIR=$(PROJECT_ROOT)
else
  BUILD_ROOT_DIR=$(APP_ROOT)
endif

SRC_DIR=src
DOCS_DIR=docs
MAKE_DIR=make

JAVA_DIR=$(SRC_DIR)/java
CONFIG_DIR=$(SRC_DIR)/config
IMAGES_DIR=${SRC_DIR}/images
NATIVE_DIR=$(SRC_DIR)/native
INCLUDE_DIR=$(NATIVE_DIR)/include
EXAMPLES_DIR=$(PROJECT_ROOT)/examples
ECLIPSE_DIR=${SRC_DIR}/eclipse

DELETE    = rm -f
MAKEDIR   = mkdir -p
RMDIR     = rm -fr
PRINT     = @echo
COPY      = cp 
COPY_PATH = cp --parents
CHMOD     = chmod
CD        = cd

# Pass these variables down between the environments
export JAR_VERSION

# General runtime flags for various commands
JAVAC_FLAGS=-Xlint:unchecked -source 1.6 -target 1.6
#JAVAC_FLAGS=-deprecation
#JAVADOC_FLAGS=

MAKEFLAGS=-s
