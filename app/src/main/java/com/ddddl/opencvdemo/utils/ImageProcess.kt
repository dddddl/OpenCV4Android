package com.ddddl.opencvdemo.utils

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.core.Scalar
import org.opencv.core.Core
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.core.Mat
import org.opencv.core.CvType
import org.opencv.photo.Photo


object ImageProcess {

    fun blur(src: Mat, process: (Mat) -> Unit) {
        val dst = Mat()
        Imgproc.blur(src, dst, Size(5.0, 5.0), Point(-1.0, -1.0), Core.BORDER_DEFAULT)

        process.invoke(dst)
        src.release()
        dst.release()
    }

    fun gaussianBlur(src: Mat, process: (Mat) -> Unit) {
        val dst = Mat()
        Imgproc.GaussianBlur(src, dst, Size(3.0, 3.0), 3.0, 3.0)

        process.invoke(dst)
        src.release()
        dst.release()
    }

    fun medianBlur(src: Mat, process: (Mat) -> Unit) {
        val dst = Mat()
        //ksize 为3、5的时候输入图像可以为浮点数或者整型，大于5只能为字节型图像，CV_8UC
        Imgproc.medianBlur(src, dst, 5)
        process.invoke(dst)
        src.release()
        dst.release()
    }


    fun dilate(src: Mat, process: (Mat) -> Unit) {
        val dst = Mat()
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(5.0, 5.0))
        Imgproc.dilate(src, dst, kernel) //膨胀 最大替换中心像素
        Imgproc.dilate(dst, dst, kernel) //膨胀 最大替换中心像素
        Imgproc.dilate(dst, dst, kernel) //膨胀 最大替换中心像素
        process.invoke(dst)
        src.release()
        dst.release()
    }

    fun erode(src: Mat, process: (Mat) -> Unit) {
        val dst = Mat()
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(5.0, 5.0))
        Imgproc.erode(src, dst, kernel) //腐蚀 大小替换中心像素
        process.invoke(dst)
        src.release()
        dst.release()
    }

    fun erode(src: Mat, size: Double, process: (Mat) -> Unit) {
        val dst = Mat()
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(size, size))
        Imgproc.erode(src, dst, kernel) //腐蚀 大小替换中心像素
        process.invoke(dst)
        src.release()
        dst.release()
    }

    fun pyrMeanShiftFiltering(src: Mat, process: (Mat) -> Unit) {
        val dst = Mat()
        Imgproc.pyrMeanShiftFiltering(src, dst, 10.0, 50.0)
        process.invoke(dst)
        src.release()
        dst.release()
    }

    fun bilateralFilter(src: Mat, process: (Mat) -> Unit) {
        val dst = Mat()
        Imgproc.bilateralFilter(src, dst, 0, 150.0, 15.0)
        process.invoke(dst)
        src.release()
        dst.release()
    }

    fun edge(src: Mat, process: (Mat) -> Unit) {
        val dst = Mat()
        val gradx = Mat()
        Imgproc.Sobel(src, gradx, CvType.CV_16S, 1, 0)

        val grady = Mat()
        Imgproc.Sobel(src, grady, CvType.CV_16S, 0, 1)

        val edges = Mat()
        Imgproc.Canny(gradx, grady, edges, 50.0, 150.0)
        Core.bitwise_and(src, src, dst, edges)
        process.invoke(dst)

        src.release()
        dst.release()
        edges.release()
        gradx.release()
        grady.release()
    }

    fun houghLines(src: Mat, process: (Mat) -> Unit) {
        val edges = Mat()
        Imgproc.Canny(src, edges, 50.0, 150.0, 3, true)

        val lines = Mat()
        Imgproc.HoughLines(edges, lines, 1.0, Math.PI / 180.0, 200)

        val out = Mat.zeros(src.size(), src.type())
        val data = FloatArray(2)

        for (i in 0 until lines.rows()) {
            lines.get(i, 0, data)

            val rho = data[0].toDouble()
            val theta = data[1].toDouble()

            val a = Math.cos(theta)
            val b = Math.sin(theta)

            val x0 = a * rho
            val y0 = b * rho

            val pt1 = Point()
            val pt2 = Point()
            pt1.x = Math.round(x0 + 1000 * (-b)).toDouble()
            pt1.y = Math.round(y0 + 1000 * (a)).toDouble()
            pt2.x = Math.round(x0 - 1000 * (-b)).toDouble()
            pt2.y = Math.round(y0 - 1000 * (a)).toDouble()
            Imgproc.line(out, pt1, pt2, Scalar(0.0, 0.0, 255.0), 3, Imgproc.LINE_AA, 0)
        }
        process.invoke(out)

        src.release()
        out.release()
        edges.release()
    }

    fun houghCircle(src: Mat, process: (Mat) -> Unit) {
        val gray = Mat()
        Imgproc.pyrMeanShiftFiltering(src, gray, 15.0, 80.0)
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGRA2GRAY)

        Imgproc.GaussianBlur(gray, gray, Size(3.0, 3.0), 0.0)

        val circles = Mat()
        val dst = Mat()

        dst.create(src.size(), src.type())
        Imgproc.HoughCircles(
            gray, circles, Imgproc.HOUGH_GRADIENT, 1.0, 20.0,
            100.0, 30.0, 10, 200
        )

        for (i in 0 until circles.cols()) {
            val info = FloatArray(3)
            circles.get(0, i, info)
            Imgproc.circle(
                dst,
                Point(info[0].toDouble(), info[1].toDouble()),
                info[2].toInt(),
                Scalar(0.0, 255.0, 0.0),
                2,
                8,
                0
            )
        }

        circles.release()
        gray.release()
        process.invoke(dst)

        src.release()
        dst.release()
    }

    fun inPaint(original: Mat, mask: Mat, process: (Mat) -> Unit) {

        Imgproc.cvtColor(original, original, Imgproc.COLOR_BGRA2BGR)

        val gray = Mat()
        Imgproc.cvtColor(mask, gray, Imgproc.COLOR_RGBA2GRAY)

        dilate(gray) { dilate ->
            val dst = Mat(original.size(), original.type())
            Photo.inpaint(original, dilate, dst, 5.0, Photo.INPAINT_TELEA)
            process.invoke(dst)
            dst.release()
            dilate.release()
        }
        original.release()
        mask.release()
        gray.release()
    }

    fun getBackground(original: Mat, overlayMat: Mat): Mat {
        val gray = Mat()
        Imgproc.cvtColor(overlayMat, gray, Imgproc.COLOR_RGBA2GRAY)
        val background = Mat(original.size(), CvType.CV_8UC3, Scalar(255.0, 255.0, 255.0))

        erode(gray, 10.0) { erode ->
            Imgproc.cvtColor(original, original, Imgproc.COLOR_RGBA2BGR)
            original.copyTo(background, erode)
            erode.release()
        }
        overlayMat.release()
        original.release()
        gray.release()
        return background
    }

    fun getForeground(original: Mat, overlayMat: Mat): Pair<Mat, Rect> {
        val gray = Mat()
        Imgproc.cvtColor(overlayMat, gray, Imgproc.COLOR_RGBA2GRAY)
        var foregroundMat = Mat()
        var rect = Rect()
        erode(gray) {
            gaussianBlur(it) { blurMat ->
                val foreground = Mat(original.size(), CvType.CV_8UC4, Scalar(0.0, 0.0, 0.0, 0.0))
                Imgproc.cvtColor(original, original, Imgproc.COLOR_RGBA2BGRA)
                original.copyTo(foreground, blurMat)

                val pair = findContours(blurMat)
                val contours = pair.first
                val j = pair.second
                rect = Imgproc.boundingRect(contours[j])
                foregroundMat = foreground.submat(rect)
                contours.clear()
                original.release()
                blurMat.release()
                it.release()
            }
            overlayMat.release()
            gray.release()
        }
        return Pair(foregroundMat, rect)
    }

    private fun findContours(
        grayMat: Mat,
        orginal: Mat? = null,
        draw: Boolean = false
    ): Pair<ArrayList<MatOfPoint>, Int> {
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()

        Imgproc.findContours(
            grayMat,
            contours,
            hierarchy,
            Imgproc.RETR_TREE,
            Imgproc.CHAIN_APPROX_SIMPLE,
            Point(0.0, 0.0)
        )

        var temp: Double = 0.0
        var j = 0
        for (i in contours.indices) {
            val area = Imgproc.contourArea(contours[i])
            if (area > temp) {
                temp = area
                j = i
            }
            if (draw) {
                Imgproc.drawContours(orginal, contours, i, Scalar(255.0), 1)
            }
        }
        hierarchy.release()
        return Pair(contours, j)
    }

    fun findContours(src: Mat, process: (Mat) -> Unit) {
        val gray = Mat()
        val binary = Mat()

        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGRA2GRAY)
        Imgproc.threshold(gray, binary, 0.0, 255.0, Imgproc.THRESH_BINARY or Imgproc.THRESH_OTSU)

        val contours = ArrayList<MatOfPoint>()

        val hierarchy = Mat()
        Imgproc.findContours(
            binary,
            contours,
            hierarchy,
            Imgproc.RETR_TREE,
            Imgproc.CHAIN_APPROX_SIMPLE,
            Point(0.0, 0.0)
        )

        val dst = Mat.zeros(src.size(), src.type())

        for (i in contours.indices) {
            Imgproc.drawContours(dst, contours, i, Scalar(0.0, 0.0, 255.0), 2)
        }
        process.invoke(dst)
        src.release()
        dst.release()
        gray.release()
        binary.release()
    }

    fun displayHistogram(src: Mat, process: (Mat) -> Unit) {
        val dst = Mat()
        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

        // 计算直方图数据并归一化
        val images = ArrayList<Mat>()
        images.add(gray)
        val mask = Mat.ones(src.size(), CvType.CV_8UC1)
        val hist = Mat()
        Imgproc.calcHist(images, MatOfInt(0), mask, hist, MatOfInt(256), MatOfFloat(0F, 255F))
        Core.normalize(hist, hist, 0.0, 255.0, Core.NORM_MINMAX)
        val height = hist.rows()

        dst.create(400, 400, src.type())
        dst.setTo(Scalar(200.0, 200.0, 200.0))
        val histdata = FloatArray(256)
        hist.get(0, 0, histdata)
        val offsetx = 50.toDouble()
        val offsety = 350.toDouble()

        // 绘制直方图
        Imgproc.line(dst, Point(offsetx, 0.0), Point(offsetx, offsety), Scalar(0.0, 0.0, 0.0))
        Imgproc.line(dst, Point(offsetx, offsety), Point(400.0, offsety), Scalar(0.0, 0.0, 0.0))
        for (i in 0 until height - 1) {
            val y1 = histdata[i].toInt()
            val y2 = histdata[i + 1].toInt()
            val rect = Rect()
            rect.x = (offsetx + i).toInt()
            rect.y = (offsety - y1).toInt()
            rect.width = 1
            rect.height = y1
            Imgproc.rectangle(dst, rect.tl(), rect.br(), Scalar(15.0, 15.0, 15.0))
        }
        process.invoke(dst)
        // 释放内存
        src.release()
        gray.release()
        hist.release()
    }

    fun matchTemplateDemo(src: Mat, tpl: Mat, process: (Mat) -> Unit) {
        val dst = Mat()
        val height = src.rows() - tpl.rows() + 1
        val width = src.cols() - tpl.cols() + 1
        val result = Mat(height, width, CvType.CV_32FC1)

        val method = Imgproc.TM_CCOEFF_NORMED
        Imgproc.matchTemplate(src, tpl, result, method)
        val minMaxResult = Core.minMaxLoc(result)
        val maxloc = minMaxResult.maxLoc
        val minloc = minMaxResult.minLoc

        var matchloc: Point? = null

        matchloc = if (method == Imgproc.TM_SQDIFF || method == Imgproc.TM_SQDIFF_NORMED) {
            minloc
        } else {
            maxloc
        }

        //绘制
        src.copyTo(dst)
        Imgproc.rectangle(
            dst,
            matchloc,
            Point(matchloc.x + tpl.cols(), matchloc.y + tpl.rows()),
            Scalar(0.0, 0.0, 255.0),
            2,
            8,
            0
        )

        tpl.release()
        result.release()

        process.invoke(dst)
        src.release()
        dst.release()
    }

    fun hahaMirror(src: Mat, type: Int, process: (Mat) -> Unit) {
        val channels = src.channels()
        val width = src.cols()
        val height = src.rows()

        val colsByte = ByteArray(channels * width)
        val dataByte = ByteArray(channels)
        var b = 0
        var g = 0
        var r = 0

        val centerX = width / 2
        val centerY = height / 2
        val R = Math.sqrt((width * width + height * height).toDouble()) / 2 //直接关系到放大的力度,与R成正比;

        for (row in 0 until height) {
            src.get(row, 0, colsByte)
            for (col in 0 until width) {

                val tX = col - centerX
                val tY = row - centerY
                val distance = Math.sqrt((tX * tX + tY * tY).toDouble())

                if (distance < R) {

                    val newX = ((row - centerX) * distance / R).toInt() + centerX
                    val newY = ((col - centerY) * distance / R).toInt() + centerY

                    src.get(newX * channels, newY, dataByte)

                    b = dataByte[0].toInt() and 0xff
                    g = dataByte[1].toInt() and 0xff
                    r = dataByte[2].toInt() and 0xff

                    colsByte[channels * col + 0] = b.toByte()
                    colsByte[channels * col + 1] = g.toByte()
                    colsByte[channels * col + 2] = r.toByte()
                }
            }
            src.put(row, 0, colsByte)

        }

        process.invoke(src)
        src.release()
    }

}