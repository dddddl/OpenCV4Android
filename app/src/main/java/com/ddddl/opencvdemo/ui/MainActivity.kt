package com.ddddl.opencvdemo.ui

import android.app.Activity
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.OpenCVLoader
import org.opencv.imgcodecs.Imgcodecs
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import com.ddddl.opencvdemo.utils.FeatureMatchUtil
import com.ddddl.opencvdemo.utils.MatUtil
import com.ddddl.opencvdemo.R
import com.ddddl.opencvdemo.utils.RealPathFromUriUtils
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("opencv_java3")
            System.loadLibrary("detection_based_tracker")
            System.loadLibrary("haar_detect")
            System.loadLibrary("pic_fix")
        }
    }

    private val strs = arrayOf("utils", "camera", "display", "ocr","face","inpain")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
        listview.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strs) as ListAdapter?
        listview.setOnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> {
                    startActivity(Intent(this, UtilActivity::class.java))
                }
                1 -> {
                    startActivity(Intent(this, CameraActivity::class.java))
                }
                2 -> {
                    startActivity(Intent(this, DisplayModeActivity::class.java))
                }
                3 -> {
                    startActivity(Intent(this, OCRActivity::class.java))
                }
                4 -> {
                    startActivity(Intent(this, FaceBeautyActivity::class.java))
                }
                5 -> {
                    startActivity(Intent(this, InpaintActivity::class.java))
                }
            }
        }

    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA
                    ),
                    0x11
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0x11) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }


}
