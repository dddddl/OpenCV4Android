package com.ddddl.opencvdemo.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import com.ddddl.opencvdemo.R
import com.ddddl.opencvdemo.nativehelper.FaceHelper
import com.ddddl.opencvdemo.utils.ImageSelectUtils
import kotlinx.android.synthetic.main.activity_inpaint.*
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.util.*

class InpaintActivity : AppCompatActivity() {

    private var rectArray: IntArray = IntArray(4)
    private var gray: Int = 240
    private val TAG = "InpaintActivity"
    private val REQUEST_CAPTURE_IMAGE = 3
    private var fileUri: Uri? = null
    private val faceHelper = FaceHelper()
    private var mImageFile: File? = null
    private var mCurrentToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inpaint)

        select_image_btn.setOnClickListener {
            pickUpImage()
//            chooseImages()
        }

        btn_inpaint.setOnClickListener {
            if (fileUri == null) {
                return@setOnClickListener
            }
            fixPic()
        }

        btn_cut.setOnClickListener {
            if (cutview.visibility == View.VISIBLE) {
                btn_cut.text = "修复范围选择"
                val imgDrawable = (iv as ImageView).drawable
                val bitmap = (imgDrawable as BitmapDrawable).bitmap
                val cutArr = cutview.cutArr
                val displayRect = iv.displayRect
                rectArray = IntArray(4)
                rectArray[0] =
                    (bitmap.width * (cutArr[0] - displayRect.left) / (displayRect.right - displayRect.left)).toInt()
                rectArray[1] =
                    (bitmap.height * cutArr[1] / (displayRect.bottom - displayRect.top)).toInt()
                rectArray[2] =
                    (bitmap.width * (cutArr[2] - displayRect.left) / (displayRect.right - displayRect.left)).toInt()
                rectArray[3] = (bitmap.height * cutArr[3] / (displayRect.bottom - displayRect.top)).toInt()
                cutview.visibility = View.GONE

                val src = Imgcodecs.imread(fileUri!!.path)
                if (!src.empty()) {
                    faceHelper.drawRect(src.nativeObjAddr, rectArray)
                    val bm = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888)
                    val result = Mat()
                    Imgproc.cvtColor(src, result, Imgproc.COLOR_BGR2RGBA)
                    Utils.matToBitmap(result, bm)
                    runOnUiThread {
                        iv.setImageBitmap(bm)
                    }
                    src.release()
                    result.release()
                }
            } else {
                val rect = iv.displayRect
                val ivLayoutParams = iv.layoutParams as RelativeLayout.LayoutParams
                val layoutParams = cutview.layoutParams as RelativeLayout.LayoutParams

                layoutParams.topMargin = if (rect.top.toInt() >= 0) {
                    rect.top.toInt() + ivLayoutParams.topMargin
                } else {
                    ivLayoutParams.topMargin
                }
                cutview.layoutParams = layoutParams
                cutview.measuredWidth = (rect.right - rect.left).toInt()
                cutview.measuredHeight = if ((rect.bottom - rect.top).toInt() > iv.height) {
                    if (rect.top >= 0) {
                        cutview.setEdgeTop(Math.abs(rect.top.toInt()))
                    } else {
                        cutview.setEdgeTop(0)
                    }

                    if (Math.abs(rect.bottom.toInt()) > iv.height) {
                        cutview.setEdgeBottom(iv.height)
                    } else {
                        cutview.setEdgeBottom(Math.abs(rect.bottom.toInt()))
                    }

                    iv.height
                } else {
                    cutview.setEdgeTop(0)
                    cutview.setEdgeBottom((rect.bottom - rect.top).toInt())

                    (rect.bottom - rect.top).toInt()
                }
                cutview.visibility = View.VISIBLE
                btn_cut.text = "完成选择"
            }
        }

        iv.setOnPhotoTapListener { view, x, y ->
            val imgDrawable = (view as ImageView).drawable
            val bitmap = (imgDrawable as BitmapDrawable).bitmap

            val color = bitmap.getPixel((bitmap.width * x).toInt(), (bitmap.height * y).toInt())
            btn_color.setBackgroundColor(color)

            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            gray = (red * 30 + green * 59 + blue * 11) / 100

            showToast("$x / $y")
        }
    }

    private fun showToast(text: CharSequence) {
        if (mCurrentToast != null) {
            mCurrentToast!!.cancel()
        }

        mCurrentToast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        mCurrentToast!!.show()
    }

    private fun fixPic() {
        val src = Imgcodecs.imread(fileUri!!.path)
        if (src.empty()) {
            return
        }
        val dst = Mat(src.size(), src.type())

        faceHelper.PhotoFix(src.nativeObjAddr, dst.nativeObjAddr, gray, rectArray)

        val bm = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888)
        val result = Mat()
        Imgproc.cvtColor(dst, result, Imgproc.COLOR_BGR2RGBA)
        Utils.matToBitmap(result, bm)
        val image =
            File(Environment.getExternalStorageDirectory().toString() + "/Pictures/1.jpg")
        bm.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(image))
        fileUri = Uri.fromFile(image)
        runOnUiThread {
            iv.setImageBitmap(bm)
        }

        src.release()
        dst.release()
        result.release()
    }

    private fun pickUpImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "图像选择..."), REQUEST_CAPTURE_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CAPTURE_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        val uri = data.data
                        val f = File(ImageSelectUtils.getRealPath(uri, applicationContext))
                        fileUri = Uri.fromFile(f)
                    }
                    displaySelectedImage()
                }
            }
        }
    }

    private fun displaySelectedImage() {
        if (fileUri == null) return
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(fileUri!!.getPath(), options)
        val w = options.outWidth
        val h = options.outHeight
        var inSample = 1
        if (w > 1000 || h > 1000) {
            while (Math.max(w / inSample, h / inSample) > 1000) {
                inSample *= 2
            }
        }
        options.inJustDecodeBounds = false
        options.inSampleSize = inSample
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bm = BitmapFactory.decodeFile(fileUri!!.getPath(), options)
        iv.setImageURI(fileUri)
    }
}
