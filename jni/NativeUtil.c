#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <setjmp.h>
#include <math.h>
#include <stdint.h>
#include "NativeUtil.h"
#include <stdio.h>
#include "vanitygen/vanitygen.h"



char* jstringTostring(JNIEnv* env, jstring jstr)
{
       char* rtn = NULL;
       jclass clsstring = (*env)->FindClass(env,"java/lang/String");
       jstring strencode = (*env)->NewStringUTF(env,"utf-8");
       jmethodID mid = (*env)->GetMethodID(env,clsstring, "getBytes", "(Ljava/lang/String;)[B");
       jbyteArray barr= (jbyteArray)(*env)->CallObjectMethod(env,jstr, mid, strencode);
       jsize alen = (*env)->GetArrayLength(env,barr);
       jbyte* ba = (*env)->GetByteArrayElements(env,barr, JNI_FALSE);
       if (alen > 0)
       {
                 rtn = (char*)malloc(alen + 1);
                 memcpy(rtn, ba, alen);
                 rtn[alen] = 0;
       }
       (*env)->ReleaseByteArrayElements(env,barr, ba, 0);
       return rtn;
}

//char* to jstring
jstring stoJstring(JNIEnv* env, const char* pat)
{
       jclass strClass = (*env)->FindClass(env,"Ljava/lang/String;");
       jmethodID ctorID = (*env)->GetMethodID(env,strClass, "<init>", "([BLjava/lang/String;)V");
       jbyteArray bytes = (*env)->NewByteArray(env,strlen(pat));
       (*env)->SetByteArrayRegion(env,bytes, 0, strlen(pat), (jbyte*)pat);
       jstring encoding = (*env)->NewStringUTF(env,"utf-8");
       return (jstring)(*env)->NewObject(env,strClass, ctorID, bytes, encoding);
}


JNIEXPORT void JNICALL Java_net_bither_utils_NativeUtil_generateAddress
  (JNIEnv * env, jobject object, jstring string){
      char** pP = NULL;
      pP = (char**)calloc(2, sizeof(char*));
      pP[0] = "./vanitygen";
      pP[1] = jstringTostring(env,string);
      vanitygen(2,pP);
      return;

  }

JNIEXPORT jobjectArray JNICALL Java_net_bither_utils_NativeUtil_getPrivateKey
  (JNIEnv * env, jobject object){
      jstring      str;
      jobjectArray args = 0;
      char** sa = getPrivatekey();
      jsize len =2;
      if(!sa){
          printf("result isnull");
          return NULL;
      }
      int          i=0;
      args = (*env)->NewObjectArray(env,len,(*env)->FindClass(env,"java/lang/String"),0);
      for( i=0; i < len; i++ )
      {
          str = stoJstring(env,sa[i]);
          (*env)->SetObjectArrayElement(env,args, i, str);
          free(sa[i]);
          sa[i]=NULL;
          str=NULL;
      }
      return args;


  }

JNIEXPORT jobjectArray JNICALL Java_net_bither_utils_NativeUtil_getProgress
  (JNIEnv * env, jobject object){
        jdouble   str;
        jobjectArray args = 0;
        double* sa = getProgresses();
        jsize len =4;
        if(!sa){
            printf("result isnull");
            return NULL;
        }
        args = (*env)->NewDoubleArray(env,len);
        (*env)->SetDoubleArrayRegion(env,args,0,len,sa);
        return args;


  }


