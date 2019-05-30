package com.ddddl.opencvdemo

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
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("opencv_java3")
        }
    }

    private fun initLoadOpenCV() {
        val success = OpenCVLoader.initDebug()
        Log.i(CV_TAG, "${success ?: false}")
    }

    private val IMAGE_TYPE = "image/*"
    private val CV_TAG = "CV_TAG"
    private lateinit var bitmap: Bitmap
    var REQUEST_CODE = 0
    val IMAGE_REQUEST_CODE = 0x102
    val IMAGE_REQUEST_CODE_1 = 0x103
    var src1: Mat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initLoadOpenCV()

        btn_open.setOnClickListener {
            requestPermission(IMAGE_REQUEST_CODE)
        }
        btn_addweighted.setOnClickListener {
            requestPermission(IMAGE_REQUEST_CODE_1)
        }
    }

    private fun requestPermission(requestCode: Int) {
        REQUEST_CODE = requestCode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openAlbum(requestCode)
            } else {
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
        } else {
            openAlbum(requestCode)
        }
    }

    private fun openAlbum(requestCode: Int) {
        val intent = Intent()
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType(IMAGE_TYPE)
        if (Build.VERSION.SDK_INT < 19) {
            intent.action = Intent.ACTION_GET_CONTENT
        } else {
            intent.action = Intent.ACTION_OPEN_DOCUMENT
        }
        startActivityForResult(intent, requestCode)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_REQUEST_CODE) {
                val imageUri = data?.data
//                bitmap = MediaStore.Images.Media.getBitmap(
//                    contentResolver, imageUri
//                )
//                iv.setImageBitmap(bitmap)

                ImageProcess.matchTemplateDemo(RealPathFromUriUtils.getRealPathFromUri(this, imageUri)){
                    iv.setImageBitmap(it)
                }
            } else if (requestCode == IMAGE_REQUEST_CODE_1) {
                val imageUri = data?.data
                val src = Imgcodecs.imread(RealPathFromUriUtils.getRealPathFromUri(this, imageUri))
                if (src.empty()) {
                    Log.e(CV_TAG, "src is empty")
                    return
                }
                if (src1 != null) {
                    MatUtil.addWeightMat(src1!!, src!!, 0.7, 3.0) {
                        iv.setImageBitmap(it)
                    }
                }
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0x11) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e(CV_TAG, "PERMISSION_GRANTED")
                openAlbum(REQUEST_CODE)
            } else {
                Log.e(CV_TAG, "forbid")
            }
        }
    }
}
