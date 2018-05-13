
.PHONY: all clean doc distclean 

include system.inc

CXX=g++
CPPFLAGS=-Idist/include/
#CPPFLAGS+=-DPREFETCH
#CPPFLAGS+=-DOUTPUT_AGGREGATE
#CPPFLAGS+=-DOUTPUT_WRITE_NT
#CPPFLAGS+=-DOUTPUT_WRITE_NORMAL
CPPFLAGS+=-DOUTPUT_ASSEMBLE
#CPPFLAGS+=-DDEBUG #-DDEBUG2
CXXFLAGS=$(SYSFLAGS)
#CXXFLAGS+=-g -O0 #-Wall
CXXFLAGS+=-O3
LDFLAGS=-Ldist/lib/
LDLIBS=-lconfig++ -lpthread -lbz2

ifeq ($(HOSTTYPE),sparc)
LDLIBS+=-lcpc
CXXFLAGS+=-mcpu=ultrasparc
endif

all: dist multijoin multijoin-serial

FILES = schema.o hash.o parser.o table.o joinerfactory.o page.o \
	algo/nl.o algo/base.cpp ProcessorMap.o Barrier.o loader.o \
	partitionerfactory.o partitioner.o algo/hashbase.o \
	algo/storage.o affinitizer.o algo/hashtable.o \
	comparator.o algo/flatmem.o


clean:
	rm -f *.o
	rm -f algo/*.o
	rm -f multijoin multijoin-serial

distclean: clean
	rm -rf dist

doc: Doxyfile
	doxygen


multijoin-serial: $(FILES) main-serial.o

multijoin: $(FILES) main.o

dist:
	./pre-init.sh
