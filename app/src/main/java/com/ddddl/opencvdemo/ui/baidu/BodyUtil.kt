package com.ddddl.opencvdemo.ui.baidu

import android.graphics.*
import android.support.v4.view.ViewCompat.setLayerType
import android.util.Log
import android.view.View
import com.ddddl.opencvdemo.utils.MatUtil
import kotlinx.android.synthetic.main.activity_util.*
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URLEncoder

object BodyUtil {

    /**
     * 重要提示代码中所需工具类
     * FileUtil,Base64Util,HttpUtil,GsonUtils请从
     * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
     * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
     * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
     * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3
     * 下载
     */
    fun bodySeg(pathName: String): BodyBean? {
        // 请求url
        val url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/body_seg"
        try {
            val imgData = FileUtil.readFileByBytes(pathName)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)
            return GsonUtils.fromJson(result, BodyBean::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun Bitmap2Bytes(bm: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }


    fun convert(labelmapBase64: String, realWidth: Int, realHeight: Int, original: Bitmap): Bitmap {
        val startTime = System.currentTimeMillis()

        val bytes = android.util.Base64.decode(labelmapBase64.toByteArray(), android.util.Base64.DEFAULT)
        val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        val newImage = Bitmap.createScaledBitmap(image, realWidth, realHeight, false)
        val w = newImage.getWidth()
        val h = newImage.getHeight()

        val grayImage = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)

        val mIntValues = IntArray(w * h)
        newImage.getPixels(mIntValues, 0, w, 0, 0, w, h)

        val mPixelsValues = IntArray(w * h)

        for (i in mIntValues.indices) {
            val pixel = mIntValues[i] * 255
            mPixelsValues[i] = pixel
        }

        grayImage.setPixels(mPixelsValues, 0, w, 0, 0, w, h)


        val originalMat = Mat()
        Utils.bitmapToMat(original, originalMat)

        val grayMat = Mat()
        Utils.bitmapToMat(grayImage, grayMat)

//        val mask = Mat(w, h, CvType.CV_8UC3)
//        mask.setTo(Scalar(255.0))
//        val rect = Rect()
//        rect.x = 50
//        rect.y = 50
//        rect.width = 100
//        rect.height = 100
//
//        Imgproc.rectangle(
//            mask, rect.tl(), rect.br(),
//            Scalar(255.0), -1
//        )


        val dst = Mat()
        originalMat.copyTo(dst)
        dst.setTo(Scalar(255.0), grayMat)

        val result = Mat()
        val bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888)
        Imgproc.cvtColor(dst, result, Imgproc.COLOR_BGRA2RGB)
        Utils.matToBitmap(result, bitmap)

        image.recycle()
        original.recycle()
        newImage.recycle()
        grayImage.recycle()

        originalMat.release()
        grayMat.release()
        dst.release()
        result.release()

        val finishTime = System.currentTimeMillis()
        Log.e("Time", "${finishTime - startTime}")
        return bitmap
    }

}

























