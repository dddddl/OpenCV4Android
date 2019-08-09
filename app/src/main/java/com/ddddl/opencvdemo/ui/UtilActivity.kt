package com.ddddl.opencvdemo.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.ddddl.opencvdemo.R
import com.ddddl.opencvdemo.nativehelper.FaceHelper
import com.ddddl.opencvdemo.ui.baidu.BodyUtil
import com.ddddl.opencvdemo.utils.*
import kotlinx.android.synthetic.main.activity_util.*
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

class UtilActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private val IMAGE_TYPE = "image/*"
    private val CV_TAG = "CV_TAG"
    private lateinit var bitmap: Bitmap
    var REQUEST_CODE = 0
    val IMAGE_REQUEST_CODE = 0x102
    val IMAGE_REQUEST_CODE_1 = 0x103
    val IMAGE_REQUEST_CODE_2 = 0x104

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_util)

        btn_open.setOnClickListener {
            openAlbum(IMAGE_REQUEST_CODE)
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
                imageUri = data?.data
                bitmap = MediaStore.Images.Media.getBitmap(
                    contentResolver, imageUri
                )
                iv.setImageBitmap(bitmap)
            } else if (requestCode == IMAGE_REQUEST_CODE_1) {
                val imageUri = data?.data
                val src = Imgcodecs.imread(RealPathFromUriUtils.getRealPathFromUri(this, imageUri))
                if (src.empty()) {
                    Log.e(CV_TAG, "src is empty")
                    return
                }
                if (imageUri != null) {
                    val src1 = Imgcodecs.imread(RealPathFromUriUtils.getRealPathFromUri(this, imageUri))
                    if (src1.empty()) {
                        Log.e(CV_TAG, "src is empty")
                        return
                    }
                    MatUtil.addWeightMat(src1!!, src!!, 0.7, 3.0) {
                        loadBitmap(it)
                    }
                }
            } else if (requestCode == IMAGE_REQUEST_CODE_2) {
                val imageUri = data?.data
                val src = Imgcodecs.imread(RealPathFromUriUtils.getRealPathFromUri(this, imageUri))
                if (src.empty()) {
                    Log.e(CV_TAG, "src is empty")
                    return
                }
                if (imageUri != null) {
                    val src1 = Imgcodecs.imread(RealPathFromUriUtils.getRealPathFromUri(this, imageUri))
                    if (src1.empty()) {
                        Log.e(CV_TAG, "src is empty")
                        return
                    }
                    ImageProcess.matchTemplateDemo(src, src1) {
                        loadBitmap(it)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.util_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val src: Mat = Imgcodecs.imread(RealPathFromUriUtils.getRealPathFromUri(this, imageUri)) ?: return false
        when (item!!.itemId) {
            R.id.drawMat -> {
                MatUtil.drawMat {
                    val destbitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
                    Utils.matToBitmap(it, destbitmap)
                    iv.setImageBitmap(destbitmap)
                }
            }
            R.id.cvtColor -> {
                MatUtil.cvtColor(src) {
                    Utils.matToBitmap(it, bitmap)
                    iv.setImageBitmap(bitmap)
                }
            }
            R.id.blurImage -> {
                MatUtil.blurImage(src) {
                    loadBitmap(it)
                }
            }
            R.id.getMatByteByPixel -> {
                MatUtil.getMatByteByPixel(src) {
                    loadBitmap(it)
                }
            }
            R.id.getMatByteByRow -> {
                MatUtil.getMatByteByRow(src) {
                    loadBitmap(it)
                }
            }
            R.id.getAllMatByte -> {
                MatUtil.getAllMatByte(src) {
                    loadBitmap(it)
                }
            }
            R.id.getBinaryImage -> {
                MatUtil.getBinaryImage(src) {
                    val result = Mat()
                    val bitmap = Bitmap.createBitmap(it.cols(), it.rows(), Bitmap.Config.ARGB_8888)
                    Imgproc.cvtColor(it, result, Imgproc.COLOR_GRAY2RGBA)
                    Utils.matToBitmap(result, bitmap)
                    iv.setImageBitmap(bitmap)
                    result.release()
                }
            }
            R.id.addMat -> {
                MatUtil.addMat(src) {
                    loadBitmap(it)
                }
            }
            R.id.subtractMat -> {
                MatUtil.subtractMat(src) {
                    loadBitmap(it)
                }
            }
            R.id.multiplyMat -> {
                MatUtil.multiplyMat(src) {
                    loadBitmap(it)
                }
            }
            R.id.divideMat -> {
                MatUtil.divideMat(src) {
                    loadBitmap(it)
                }
            }
            R.id.addWeightMat -> {
                openAlbum(IMAGE_REQUEST_CODE_1)
            }
            R.id.bitwiseMat -> {
                MatUtil.bitwiseMat {
                    loadBitmap(it)
                }
            }
            R.id.blur -> {
                ImageProcess.blur(src) {
                    loadBitmap(it)
                }
            }
            R.id.gaussianBlur -> {
                ImageProcess.gaussianBlur(src) {
                    loadBitmap(it)
                }
            }
            R.id.medianBlur -> {
                ImageProcess.medianBlur(src) {
                    loadBitmap(it)
                }
            }
            R.id.dilate -> {
                ImageProcess.dilate(src) {
                    loadBitmap(it)
                }
            }
            R.id.erode -> {
                ImageProcess.erode(src) {
                    loadBitmap(it)
                }
            }
            R.id.pyrMeanShiftFiltering -> {
                ImageProcess.pyrMeanShiftFiltering(src) {
                    loadBitmap(it)
                }
            }
            R.id.bilateralFilter -> {
                ImageProcess.bilateralFilter(src) {
                    loadBitmap(it)
                }
            }
            R.id.edge -> {
                ImageProcess.edge(src) {
                    loadBitmap(it)
                }
            }
            R.id.houghLines -> {
                ImageProcess.houghLines(src) {
                    loadBitmap(it)
                }
            }
            R.id.houghCircle -> {
                ImageProcess.houghCircle(src) {
                    loadBitmap(it)
                }
            }
            R.id.findContours -> {
                ImageProcess.findContours(src) {
                    loadBitmap(it)
                }
            }
            R.id.displayHistogram -> {
                ImageProcess.displayHistogram(src) {
                    loadBitmap(it)
                }
            }
            R.id.multiply -> {
                FaceHelper().MultipleMagnifyGlass(
                    src.nativeObjAddr,
                    src.cols() / 4,
                    src.rows() / 4,
                    src.cols() / 2,
                    src.rows() / 2
                )
                loadBitmap(src)
            }
            R.id.magnifyGlass -> {
                FaceHelper().magnifyGlass(src.nativeObjAddr)
                loadBitmap(src)
            }
            R.id.compressGlass -> {
                FaceHelper().compressGlass(src.nativeObjAddr)
                loadBitmap(src)
            }
            R.id.matchTemplateDemo -> {
                openAlbum(IMAGE_REQUEST_CODE_2)
            }
            R.id.harrisCornerDemo -> {
                FeatureMatchUtil.harrisCornerDemo(src) {
                    loadBitmap(it)
                }
            }
            R.id.shiTomasicornerDemo -> {
                FeatureMatchUtil.shiTomasicornerDemo(src) {
                    loadBitmap(it)
                }
            }
//            R.id.bodySeq -> {
//                Thread {
//                    val pathName = ImageSelectUtils.getRealPath(imageUri, this)
//                    val bodyBean = BodyUtil.bodySeg(pathName)
//                    val bodyBitmap = BodyUtil.convert(bodyBean?.labelmap!!, bitmap.width, bitmap.height, bitmap)
//                    runOnUiThread {
//                        iv.setImageBitmap(bodyBitmap)
//                    }
//                }.start()
//            }

        }

        return super.onOptionsItemSelected(item)
    }

    private fun loadBitmap(it: Mat) {
        val result = Mat()
        val bitmap = Bitmap.createBitmap(it.cols(), it.rows(), Bitmap.Config.ARGB_8888)
        Imgproc.cvtColor(it, result, Imgproc.COLOR_BGRA2RGB)
        Utils.matToBitmap(result, bitmap)
        iv.setImageBitmap(bitmap)
        result.release()
    }

}
