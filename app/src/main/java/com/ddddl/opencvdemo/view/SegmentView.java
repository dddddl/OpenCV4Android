package com.ddddl.opencvdemo.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class SegmentView extends View {

    private final static long DEFAULT_ANIMATION_TIME = 300L;
    private final static int BITMAP_GENERATE_RESULT = 0x000001;
    private final static int BITMAP_GENERATE_ERROR = 0x000002;
    private final static String BITMAP_ERROR = "bitmapError";
    /**
     * 正在执行动画
     */
    private final static int VIEW_MODE_RUN_ANIMATION = 0x000003;
    /**
     * 正在控制图片
     */
    private final static int VIEW_MODE_ON_CONTROL_IMG = 0x000004;
    /**
     * 空闲状态
     */
    private final static int VIEW_MODE_IDLE = 0x000005;
    /**
     * 图片滑动
     */
    private final static int VIEW_MODE_TRANSLATE = 0x000006;

    private Paint stitchingPaint;
    private Paint framePaint;

    private Rect clipScope = new Rect();

    private ImageData curImageData;
    private ImageData bgImageData;
    private Bitmap outputBitmap;

    private OnGenerateBitmapListener onGenerateBitmapListener;
    // 触摸是否在对视图进行控制
    private int mViewMode = VIEW_MODE_IDLE;

    private int findIndex = -1;

    private Thread handleBitmapThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                outputBitmap = Bitmap.createBitmap(getMeasuredWidth(),
                        getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(outputBitmap);
                draw(canvas);
                generateBitmapHandler.sendEmptyMessage(BITMAP_GENERATE_RESULT);
            } catch (Exception e) {
                // 扔到主线程抛出
                Message message = new Message();
                message.what = BITMAP_GENERATE_ERROR;
                Bundle bundle = new Bundle();
                bundle.putSerializable(BITMAP_ERROR, e);
                message.setData(bundle);
                generateBitmapHandler.sendMessage(message);
            }
        }
    });

    private Handler generateBitmapHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case BITMAP_GENERATE_RESULT:
                    if (null != onGenerateBitmapListener)
                        onGenerateBitmapListener.onResourceReady(outputBitmap);
                    SegmentView.this.setDrawingCacheEnabled(false);
                    break;
                case BITMAP_GENERATE_ERROR:
                    if (null != onGenerateBitmapListener) {
                        Bundle bundle = msg.getData();
                        Exception e = (Exception) bundle.getSerializable(BITMAP_ERROR);
                        onGenerateBitmapListener.onError(e);
                    }
                    SegmentView.this.setDrawingCacheEnabled(false);
                    break;
            }
            return false;
        }
    });

    public SegmentView(Context context) {
        this(context, null);
    }

    public SegmentView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SegmentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initProperty();
        applyAttr(attrs);
    }

    private void initProperty() {
        stitchingPaint = new Paint();
        framePaint = new Paint();

        framePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        initPaintProperty(stitchingPaint);
        initPaintProperty(framePaint);

    }

    private void applyAttr(AttributeSet attrs) {
    }

    private void initPaintProperty(Paint paint) {
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // measure child img
        final int maxImgWidth = getMeasuredWidth();
        final int maxImgHeight = getMeasuredHeight();
        final int measureWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int measureHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        int totalImageHeight = 0;
        // 缩放和旋转影响size的交给measure
        if (curImageData != null) {
// 缩放和旋转影响size的交给measure
            ImageData imageData = curImageData;
            final int imgOrgWidth = imageData.getImgWidth();
            final int imgOrgHeight = imageData.getImgHeight();
            int imgRotateWidth;
            int imgRotateHeight;
            if (imageData.scale > 0) {
                imageData.matrix.setScale(imageData.scale, imageData.scale, imageData.drawRect.centerX(), imageData.drawRect.centerY());
            } else {
                final float sizeProportion = (float) imgOrgWidth / imgOrgHeight;
                if (imgOrgHeight > imgOrgWidth) {
                    if (measureHeightSize == MeasureSpec.EXACTLY &&
                            imgOrgHeight > maxImgHeight) {
                        imgRotateWidth = (int) (maxImgHeight * sizeProportion);
                        imgRotateHeight = maxImgHeight;
                    } else {
                        imgRotateWidth = imgOrgWidth;
                        imgRotateHeight = imgOrgHeight;
                    }
                } else {
                    if (imgOrgWidth > maxImgWidth) {
                        imgRotateHeight = (int) (maxImgWidth / sizeProportion);
                        imgRotateWidth = maxImgWidth;
                    } else {
                        imgRotateWidth = imgOrgWidth;
                        imgRotateHeight = imgOrgHeight;
                    }
                }

                // resize
                imageData.reSize(imgRotateWidth, imgRotateHeight);
            }

//            imageData.matrix.postTranslate(imageData.drawRect.left,
//                    imageData.drawRect.top);
            Log.e("matrix", "centerX" + imageData.drawRect.centerX());
            Log.e("matrix", "centerY" + imageData.drawRect.centerY());
            Log.e("matrix", "rotateAngle" + imageData.rotateAngle);
            Log.e("matrix", "------------------------------------------1");

            imageData.matrix.postRotate(imageData.rotateAngle, imageData.drawRect.centerX(),
                    imageData.drawRect.centerY());
            Log.e("matrix", "centerX" + imageData.drawRect.centerX());
            Log.e("matrix", "centerY" + imageData.drawRect.centerY());
            Log.e("matrix", "rotateAngle" + imageData.rotateAngle);
            Log.e("matrix", "------------------------------------------2");
            totalImageHeight += bgImageData.drawRect.height();
        }

        switch (measureHeightSize) {
            // wrap_content
            case MeasureSpec.AT_MOST:
                setMeasuredDimension(MeasureSpec.makeMeasureSpec(maxImgWidth,
                        measureWidthSize), MeasureSpec.makeMeasureSpec(totalImageHeight, measureHeightSize));
                break;
            // match_parent or accurate num
            case MeasureSpec.EXACTLY:
                setMeasuredDimension(MeasureSpec.makeMeasureSpec(maxImgWidth,
                        measureWidthSize), MeasureSpec.makeMeasureSpec(maxImgHeight, measureHeightSize));
                break;
            case MeasureSpec.UNSPECIFIED:
                setMeasuredDimension(MeasureSpec.makeMeasureSpec(maxImgWidth,
                        measureWidthSize), MeasureSpec.makeMeasureSpec(totalImageHeight, measureHeightSize));
                break;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // measure child layout
        int mid = (right - left) >> 1;
        if (curImageData != null) {
            final ImageData imageData = curImageData;
            // fix layout translate
            imageData.matrix.mapRect(imageData.drawRect, imageData.orgRect);
            int translateTop = (int) imageData.translateY;
            int translateLeft = (int) imageData.translateX;
            imageData.matrix.postTranslate(translateLeft, translateTop);
            imageData.matrix.mapRect(imageData.drawRect, imageData.orgRect);
        }

        if (bgImageData != null) {
            final ImageData imageData = bgImageData;
            // fix layout translate
            imageData.matrix.mapRect(imageData.drawRect, imageData.orgRect);
            int translateTop = (int) (imageData.orgRect.top - imageData.drawRect.top);
            int translateLeft = (int) (mid - imageData.drawRect.centerX());
            imageData.matrix.postTranslate(translateLeft, translateTop);
            imageData.matrix.mapRect(imageData.drawRect, imageData.orgRect);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.saveLayer(0, 0, getMeasuredWidth(), getMeasuredHeight(), stitchingPaint, Canvas.ALL_SAVE_FLAG);

        drawImg(canvas);

        drawFrame(canvas);

        canvas.restore();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // 分发各个img的触摸事件
        if (mViewMode != VIEW_MODE_IDLE && findIndex >= 0) {
            curImageData.onTouchEvent(event);
            return true;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (mViewMode == VIEW_MODE_IDLE) {
                    findIndex = findTouchImg(event);
                    if (findIndex >= 0) {
                        curImageData.onTouchEvent(event);
                        if (getParent() != null)
                            getParent().requestDisallowInterceptTouchEvent(true);
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // 判断落点是否在img中
                if (mViewMode == VIEW_MODE_IDLE) {
                    findIndex = findTouchImg(event);
                    if (findIndex >= 0) {
                        curImageData.onTouchEvent(event);
                        if (getParent() != null)
                            getParent().requestDisallowInterceptTouchEvent(true);
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    /**
     * @return -1 is not find
     */
    private int findTouchImg(MotionEvent event) {
        final float touchX = event.getX();
        final float touchY = event.getY();
        ImageData imageData = curImageData;
        if (imageData.drawRect.contains(touchX, touchY)) {
            return 0;
        }
        return -1;
    }

    protected void requestDisallowInterceptTouchEvent(int flag) {
        this.mViewMode = flag == 1 ? VIEW_MODE_ON_CONTROL_IMG : flag == 2 ? VIEW_MODE_TRANSLATE : VIEW_MODE_IDLE;
    }

    private void drawFrame(Canvas canvas) {
        canvas.drawRect(clipScope, framePaint);
    }

    private void drawImg(Canvas canvas) {
        if (bgImageData != null)
            canvas.drawBitmap(bgImageData.getBitmap(), bgImageData.matrix, stitchingPaint);
        if (curImageData != null)
            canvas.drawBitmap(curImageData.getBitmap(), curImageData.matrix, stitchingPaint);
    }

    public void addImage(Bitmap bitmap, Bitmap bgBitmap) {
        if (null == bitmap)
            return;
        curImageData = new ImageData(bitmap);
//        curImageData.rotateAngle = (int) (10 * Math.random()) * 90;
        bgImageData = new ImageData(bgBitmap);
        clearMatrixCache();
        post(new Runnable() {
            @Override
            public void run() {
                reDraw();
            }
        });
    }

    public void clearImage() {
        curImageData = null;
        reDraw();
    }

    private void clearMatrixCache() {
        curImageData.clearMatrixCache();
    }

    private void reDraw() {
//        requestLayout();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            postInvalidateOnAnimation();
        } else {
            postInvalidate();
        }
    }

    public OnGenerateBitmapListener getOnGenerateBitmapListener() {
        return onGenerateBitmapListener;
    }

    public void setOnGenerateBitmapListener(OnGenerateBitmapListener onGenerateBitmapListener) {
        this.onGenerateBitmapListener = onGenerateBitmapListener;
    }

    public void generateBitmap() {
        generateBitmap(onGenerateBitmapListener);
    }

    public void generateBitmap(OnGenerateBitmapListener onGenerateBitmapListener) {
        this.onGenerateBitmapListener = onGenerateBitmapListener;
        handleBitmapThread.start();
    }

    public class ImageData {
        public ImageData(Bitmap bitmap) {
            this.bitmap = bitmap;
            this.matrix = new Matrix();
            orgRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        }

        // 默认置0
        float scale = 0f;
        // 0点在3点钟方向，达到垂直居中的效果，需要置为-90度
        float rotateAngle = 0;
        RectF drawRect = new RectF();
        RectF orgRect = new RectF();
        Bitmap bitmap;
        Matrix matrix;

        float translateX = 0f;
        float translateY = 0f;

        private float distanceStub = 0f;
        private float angleStub = 0f;

        private float xStub = 0f;
        private float yStub = 0f;

        public float getTranslateX() {
            return translateX;
        }

        public float getTranslateY() {
            return translateY;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public RectF getDrawRect() {
            return drawRect;
        }

        public int getImgWidth() {
            return bitmap.getWidth();
        }

        public int getImgHeight() {
            return bitmap.getHeight();
        }

        public void layout(int l, int t, int r, int b) {
            drawRect.set(l, t, r, b);
        }

        void reSize(int w, int h) {
            int orgWidth = bitmap.getWidth();
            int orgHeight = bitmap.getHeight();
            // 计算缩放比例
            float scaleWidth = ((float) w) / orgWidth;
            float scaleHeight = ((float) h) / orgHeight;
            scale = (scaleWidth + scaleHeight) * 0.5f;
            matrix.postScale(scale, scale);
        }

        void clearMatrixCache() {
            matrix.reset();
        }

        void setScale(float scale) {
            this.scale = scale;
        }

        float getScale() {
            return this.scale;
        }

        void setRotateAngle(float angle) {
            this.rotateAngle = angle;
        }

        float getRotateAngle() {
            return this.rotateAngle;
        }

        /**
         * imageData的触摸处理事件
         *
         * @param e 触摸事件
         */
        protected void onTouchEvent(MotionEvent e) {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    requestDisallowInterceptTouchEvent(2);
                    xStub = e.getX();
                    yStub = e.getY();
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (e.getPointerCount() == 2) {
                        requestDisallowInterceptTouchEvent(1);
                        distanceStub = getPointDistance(e);
                        angleStub = getPointAngle(e);
                    } else {
                        requestDisallowInterceptTouchEvent(0);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    // confirm multi touch
                    if (e.getPointerCount() == 2) {
                        float tempDistance = getPointDistance(e);
                        float tempAngle = getPointAngle(e);
                        float tempScale = this.getScale();
                        float tempRotateAngle = this.getRotateAngle();

                        tempScale += (tempDistance / distanceStub) - 1;
                        tempRotateAngle += tempAngle - angleStub;

                        angleStub = tempAngle;
                        distanceStub = tempDistance;

                        this.setRotateAngle(tempRotateAngle);
                        this.setScale(tempScale);
                        reDraw();
                    } else if (e.getPointerCount() == 1) {
                        if (mViewMode == VIEW_MODE_TRANSLATE) {
                            final float touchX1 = e.getX();
                            final float touchY1 = e.getY();
                            translateX = touchX1 - xStub;
                            translateY = touchY1 - yStub;
                            xStub = touchX1;
                            yStub = touchY1;
                            reDraw();
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
//                    runAngleAdsorbentAnim(findIndex);
                    requestDisallowInterceptTouchEvent(0);
                    if (getParent() != null)
                        getParent().requestDisallowInterceptTouchEvent(false);
                    translateX = 0;
                    translateY = 0;
                    xStub = 0;
                    yStub = 0;
                    distanceStub = 0;
                    angleStub = 0;
                    findIndex = -1;
                    break;
            }
        }

        private float getPointDistance(MotionEvent e) {
            if (e.getPointerCount() > 1) {
                final float touchX1 = e.getX(0);
                final float touchY1 = e.getY(0);
                final float touchX2 = e.getX(1);
                final float touchY2 = e.getY(1);
                return (float) Math.abs(Math.sqrt(Math.pow(touchX2 - touchX1, 2) + Math.pow(touchY2 - touchY1, 2)));
            }
            return 0;
        }

        private float getPointAngle(MotionEvent e) {
            if (e.getPointerCount() > 1) {
                final float touchX1 = e.getX(0);
                final float touchY1 = e.getY(0);
                final float touchX2 = e.getX(1);
                final float touchY2 = e.getY(1);
                return (float) (Math.atan2(touchY2 - touchY1, touchX2 - touchX1) * (180f / Math.PI));
            }
            return 0;
        }
    }

    public interface OnGenerateBitmapListener {
        void onError(Throwable t);

        void onResourceReady(Bitmap bitmap);
    }
}
