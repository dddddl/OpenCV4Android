package com.ddddl.opencvdemo.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.ddddl.opencvdemo.R;
import com.ddddl.opencvdemo.nativehelper.FaceHelper;
import com.ddddl.opencvdemo.utils.ImageSelectUtils;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FaceBeautyActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "FaceBeautyActivity";
    private int REQUEST_CAPTURE_IMAGE = 1;
    private Uri fileUri;
    private FaceHelper faceHelper = new FaceHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_beauty);

        Button selectBtn = (Button) this.findViewById(R.id.select_image_btn);
        Button btnface = (Button) this.findViewById(R.id.btn_face);
        selectBtn.setOnClickListener(this);
        btnface.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.select_image_btn:
                pickUpImage();
                break;
            case R.id.btn_face:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Integral_Image_Demo();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            default:
                break;
        }
    }

    public void Integral_Image_Demo() throws FileNotFoundException {
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if (src.empty()) {
            return;
        }
        Mat dst = new Mat(src.size(), src.type());
        Mat mask = new Mat(src.size(), CvType.CV_8UC1);
        Mat sum = new Mat();
        Mat sqsum = new Mat();
        int w = src.cols();
        int h = src.rows();
        int ch = src.channels();
        int[] data1 = new int[(w + 1) * (h + 1) * ch];
        float[] data2 = new float[(w + 1) * (h + 1) * ch];
        Imgproc.integral2(src, sum, sqsum, CvType.CV_32S, CvType.CV_32F);
        sum.get(0, 0, data1);
        sqsum.get(0, 0, data2);

        faceHelper.beautySkinFilter(src.getNativeObjAddr(), dst.getNativeObjAddr(), 100, false);
        //generateMask(src, mask);
        //FastEPFilter(src, data1, data2, dst);
        //blendImage(src, dst, mask);
        //enhanceEdge(src, dst, mask);

        // 转换为Bitmap，显示
        final Bitmap bm = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bm);
        File image = new File(Environment.getExternalStorageDirectory() + "/Pictures/Screenshots/" + System.currentTimeMillis() + ".jpg");

        bm.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(image));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // show
                ImageView iv = (ImageView) FaceBeautyActivity.this.findViewById(R.id.chapter8_imageView);
                iv.setImageBitmap(bm);
            }
        });

        // release memory
        src.release();
        dst.release();
        sum.release();
        sqsum.release();
        data1 = null;
        data2 = null;
        mask.release();
        result.release();
    }


    private void pickUpImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "图像选择..."), REQUEST_CAPTURE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                File f = new File(ImageSelectUtils.getRealPath(uri, getApplicationContext()));
                fileUri = Uri.fromFile(f);
            }
        }
        // display it
        displaySelectedImage();
    }

    private void displaySelectedImage() {
        if (fileUri == null) return;
        ImageView imageView = (ImageView) this.findViewById(R.id.chapter8_imageView);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileUri.getPath(), options);
        int w = options.outWidth;
        int h = options.outHeight;
        int inSample = 1;
        if (w > 1000 || h > 1000) {
            while (Math.max(w / inSample, h / inSample) > 1000) {
                inSample *= 2;
            }
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSample;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bm = BitmapFactory.decodeFile(fileUri.getPath(), options);
        imageView.setImageBitmap(bm);
    }
}
