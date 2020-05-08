#include <jni.h>
#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <iostream>
#include <string>
#include <sstream>
#include <string.h>
#include "utf16char.h"
using namespace std;

struct HanZi{
    char* hanzi;
    char* pinyin;
    int cishu;
};

bool isok(char* yuanshi, int cishu, const char* neirong, int geshu) {
    int chuxianchisu = 0;
    char* shaixuahou;
    shaixuahou = strchr(yuanshi, ',');
    cishu--;
    if (cishu < 0 || shaixuahou == NULL) {
        return false;
    }
    unsigned char i = 0;
    while (shaixuahou[i] != '\0')
    {
        shaixuahou[i] = shaixuahou[i + 1];
        i++;  //源一直移动
    }
//    cout << "比较内容" << shaixuahou << endl;
    if (!strncmp(shaixuahou, neirong, geshu) && cishu == 0) {
        cout << "比较内容" << shaixuahou << endl;
        return true;
    }
    else
    {
        return isok(shaixuahou, cishu, neirong, geshu);
    }
}

HanZi *hanzis[100000] = {};
int allofhanzi;
int* houxuanarray;
int xianyougeshu = -1;
/**
 * 把txt中的数据加入到内存中 大小占用19MB
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_nuc_omeletteinputmethod_inputC_InputC_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    int *beixuanarrary;
    ifstream ifs("/storage/emulated/0/搜狗聊天用于.txt");
    string str;
    allofhanzi = 0;
    int allallofhanzi = 0;
    while (ifs >> str)
    {
        if (allallofhanzi % 3 == 0)
        {
            hanzis[allofhanzi] = (HanZi*)malloc(sizeof(HanZi));
            hanzis[allofhanzi]->pinyin =(char*)malloc(str.length()+3);
//            strcpy_s(hanzis[allofhanzi]->pinyin, str.length() +1, str.c_str());
            strcpy(hanzis[allofhanzi]->pinyin,str.c_str());
        }
        if (allallofhanzi % 3 == 1)
        {
            hanzis[allofhanzi]->hanzi = (char*)malloc(str.length() + 1);
 //           strcpy_s(hanzis[allofhanzi]->hanzi, str.length()+1, str.c_str());
            strcpy(hanzis[allofhanzi]->hanzi,str.c_str());
        }
        if (allallofhanzi % 3 == 2)
        {
            int n = atoi(str.c_str());
            hanzis[allofhanzi]->cishu = n;
            allofhanzi++;
        }
        allallofhanzi++;
    }
    ifs.close();
    houxuanarray = (int*)calloc(allofhanzi, sizeof(int));
    string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

//工具 用于 string转为java String
jstring str2jstring(JNIEnv *env, const char *pat) {
    //定义java String类 strClass
    jclass strClass = (env)->FindClass("java/lang/String");
    //获取String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
    jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    //建立byte数组
    jbyteArray bytes = (env)->NewByteArray(strlen(pat));
    //将char* 转换为byte数组
    (env)->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte *) pat);
    // 设置String, 保存语言类型,用于byte数组转换至String时的参数
    jstring encoding = (env)->NewStringUTF("GB2312");
    //将byte数组转换为java String,并输出
    return (jstring) (env)->NewObject(strClass, ctorID, bytes, encoding);
}

//工具 用于 java String 转为 string
std::string jstring2str(JNIEnv* env, jstring jstr)
{
char*   rtn   =   NULL;
jclass   clsstring   =   env->FindClass("java/lang/String");
jstring   strencode   =   env->NewStringUTF("GB2312");
jmethodID   mid   =   env->GetMethodID(clsstring,   "getBytes",   "(Ljava/lang/String;)[B");
jbyteArray   barr=   (jbyteArray)env->CallObjectMethod(jstr,mid,strencode);
jsize   alen   =   env->GetArrayLength(barr);
jbyte*   ba   =   env->GetByteArrayElements(barr,JNI_FALSE);
if(alen   >   0)
{
rtn   =   (char*)malloc(alen+1);
memcpy(rtn,ba,alen);
rtn[alen]=0;
}
env->ReleaseByteArrayElements(barr,ba,0);
std::string stemp(rtn);
free(rtn);
return   stemp;
}


char* jstringToChar(JNIEnv* env, jstring jstr) {
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("GB2312");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char*) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}
extern "C" JNIEXPORT jstring JNICALL
Java_com_nuc_omeletteinputmethod_inputC_InputC_getStringOfScendFromJNI(
        JNIEnv* env,jobject /* this */,jstring put,jint cishu) {
    string retu;
    string insome = jstring2str(env,put);
    int geshu = 0;
    char* xinshujv;
    for (size_t i = 0; i < xianyougeshu; i++)
    {
        int j = 0;
        while (hanzis[houxuanarray[i]]->pinyin[j] != '\0')
        {
            j++;  //源一直移动
        }
        xinshujv = (char*)malloc(j+1);
        strcpy(xinshujv,hanzis[houxuanarray[i]]->pinyin);
        if (isok(xinshujv,cishu,insome.c_str(),insome.length()))
        {
            houxuanarray[geshu] = houxuanarray[i];
            geshu++;
        }
        free(xinshujv);
    }
    if (xianyougeshu!=-1&&geshu != 0){
        xianyougeshu = geshu;
    }
    //retu = retu + "现在有"+  to_string(xianyougeshu);
    for (size_t i = 0; i < geshu; i++)
    {
        retu = retu + hanzis[houxuanarray[i]]->hanzi+",";
    }
    return str2jstring(env,retu.c_str());
}
extern "C" JNIEXPORT jstring JNICALL
Java_com_nuc_omeletteinputmethod_inputC_InputC_getStringOfFristFromJNI(
        JNIEnv* env,jobject /* this */,jstring put) {
    string retu;
    int ls = jstring2str(env,put).length();
    char* insome = jstringToChar(env,put);
    int geshu = 0;
    for (size_t i = 0; i < allofhanzi; i++)
    {
        if (!strncmp(hanzis[i]->pinyin, insome,ls)) {
            houxuanarray[geshu] = i;
            geshu++;
        }
    }
    if (geshu != 0){
        xianyougeshu = geshu;
    }
    //retu = retu + "现在有"+  to_string(xianyougeshu);
    for (size_t i = 0; i < geshu; i++)
    {
        retu = retu + hanzis[houxuanarray[i]]->hanzi+",";
    }
    return str2jstring(env,retu.c_str());
}
extern "C" JNIEXPORT jstring JNICALL
Java_com_nuc_omeletteinputmethod_inputC_InputC_removeHanziArraryFromJNI(
        JNIEnv* env,jobject /* this */) {
    string retu;
    return str2jstring(env,retu.c_str());
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_nuc_omeletteinputmethod_inputC_InputC_getStringForReadyFromJNI(JNIEnv *env, jobject thiz,
                                                                        jstring wenzi) {
    int ls = jstring2str(env,wenzi).length();
    char* insome = jstringToChar(env,wenzi);
    string retu;
    int geshu = 0;

    for (size_t i = 0; i < allofhanzi; i++)
    {
        if (!strncmp(hanzis[i]->hanzi, insome,ls)) {
            retu = retu + hanzis[i]->hanzi+",";
        }
    }
    return str2jstring(env,retu.c_str());

    // TODO: implement getStringForReadyFromJNI()
}