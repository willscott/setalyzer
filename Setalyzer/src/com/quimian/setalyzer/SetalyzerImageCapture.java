package com.quimian.setalyzer;

import java.io.*;

import boofcv.android.ConvertBitmap;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.Log;

public class SetalyzerImageCapture implements PictureCallback {

	public static final String RAW = "raw";
	public static final String POSTVIEW = "postview";
	public static final String JPEG = "jpeg";
	
	Context context;
	String type;
	SetViewerActivity activity;
	
	public SetalyzerImageCapture(Context context, String type, SetViewerActivity activity) {
		    this.context = context;
		    this.type = type;
		    this.activity = activity;
	}
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.i("Setalyzer", "Picture Callback: " + type);
		if (type == POSTVIEW) {
			if (data != null) {
				Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
				if (bmp != null) {
					this.activity.displayImage(bmp);
				}
				else {
					Log.i("Setalyzer", "bmp is null, data.legnth is " + data.length);
				}
			}
			
		}
//		if (type == JPEG) {
//			try {
//				File myFile = new File(Environment.getExternalStorageDirectory().getPath() + "/setalyzer.jpg");
//				myFile.createNewFile();
//				FileOutputStream fOut = new FileOutputStream(myFile);
//				BufferedOutputStream myOutWriter = new BufferedOutputStream(fOut);
//				myOutWriter.write(data);
//				myOutWriter.close();
//				fOut.close();
//			} 
//			catch (Exception e) {
//				Log.e("Setalyzer", "File writing error: " + e);
//			}
//			
//			// Assuming that the file is guaranteed to be readable now... I assume I read my own writes?
//			Log.i("Setalyzer", "opening: " + Environment.getExternalStorageDirectory().getPath() + "/setalyzer.jpg");
//			BitmapFactory.Options options = new BitmapFactory.Options();
//			options.inSampleSize = 8;
//			Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath() + "/setalyzer.jpg", options);
//			Log.i("Setalyzer", "bmp has " + bmp.getByteCount() + " pixels");
//	    	ImageUInt8 image = ConvertBitmap.bitmapToGray(bmp, (ImageUInt8)null, null);
//			ImageSInt16 lines = LineDetector.detectLines(image, ImageUInt8.class, ImageSInt16.class);
			
//	    	Bitmap result = ConvertBitmap.grayToBitmap(lines, Bitmap.Config.ARGB_8888);

			
//			try {
//				File myFile = new File(Environment.getExternalStorageDirectory().getPath() + "/setalyzer_lines.jpg");
//				myFile.createNewFile();
//				FileOutputStream fOut = new FileOutputStream(myFile);
//				BufferedOutputStream myOutWriter = new BufferedOutputStream(fOut);
//				result.compress(Bitmap.CompressFormat.JPEG, 100, myOutWriter);
//				myOutWriter.close();
//				fOut.close();
//			} 
//			catch (Exception e) {
//				Log.e("Setalyzer", "File writing error: " + e);
//			}
			
//			camera.startPreview();
//			Bitmap bmp;
//			if (data == null) {
//				Log.i("Setalyzer", "data is null");
//				camera.startPreview();
//				return;
//			}
//
//			bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
//			if (bmp == null) {
//				Log.i("Setalyzer", "bmp is null");
//			}
//			Log.i("Setalyzer", "bmp has " + bmp.getByteCount() + " pixels");
//			byte[] workBuffer = ConvertBitmap.declareStorage(bmp, null);
//			ImageUInt8 image = null;
//	    	ConvertBitmap.bitmapToGray(bmp, image, workBuffer);
//			ImageUInt8 lines = LineDetector.detectLines(image, ImageUInt8.class, ImageSInt16.class);
//			
//			this.activity.displayImage(lines);
//		}
		Log.i("Setalyzer", "Picture Callback: " + type + " done");
	}
}
