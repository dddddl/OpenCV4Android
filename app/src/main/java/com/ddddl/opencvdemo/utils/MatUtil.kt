package com.ddddl.opencvdemo.utils

import android.graphics.Bitmap
import android.widget.ImageView
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

object MatUtil {

    fun drawMat(process: (Mat) -> Unit) {
        val src = Mat.zeros(500, 500, CvType.CV_8U)

        Imgproc.ellipse(
            src, Point(250.0, 250.0), Size(100.0, 50.0), 360.0, 0.0,
            360.0, Scalar(255.0, 0.0, 0.0), 2, 8, 0
        )

        Imgproc.putText(
            src, "Basic Drawing Demo", Point(20.0, 20.0),
            Core.FONT_HERSHEY_PLAIN, 1.0, Scalar(255.0, 0.0, 0.0), 2
        )

        val rect = Rect()
        rect.x = 50
        rect.y = 50
        rect.width = 100
        rect.height = 100

        Imgproc.line(
            src, Point(10.0, 10.0), Point(490.0, 490.0),
            Scalar(255.0, 0.0, 0.0), 2, 8, 0
        )
        Imgproc.line(
            src, Point(10.0, 490.0), Point(490.0, 10.0),
            Scalar(255.0, 0.0, 0.0), 2, 8, 0
        )
        Imgproc.rectangle(
            src, rect.tl(), rect.br(),
            Scalar(255.0, 0.0, 0.0), 2, 8, 0
        )
        Imgproc.circle(
            src, Point(400.0, 400.0), 50,
            Scalar(255.0, 0.0, 0.0), 2, 8, 0
        )

        process.invoke(src)
        src.release()
    }

