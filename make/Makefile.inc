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

#JAVAC_FLAGS=-deprecation
#JAVADOC_FLAGS=

MAKEFLAGS=-s

