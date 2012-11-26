package com.quimian.setalyzer;

import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.quimian.setalyzer.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class SetViewerActivity extends Activity implements TextureView.SurfaceTextureListener {
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(getString(R.string.app_name), "onCreate");
		setContentView(R.layout.activity_set_viewer);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final TextureView contentView = (TextureView) findViewById(R.id.fullscreen_content);
		
		if (contentView == null) {
			Log.i("Setalyzer", "contentView is null");
		}
        contentView.setSurfaceTextureListener(this);

//        setContentView(contentView);
//		contentView.getHolder().addCallback(this);
//		mCamera = Camera.open();
//		try {
//			mCamera.setPreviewDisplay(contentView.getHolder());
//		} catch (IOException e) {
//			Log.e(getString(R.string.app_name), "Failed to open camera");
//		}
//		mCamera.startPreview();
		
		
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
		
		
		// Setup handlers to grab pictures when they're taken
//		final PictureCallback rawHandler = new SetalyzerImageCapture(getApplicationContext(), SetalyzerImageCapture.RAW, this);
//		final PictureCallback postviewHandler = new SetalyzerImageCapture(getApplicationContext(), SetalyzerImageCapture.POSTVIEW, this);
//		final PictureCallback jpegHandler = new SetalyzerImageCapture(getApplicationContext(), SetalyzerImageCapture.JPEG, this);
//		final ShutterCallback shutterHandler = new SetalyzerShutterCallback(getApplicationContext());

		Button button = (Button) findViewById(R.id.dummy_button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				mCamera.takePicture(shutterHandler, rawHandler, postviewHandler, jpegHandler);
				Bitmap bmp = contentView.getBitmap();
				displayImage(bmp);
				
			}
		});
		
	} //end of onCreate
	
	
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		initializeCamera(surface);
        
    }

    private void initializeCamera(SurfaceTexture surface) {
    	Log.i("Setalyzer", "initializeCamera");
    	mCamera = Camera.open();

        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }		
	}


	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCamera != null) {
        	mCamera.stopPreview();
        	mCamera.setPreviewCallback(null);
        	mCamera.release();
        	mCamera = null;
        }
        return true;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Invoked every time there's a new Camera preview frame
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

//	@Override
//	public void surfaceChanged(SurfaceHolder holder, int format, int width,
//			int height) {
//		Camera.Parameters parameters = mCamera.getParameters();
//
//	    List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
//	    Camera.Size max = previewSizes.get(0);
//	    
//		Log.i("setalyzer","size is " + max.width + "x" + max.height);
//		parameters.setPreviewSize(max.width, max.height);
//	    mCamera.setParameters(parameters);
//	    setCameraDisplayOrientation(this, 0, mCamera);
//
//	    mCamera.startPreview();
//	}
	
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

//	@Override
//	public void surfaceCreated(SurfaceHolder holder) {
//		if (mCamera != null) {
//			mCamera.stopPreview();
//			mCamera.release();
//		}
//		Log.i(getString(R.string.app_name), "Opening camera");
//
//		mCamera = Camera.open();
//
//		// Set the previewSize of the camera by getting supported preview sizes and picking one
//		Camera.Parameters parameters = mCamera.getParameters();
//		List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
//	    Camera.Size max = previewSizes.get(previewSizes.size()-1);
//		Log.i("Setalyzer","size is " + max.width + "x" + max.height);
//		parameters.setPreviewSize(max.width, max.height);
//		
//	    mCamera.setParameters(parameters);
//	    
//	    setCameraDisplayOrientation(this, 0, mCamera);
//	
//	    holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//	    
//		try {
//			mCamera.setPreviewDisplay(holder);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		Log.i("Setalyzer", "About to startPreview()");
//		mCamera.startPreview();
//	}
//
//	@Override
//	public void surfaceDestroyed(SurfaceHolder holder) {
//		if (mCamera != null) {
//			mCamera.stopPreview();
//			mCamera.release();
//			mCamera = null;
//		}
//	}
	
	@Override 
	protected void onPause() {
		super.onPause();
	    if (mCamera != null) {
	      mCamera.release();
	    }
	    
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		TextureView previewView = (TextureView) findViewById(R.id.fullscreen_content);
		SurfaceTexture previewSurfaceTexture = previewView.getSurfaceTexture();
		if (previewSurfaceTexture != null) {
			initializeCamera(previewSurfaceTexture);
		}
	}

	public void displayImage(Bitmap bmp) {
		String bmpTemporaryFile = Environment.getExternalStorageDirectory().getPath() + "/tmp.setalyzer.bmp";
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