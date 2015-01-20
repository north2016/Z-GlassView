package com.accumulation.imageblurring.app.jni;

import android.graphics.Bitmap;

public class ImageBlur {
	
    public static native void blurBitMap(Bitmap bitmap, int r);

    static {
        System.loadLibrary("JNI_ImageBlur");
    }
}
