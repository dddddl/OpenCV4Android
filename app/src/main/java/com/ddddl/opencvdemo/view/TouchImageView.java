package com.ddddl.opencvdemo.view;

import android.content.Context;
import android.graphics.*;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import com.ddddl.opencvdemo.mace.common.MessageEvent;
import org.greenrobot.eventbus.EventBus;
import org.opencv.core.Rect;

public class TouchImageView extends View {

    // 绘制图片的边框
//    private Paint paintEdge;
    private Matrix bgMatrix = new Matrix();
    // 绘制图片的矩阵
    private Matrix matrix = new Matrix();
    // 手指按下时图片的矩阵
    private Matrix downMatrix = new Matrix();
    // 手指移动时图片的矩阵
    private Matrix moveMatrix = new Matrix();
    // 资源图片的位图
    private Bitmap srcImage;
    private Bitmap bgBitmap;
    // 多点触屏时的中心点
    private PointF midPoint = new PointF();
    // 触控模式
    private int mode;
    private static final int NONE = 0; // 无模式
    private static final int TRANS = 1; // 拖拽模式
    private static final int ZOOM = 2; // 缩放模式
    // 是否超过边界
    private boolean withinBorder;

    private boolean bgInpaint = false;
    private int mViewWidth;
    private int mViewHeight;

    public TouchImageView(Context context) {
        this(context, null);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
//        paintEdge = new Paint();
//        paintEdge.setColor(Color.BLACK);
//        paintEdge.setAlpha(170);
//        paintEdge.setAntiAlias(true);
    }

    public void addImage(Bitmap bitmap, Rect rect, Bitmap bgbitmap) {
        clearMatrix();
        srcImage = bitmap;
        bgBitmap = bgbitmap;
        matrix.postTranslate((float) rect.tl().x, (float) rect.tl().y);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mViewWidth = getWidth();
        mViewHeight = getHeight();
        float scaleW = (float) mViewWidth / bgBitmap.getWidth();
        float scaleH = (float) mViewHeight / bgBitmap.getHeight();

        if (scaleW > scaleH) {
            matrix.postScale(scaleH, scaleH);
            bgMatrix.postScale(scaleH, scaleH);
        } else {
            matrix.postScale(scaleW, scaleW);
            bgMatrix.postScale(scaleW, scaleW);
        }
        invalidate();
    }

    public Bitmap getBgBitmap() {
        return bgBitmap;
    }

    public void inpaintBgBitmap(Bitmap bgBitmap) {
        this.bgBitmap = bgBitmap;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mViewWidth = displayMetrics.widthPixels;
        mViewHeight = displayMetrics.heightPixels;
        int scaleW = mViewWidth / bgBitmap.getWidth();
        int scaleH = mViewHeight / bgBitmap.getHeight();


        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (srcImage == null)
            return;

//        float[] points = getBitmapPoints(srcImage, matrix);
//        float x1 = points[0];
//        float y1 = points[1];
//        float x2 = points[2];
//        float y2 = points[3];
//        float x3 = points[4];
//        float y3 = points[5];
//        float x4 = points[6];
//        float y4 = points[7];
//        // 画边框
//        canvas.drawLine(x1, y1, x2, y2, paintEdge);
//        canvas.drawLine(x2, y2, x4, y4, paintEdge);
//        canvas.drawLine(x4, y4, x3, y3, paintEdge);
//        canvas.drawLine(x3, y3, x1, y1, paintEdge);
        // 画图片
        canvas.drawBitmap(bgBitmap, bgMatrix, null);
        canvas.drawBitmap(srcImage, matrix, null);

    }

