package com.ddddl.opencvdemo.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ddddl.opencvdemo.R
import com.ddddl.opencvdemo.utils.FeatureMatchUtil
import com.ddddl.opencvdemo.utils.MatUtil
import com.ddddl.opencvdemo.utils.RealPathFromUriUtils
import kotlinx.android.synthetic.main.activity_util.*
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

class UtilActivity : AppCompatActivity() {

    private val IMAGE_TYPE = "image/*"
    private val CV_TAG = "CV_TAG"
    private lateinit var bitmap: Bitmap
    var REQUEST_CODE = 0
    val IMAGE_REQUEST_CODE = 0x102
    val IMAGE_REQUEST_CODE_1 = 0x103
    var src1: Mat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_util)

        btn_open.setOnClickListener {
            openAlbum(IMAGE_REQUEST_CODE)
        }
        btn_addweighted.setOnClickListener {
            openAlbum(IMAGE_REQUEST_CODE_1)
        }
    }

    private fun openAlbum(requestCode: Int) {
        REQUEST_CODE = requestCode
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

                val src = Imgcodecs.imread(RealPathFromUriUtils.getRealPathFromUri(this, imageUri))

                FeatureMatchUtil.shiTomasicornerDemo(src) {
                    val result = Mat()
                    val bitmap = Bitmap.createBitmap(it.cols(), it.rows(), Bitmap.Config.ARGB_8888)
                    Imgproc.cvtColor(it, result, Imgproc.COLOR_BGRA2GRAY)
                    Utils.matToBitmap(result, bitmap)
                    iv.setImageBitmap(bitmap)
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



}
