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
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_MaskPhoto(JNIEnv *env, jobject, jlong addrsrc,
                                                            jlong addrdst, jlong addmask) {
    Mat &src = *(Mat *) addrsrc;
    Mat &dst = *(Mat *) addrdst;
    Mat &mask = *(Mat *) addmask;


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
    } else if (dstColor <= 10) {
        threshold(imageGray, imageGray, 10, 255, CV_THRESH_TOZERO_INV);
        threshold(imageGray, imageMask, 0, 255, CV_THRESH_BINARY);
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

JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_FixBackground(JNIEnv *env, jobject, jlong addrsrc, jlong addrmask,
                                                                jlong addrdst) {

    Mat &src = *(Mat *) addrsrc;
    Mat &mask = *(Mat *) addrmask;
    Mat &dst = *(Mat *) addrdst;

    cvtColor(src, src, COLOR_BGRA2BGR);
    cvtColor(mask, mask, COLOR_RGBA2GRAY);

    Mat Kernel = getStructuringElement(MORPH_RECT, Size(5, 5));
    //对Mask膨胀处理，增加Mask面积
    dilate(mask, mask, Kernel);
    dilate(mask, mask, Kernel);
    dilate(mask, mask, Kernel);
    //图像修复
    inpaint(src, mask, dst, 5, INPAINT_TELEA);

    src.release();
    mask.release();
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

JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_feather(JNIEnv *env, jobject, jlong addrsrc, jlong addrdst) {
    Mat &src = *(Mat *) addrsrc;
    Mat &dst = *(Mat *) addrdst;

    float mSize = 0.5;
    int width = src.cols;
    int heigh = src.rows;
    int centerX = width >> 1;
    int centerY = heigh >> 1;

    int maxV = centerX * centerX + centerY * centerY;
    int minV = (int) (maxV * (1 - mSize));
    int diff = maxV - minV;
    float ratio = width > heigh ? (float) heigh / (float) width : (float) width / (float) heigh;

    Mat img;
    src.copyTo(img);

    Scalar avg = mean(src);

    Mat mask1u[3];
    float tmp, r;
    for (int y = 0; y < heigh; y++) {
        uchar *imgP = img.ptr<uchar>(y);
        uchar *dstP = dst.ptr<uchar>(y);
        for (int x = 0; x < width; x++) {
            int b = imgP[3 * x];
            int g = imgP[3 * x + 1];
            int r = imgP[3 * x + 2];

            float dx = centerX - x;
            float dy = centerY - y;

            if (width > heigh)
                dx = (dx * ratio);
            else
                dy = (dy * ratio);

            int dstSq = dx * dx + dy * dy;

            float v = ((float) dstSq / diff) * 255;

            r = (int) (r + v);
            g = (int) (g + v);
            b = (int) (b + v);
            r = (r > 255 ? 255 : (r < 0 ? 0 : r));
            g = (g > 255 ? 255 : (g < 0 ? 0 : g));
            b = (b > 255 ? 255 : (b < 0 ? 0 : b));

            dstP[3 * x] = (uchar) b;
            dstP[3 * x + 1] = (uchar) g;
            dstP[3 * x + 2] = (uchar) r;
        }
    }
}

JNIEXPORT int JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_p2p(JNIEnv *env, jobject, jlong addrsrc, jlong addrmask,
                                                      jlong addrdst) {

    Mat &src = *(Mat *) addrsrc;
    Mat &mask = *(Mat *) addrmask;
    Mat &result = *(Mat *) addrdst;

//    namedWindow("input", CV_WINDOW_AUTOSIZE);
//    imshow("input", src);

//组装数据并运行KMeans
    int width = src.cols;
    int height = src.rows;
//    int dims = src.channels();
//    int pointsCount = width * height;
//    Mat points(pointsCount, dims, CV_32F);//kmeans要求的数据为float类型的
//    int index = 0;
//    for (int i = 0; i < height; i++) {
//        for (int j = 0; j < width; j++) {
//            index = i * width + j;
//            points.at<float>(index, 0) = src.at<Vec3b>(i, j)[0];
//            points.at<float>(index, 1) = src.at<Vec3b>(i, j)[1];
//            points.at<float>(index, 2) = src.at<Vec3b>(i, j)[2];
//        }
//    }
//
//    Mat bestLabels;
//    Mat centers;
//    kmeans(points, 4, bestLabels, TermCriteria(TermCriteria::COUNT + TermCriteria::EPS, 10, 0.1), 3, 2, centers);
////去背景+遮罩层
//    Mat mask(src.size(), CV_8UC1);
//    index = src.cols * 2 + 2;
//    int bindex = bestLabels.at<int>(index, 0);//获得kmeans后背景的标签
//    Mat dst;
//    src.copyTo(dst);
//    for (int i = 0; i < height; i++) {
//        for (int j = 0; j < width; j++) {
//            index = i * width + j;
//            int label = bestLabels.at<int>(index, 0);
//            if (label == bindex) {
//                dst.at<Vec3b>(i, j)[0] = 0;
//                dst.at<Vec3b>(i, j)[1] = 0;
//                dst.at<Vec3b>(i, j)[2] = 0;
//                mask.at<uchar>(i, j) = 0;
//            } else {
//                mask.at<uchar>(i, j) = 255;
//            }
//        }
//    }
//    imshow("mask", mask);
//    imshow("kmeans", dst);
    //对掩码进行腐蚀+高斯模糊
    Mat kernel = getStructuringElement(MORPH_RECT, Size(3, 3));
    erode(mask, mask, kernel);
//    imshow("erode mask", mask);
    GaussianBlur(mask, mask, Size(3, 3), 0, 0);
//    imshow("blur mask", mask);
//通道混合
    Vec3b color;
    color[0] = theRNG().uniform(0, 255);
    color[1] = theRNG().uniform(0, 255);
    color[2] = theRNG().uniform(0, 255);

//    Mat result(src.size(), src.type());

    double w = 0.0;
    int b = 0, g = 0, r = 0;
    int b1 = 0, g1 = 0, r1 = 0;
    int b2 = 0, g2 = 0, r2 = 0;
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            int m = mask.at<uchar>(i, j);
            if (m == 255) {
                result.at<Vec3b>(i, j) = src.at<Vec3b>(i, j);//将原图像中前景赋给结果图像中的前景
            } else if (m == 0) {
                result.at<Vec3b>(i, j) = color;//将随机生成的颜色赋给结果图像中的背景
            } else {
                w = m / 255.0;//权重
//边缘前景
                b1 = src.at<Vec3b>(i, j)[0];
                g1 = src.at<Vec3b>(i, j)[1];
                r1 = src.at<Vec3b>(i, j)[2];
//边缘背景
                b2 = color[0];
                g2 = color[1];
                r2 = color[2];
//边缘融合
                b = b1 * w + b2 * (1.0 - w);
                g = g1 * w + g2 * (1.0 - w);
                r = r1 * w + r2 * (1.0 - w);
                result.at<Vec3b>(i, j)[0] = b;
                result.at<Vec3b>(i, j)[1] = g;
                result.at<Vec3b>(i, j)[2] = r;
            }
        }
    }

//    imshow("result", result);
    return 0;

}

}