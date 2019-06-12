package com.github.chrisbanes.photoview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class CutView extends View {
    float downX;
    float downY;
    boolean isLeft;
    boolean isRight;
    boolean isTop;
    boolean isBottom;
    boolean isMove;
    boolean isSlideLeft;
    boolean isSlideRight;
    boolean isSlideTop;
    boolean isSlideBottom;

    float rectLeft;
    float rectRight;
    float rectTop;
    float rectBottom;
    private int measuredWidth;
    private int measuredHeight;
    private Paint paint;
    private int dp3;
    private int cornerLength;
    private int dp1;

    private int edgeTop;
    private int edgeBottom;

    public void setEdgeTop(int edgeTop) {
        this.edgeTop = edgeTop;
    }

    public void setEdgeBottom(int edgeBottom) {
        this.edgeBottom = edgeBottom;
    }

    public CutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CutView(Context context) {
        super(context);
        init();
    }

    private void init() {
        dp3 = 3;
        dp1 = 1;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                downX = event.getX();
                downY = event.getY();

                if (downX >= rectLeft && downX <= rectRight && downY >= rectTop && downY <= rectBottom) {
                    //判断手指的范围在左面还是右面
                    int w = (int) ((rectRight - rectLeft) / 3);
                    if (downX >= rectLeft && downX <= rectLeft + w) {
                        isLeft = true;
                    } else if (downX <= rectRight && downX >= rectRight - w) {
                        isRight = true;
                    }
                    //判断手指的范围在上面还是下面
                    int h = (int) ((rectBottom - rectTop) / 3);
                    if (downY >= rectTop && downY <= rectTop + h) {
                        isTop = true;
                    } else if (downY <= rectBottom && downY >= rectBottom - h) {
                        isBottom = true;
                    }
                    //如果手指范围没有在任何边界位置, 那么我们就认为用户是想拖拽框体
                    if (!isLeft && !isTop && !isRight && !isBottom) {
                        isMove = true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                //得到手指移动距离
                float slideX = moveX - downX;
                float slideY = moveY - downY;

                if (isMove) {//判断是否是拖拽模式
                    rectLeft += slideX;
                    rectRight += slideX;
                    rectTop += slideY;
                    rectBottom += slideY;
                    //同时改变left和right值, 达到左右移动的效果
                    if (rectLeft < 0 || rectRight > measuredWidth) {//判断x轴的移动边界
                        rectLeft -= slideX;
                        rectRight -= slideX;
                    }
                    //同时改变top和bottom值, 达到上下移动的效果
                    if (rectTop < 0 || rectBottom > measuredHeight) {//判断y轴的移动边界
                        rectTop -= slideY;
                        rectBottom -= slideY;
                    }
                    //实时触发onDraw()方法
                    invalidate();
                    downX = moveX;
                    downY = moveY;
                } else {
                    if (isLeft) {
                        rectLeft += slideX;
                        if (rectLeft < 0) rectLeft = 0;
                        if (rectLeft > rectRight - cornerLength * 2)
                            rectLeft = rectRight - cornerLength * 2;
                    } else if (isRight) {
                        rectRight += slideX;
                        if (rectRight > measuredWidth)
                            rectRight = measuredWidth;
                        if (rectRight < rectLeft + cornerLength * 2)
                            rectRight = rectLeft + cornerLength * 2;
                    }
                    //改变边框的高度, 如果两个都满足(比如手指在边角位置),那么就呈现一种缩放状态
                    if (isTop) {
                        rectTop += slideY;
                        if (rectTop < edgeTop) rectTop = edgeTop;
                        if (rectTop > rectBottom - cornerLength * 2)
                            rectTop = rectBottom - cornerLength * 2;
                    } else if (isBottom) {
                        rectBottom += slideY;
                        if (rectBottom > edgeBottom)
                            rectBottom = edgeBottom;
                        if (rectBottom < rectTop + cornerLength * 2)
                            rectBottom = rectTop + cornerLength * 2;
                    }
                    invalidate();
                    downX = moveX;
                    downY = moveY;

                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isLeft = false;
                isRight = false;
                isTop = false;
                isBottom = false;
                isMove = false;
                isSlideLeft = false;
                isSlideRight = false;
                isSlideTop = false;
                isSlideBottom = false;
                break;
        }
        return true;
    }

    /**
     * 得到裁剪区域的margin值
     */
    public int[] getCutArr() {

        int[] arr = new int[4];
        arr[0] = (int) rectLeft;
        arr[1] = (int) rectTop;
        arr[2] = (int) rectRight;
        arr[3] = (int) rectBottom;
        return arr;
    }

    public void setMeasuredWidth(int measuredWidth) {
        this.measuredWidth = measuredWidth;
    }

    public void setMeasuredHeight(int measuredHeight) {
        this.measuredHeight = measuredHeight;
        initParams();
    }

    public int getRectWidth() {
        return (int) (measuredWidth);
    }

    public int getRectHeight() {
        return (int) (measuredHeight);
    }

    private void initParams() {
        cornerLength = measuredWidth / 12;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (measuredWidth > getDisplay().getWidth()) {
                measuredWidth = getDisplay().getWidth();
            }
            if (measuredHeight > getDisplay().getHeight()) {
                measuredHeight = getDisplay().getHeight();
            }
        } else {
            WindowManager wm = (WindowManager) getContext()
                    .getSystemService(Context.WINDOW_SERVICE);

            int width = wm.getDefaultDisplay().getWidth();
            int height = wm.getDefaultDisplay().getHeight();
            if (measuredWidth > width) {
                measuredWidth = width;
            }
            if (measuredHeight > height) {
                measuredHeight = height;
            }
        }

        rectRight = measuredWidth;
        rectLeft = 0;
        rectTop = edgeTop;
        rectBottom = edgeBottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        paint.setStrokeWidth(dp1);
        //绘制裁剪区域的矩形, 传入margin值来确定大小
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);
        //绘制四条分割线和四个角
        drawLine(canvas, rectLeft, rectTop, rectRight, rectBottom);
    }

    /**
     * 绘制四条分割线和四个角
     */
    private void drawLine(Canvas canvas, float left, float top, float right, float bottom) {

        paint.setStrokeWidth(1);
        //绘制四条分割线
        float startX = (right - left) / 3 + left;
        float startY = top;
        float stopX = (right - left) / 3 + left;
        float stopY = bottom;
        canvas.drawLine(startX, startY, stopX, stopY, paint);

        startX = (right - left) / 3 * 2 + left;
        startY = top;
        stopX = (right - left) / 3 * 2 + left;
        stopY = bottom;
        canvas.drawLine(startX, startY, stopX, stopY, paint);

        startX = left;
        startY = (bottom - top) / 3 + top;
        stopX = right;
        stopY = (bottom - top) / 3 + top;
        canvas.drawLine(startX, startY, stopX, stopY, paint);

        startX = left;
        startY = (bottom - top) / 3 * 2 + top;
        stopX = right;
        stopY = (bottom - top) / 3 * 2 + top;
        canvas.drawLine(startX, startY, stopX, stopY, paint);

        paint.setStrokeWidth(dp3);
        //绘制四个角
        startX = left - dp3 / 2;
        startY = top;
        stopX = left + cornerLength;
        stopY = top;
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        startX = left;
        startY = top;
        stopX = left;
        stopY = top + cornerLength;
        canvas.drawLine(startX, startY, stopX, stopY, paint);

        startX = right + dp3 / 2;
        startY = top;
        stopX = right - cornerLength;
        stopY = top;
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        startX = right;
        startY = top;
        stopX = right;
        stopY = top + cornerLength;
        canvas.drawLine(startX, startY, stopX, stopY, paint);

        startX = left;
        startY = bottom;
        stopX = left;
        stopY = bottom - cornerLength;
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        startX = left - dp3 / 2;
        startY = bottom;
        stopX = left + cornerLength;
        stopY = bottom;
        canvas.drawLine(startX, startY, stopX, stopY, paint);

        startX = right + dp3 / 2;
        startY = bottom;
        stopX = right - cornerLength;
        stopY = bottom;
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        startX = right;
        startY = bottom;
        stopX = right;
        stopY = bottom - cornerLength;
        canvas.drawLine(startX, startY, stopX, stopY, paint);
    }
}