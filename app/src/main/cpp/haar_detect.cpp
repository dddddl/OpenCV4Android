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
#include <include/bm3d.h>

#define zoom 3 // 缩放因子, 将大图像缩小 n 倍显示
#define pi 3.1415926
#define  LOG_TAG    "PHOTO_FIX"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
using namespace cv;
using namespace std;

extern "C" {

int get_block_sum(Mat &sum, int x1, int y1, int x2, int y2, int i) {
    int tl = sum.ptr<int>(y1)[x1 * 3 + i]; //at<Vec3i>(y1, x1)[i];
    int tr = sum.ptr<int>(y2)[x1 * 3 + i];// at<Vec3i>(y2, x1)[i];
    int bl = sum.ptr<int>(y1)[x2 * 3 + i];//at<Vec3i>(y1, x2)[i];
    int br = sum.ptr<int>(y2)[x2 * 3 + i];// at<Vec3i>(y2, x2)[i];
    int s = (br - bl - tr + tl);
    return s;
}

float get_block_sqrt_sum(Mat &sum, int x1, int y1, int x2, int y2, int i) {
    float tl = sum.ptr<float>(y1)[x1 * 3 + i];// .at<Vec3f>(y1, x1)[i];
    float tr = sum.ptr<float>(y2)[x1 * 3 + i];// at<Vec3f>(y2, x1)[i];
    float bl = sum.ptr<float>(y1)[x2 * 3 + i];// <Vec3f>(y1, x2)[i];
    float br = sum.ptr<float>(y2)[x2 * 3 + i];// at<Vec3f>(y2, x2)[i];
    float var = (br - bl - tr + tl);
    return var;
}

// 填充Holes
void fillHole(const Mat srcBw, Mat &dstBw) {
    Size m_Size = srcBw.size();
    Mat Temp = Mat::zeros(m_Size.height + 2, m_Size.width + 2, srcBw.type());//延展图像
    srcBw.copyTo(Temp(Range(1, m_Size.height + 1), Range(1, m_Size.width + 1)));

    floodFill(Temp, Point(0, 0), Scalar(255));

    Mat cutImg;//裁剪延展的图像
    Temp(Range(1, m_Size.height + 1), Range(1, m_Size.width + 1)).copyTo(cutImg);

    dstBw = srcBw | (~cutImg);
}


CascadeClassifier face_detector;
JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_initLoad(JNIEnv *env, jobject,
                                                           jstring haarfilePath) {
    const char *nativeString = env->GetStringUTFChars(haarfilePath, 0);
    face_detector.load(nativeString);
    env->ReleaseStringUTFChars(haarfilePath, nativeString);
}
JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_faceDetection(JNIEnv *, jobject, jlong addrRgba) {
    int flag = 1000;
    Mat &mRgb = *(Mat *) addrRgba;
    Mat gray;
    cvtColor(mRgb, gray, COLOR_BGR2GRAY);
    vector<Rect> faces;
    //LOGD( "This is a number from JNI: %d", flag*2);
//    face_detector.detectMultiScale(gray, faces, 1.1, 1, 0, Size(50, 50), Size(300, 300));
    //LOGD( "This is a number from JNI: %d", flag*3);
    if (faces.empty()) return;
    for (int i = 0; i < faces.size(); i++) {
        rectangle(mRgb, faces[i], Scalar(255, 0, 0), 2, 8, 0);
    }

}

JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_compressGlass(JNIEnv *, jobject,
                                                                jlong addrFrame) {
    Mat &mFrame = *(Mat *) addrFrame;
    Mat mdstFrame;
    mFrame.copyTo(mdstFrame);
    int width = mFrame.cols;
    int height = mFrame.rows;

    float R;
    float a, b;
    float alpha = 0.75;
    float K = pi / 2;

    a = height / 2.0;
    b = width / 2.0;
    R = std::min(a, b);

    Point Center(width / 2, height / 2);

    float radius, r0, Dis, new_x, new_y;
    float p, q, x1, y1, x0, y0;
    float theta;

    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            y0 = Center.y - y;
            x0 = x - Center.x;
            Dis = x0 * x0 + y0 * y0;
            r0 = sqrt(Dis);
            if (Dis < R * R) {
                theta = atan(y0 / (x0 + 0.00001));
                if (x0 < 0) theta = theta + pi;

                radius = R * sin(r0 / R * K);
                radius = (radius - r0) * (alpha) + r0;
                new_x = radius * cos(theta);
                new_y = radius * sin(theta);
                new_x = Center.x + new_x;
                new_y = Center.y - new_y;

                if (new_x < 0) new_x = 0;
                if (new_x >= width - 1) new_x = width - 2;
                if (new_y < 0) new_y = 0;
                if (new_y >= height - 1) new_y = height - 2;

                x1 = (int) new_x;
                y1 = (int) new_y;

                p = new_x - x1;
                q = new_y - y1;

                for (int k = 0; k < 3; k++) {
                    mFrame.at<Vec3b>(y, x)[k] = (1 - p) * (1 - q) * mdstFrame.at<Vec3b>(y1, x1)[k] +
                                                (p) * (1 - q) * mdstFrame.at<Vec3b>(y1, x1 + 1)[k] +
                                                (1 - p) * (q) * mdstFrame.at<Vec3b>(y1 + 1, x1)[k] +
                                                (p) * (q) * mdstFrame.at<Vec3b>(y1 + 1, x1 + 1)[k];
                }
            }
        }
    }
    mdstFrame.release();
}
JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_magnifyGlass(JNIEnv *, jobject, jlong addrFrame) {
    Mat &mFrame = *(Mat *) addrFrame;
    Mat mdstFrame;
    mFrame.copyTo(mdstFrame);
    int width = mFrame.cols;
    int height = mFrame.rows;
    Point center(width / 2, height / 2);
    int R = sqrtf(width * width + height * height) / 4; //直接关系到放大的力度,与R成正比;
    for (int y = 0; y < height; y++) {
        uchar *img_p = mFrame.ptr<uchar>(y);//定义一个指针，指向第y列，从而可以访问行数据。
        for (int x = 0; x < width; x++) {

            int dis = norm(Point(x, y) - center);//获得当前点到中心点的距离
            if (dis < R)//设置变化区间
            {
                int newX = (x - center.x) * dis / R + center.x;
                int newY = (y - center.y) * dis / R + center.y;
                img_p[3 * x] = mdstFrame.at<uchar>(newY, newX * 3);
                img_p[3 * x + 1] = mdstFrame.at<uchar>(newY, newX * 3 + 1);
                img_p[3 * x + 2] = mdstFrame.at<uchar>(newY, newX * 3 + 2);
            }
        }
    }

    mdstFrame.release();

}

