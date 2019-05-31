package com.ddddl.opencvdemo

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.RadioButton
import kotlinx.android.synthetic.main.activity_camera.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.InstallCallbackInterface
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import java.io.File

class CameraActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2,
    View.OnClickListener {


    private var cameraIndex = 0
    private var TAG = "camera"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        cv_camera_id.visibility = View.VISIBLE
        cv_camera_id.setCvCameraViewListener(this)
        camera_group.check(backCamera.id)
        backCamera.isSelected = true
        frontCamera.setOnClickListener(this)
        backCamera.setOnClickListener(this)
        takePicture.setOnClickListener {
            takePicture()
        }
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


    private fun takePicture() {
        val fileDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "myOcrImages")
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        val name = System.currentTimeMillis().toString() + "_ocr.jpg"
        val tempFile = File(fileDir.absoluteFile.toString() + File.separator, name)
        val fileName = tempFile.absolutePath
        Log.e(TAG, "take picture $fileName")
        cv_camera_id.takePicture(fileName)
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, baseLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    private val baseLoaderCallback: LoaderCallbackInterface = object : LoaderCallbackInterface {
        override fun onManagerConnected(status: Int) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                cv_camera_id.setCameraIndex(cameraIndex)
                cv_camera_id.enableView()
            }
        }

        override fun onPackageInstall(operation: Int, callback: InstallCallbackInterface?) {

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (cv_camera_id != null) {
            cv_camera_id.disableView()
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {

    }

    override fun onCameraViewStopped() {

    }


    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        val frame = inputFrame!!.rgba()

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i("camera", "竖屏显示")
            Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE)
        }
        return frame!!
    }
}
