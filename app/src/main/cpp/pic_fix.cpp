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

#define  LOG_TAG    "MYHAARDETECTION"

using namespace cv;
using namespace std;

extern "C" {
JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_fixpic(JNIEnv *, jobject, jlong addrsrc,
                                                         jlong addrdst) {

    Mat &src = *(Mat *) addrsrc;
//    Mat &mask = *(Mat *) addrmask;
    Mat &dst = *(Mat *) addrdst;

    Mat imageGray;
    cvtColor(src, imageGray, COLOR_BGR2GRAY);

    Mat imageMask = Mat(src.size(), CV_8UC1, Scalar::all(0));

    //通过阈值处理生成Mask
    threshold(imageGray, imageMask, 240, 255, CV_THRESH_BINARY);
    Mat Kernel = getStructuringElement(MORPH_RECT, Size(3, 3));
    //对Mask膨胀处理，增加Mask面积
    dilate(imageMask, imageMask, Kernel);
    //图像修复
    inpaint(src, imageMask, dst, 5, INPAINT_TELEA);

    imageGray.release();
    imageMask.release();
    Kernel.release();
}
}