package com.ddddl.opencvdemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("opencv_java3")
        }
    }

    private val CV_TAG = "CV_TAG"
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.kt109)
        iv.setImageBitmap(bitmap)

        initLoadOpenCV()

        iv.setOnClickListener {
            //            blurImage()
            cvtColor()
        }
    }

    private fun cvtColor() {
        val src = Mat()
        val dst = Mat()
        Utils.bitmapToMat(bitmap, src)
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGRA2GRAY)
        Utils.matToBitmap(dst, bitmap)
        iv.setImageBitmap(bitmap)
        src.release()
        dst.release()
    }

    private fun blurImage() {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        Imgproc.GaussianBlur(mat, mat, Size(15.0, 15.0), 0.0)
        Utils.matToBitmap(mat, bitmap)
        iv.setImageBitmap(bitmap)
        mat.release()
    }

    private fun initLoadOpenCV() {
        val success = OpenCVLoader.initDebug()
        Log.i(CV_TAG, "${success ?: false}")
    }

}
