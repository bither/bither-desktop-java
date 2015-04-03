#include "Vanitygen.h"
#include "vanitygen/vanitygen.h"
#include "JniUtil.h"


JNIEXPORT jint JNICALL Java_net_bither_utils_Vanitygen_generateAddress
  (JNIEnv * env, jclass object, jstring string,jboolean ignore){
      char** pP = NULL;
      int index=0;
      int count=2;
      if(ignore){
        count++;
      }
      pP = (char**)calloc(count, sizeof(char*));
      pP[index] = "./vanitygen";
      index++;
      if(ignore){
        pP[index]="-i";
        index++;
      }
      pP[index] = jstringTostring(env,string);
      printf("vanjni\n%s",pP[1]);
      return vanitygen(count,pP);

  }

JNIEXPORT jobjectArray JNICALL Java_net_bither_utils_Vanitygen_getPrivateKey
  (JNIEnv * env, jclass object){
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


JNIEXPORT jdoubleArray JNICALL Java_net_bither_utils_Vanitygen_getProgress
  (JNIEnv * env, jclass object){
        jdouble   str;
        jobjectArray args = 0;
        double* sa = getProgresses();
        jsize len =getdoublelen(sa);
        if(!sa){
            printf("result isnull");
            return NULL;
        }
        args = (*env)->NewDoubleArray(env,len);
        (*env)->SetDoubleArrayRegion(env,args,0,len,sa);
        return args;


  }

JNIEXPORT void JNICALL Java_net_bither_utils_Vanitygen_quit
  (JNIEnv * env, jclass object){

 }

