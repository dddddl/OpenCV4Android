package com.ddddl.opencvdemo.nativehelper;

public class FaceHelper {

    public native void faceDetection(long frameAddress);

    public native void initLoad(String haarFilePath);
}
