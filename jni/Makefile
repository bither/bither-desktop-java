# http://www.inonit.com/cygwin/jni/helloWorld/c.html
#JAVA_HOME=/System/Library/Frameworks/JavaVM.framework
#final:JAVAHOME clear
#	gcc -dynamiclib -o libHello.jnilib jni_helloworldImpl.cpp -framework JavaVM -I$(JAVA_HOME)/Headers
#clear:
#	rm -f *.jnilib


# Define a variable for classpath
export MAKEROOT := $(shell pwd)
JAVA_HOME=/System/Library/Frameworks/JavaVM.framework

vanitygenDir=$(MAKEROOT)/vanitygen


PLATFORM=$(shell uname -s)
ifeq ($(PLATFORM),Darwin)
OPENCL_LIBS=-framework OpenCL
else
OPENCL_LIBS=-lOpenCL
endif

LIBS=-lpcre -lcrypto -lm -lpthread


OBJS=Vanitygen.o OclVanitygen.o JniUtil.o
#CFLAGS=-dynamiclib

all : libvanitygen.jnilib liboclvanitygen.jnilib
LIBS+=-framework JavaVM

CFLAGS          :=      $(CFLAGS) -I$(JAVA_HOME)/Headers -I$(vanitygenDir) -ggdb -O3 -Wall


liboclvanitygen.jnilib : OclVanitygen.o JniUtil.o $(vanitygenDir)/oclvanitygen.o $(vanitygenDir)/oclengine.o $(vanitygenDir)/pattern.o $(vanitygenDir)/util.o
	$(CC) -shared -fpic $^ -o $@ $(CFLAGS) $(LIBS) $(OPENCL_LIBS)
	
libvanitygen.jnilib : Vanitygen.o JniUtil.o $(vanitygenDir)/vanitygen.o $(vanitygenDir)/pattern.o $(vanitygenDir)/util.o
	$(CC) -shared -fpic $^ -o $@ $(CFLAGS) $(LIBS) $(OPENCL_LIBS)

# $@ matches the target, $< matches the first dependancy
Vanitygen.o : Vanitygen.c Vanitygen.h 
	gcc -dynamiclib -I$(JAVA_HOME)/Headers -c $< -o $@  
	
OclVanitygen.o : OclVanitygen.c OclVanitygen.h
	gcc -dynamiclib -I$(JAVA_HOME)/Headers -c $< -o $@
	 
JniUtil.o : JniUtil.c JniUtil.h 
	gcc -dynamiclib -I$(JAVA_HOME)/Headers -c $< -o $@ 

# $* matches the target filename without the extension
#TapeJNI.h : TapeJNI.class
#javah -classpath $(CLASS_PATH) $*

clean:
	rm -rf *.o
	rm -rf *.jnilib
	rm -rf *.so
