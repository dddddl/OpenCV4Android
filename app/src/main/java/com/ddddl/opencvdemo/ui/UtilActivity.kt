package com.ddddl.opencvdemo.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import com.ddddl.opencvdemo.R
import com.ddddl.opencvdemo.nativehelper.FaceHelper
import com.ddddl.opencvdemo.utils.*
import kotlinx.android.synthetic.main.activity_inpaint.*
import kotlinx.android.synthetic.main.activity_util.*
import kotlinx.android.synthetic.main.activity_util.iv
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream

class UtilActivity : AppCompatActivity() {

    private var srccc: Mat? = null
    private var imageUri: Uri? = null
    private val IMAGE_TYPE = "image/*"
    private val CV_TAG = "CV_TAG"
    private lateinit var bitmap: Bitmap
    var REQUEST_CODE = 0
    val IMAGE_REQUEST_CODE = 0x102
    val IMAGE_REQUEST_CODE_1 = 0x103
    val IMAGE_REQUEST_CODE_2 = 0x104

    var startX: Float = 0f;
    var startY: Float = 0f;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_util)

//        iv.setOnTouchListener { v, event ->
//
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    startX = event.x
//                    startY = event.y
//                }
//                MotionEvent.ACTION_UP -> {
//
//                    FaceHelper().MultipleMagnifyGlass(
//                        srccc!!.nativeObjAddr,
//                        startX.toInt(),
//                        startY.toInt(),
//                        event.x.toInt(),
//                        event.y.toInt()
//                    )
//                    loadBitmap(srccc!!)
//                }
//            }
//
//            return@setOnTouchListener true
//        }

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
                srccc = Imgcodecs.imread(
                    RealPathFromUriUtils.getRealPathFromUri(this, imageUri)
                )

            } else if (requestCode == IMAGE_REQUEST_CODE_1) {
                val imageUri = data?.data
                val src = Imgcodecs.imread(RealPathFromUriUtils.getRealPathFromUri(this, imageUri))
                if (src.empty()) {
                    Log.e(CV_TAG, "src is empty")
                    return
                }
                if (imageUri != null) {
                    val src1 =
                        Imgcodecs.imread(RealPathFromUriUtils.getRealPathFromUri(this, imageUri))
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
                    val src1 =
                        Imgcodecs.imread(RealPathFromUriUtils.getRealPathFromUri(this, imageUri))
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

        when (item!!.itemId) {
            R.id.drawMat -> {
                MatUtil.drawMat {
                    val destbitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
                    Utils.matToBitmap(it, destbitmap)
                    iv.setImageBitmap(destbitmap)
                }
            }
            R.id.cvtColor -> {
                MatUtil.cvtColor(srccc!!) {
                    Utils.matToBitmap(it, bitmap)
                    iv.setImageBitmap(bitmap)
                }
            }
            R.id.blurImage -> {
                MatUtil.blurImage(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.getMatByteByPixel -> {
                MatUtil.getMatByteByPixel(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.getMatByteByRow -> {
                MatUtil.getMatByteByRow(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.getAllMatByte -> {
                MatUtil.getAllMatByte(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.getBinaryImage -> {
                MatUtil.getBinaryImage(srccc!!) {
                    val result = Mat()
                    val bitmap = Bitmap.createBitmap(it.cols(), it.rows(), Bitmap.Config.ARGB_8888)
                    Imgproc.cvtColor(it, result, Imgproc.COLOR_GRAY2RGBA)
                    Utils.matToBitmap(result, bitmap)
                    iv.setImageBitmap(bitmap)
                    result.release()
                }
            }
            R.id.addMat -> {
                MatUtil.addMat(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.subtractMat -> {
                MatUtil.subtractMat(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.multiplyMat -> {
                MatUtil.multiplyMat(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.divideMat -> {
                MatUtil.divideMat(srccc!!) {
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
                ImageProcess.blur(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.gaussianBlur -> {
                ImageProcess.gaussianBlur(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.medianBlur -> {
                ImageProcess.medianBlur(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.dilate -> {
                ImageProcess.dilate(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.erode -> {
                ImageProcess.erode(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.pyrMeanShiftFiltering -> {
                ImageProcess.pyrMeanShiftFiltering(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.bilateralFilter -> {
                ImageProcess.bilateralFilter(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.guidedFilter -> {
//                val dst = Mat(srccc?.size(), srccc?.type()!!)
//                FaceHelper().guidedFilter(srccc?.nativeObjAddr!!, dst.nativeObjAddr)
//                val bm = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888)
//                val result = Mat()
//                Imgproc.cvtColor(dst, result, Imgproc.COLOR_GRAY2RGB)
//                Utils.matToBitmap(result, bm)
//                runOnUiThread {
//                    iv.setImageBitmap(bm)
//                }
//                srccc?.release()
//                dst.release()
//                result.release()
                ImageProcess.guidedFilter(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.nlmFilter -> {
                ImageProcess.nlmFilter(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.bm3dFilter -> {
                val dst = Mat(srccc?.size(), CvType.CV_8U)
                FaceHelper().bm3dFilter(srccc?.nativeObjAddr!!, dst.nativeObjAddr)
                val result = Mat()
                val bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888)
                Imgproc.cvtColor(dst, result, Imgproc.COLOR_GRAY2RGB)
                Utils.matToBitmap(result, bitmap)
                iv.setImageBitmap(bitmap)
                result.release()
            }
            R.id.Fourier_Transform -> {
//                ImageProcess.dftFilter(srccc!!) {
//                    val bitmap = Bitmap.createBitmap(it.cols(), it.rows(), Bitmap.Config.ARGB_8888)
//                    Imgproc.cvtColor(it, it, Imgproc.COLOR_GRAY2RGB)
//                    Utils.matToBitmap(it, bitmap)
//                    iv.setImageBitmap(bitmap)
//                }
                Imgproc.cvtColor(srccc,srccc,Imgproc.COLOR_BGRA2GRAY)
                val dst = Mat.zeros(srccc?.size(), srccc?.type()!!)
                FaceHelper().homoFilter(srccc?.nativeObjAddr!!, dst?.nativeObjAddr!!)
                val result = Mat()
                val bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888)
                Imgproc.cvtColor(dst, result, Imgproc.COLOR_GRAY2RGB)
                Utils.matToBitmap(result, bitmap)
                iv.setImageBitmap(bitmap)
                result.release()

            }
            R.id.edge -> {
                ImageProcess.edge(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.houghLines -> {
                ImageProcess.houghLines(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.houghCircle -> {
                ImageProcess.houghCircle(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.findContours -> {
                ImageProcess.findContours(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.displayHistogram -> {
                ImageProcess.displayHistogram(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.multiply -> {
                FaceHelper().MultipleMagnifyGlass(
                    srccc!!.nativeObjAddr,
                    srccc!!.cols() / 4,
                    srccc!!.rows() / 4,
                    srccc!!.cols() / 2,
                    srccc!!.rows() / 2
                )
                loadBitmap(srccc!!)
            }
            R.id.magnifyGlass -> {
                FaceHelper().magnifyGlass(srccc!!.nativeObjAddr)
                loadBitmap(srccc!!)
            }
            R.id.compressGlass -> {
                FaceHelper().compressGlass(srccc!!.nativeObjAddr)
                loadBitmap(srccc!!)
            }
            R.id.matchTemplateDemo -> {
                openAlbum(IMAGE_REQUEST_CODE_2)
            }
            R.id.harrisCornerDemo -> {
                FeatureMatchUtil.harrisCornerDemo(srccc!!) {
                    loadBitmap(it)
                }
            }
            R.id.shiTomasicornerDemo -> {
                FeatureMatchUtil.shiTomasicornerDemo(srccc!!) {
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
        Imgproc.cvtColor(it, result, Imgproc.COLOR_BGR2RGB)
        Utils.matToBitmap(result, bitmap)
        iv.setImageBitmap(bitmap)
        result.release()
    }

}
