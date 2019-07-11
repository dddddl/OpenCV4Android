package com.ddddl.opencvdemo.nativehelper;

public class FaceHelper {

    public native void faceDetection(long frameAddress);

    public native void initLoad(String haarFilePath);

    public native void beautySkinFilter(long srcAddress, long dstAddress, float sigma, boolean blur);

    public native void PhotoFix(long srcAddress, long dstAddress, int dstColor, int[] cutArr);

    public native int drawRect(long srcAddress, int[] cutArr);

    public native int segmentation(long srcAddress, long dstAddress);
}