double angle;
int deltaI = 1;    //波浪周期;
int A = 30;        //波浪振幅;

JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_MultipleMagnifyGlass(JNIEnv *, jobject,
                                                                       jlong addrFrame, jint cx,
                                                                       jint cy, jint tx, jint ty) {
    Mat &mFrame = *(Mat *) addrFrame;
    Mat mdstFrame;
    mFrame.copyTo(mdstFrame);
    int width = mFrame.cols;
    int height = mFrame.rows;

    int channel = mFrame.channels();
    double r = 100;
    // 平移方向矢量/模
    double transVecX = tx - cx;
    double transVecY = ty - cy;
    double transVecModel = sqrt(transVecX * transVecX + transVecY * transVecY);

    for (int y = 0; y < height; y++) {
        uchar *img_p = mFrame.ptr<uchar>(y);//定义一个指针，指向第y列，从而可以访问行数据。
        for (int x = 0; x < width; x++) {
            //计算每个坐标点与触摸点之间的距离
            float dx = x - cx;
            float dy = y - cy;
            float dd = dx * dx + dy * dy;
            float d = sqrt(dd);

            if (d < r) {
                //变形系数，扭曲度
                double e = (r * r - dd) * (r * r - dd) /
                           ((r * r - dd + transVecModel * transVecModel) *
                            (r * r - dd + transVecModel * transVecModel));
                double pullX = e * (tx - cx);
                double pullY = e * (ty - cy);

                double oriX = x - pullX;
                double oriY = y - pullY;

                int x1 = int(oriX);
                int x2 = x1 + 1;
                int y1 = int(oriY);
                int y2 = y1 + 1;

                double part1 =
                        mdstFrame.at<uchar>(y1, x1) * (float(x2) - oriX) * (float(y2) - oriY);
                double part2 =
                        mdstFrame.at<uchar>(y1, x2) * (oriX - float(x1)) * (float(y2) - oriY);
                double part3 =
                        mdstFrame.at<uchar>(y2, x1) * (float(x2) - oriX) * (oriY - float(y1));
                double part4 =
                        mdstFrame.at<uchar>(y2, x2) * (oriX - float(x1)) * (oriY - float(y1));

                double insertValue = part1 * part2 * part3 * part4;

                LOGE("insertValue");


//                mFrame.at<uchar>(y, x) = insertValue;

//                for (int i = 0; i < channel; i++) {
//
//
//                    img_p[3 * x] = mdstFrame.at<uchar>(newY, newX * 3);
//                    img_p[3 * x + 1] = mdstFrame.at<uchar>(newY, newX * 3 + 1);
//                    img_p[3 * x + 2] = mdstFrame.at<uchar>(newY, newX * 3 + 2);
//
//
//
//                }
            }
        }
    }

    mdstFrame.release();
}

JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_segmentation(JNIEnv *, jobject, jlong addrsrc,
                                                               jlong addrdst) {

    Mat &src = *(Mat *) addrsrc;
    Mat &dst = *(Mat *) addrdst;

    cvtColor(src, src, COLOR_BGR2GRAY);
    Mat temp;
    temp = src;

    Mat edge;
    blur(src, edge, Size(3, 3));
    Canny(src, edge, 150, 100, 3);
    temp = edge;

    Mat element = getStructuringElement(MORPH_RECT, Size(3, 3));
    for (int i = 0; i < 3; ++i) {
        dilate(edge, edge, element);
    }
    temp = edge;

    for (int i = 0; i < 10; i++) // 填充10次
    {
        fillHole(edge, edge);
    }
//    dst = edge;

    inpaint(src, edge, dst, 5, INPAINT_TELEA);

    edge.release();
    temp.release();
}

JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_beautySkinFilter(JNIEnv *, jobject, jlong addrsrc,
                                                                   jlong addrdst,
                                                                   jfloat sigma, jboolean blur) {
    bool flag = (bool) blur;
    Mat &src = *(Mat *) addrsrc;
    Mat &dst = *(Mat *) addrdst;
    // 计算积分图
    Mat sum, sqrsum, ycrcb;
    cvtColor(src, ycrcb, COLOR_BGR2YCrCb);
    integral(src, sum, sqrsum, CV_32S, CV_32F);
    int w = src.cols;
    int h = src.rows;

    int x2 = 0, y2 = 0;
    int x1 = 0, y1 = 0;
    int ksize = 15;
    int radius = ksize / 2;
    int ch = src.channels();
    int cx = 0, cy = 0;
    float sigma2 = sigma * sigma;
    Mat mask = Mat::zeros(src.size(), CV_8UC1);
    int bgr[] = {0, 0, 0};
    for (int row = 0; row < h + radius; row++) {
        y2 = (row + 1) > h ? h : (row + 1);
        y1 = (row - ksize) < 0 ? 0 : (row - ksize);
        for (int col = 0; col < w + radius; col++) {
            x2 = (col + 1) > w ? w : (col + 1);
            x1 = (col - ksize) < 0 ? 0 : (col - ksize);
            cx = (col - radius) < 0 ? 0 : col - radius;
            cy = (row - radius) < 0 ? 0 : row - radius;
            int num = (x2 - x1) * (y2 - y1);
            for (int i = 0; i < ch; i++) {
                int s = get_block_sum(sum, x1, y1, x2, y2, i);
                float var = get_block_sqrt_sum(sqrsum, x1, y1, x2, y2, i);

                // 计算系数K
                float dr = (var - (s * s) / num) / num;
                float mean = s / num;
                float kr = dr / (dr + sigma2);

                // 得到滤波后的像素值
                int r = src.ptr<uchar>(cy)[cx * 3 + i];// at<Vec3b>(cy, cx)[i];
                bgr[i] = ycrcb.ptr<uchar>(cy)[cx * 3 + i];
                r = (int) ((1 - kr) * mean + kr * r);
                dst.ptr<uchar>(cy)[cx * 3 + i] = saturate_cast<uchar>(r);
            }
            if ((bgr[0] > 80) && (85 < bgr[2] && bgr[2] < 135) && (135 < bgr[1] && bgr[1] < 180)) {
                mask.at<uchar>(cy, cx) = 255;
            }
        }
    }
    sum.release();
    ycrcb.release();
    sqrsum.release();

    Mat blur_mask, blur_mask_f;

    // 高斯模糊
    GaussianBlur(mask, blur_mask, Size(3, 3), 0.0);
    blur_mask.convertTo(blur_mask_f, CV_32F);
    normalize(blur_mask_f, blur_mask_f, 1.0, 0, NORM_MINMAX);

    // 高斯权重混合
    Mat clone = dst.clone();
    for (int row = 0; row < h; row++) {
        uchar *srcRow = src.ptr<uchar>(row);
        uchar *dstRow = dst.ptr<uchar>(row);
        uchar *cloneRow = clone.ptr<uchar>(row);
        float *mask_row = blur_mask_f.ptr<float>(row);
        for (int col = 0; col < w; col++) {
            int b1 = *srcRow++;
            int g1 = *srcRow++;
            int r1 = *srcRow++;

            int b2 = *cloneRow++;
            int g2 = *cloneRow++;
            int r2 = *cloneRow++;

            float w2 = *mask_row++;
            float w1 = 1.0f - w2;

            b2 = (int) (b2 * w2 + w1 * b1);
            g2 = (int) (g2 * w2 + w1 * g1);
            r2 = (int) (r2 * w2 + w1 * r1);

            *dstRow++ = saturate_cast<uchar>(b2);
            *dstRow++ = saturate_cast<uchar>(g2);
            *dstRow++ = saturate_cast<uchar>(r2);
        }
    }
    clone.release();
    blur_mask.release();
    blur_mask_f.release();

    // 边缘提升
    Canny(src, mask, 150, 300, 3, true);
    bitwise_and(src, src, dst, mask);

    // 亮度提升
    add(dst, Scalar(10, 10, 10), dst);
    if (flag) {
        GaussianBlur(dst, dst, Size(3, 3), 0);
    }
}



JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_bm3dFilter(JNIEnv *, jobject, jlong addrsrc,
                                                             jlong addrdst) {

    Mat &src = *(Mat *) addrsrc;
    Mat &dst = *(Mat *) addrdst;

    int sigma = 25;

    //convert data type
    Mat Pic(src.size(), CV_32FC1);
    Mat Noisy(src.size(), CV_32FC1);
    Mat Basic(src.size(), CV_32FC1);
    Mat Denoised(src.size(), CV_32FC1);

    uchar2float(src, Pic);
    addNoise(sigma, Pic, Noisy);

    //convert type for displaying
    Mat basic(src.size(), CV_8U);
    Mat noisy(src.size(), CV_8U);
    Mat denoised(src.size(), CV_8U);

    float2uchar(Noisy, noisy);

//    imshow("origin", src);
//    imshow("noisy", noisy);

    //caiculate time used and psnr
    runBm3d(sigma, Noisy, Basic, Denoised);//main denoising method
//
    float2uchar(Basic, basic);
    float2uchar(Denoised, dst);
//    imshow("basic", basic);
//    imshow("denoised", denoised);
}

JNIEXPORT void JNICALL
Java_com_ddddl_opencvdemo_nativehelper_FaceHelper_homoFilter(JNIEnv *, jobject, jlong addrsrc,
                                                             jlong addrdst) {

    Mat &src = *(Mat *) addrsrc;
    Mat &dst = *(Mat *) addrdst;

    src.convertTo(src, CV_64FC1);  //64位，对应double
    dst.convertTo(dst, CV_64FC1);
    //对数变换
    for (int i = 0; i < src.rows; i++)
    {
        double* srcdata = src.ptr<double>(i);
        for (int j = 0; j < src.cols; j++)
        {
            srcdata[j] = log(srcdata[j] + 0.0001);
        }
    }

    //离散余弦变换
    Mat mat_dct = Mat::zeros(src.rows, src.cols, CV_64FC1);
    dct(src, mat_dct);

    //滤波
    Mat H;
    double gammaH = 1.5;
    double gammaL = 0.5;
    double C = 1;
    double d0 = (src.rows / 2)*(src.rows / 2) + (src.cols / 2)*(src.cols / 2);
    double d2 = 0;
    H = Mat::zeros(src.rows, src.cols, CV_64FC1);

    double totalWeight = 0.0;
    for (int i = 0; i < src.rows; i++)
    {
        double * dataH = H.ptr<double>(i);
        for (int j = 0; j < src.cols; j++)
        {
            d2 = pow((i), 2.0) + pow((j), 2.0);
            dataH[j] = (gammaH - gammaL)*(1 - exp(-C*d2 / d0)) + gammaL;
            totalWeight += dataH[j];
        }
    }
    H.ptr<double>(0)[0] = 1.1;
    mat_dct = mat_dct.mul(H);

    //逆变换
    idct(mat_dct, dst);

    //取指数
    for (int i = 0; i < src.rows; i++)
    {
        double* srcdata = dst.ptr<double>(i);
        double* dstdata = dst.ptr<double>(i);
        for (int j = 0; j < src.cols; j++)
        {
            dstdata[j] = exp(srcdata[j]);
        }
    }
    normalize(dst,dst,255,0,NORM_MINMAX);

    //转换为8位灰度图像
    dst.convertTo(dst, CV_8UC1);

}

}

