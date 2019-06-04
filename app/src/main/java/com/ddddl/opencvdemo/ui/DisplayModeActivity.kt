package com.ddddl.opencvdemo.ui

import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_camera.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.InstallCallbackInterface
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.imgproc.Imgproc
import java.io.File
import android.content.Context
import com.ddddl.opencvdemo.R
import com.ddddl.opencvdemo.nativehelper.FaceHelper
import com.ddddl.opencvdemo.utils.FileUtil
import org.opencv.core.*
import org.opencv.objdetect.CascadeClassifier
import java.io.FileOutputStream
import org.opencv.core.CvType
import org.opencv.core.Mat


class DisplayModeActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2,
    View.OnClickListener {

    private var absoluteFaceSize: Double = 0.0
    private lateinit var grayscaleImage: Mat
    var option = 0
    private var cameraIndex = 0
    private var TAG = "camera"
    private val helper by lazy {
        FaceHelper()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_mode)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        cv_camera_id.visibility = View.VISIBLE
        cv_camera_id.setCvCameraViewListener(this)
        cv_camera_id.enableFpsMeter()
        camera_group.check(backCamera.id)
        backCamera.isSelected = true
        frontCamera.setOnClickListener(this)
        backCamera.setOnClickListener(this)

        cv_camera_id.setCameraIndex(cameraIndex)
        cv_camera_id.enableView()
        initFaceDetector()
    }

    private fun initFaceDetector() {
        val input = resources.openRawResource(R.raw.lbpcascade_frontalface)
        FileUtil.saveToSDCard(input, "sdcard/lbpcascade_frontalface.xml")
        helper.initLoad("sdcard/lbpcascade_frontalface.xml")
//        var cascadeClassifier = CascadeClassifier("sdcard/lbpcascade_frontalface.xml")
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.frontCamera -> {
                cameraIndex = 1
            }
            R.id.backCamera -> {
                cameraIndex = 0
            }
        }

        cv_camera_id.setCameraIndex(cameraIndex)
        if (cv_camera_id != null) {
            cv_camera_id.disableView()
        }
        cv_camera_id.enableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cv_camera_id != null) {
            cv_camera_id.disableView()
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        grayscaleImage = Mat(height, width, CvType.CV_8UC4)
        absoluteFaceSize = (height * 0.2)
    }

    override fun onCameraViewStopped() {

    }


    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        val frame = inputFrame!!.rgba()

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i("camera", "竖屏显示")
            Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE)
        }

        process(frame)

        return frame
    }

    private fun process(frame: Mat) {
        when (option) {
            1 -> {
                Core.bitwise_not(frame, frame)
            }
            2 -> {
                val edges = Mat()
                Imgproc.Canny(frame, edges, 100.0, 200.0, 3, false)
                val result = Mat.zeros(frame.size(), frame.type())
                frame.copyTo(result, edges)
                result.copyTo(frame)
                edges.release()
                result.release()
            }
            3 -> {
                val gradx = Mat()
                Imgproc.Sobel(frame, gradx, CvType.CV_32F, 1, 0)
                Core.convertScaleAbs(gradx, gradx)
                gradx.copyTo(frame)
                gradx.release()
            }
            4 -> {
                val temp = Mat()
                Imgproc.blur(frame, temp, Size(15.0, 15.0))
                temp.copyTo(frame)
                temp.release()
            }
            5 -> {
                helper.faceDetection(frame.nativeObjAddr)
            }
            else -> {

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.cv_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item!!.itemId) {
            R.id.invert -> {
                option = 1
            }
            R.id.edge -> {
                option = 2
            }
            R.id.sobel -> {
                option = 3
            }
            R.id.boxblur -> {
                option = 4
            }
            R.id.facceDetection -> {
                option = 5
            }
            else -> {
                option = 0
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
