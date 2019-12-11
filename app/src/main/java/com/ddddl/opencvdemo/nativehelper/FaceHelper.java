package com.ddddl.opencvdemo.nativehelper;

public class FaceHelper {

    public native void faceDetection(long frameAddress);

    public native void magnifyGlass(long frameAddress);

    public native void MultipleMagnifyGlass(long frameAddress, int x, int y, int tX, int tY);

    public native void compressGlass(long frameAddress);

    public native void initLoad(String haarFilePath);

    public native void beautySkinFilter(long srcAddress, long dstAddress, float sigma, boolean blur);

    public native void PhotoFix(long srcAddress, long dstAddress, int dstColor, int[] cutArr);

    public native void FixBackground(long srcAddress, long maskAddress, long dstAddress);

    public native int drawRect(long srcAddress, int[] cutArr);

    public native int segmentation(long srcAddress, long dstAddress);

    public native int p2p(long srcAddress, long maskAddress, long dstAddress);

    public native int bm3dFilter(long srcAddress, long dstAddress);

    public native int homoFilter(long srcAddress, long dstAddress);
}