    // 手指按下屏幕的X坐标
    private float downX;
    // 手指按下屏幕的Y坐标
    private float downY;
    // 手指之间的初始距离
    private float oldDistance;
    // 手指之间的初始角度
    private float oldRotation;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mode = TRANS;
                downX = event.getX();
                downY = event.getY();
                downMatrix.set(matrix);
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // 多点触控
                mode = ZOOM;
                oldDistance = getSpaceDistance(event);
                oldRotation = getSpaceRotation(event);
                downMatrix.set(matrix);
                midPoint = getMidPoint(event);
                break;
            case MotionEvent.ACTION_MOVE:
                // 缩放
                if (mode == ZOOM) {
                    moveMatrix.set(downMatrix);
                    float deltaRotation = getSpaceRotation(event) - oldRotation;
                    float scale = getSpaceDistance(event) / oldDistance;
                    moveMatrix.postScale(scale, scale, midPoint.x, midPoint.y);
                    moveMatrix.postRotate(deltaRotation, midPoint.x, midPoint.y);
                    withinBorder = getMatrixBorderCheck(srcImage, event.getX(), event.getY());
                    if (withinBorder) {
                        matrix.set(moveMatrix);
                        invalidate();
                    }
                }
                // 平移
                else if (mode == TRANS) {
                    moveMatrix.set(downMatrix);
                    moveMatrix.postTranslate(event.getX() - downX, event.getY() - downY);
                    withinBorder = getMatrixBorderCheck(srcImage, event.getX(), event.getY());
                    if (withinBorder) {
                        matrix.set(moveMatrix);
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                if (mode != NONE && !bgInpaint) {
                    bgInpaint = true;
                    EventBus.getDefault().post(new MessageEvent.BgChangeEvent());
                }
                mode = NONE;
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 获取手指的旋转角度
     *
     * @param event
     * @return
     */
    private float getSpaceRotation(MotionEvent event) {
        double deltaX = event.getX(0) - event.getX(1);
        double deltaY = event.getY(0) - event.getY(1);
        double radians = Math.atan2(deltaY, deltaX);
        return (float) Math.toDegrees(radians);
    }

    /**
     * 获取手指间的距离
     *
     * @param event
     * @return
     */
    private float getSpaceDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 获取手势中心点
     *
     * @param event
     */
    private PointF getMidPoint(MotionEvent event) {
        PointF point = new PointF();
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
        return point;
    }

    /**
     * 将matrix的点映射成坐标点
     *
     * @return
     */
    protected float[] getBitmapPoints(Bitmap bitmap, Matrix matrix) {
        float[] dst = new float[8];
        float[] src = new float[]{
                0, 0,
                bitmap.getWidth(), 0,
                0, bitmap.getHeight(),
                bitmap.getWidth(), bitmap.getHeight()
        };
        matrix.mapPoints(dst, src);
        return dst;
    }

    /**
     * 检查边界
     *
     * @param x
     * @param y
     * @return true - 在边界内 ｜ false － 超出边界
     */
    private boolean getMatrixBorderCheck(Bitmap bitmap, float x, float y) {
        if (bitmap == null) return false;
        float[] points = getBitmapPoints(bitmap, moveMatrix);
        float x1 = points[0];
        float y1 = points[1];
        float x2 = points[2];
        float y2 = points[3];
        float x3 = points[4];
        float y3 = points[5];
        float x4 = points[6];
        float y4 = points[7];
        float edge = (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y3, 2));
        if ((2 + Math.sqrt(2)) * edge >=
                Math.sqrt(Math.pow(x - x1, 2) + Math.pow(y - y1, 2))
                        + Math.sqrt(Math.pow(x - x2, 2) + Math.pow(y - y2, 2))
                        + Math.sqrt(Math.pow(x - x3, 2) + Math.pow(y - y3, 2))
                        + Math.sqrt(Math.pow(x - x4, 2) + Math.pow(y - y4, 2))) {
            return true;
        }
        return false;
    }

    private void clearMatrix() {
        bgMatrix.reset();
        matrix.reset();
        downMatrix.reset();
        moveMatrix.reset();
        bgBitmap = null;
        srcImage = null;
    }
}
