// Copyright 2019 The MACE Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.ddddl.opencvdemo.mace

import android.app.AlertDialog
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.ddddl.opencvdemo.R
import com.ddddl.opencvdemo.mace.common.Configuration
import com.ddddl.opencvdemo.mace.common.MessageEvent
import com.ddddl.opencvdemo.mace.segmentation.MaskProcessor
import com.ddddl.opencvdemo.mace.segmentation.ModelSelector
import com.ddddl.opencvdemo.mace.segmentation.Segmenter
import com.ddddl.opencvdemo.utils.ImageProcess
import kotlinx.android.synthetic.main.activity_segment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc

import java.io.File
import java.io.IOException
import java.util.Arrays
import java.util.Calendar
import java.util.concurrent.locks.ReentrantLock

class SegmentActivity : AppCompatActivity() {
    private lateinit var inpaintMat: Mat
    private val GALLERY = 1
    private val CAMERA = 2

    private var btn: Button? = null
    private var infoTextView: TextView? = null
    private var inProgressDialog: AlertDialog? = null

    private var runThread: Handler? = null
    private var initFlag = 0 // -1 not available, 0 not ready, 1 ready.
    private val lock = ReentrantLock()

    private var storagePath: String? = null
    private var photoPicPath: String? = null
    private var inputSize: Int = 0
    private var inputImage: Bitmap? = null
    private var segmentation: Segmenter.Segmentation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_segment)

        btn = findViewById<View>(R.id.btn) as Button
        infoTextView = findViewById<View>(R.id.info_text) as TextView

        val thread = HandlerThread("initThread")
        thread.start()
        runThread = Handler(thread.looper)

        btn!!.setOnClickListener { showPictureDialog() }
        image_view!!.setOnClickListener { xiufu() }

        inProgressDialog = AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
            .setTitle("Running...")
            .setView(R.layout.progress_bar)
            .create()

        storagePath = (Environment.getExternalStorageDirectory().absolutePath
                + File.separator + STORAGE_DIRECTORY)
        val file = File(storagePath)
        if (!file.exists()) {
            file.mkdirs()
        }
        initMace()
    }

    private fun xiufu() {

    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun scaleRatio(inHeight: Int, inWidth: Int, outHeight: Int, outWidth: Int): Float {
        val ratio: Float
        if (outHeight > inHeight || outWidth > inWidth) {
            val heightRatio = outHeight.toFloat() / inHeight.toFloat()
            val widthRatio = outWidth.toFloat() / inWidth.toFloat()
            ratio = Math.min(heightRatio, widthRatio)
        } else {
            val heightRatio = inHeight.toFloat() / outHeight.toFloat()
            val widthRatio = inWidth.toFloat() / outWidth.toFloat()
            ratio = Math.max(heightRatio, widthRatio)
        }
        return ratio
    }


    private fun initMace() {
        runThread!!.post {
            lock.lock()
            // select proper model
            val modelSelector = ModelSelector()
            val modelConfig = modelSelector.select()
            if (modelConfig == null) {
                Log.e("Segmentation", "Initialize failed: there is no proper model to use")
                initFlag = -1
            } else {
                try {
                    val maskProcessor = MaskProcessor(
                        assets,
                        PASCAL_LABELS_FILE
                    )
                    val config = Configuration(
                        STORAGE_DIRECTORY,
                        modelConfig, maskProcessor
                    )
                    // Init MaceEngine
                    val status = Segmenter.instance.initialize(assets, config)
                    if (status != 0) {
                        Log.e("Segmentation", "Initialize failed: create engine failed")
                        initFlag = -1
                    } else {
                        Log.i("Segmentation", "Initialize successful")
                        initFlag = 1
                    }
                    inputSize = modelConfig.getModelInputShape(0)[1]
                } catch (e: IOException) {
                    Log.e("Initialization", e.message)
                    initFlag = -1
                }

            }
            lock.unlock()
        }
    }

    private fun loadBitmap(it: Mat): Bitmap {
        val result = Mat()
        val bitmap = Bitmap.createBitmap(it.cols(), it.rows(), Bitmap.Config.ARGB_8888)
        Imgproc.cvtColor(it, result, Imgproc.COLOR_BGR2RGBA)
        Utils.matToBitmap(result, bitmap)
        result.release()
        it.release()
        //        Bitmap bitmap1 = FeatherUtil.render(bitmap);
        //        bitmap.recycle();
        return bitmap
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeEvent(result: MessageEvent.BgChangeEvent?) {
        val bgMat = Mat()
        Utils.bitmapToMat(image_view.bgBitmap, bgMat)
        ImageProcess.inPaint(bgMat, inpaintMat) {
            image_view.inpaintBgBitmap(loadBitmap(it))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSegEvent(result: MessageEvent.SegEvent?) {
        if (result != null && inputImage != null) {
            val foreground = initForeground()
            val background = initBackground()
            image_view.addImage(loadBitmap(foreground.first), foreground.second, background)
            inputImage!!.recycle()

            // show information
            val config = Segmenter.instance.config
            var runTimeInfo = "<html><body>"
            if (initFlag == -1) {
                runTimeInfo += "<b>There is no proper model for the phone because of " + "it's weak computing capability.<b>"
            } else if (segmentation == null || !segmentation!!.isSuccessful) {
                runTimeInfo += "<b>Segmentation failed.<b>"
            } else {
                runTimeInfo += "Model name: <b>" + config.modelConfig.modelName + "</b><br>"
                runTimeInfo += "Device: <b>" + config.modelConfig.deviceType + "</b><br>"
                runTimeInfo += "Input size: <b>" + Integer.toString(inputSize) + "</b><br>"
                runTimeInfo += ("Preprocess time: <b>" + java.lang.Long.toString(segmentation!!.preProcessTime)
                        + "ms</b><br>")
                runTimeInfo += ("Run time: <b>" + java.lang.Long.toString(segmentation!!.inferenceTime)
                        + "ms</b><br>")
                runTimeInfo += ("Postprocess time: <b>" + java.lang.Long.toString(segmentation!!.postProcessTime)
                        + "ms</b><br>")
                runTimeInfo += "labels: <b>" + Arrays.toString(segmentation!!.labels) + "</b><br>"
            }
            runTimeInfo += "</body></html>"
            infoTextView!!.text = Html.fromHtml(runTimeInfo)

            if (inProgressDialog!!.isShowing) {
                inProgressDialog!!.dismiss()
            }
        }
    }

    private fun initBackground(): Bitmap {
        val matrix = Matrix()
        val originalBitmap = inputImage!!.copy(inputImage!!.config, true)
        val segBitmap = Bitmap.createBitmap(inputImage!!.width, inputImage!!.height, Bitmap.Config.ARGB_8888)
        val inpaintBitmap = Bitmap.createBitmap(inputImage!!.width, inputImage!!.height, Bitmap.Config.ARGB_8888)
        if (segmentation != null && segmentation!!.isSuccessful) {
            val ratio = scaleRatio(
                segmentation!!.backgroundBitmap.height,
                segmentation!!.backgroundBitmap.width,
                inputImage!!.height,
                inputImage!!.width
            )
            val canvas = Canvas(segBitmap)
            matrix.postScale(ratio, ratio)

            canvas.drawBitmap(
                segmentation!!.backgroundBitmap, matrix,
                Paint(Paint.FILTER_BITMAP_FLAG)
            )

            val inpaintCanvas = Canvas(inpaintBitmap)
            inpaintCanvas.drawBitmap(
                segmentation!!.segBitmap, matrix,
                Paint(Paint.FILTER_BITMAP_FLAG)
            )
        } else {
            Log.i("Segmentation", "segmentation failed")
        }

        val segMat = Mat()
        Utils.bitmapToMat(segBitmap, segMat)
        val original = Mat()
        Utils.bitmapToMat(originalBitmap, original)

        inpaintMat = Mat()
        Utils.bitmapToMat(inpaintBitmap, inpaintMat)
        segBitmap.recycle()
        originalBitmap.recycle()
        inpaintBitmap.recycle()
        val dst = ImageProcess.getBackground(original, segMat)
        return loadBitmap(dst)
    }

    private fun initForeground(): Pair<Mat, Rect> {
        val matrix = Matrix()
        val originalBitmap = inputImage!!.copy(inputImage!!.config, true)
        val segBitmap = Bitmap.createBitmap(inputImage!!.width, inputImage!!.height, Bitmap.Config.ARGB_8888)

        if (segmentation != null && segmentation!!.isSuccessful) {
            val canvas = Canvas(segBitmap)
            val ratio = scaleRatio(
                segmentation!!.segBitmap.height,
                segmentation!!.segBitmap.width,
                inputImage!!.height,
                inputImage!!.width
            )
            matrix.postScale(ratio, ratio)
            canvas.drawBitmap(
                segmentation!!.segBitmap, matrix,
                Paint(Paint.FILTER_BITMAP_FLAG)
            )
        } else {
            Log.i("Segmentation", "segmentation failed")
        }

        val segMat = Mat()
        Utils.bitmapToMat(segBitmap, segMat)
        val original = Mat()
        Utils.bitmapToMat(originalBitmap, original)
        segBitmap.recycle()
        originalBitmap.recycle()
        val pair = ImageProcess.getForeground(original, segMat)
        return pair
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Actions")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallary()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        photoPicPath = (storagePath + File.separator
                + Calendar.getInstance().timeInMillis + ".jpg")
        val file = File(photoPicPath)
        val photoUri = FileProvider.getUriForFile(
            applicationContext,
            "$packageName.provider",
            file
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

        startActivityForResult(intent, CAMERA)
    }

    @Throws(IOException::class)
    private fun getBitmap(uri: Uri?, height: Int, width: Int): Bitmap? {
        var input = this.contentResolver.openInputStream(uri!!)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inPreferredConfig = Bitmap.Config.ARGB_8888//optional
        BitmapFactory.decodeStream(input, null, options)
        input!!.close()
        if (options.outWidth == -1 || options.outHeight == -1)
            return null
        options.inJustDecodeBounds = false
        //        options.inSampleSize = (int) (Math.ceil(scaleRatio(options.outHeight, options.outWidth,
        //                height, width)));
        input = this.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(input, null, options)
        input!!.close()
        return bitmap
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED) {
            return
        }
        var contentURI: Uri? = null
        if (requestCode == GALLERY) {
            if (data != null) {
                contentURI = data.data
            }
        } else if (requestCode == CAMERA) {
            contentURI = Uri.fromFile(File(photoPicPath))
        } else {
            return
        }
        try {
            inputImage = getBitmap(
                contentURI,
                image_view!!.height,
                image_view!!.width
            )
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(
                this@SegmentActivity, "Failed!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // run segmentation on background thread
        runThread!!.post {
            Log.i("Segmentation", "start run " + Integer.toString(initFlag))
            lock.lock()
            if (initFlag == 1) {
                val resizedBitmap = preProcess(inputImage!!)
                segmentation = Segmenter.instance.segment(resizedBitmap)
                if (resizedBitmap != inputImage) {
                    resizedBitmap.recycle()
                }
            }
            lock.unlock()
            EventBus.getDefault().post(MessageEvent.SegEvent())
            Log.i("Segmentation", "end run " + Integer.toString(initFlag))
        }
        if (!inProgressDialog!!.isShowing) {
            inProgressDialog!!.show()
        }
    }

    private fun preProcess(inBitmap: Bitmap): Bitmap {
        val width = inBitmap.width
        val height = inBitmap.height
        var newWidth = width
        var newHeight = height

        if (width > height && width > inputSize) {
            newWidth = inputSize
            newHeight = (height * newWidth.toFloat() / width).toInt()
        }

        if (width > height && width <= inputSize) {
            //the bitmap is already smaller than our required dimension, no need to resize it
            return inBitmap
        }

        if (width < height && height > inputSize) {
            newHeight = inputSize
            newWidth = (width * newHeight.toFloat() / height).toInt()
        }

        if (width < height && height <= inputSize) {
            //the bitmap is already smaller than our required dimension, no need to resize it
            return inBitmap
        }

        if (width == height && width > inputSize) {
            newWidth = inputSize
            newHeight = newWidth
        }

        if (width == height && width <= inputSize) {
            //the bitmap is already smaller than our required dimension, no need to resize it
            return inBitmap
        }
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        return Bitmap.createBitmap(
            inBitmap, 0, 0, width, height, matrix, false
        )
    }

    companion object {
        private val STORAGE_DIRECTORY = "mace_segmentation_demo"
        private val PASCAL_LABELS_FILE = "file:///android_asset/labels_list.txt"
    }

}

