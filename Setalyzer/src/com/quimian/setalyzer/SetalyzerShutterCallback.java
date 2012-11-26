package com.quimian.setalyzer;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.util.Log;


public class SetalyzerShutterCallback implements ShutterCallback {

	Context context;
	
	public SetalyzerShutterCallback(Context context) {
		    this.context = context;
	}
	
	public void onShutter() {
		Log.i("Setalyzer", "Shutter Callback!");
	}

}
