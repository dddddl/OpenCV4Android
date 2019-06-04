package com.ddddl.opencvdemo.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.ddddl.opencvdemo.R
import com.ddddl.opencvdemo.nativehelper.FaceHelper
import com.ddddl.opencvdemo.utils.ImageSelectUtils
import kotlinx.android.synthetic.main.activity_inpaint.*
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File

class InpaintActivity : AppCompatActivity() {

    private val TAG = "FaceBeautyActivity"
    private val REQUEST_CAPTURE_IMAGE = 1
    private var fileUri: Uri? = null
    private val faceHelper = FaceHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inpaint)

        select_image_btn.setOnClickListener {
            pickUpImage()
        }

        btn_inpaint.setOnClickListener {
            fixPic()
        }
    }

    private fun fixPic() {
        val src = Imgcodecs.imread(fileUri!!.getPath())
        if (src.empty()) {
            return
        }
        val dst = Mat(src.size(), src.type())
        faceHelper.fixpic(src.nativeObjAddr, dst.nativeObjAddr)

        val bm = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888)
        val result = Mat()
        Imgproc.cvtColor(dst, result, Imgproc.COLOR_BGR2RGBA)
        Utils.matToBitmap(result, bm)

        runOnUiThread {
            iv.setImageBitmap(bm)
        }

        src.release()
        dst.release()
    }

    private fun pickUpImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "图像选择..."), REQUEST_CAPTURE_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val uri = data.data
                val f = File(ImageSelectUtils.getRealPath(uri, applicationContext))
                fileUri = Uri.fromFile(f)
            }
        }
        // display it
        displaySelectedImage()
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
        iv.setImageBitmap(bm)
    }
}
