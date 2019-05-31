package com.ddddl.opencvdemo

import android.util.Log
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

object FeatureMatchUtil {


    fun harrisCornerDemo(src: Mat, process: (Mat) -> Unit) {

        val threshold = 100
        val gray = Mat()
        val response = Mat()
        val response_norm = Mat()
        val dst = Mat()

        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGRA2GRAY)
        Imgproc.cornerHarris(gray, response, 2, 3, 0.05)
        Core.normalize(response, response_norm, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_32F)

        dst.create(src.size(), src.type())
        src.copyTo(dst)

        val data = FloatArray(1)
        for (j in 0 until response_norm.rows()) {
            for (i in 0 until response_norm.cols()) {

                response_norm.get(j, i, data)
                if (data[0].toInt() > 100) {
                    Imgproc.circle(dst, Point(i.toDouble(), j.toDouble()), 2, Scalar(0.0, 0.0, 255.0))
                    Log.e("Feature", "found")
                }
            }
        }
        src.release()
        gray.release()
        response.release()
        response_norm.release()
        process.invoke(dst)
    }

    fun shiTomasicornerDemo(src: Mat, process: (Mat) -> Unit) {

        val k = 0.04
        val blockSize = 3
        val qualityLevel = 0.01
        val useHarrisCorner = false

        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGRA2GRAY)

        val corners = MatOfPoint()
        Imgproc.goodFeaturesToTrack(
            gray, corners, 100,
            qualityLevel, 10.0
        )

        val dst = Mat()
        dst.create(src.size(), src.type())
        src.copyTo(dst)

        val points = corners.toArray()
        for (i in points.indices) {
            Imgproc.circle(dst, points[i], 5, Scalar(0.0, 0.0, 255.0), 2, 8, 0)
        }
        gray.release()
        process.invoke(dst)

    }

}























