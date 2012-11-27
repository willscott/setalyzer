package com.quimian.setalyzer;

import georegression.struct.line.LineParametric2D_F32;
import georegression.struct.line.LineSegment2D_F32;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import boofcv.android.ConvertBitmap;
import boofcv.struct.image.FactoryImage;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;

import com.quimian.setalyzer.util.SetCard;
import com.quimian.setalyzer.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class SetViewerActivity extends Activity implements SurfaceHolder.Callback, PreviewCallback {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	/**
	 * The camera displayed by this activity.
	 */
	private Camera mCamera;
	
	/**
	 * Storage for boof image conversion.
	 */
	private ImageUInt8 mImage;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(getString(R.string.app_name), "onCreate");
		setContentView(R.layout.activity_set_viewer);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final SurfaceView contentView = (SurfaceView) findViewById(R.id.fullscreen_content);
	
		if (contentView == null) {
			Log.i("Setalyzer", "contentView is null");
		}
		SurfaceHolder holder = contentView.getHolder();
		holder.addCallback(this);
		
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.`	
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
		

		Button button = (Button) findViewById(R.id.dummy_button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleClick(v);
			}
		});
		
	} //end of onCreate
	
	public void handleClick(View v) {
		
//		BitmapFactory.Options opts = new BitmapFactory.Options();
//		opts.inSampleSize = 8;
//		Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath() + "/setgame1.jpg", opts);
//		ImageUInt8 image = ConvertBitmap.bitmapToGray(bmp, (ImageUInt8)null, null); 
//		List<LineParametric2D_F32> lines = 
//				LineDetector.detectLines(image, ImageUInt8.class, ImageSInt16.class);
//		LineDetector.overlayLines(image, lines);
//		bmp = ConvertBitmap.grayToBitmap(image, Bitmap.Config.ARGB_8888);
//		displayImage(bmp);
		
		ImageUInt8 linesImage = mImage.clone();		

		// Segment.
		List<Region> cards = Segmenter.segment(linesImage);
		
		// Classify.
		List<SetCard> setCards = new ArrayList<SetCard>();
		for(Region card: cards) {
			CardClassifier cc = new CardClassifier(linesImage, card);
			setCards.add(cc.getCard());
		}
		
		// Solve.
		List<List<SetCard>> sets = SetFinder.findSets(setCards);
		
		// Display.
		Bitmap bmp = ConvertBitmap.grayToBitmap(linesImage, Bitmap.Config.ARGB_8888);
		
		for(int i = 0; i < sets.size(); i++) {
			drawSet(bmp, sets.get(i), i, sets.size());
		}
		
		displayImage(bmp);
	}
	
	private void drawSet(Bitmap image, List<SetCard> set, int idx, int count) {
		int StripeWidth = 80;
		int[] colors = new int[] {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.BLACK, Color.CYAN};
		double sd = count;

		for (SetCard card : set) {
			Rect bounds = card.location.getBounds();
			for(int d = 0; d < bounds.width() + bounds.height(); d++) {
				double stripePos = (d % StripeWidth)/sd;
				if (stripePos > (idx/sd) && stripePos < ((idx+1)/sd)) {
					for (int p = 0; p < bounds.width(); p++) {
						int x = bounds.left + p;
						int y = bounds.top + d - p;
						if(card.location.contains(x, y)) {
							image.setPixel(x, y, colors[idx]);
						}
					}
				}
			}
		}
	}
		
	private byte[] allocateBuffer() {
		Camera.Size psize = mCamera.getParameters().getPreviewSize();
		int depth = android.graphics.ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat());
		int size = psize.width * psize.height * depth / 8;
		return new byte[size];
	}
	
    private void initializeCamera(SurfaceHolder holder) {
    	if (holder == null || holder.getSurface() == null)  {
    		return;
    	}

    	if (mCamera != null) {
    		surfaceDestroyed(holder);
    	}

    	Log.i("Setalyzer", "initializeCamera");
    	mCamera = Camera.open();

        try {
        	mCamera.setPreviewDisplay(holder);
        	mCamera.setPreviewCallbackWithBuffer(this);
        	mCamera.addCallbackBuffer(allocateBuffer());
    		setCameraDisplayOrientation(this, 0, mCamera);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }		
	}

    @Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		setCameraDisplayOrientation(this, 0, mCamera);

		mImage = null;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		initializeCamera(holder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
        	mCamera.stopPreview();
        	mCamera.setPreviewCallback(null);
        	mCamera.release();
        	mCamera = null;
        }
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (mCamera == null || camera == null) {
			return;
		}
		Camera.Size psize = camera.getParameters().getPreviewSize();
		if (mImage == null) {
			mImage = FactoryImage.create(ImageUInt8.class, psize.width, psize.height);
		}

		// Yeah YUV!
		// Reference on why this is okay: http://stackoverflow.com/questions/5272388/need-help-with-androids-nv21-format
		System.arraycopy(data, 0, mImage.data, 0, mImage.data.length);

		camera.addCallbackBuffer(data);
	}
	
    @Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		Log.i(getString(R.string.app_name), "hiding");

		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	// From: http://stackoverflow.com/questions/4645960/how-to-set-android-camera-orientation-properly
	public static void setCameraDisplayOrientation(Activity activity,
	         int cameraId, android.hardware.Camera camera) {
	     android.hardware.Camera.CameraInfo info =
	             new android.hardware.Camera.CameraInfo();
	     android.hardware.Camera.getCameraInfo(cameraId, info);
	     int rotation = activity.getWindowManager().getDefaultDisplay()
	             .getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     Log.i("Setalyzer", "Resulting display orientation is " + result + " degrees");
	     camera.setDisplayOrientation(result);
	 }
	
	@Override 
	protected void onPause() {
		super.onPause();
	    if (mCamera != null) {
	      mCamera.release();
	      mCamera = null;
	    }
	    
	}
	@Override 
	protected void onDestroy() {
		super.onPause();
	    if (mCamera != null) {
	      mCamera.release();
	    }
	    
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView preview = (SurfaceView) findViewById(R.id.fullscreen_content);
		if (preview != null && preview.getHolder() != null) {
			initializeCamera(preview.getHolder());
		}
	}

	public void displayImage(Bitmap bmp) {
		String bmpTemporaryFile = Environment.getExternalStorageDirectory().getPath() + "/tmp.setalyzer.png";
		try {
		       FileOutputStream out = new FileOutputStream(bmpTemporaryFile);
		       bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
		       e.printStackTrace();
		}
		Intent intent = new Intent(this, OutputViewerActivity.class);
		intent.putExtra("bmpTemporaryFile", bmpTemporaryFile);
		startActivity(intent);
	}
}
