#*********************************************************************
#
#                         (C) 2001-05 j3d.org
#                         http://code.j3d.org/
#
# Lowest level common makefile for both native and Java code
# 
# Author: Justin Couch
# Version: $Revision: 1.8 $
#
#*********************************************************************

ifndef PROJECT_ROOT
  export PROJECT_ROOT=/usr/local/src/projects/j3d.org
endif

SRC_DIR=$(PROJECT_ROOT)/src
DOCS_DIR=$(PROJECT_ROOT)/docs
LIB_DIR=$(PROJECT_ROOT)/lib
MAKE_DIR=$(PROJECT_ROOT)/make

JAVA_DIR=$(SRC_DIR)/java
NATIVE_DIR=$(SRC_DIR)/native
INCLUDE_DIR=$(NATIVE_DIR)/include
EXAMPLES_DIR=$(PROJECT_ROOT)/examples

DELETE    = rm -f
MAKEDIR   = mkdir -p
RMDIR     = rm -fr
PRINT     = @echo
COPY      = cp 
COPY_PATH = cp --parents
CHMOD     = chmod
CD        = cd

#JAVAC_FLAGS=-deprecation
#JAVADOC_FLAGS=

MAKEFLAGS=-s

