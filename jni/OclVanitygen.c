#include "OclVanitygen.h"
#include "vanitygen/oclvanitygen.h"
#include "vanitygen/oclengine.h"
#include "JniUtil.h"



JNIEXPORT jint JNICALL Java_net_bither_utils_OclVanitygen_oclGenerateAddress
  (JNIEnv * env, jclass object, jstring string ,jint equipment, jboolean ignore){
      char** params = NULL;
      printf("ocl\n");
      int index=0;
      int count=3;
      if(ignore){
         count++;
      }
      params = (char**)calloc(count, sizeof(char*));
      params[index] = "./oclvanitygen";
      index++;
      char * s;
      sprintf(s, "-D %d:0", (int)equipment);
      params[index]=s;

      printf("oclvanjni:%s\n",s);

      index++;
      if(ignore){
         params[index]="-i";
         index++;
      }
      params[index] = jstringTostring(env,string);
      printf("oclvanjni\n");
      return  oclvanitygen(count,params);

  }

JNIEXPORT jobjectArray JNICALL Java_net_bither_utils_OclVanitygen_oclGetPrivateKey
  (JNIEnv * env, jclass object){
      jstring      str;
      jobjectArray args = 0;
      char** sa = oclgetPrivatekey();
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

JNIEXPORT jdoubleArray JNICALL Java_net_bither_utils_OclVanitygen_oclGetProgress
  (JNIEnv * env, jclass object){
        jdouble   str;
        jobjectArray args = 0;
        double* sa = oclgetProgresses();
        jsize len =4;
        if(!sa){
            printf("result isnull");
            return NULL;
        }
        args = (*env)->NewDoubleArray(env,len);
        (*env)->SetDoubleArrayRegion(env,args,0,len,sa);
        return args;


  }

 JNIEXPORT jobjectArray JNICALL Java_net_bither_utils_OclVanitygen_getDevices
    (JNIEnv * env, jclass object){
        jstring      str;
        jobjectArray args = 0;
        char** sa = vg_ocl_enumerate_devices();
        jint len =0;
        while(getlen(sa[len])>0){
            ++len;
        }
        if(!sa){
            printf("result isnull");
            return NULL;
        }
         int i=0;
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

 JNIEXPORT void JNICALL Java_net_bither_utils_OclVanitygen_oclQuit
   (JNIEnv * env, jclass object){

   }
