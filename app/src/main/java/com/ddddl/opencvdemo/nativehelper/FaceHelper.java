package com.ddddl.opencvdemo.nativehelper;

import org.opencv.core.Mat;

public class FaceHelper {

    public native void faceDetection(long frameAddress);

    public native void initLoad(String haarFilePath);

    public native void beautySkinFilter(long srcAddress, long dstAddress, float sigma, boolean blur);

    public native void fixpic(long srcAddress,long dstAddress);
}
