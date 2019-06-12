//
// Created by liuliang on 2019/6/4.
//
#include<jni.h>
#include"include/opencv2/opencv.hpp"
#include "include/opencv2/core.hpp"
#include "include/opencv2/objdetect.hpp"
#include "include/opencv2/videostab/inpainting.hpp"
#include <iostream>
#include<vector>
#include <string>
#include <android/log.h>

#define  LOG_TAG    "PHOTO_FIX"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

using namespace cv;
using namespace std;

extern "C" {

string Int_to_String(int n) {

    ostringstream stream;

    stream << n; //n为int类型

    return stream.str();

}

JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_PhotoFix(JNIEnv *env, jobject, jlong addrsrc,
                                                           jlong addrdst, jint dstColor, jintArray rectArray) {

    Mat &src = *(Mat *) addrsrc;
    Mat &dst = *(Mat *) addrdst;
    jint *carr = (env)->GetIntArrayElements(rectArray, NULL);
    Mat imageGray;

    if (carr == NULL) {
        cvtColor(src, imageGray, COLOR_BGR2GRAY);
    } else {
        Rect rect = Rect((int) carr[0], (int) carr[1], carr[2] - carr[0], (int) carr[3] - carr[1]);
        Mat mask = Mat::zeros(src.size(), CV_8UC1);
        mask(rect).setTo(255);
        Mat ROI;
        src.copyTo(ROI, mask);
        cvtColor(ROI, imageGray, COLOR_BGR2GRAY);
        ROI.release();
    }

    env->ReleaseIntArrayElements(rectArray, carr, 0);

    Mat imageMask = Mat(src.size(), CV_8UC1, Scalar::all(0));
    //通过阈值处理生成Mask
    if (dstColor > 240) {
        threshold(imageGray, imageMask, 240, 255, CV_THRESH_BINARY);
    } else {
        threshold(imageGray, imageGray, dstColor + 5, 255, CV_THRESH_TOZERO_INV);
        threshold(imageGray, imageMask, dstColor - 5, 255, CV_THRESH_BINARY);
    }
    Mat Kernel = getStructuringElement(MORPH_RECT, Size(5, 5));
    //对Mask膨胀处理，增加Mask面积
    dilate(imageMask, imageMask, Kernel);
    //图像修复
    inpaint(src, imageMask, dst, 5, INPAINT_TELEA);

    imageGray.release();
    imageMask.release();
    Kernel.release();
}

JNIEXPORT int JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_drawRect(JNIEnv *env, jobject, jlong addrsrc, jintArray rectArray) {
    Mat &src = *(Mat *) addrsrc;
    jint *carr = (env)->GetIntArrayElements(rectArray, NULL);
    if (carr == NULL) {
        return 0;
    }
    Rect rect = Rect((int) carr[0], (int) carr[1], carr[2] - carr[0], (int) carr[3] - carr[1]);
    rectangle(src, rect, Scalar(240, 240, 240));
    env->ReleaseIntArrayElements(rectArray, carr, 0);
    return 1;
}

}