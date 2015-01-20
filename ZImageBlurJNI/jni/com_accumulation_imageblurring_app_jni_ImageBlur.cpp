#include <com_accumulation_imageblurring_app_jni_ImageBlur.h>
#include <ImageBlur.c>
#include <android/log.h>
#include <android/bitmap.h>

 void  Java_com_accumulation_imageblurring_app_jni_ImageBlur_blurBitMap
(JNIEnv *env, jclass obj, jobject bitmapIn, jint r)
{
	AndroidBitmapInfo infoIn;
	void* pixelsIn;
	int ret;

	// Get image info
	if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0)
		return;
	// Check image
	if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888)
		return;
	// Lock all images
	if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0) {
		//AndroidBitmap_lockPixels failed!
	}
	//height width
	int h = infoIn.height;
	int w = infoIn.width;

	//Start
	pixelsIn = StackBlur((int*)pixelsIn, w, h, r);
	//End

	// Unlocks everything
	AndroidBitmap_unlockPixels(env, bitmapIn);
}