    fun cvtColor(src: Mat, process: (Mat) -> Unit) {
        val dst = Mat()
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGRA2GRAY)
        process.invoke(dst)
        src.release()
        dst.release()
    }

    fun blurImage(src: Mat, process: (Mat) -> Unit) {
        Imgproc.GaussianBlur(src, src, Size(15.0, 15.0), 0.0)
        process.invoke(src)
        src.release()
    }

    //读取每个像素点并修改
    fun getMatByteByPixel(src: Mat, process: (Mat) -> Unit) {

        val channels = src.channels()
        val width = src.cols()
        val height = src.rows()

        val data = ByteArray(channels)
        var b = 0
        var g = 0
        var r = 0

        for (row in 0 until height) {
            for (col in 0 until width) {
                //读取
                src.get(row, col, data)
                b = data[0].toInt() and 0xff
                g = data[1].toInt() and 0xff
                r = data[2].toInt() and 0xff
                //修改
                b = 255 - b
                g = 255 - g
                r = 255 - r
                //写入
                data[0] = b.toByte()
                data[1] = g.toByte()
                data[2] = r.toByte()
                src.put(row, col, data)
            }
        }
        process.invoke(src)
        src.release()
    }

    //一次读取一行的像素点
    fun getMatByteByRow(src: Mat, process: (Mat) -> Unit) {
        val channels = src.channels()
        val width = src.cols()
        val height = src.rows()

        var data = ByteArray(channels * width)
        //loop
        var b = 0
        var g = 0
        var r = 0
        var pv = 0
        for (row in 0 until height) {
            src.get(row, 0, data)
            for (col in data.indices) {
                //读取
                pv = data[col].toInt() and 0xff
                //修改
                pv = 255 - pv
                data[col] = pv.toByte()
            }

            src.put(row, 0, data)
        }

        process.invoke(src)
        src.release()
    }

    //一次性读取所有像素点
    fun getAllMatByte(src: Mat, process: (Mat) -> Unit) {
        val channels = src.channels()
        val width = src.cols()
        val height = src.rows()
        var pv = 0
        var data = ByteArray(channels * width * height)
        src.get(0, 0, data)

        for (i in 0 until data.size) {
            pv = data[i].toInt() and 0xff
            pv = 255 - pv
            data[i] = pv.toByte()
        }
        src.put(0, 0, data)
        process.invoke(src)
        src.release()
    }

    //获取二值图像
    fun getBinaryImage(src: Mat, process: (Mat) -> Unit) {

        val gray = Mat()
        //转为灰度图像
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGRA2GRAY)

        //计算均值与标准方差
        val means = MatOfDouble()
        val stddevs = MatOfDouble()
        Core.meanStdDev(gray, means, stddevs)

        //显示均值与标准方差
        val mean = means.toArray()
        val stddev = stddevs.toArray()


        //读取像素数组
        val width = gray.cols()
        val height = gray.rows()
        val data = ByteArray(width * height)
        gray.get(0, 0, data)
        var pv = 0

        //根据均值进行二值分割
        val t = mean[0].toInt()
        for (i in data.indices) {
            pv = data[i].toInt() and 0xff
            if (pv > t) {
                data[i] = 255.toByte()
            } else {
                data[i] = 0.toByte()
            }
        }
        gray.put(0, 0, data)
        process.invoke(gray)
        src.release()
        gray.release()
    }

    //加法运算
    fun addMat(src: Mat, process: (Mat) -> Unit) {

        val moon = Mat.zeros(src.rows(), src.cols(), src.type())
        var cx = (src.cols() - 60).toDouble()
        var cy = 60.toDouble()
        Imgproc.circle(moon, Point(cx, cy), 50, Scalar(90.0, 95.0, 234.0), -1, 8, 0)

        val dst = Mat()
        Core.add(src, moon, dst)
        process.invoke(dst)
        moon.release()
        src.release()
        dst.release()
    }

    //减法运算
    fun subtractMat(src: Mat, process: (Mat) -> Unit) {

        val moon = Mat.zeros(src.rows(), src.cols(), src.type())
        var cx = (src.cols() - 60).toDouble()
        var cy = 60.toDouble()
        Imgproc.circle(moon, Point(cx, cy), 50, Scalar(90.0, 95.0, 234.0), -1, 8, 0)

        val dst = Mat()
        Core.subtract(src, moon, dst)
        process.invoke(dst)
        moon.release()
        src.release()
        dst.release()
    }

    //乘法运算
    fun multiplyMat(src: Mat, process: (Mat) -> Unit) {

        val moon = Mat.zeros(src.rows(), src.cols(), src.type())
        var cx = (src.cols() - 60).toDouble()
        var cy = 60.toDouble()
        Imgproc.circle(moon, Point(cx, cy), 50, Scalar(90.0, 95.0, 234.0), -1, 8, 0)

        val dst = Mat()
        Core.multiply(src, moon, dst)
        process.invoke(dst)
        moon.release()
        src.release()
        dst.release()
    }

    //除法运算
    fun divideMat(src: Mat, process: (Mat) -> Unit) {

        val moon = Mat.zeros(src.rows(), src.cols(), src.type())
        var cx = (src.cols() - 60).toDouble()
        var cy = 60.toDouble()
        Imgproc.circle(moon, Point(cx, cy), 50, Scalar(90.0, 95.0, 234.0), -1, 8, 0)

        val dst = Mat()
        Core.divide(src, moon, dst)
        process.invoke(dst)
        moon.release()
        src.release()
        dst.release()
    }

    fun addWeightMat(src: Mat, src1: Mat, alpha: Double, gamma: Double, process: (Mat) -> Unit) {

        val dst = Mat()
        val cropImage = Mat()
        Imgproc.resize(src1, cropImage, src.size())

        //像素混合 - 基于权重
        Core.addWeighted(src, alpha, cropImage, 1 - alpha, gamma, dst)
        process.invoke(dst)

        src1.release()
        dst.release()
        cropImage.release()
    }

    fun bitwiseMat(process: (Mat) -> Unit) {
        //创建图像
        val src1 = Mat.zeros(400, 400, CvType.CV_8UC3)
        val src2 = Mat(400, 400, CvType.CV_8UC3)
        src2.setTo(Scalar(255.0, 255.0, 255.0))

        // ROI区域定义
        val rect = Rect()
        rect.x = 100
        rect.y = 100
        rect.width = 200
        rect.height = 200

        Imgproc.rectangle(src1, rect.tl(), rect.br(), Scalar(0.0, 255.0, 0.0), -1)
        rect.x = 10
        rect.y = 10
        Imgproc.rectangle(src2, rect.tl(), rect.br(), Scalar(255.0, 255.0, 0.0), -1)

        //逻辑运算
        val dst1 = Mat()
        val dst2 = Mat()
        val dst3 = Mat()
        Core.bitwise_and(src1, src2, dst1)
        Core.bitwise_or(src1, src2, dst2)
        Core.bitwise_xor(src1, src2, dst3)

        //输出结果
        val dst = Mat.zeros(400, 1200, CvType.CV_8UC3)
        rect.x = 0
        rect.y = 0
        rect.width = 400
        rect.height = 400
        dst1.copyTo(dst.submat(rect))
        rect.x = 400
        dst2.copyTo(dst.submat(rect))
        rect.x = 800
        dst3.copyTo(dst.submat(rect))

        dst1.release()
        dst2.release()
        dst3.release()
        process.invoke(dst)
    }

}



















