//
// Created by liuliang on 2019/6/4.
//
#include<jni.h>
#include<opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <opencv2/objdetect.hpp>
#include <iostream>
#include<vector>
#include <string>
#include <android/log.h>

#define  LOG_TAG    "MYHAARDETECTION"

#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

using namespace cv;
using namespace std;

extern "C" {

CascadeClassifier face_detector;
JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_initLoad(JNIEnv *env, jobject, jstring haarfilePath) {
    const char *nativeString = env->GetStringUTFChars(haarfilePath, 0);
    face_detector.load(nativeString);
    env->ReleaseStringUTFChars(haarfilePath, nativeString);
    LOGD("Method Description: %s", "loaded haar files...");
}
JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_faceDetection(JNIEnv *, jobject, jlong addrRgba) {
    int flag = 1000;
    Mat &mRgb = *(Mat *) addrRgba;
    Mat gray;
    cvtColor(mRgb, gray, COLOR_BGR2GRAY);
    vector<Rect> faces;
    //LOGD( "This is a number from JNI: %d", flag*2);
    face_detector.detectMultiScale(gray, faces, 1.1, 1, 0, Size(50, 50), Size(300, 300));
    //LOGD( "This is a number from JNI: %d", flag*3);
    if (faces.empty()) return;
    for (int i = 0; i < faces.size(); i++) {
        rectangle(mRgb, faces[i], Scalar(255, 0, 0), 2, 8, 0);
        LOGD("Face Detection : %s", "Found Face");
    }

}
}
