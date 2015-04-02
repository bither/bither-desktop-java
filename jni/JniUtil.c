#include "JniUtil.h"



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

int getlen(char *result)
  {
      int i=0;
      if(result==NULL){
        return 0;
      }
      while(result[i]!='\0')
      {
          i++;

      }
      return i;

  }
