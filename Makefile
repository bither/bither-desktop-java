# gcc -shared -Wall -fPIC HelloWorld.c -I/usr/java/java-7-sun/include/ -I/usr/java/java-7-sun/include/linux/ -o libHelloWorld.so
#java -Djava.library.path='/home/jjz/java/vanitygenJni/src/main/java/' NativeUtil
export MAKEROOT := $(shell pwd)
JAVA_HOME=/usr/java/java-7-sun

vanitygenDir=$(MAKEROOT)/vanitygen


LIBS=-lpcre -lcrypto -lm -lpthread
OPENCL_LIBS=-lOpenCL



all : libvanitygen.so liboclvanitygen.so


CFLAGS          :=      $(CFLAGS) -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -I$(vanitygenDir) -ggdb -O3 -Wall -D WHITH_PKCS5_PBKDF2_HMAC=1
# $@ matches the target, $< matches the first dependancy

libvanitygen.so : Vanitygen.c Vanitygen.h JniUtil.c JniUtil.h $(vanitygenDir)/vanitygen.c $(vanitygenDir)/vanitygen.h $(vanitygenDir)/pattern.c $(vanitygenDir)/pattern.h $(vanitygenDir)/util.c $(vanitygenDir)/util.h
	$(CC) -shared -fpic $^ -o $@ $(CFLAGS) $(LIBS) 



liboclvanitygen.so : OclVanitygen.c OclVanitygen.h JniUtil.c JniUtil.h $(vanitygenDir)/oclvanitygen.c $(vanitygenDir)/oclvanitygen.h $(vanitygenDir)/oclengine.c $(vanitygenDir)/oclengine.h $(vanitygenDir)/pattern.c $(vanitygenDir)/pattern.h $(vanitygenDir)/util.c $(vanitygenDir)/util.h
	$(CC) -shared -fpic $^ -o $@ $(CFLAGS) $(LIBS) $(OPENCL_LIBS)

clean:
	rm -rf *.o
	rm -rf *.so
