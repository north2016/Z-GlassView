package com.accumulation.imageblurring.app;

import com.accumulation.imageblurring.app.jni.ImageBlur;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends Activity {

	private ImageView image;
	private TextView text;
	// 是否压缩后模糊
	private boolean isDownScale = true;
	private int screenW;
	private int screenH;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		image = (ImageView) findViewById(R.id.picture);
		text = (TextView) findViewById(R.id.text);
		image.setImageResource(R.drawable.picture);
		applyBlur();
	}

	private void applyBlur() {
		image.getViewTreeObserver().addOnPreDrawListener(
				new ViewTreeObserver.OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						image.getViewTreeObserver().removeOnPreDrawListener(
								this);
						image.buildDrawingCache();

						Bitmap bmp = image.getDrawingCache();
						blur(bmp, text);
						return true;
					}
				});
	}

	private void blur(Bitmap bkg, final View view) {

		float scaleFactor = 1;
		float radius = 20;

		if (isDownScale) {
			scaleFactor = 3;// 压缩四倍
			radius = 5;
		}

		Bitmap overlay = Bitmap.createBitmap(
				(int) (view.getMeasuredWidth() / scaleFactor),
				(int) (view.getMeasuredHeight() / scaleFactor),
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(overlay);
		canvas.translate(-view.getLeft() / scaleFactor, -view.getTop()
				/ scaleFactor);
		canvas.scale(1 / scaleFactor, 1 / scaleFactor);
		Paint paint = new Paint();
		paint.setFlags(Paint.FILTER_BITMAP_FLAG);
		canvas.drawBitmap(bkg, 0, 0, paint);

		overlay = doBlurJniBitMap(overlay, (int) radius, true);

		final Bitmap overlay1 = overlay;

		view.setBackground(new BitmapDrawable(getResources(), overlay1));

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenW = dm.widthPixels;
		screenH = dm.heightPixels;

		final int width = overlay1.getWidth();
		final int height = overlay1.getHeight();

		view.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_MOVE:
					int newx = (int) event.getX() * width / screenW;// 等比换算成bitmap上的实际像素点
					int newy = (int) event.getY() * height / screenH;

					// 将触摸点上下6个像素的位置设为透明
					for (int i = -6; i < 6; i++) {
						for (int j = -6; j < 6; j++) {
							if ((newx + i) < width && (newx + i) > 0
									&& (newy + j) > 0 && (newy + j) < screenH) {// 防止越界
								if (Math.sqrt(i * i + j * j) <= 6) {// 擦除以6为半径的圆
									overlay1.setPixel(newx + i, newy + j,
											Color.TRANSPARENT);
								}
							}
						}
					}
					view.setBackground(new BitmapDrawable(getResources(),
							overlay1));
					break;
				}
				return true;
			}
		});

	}

	public static Bitmap doBlurJniBitMap(Bitmap sentBitmap, int radius,
			boolean canReuseInBitmap) {
		Bitmap bitmap;
		if (canReuseInBitmap) {
			bitmap = sentBitmap;
		} else {
			bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
		}

		if (radius < 1) {
			return (null);
		}
		// Jni BitMap
		ImageBlur.blurBitMap(bitmap, radius);
		return (bitmap);
	}

}
