#
# Lowest level common makefile for both native and Java code
#
# Maintainer: Justin Couch
#

ifndef PROJECT_ROOT
export PROJECT_ROOT=/usr/local/src/projects/j3d.org/code
endif

SRC_DIR=$(PROJECT_ROOT)/java
DOCS_DIR=$(PROJECT_ROOT)/docs
LIB_DIR=$(PROJECT_ROOT)/lib
MAKE_DIR=$(PROJECT_ROOT)/make

JAVA_DIR=$(SRC_DIR)
NATIVE_DIR=$(SRC_DIR)/native
INCLUDE_DIR=$(NATIVE_DIR)/include

DELETE   = rm -f
MAKEDIR  = mkdir -p
RMDIR    = rm -fr
PRINT    = @echo

MAKEFLAGS=-s