package com.ddddl.opencvdemo.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.ddddl.opencvdemo.R;
import com.ddddl.opencvdemo.utils.CardNumberROIFinder;
import com.ddddl.opencvdemo.utils.ImageSelectUtils;
import com.googlecode.tesseract.android.TessBaseAPI;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.*;

public class OCRActivity extends AppCompatActivity implements View.OnClickListener {

    private TessBaseAPI baseApi;
    private static final String DEFAULT_LANGUAGE = "nums";
    private String TAG = "OcrDemoActivity";
    private int REQUEST_CAPTURE_IMAGE = 1;
    private int option;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        Button selectBtn = (Button) this.findViewById(R.id.select_image_btn);
        Button ocrRecogBtn = (Button) this.findViewById(R.id.ocr_recognize_btn);
        selectBtn.setOnClickListener(this);
        ocrRecogBtn.setOnClickListener(this);
        option = getIntent().getIntExtra("TYPE", 0);

        try {
            initTessBaseAPI();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (option == 2) {
            this.setTitle("身份证号码识别演示");
        } else if (option == 3) {
            this.setTitle("偏斜校正演示");
            ocrRecogBtn.setText("校正");
        } else {
            this.setTitle("Tesseract OCR文本识别演示");
        }
    }

    private void initTessBaseAPI() throws IOException {
        baseApi = new TessBaseAPI();
        String datapath = Environment.getExternalStorageDirectory() + "/tesseract/";
        File dir = new File(datapath + "/tessdata/");
        if (!dir.exists()) {
            dir.mkdirs();

        }
        InputStream inputStream = getResources().openRawResource(R.raw.eng);
        File file = new File(dir, "nums.traineddata");
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buff = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buff)) != -1) {
            outputStream.write(buff, 0, len);
        }
        inputStream.close();
        outputStream.close();
        boolean success = baseApi.init(datapath, DEFAULT_LANGUAGE);
        if (success) {
            Log.i("ocr", "success");
        } else {
            Log.i("ocr", "failed");
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.select_image_btn:
                pickUpImage();
                break;
            case R.id.ocr_recognize_btn:
                if (option == 2) {
                    recognizeCardId();
                } else if (option == 3) {
                    deSkewTextImage();
                } else {
                    recognizeTextImage();
                }
                break;
            default:
                break;
        }
    }

    private void deSkewTextImage() {
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if (src.empty()) {
            return;
        }
        Mat dst = new Mat();
        CardNumberROIFinder.deSkewText(src, dst);

        // 转换为Bitmap，显示
        Bitmap bm = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, bm);

        // show
        ImageView iv = this.findViewById(R.id.chapter8_imageView);
        iv.setImageBitmap(bm);

        // 释放内存
        dst.release();
        src.release();
    }

    private void recognizeCardId() {
        Bitmap template = BitmapFactory.decodeResource(this.getResources(), R.drawable.card_template);
        Bitmap cardImage = BitmapFactory.decodeFile(fileUri.getPath());
        Bitmap temp = CardNumberROIFinder.extractNumberROI(cardImage.copy(Bitmap.Config.ARGB_8888, true), template);
        baseApi.setImage(temp);
        String myIdNumber = baseApi.getUTF8Text();
        TextView txtView = findViewById(R.id.text_result_id);
        txtView.setText("身份证号码为:" + myIdNumber);
        ImageView imageView = findViewById(R.id.chapter8_imageView);
        imageView.setImageBitmap(temp);
    }

    private void recognizeTextImage() {
        if (fileUri == null) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bmp = BitmapFactory.decodeFile(fileUri.getPath());
                baseApi.setImage(bmp);
                final String recognizedText = baseApi.getUTF8Text();
                if (!recognizedText.isEmpty()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView txtView = findViewById(R.id.text_result_id);
                            txtView.append("识别结果:\n" + recognizedText);
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView txtView = findViewById(R.id.text_result_id);
                            txtView.append("无法识别:\n");
                        }
                    });
                }
            }
        }).start();
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
