package com.ddddl.opencvdemo.mace;

import android.graphics.Bitmap;

public class FeatherUtil {
    static float mSize = 0.5f;

    public static Bitmap render(Bitmap bitmap) {
        if (bitmap == null) return null;

        final int SIZE = 32768;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int ratio = width > height ? height * SIZE / width : width * SIZE / height;//这里有额外*2^15 用于放大比率；之后的比率使用时需要右移15位，或者/2^15.

        int cx = width >> 1;
        int cy = height >> 1;
        int max = cx * cx + cy * cy;
        int min = (int) (max * (1 - mSize));
        int diff = max - min;// ===>>    int diff = (int)(max * mSize);


        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = pixels[i * width + j];
                int r = (pixel & 0x00ff0000) >> 16;
                int g = (pixel & 0x0000ff00) >> 8;
                int b = (pixel & 0x000000ff);

                int dx = cx - j;
                int dy = cy - i;

                if (width > height) {
                    dx = (dx * ratio) >> 15;
                } else {
                    dy = (dy * ratio) >> 15;
                }

                int dstSq = dx * dx + dy * dy;
                float v = ((float) dstSq / diff) * 255;
                r = (int) (r + v);
                g = (int) (g + v);
                b = (int) (b + v);
                r = (r > 255 ? 255 : (r < 0 ? 0 : r));
                g = (g > 255 ? 255 : (g < 0 ? 0 : g));
                b = (b > 255 ? 255 : (b < 0 ? 0 : b));
                pixels[i * width + j] = (pixel & 0xff000000) + (r << 16) + (g << 8) + b;
            }
        }

        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }
}
