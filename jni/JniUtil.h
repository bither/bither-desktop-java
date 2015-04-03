#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <setjmp.h>
#include <math.h>
#include <stdint.h>
#include <stdio.h>

extern char* jstringTostring(JNIEnv* env, jstring jstr);

extern jstring stoJstring(JNIEnv* env, const char* pat);

extern int getlen(char *result);

extern int getdoublelen(double *result);