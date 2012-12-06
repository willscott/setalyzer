package com.quimian.setalyzer.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

public class SubImage {
	float[] dest = {
			0, 0,
			100, 0,
			100, 150,
			0, 150
	};

	Bitmap image;
	Matrix coordinateSpace;
	public SubImage(Bitmap base, Matrix coordinateSpace) {
		this.image = base;
		this.coordinateSpace = coordinateSpace;
	}
	
	/**
	 * Create a subimage of the base bitmap by undistorting a specific Region of Interest.
	 * Note: The roi argument is transformed by the coordinateSpace matrix within this method,
	 * and after calling will be in the Base bitmap coordinate space.
	 * @param roi The quad slice of the bitmap masked by this class to represent in a rectangular bitmap
	 * @return A bitmap of a specific slice of the image.
	 */
	public Bitmap getSubImage(float[] roi) {
		// Rotate 90 degrees if needed.
		double first = Math.sqrt(Math.pow(roi[0] - roi[2], 2) + Math.pow(roi[1] - roi[3], 2));
		double second = Math.sqrt(Math.pow(roi[2] - roi[4], 2) + Math.pow(roi[3] - roi[5], 2));
		if (second < first) {
			float f = roi[0];
			float g = roi[1];
			for (int i = 2; i < 8; i++) {
				roi[i - 2] = roi[i];
			}
			roi[6] = f;
			roi[7] = g;
		}
		
		Log.i("Setaylzer", "transformation mtrx is " + roi[0] +"," + roi[1] + " -> " + roi[2] +","+roi[3] + " -> " + roi[4]+","+roi[5]+" ->" + roi[6] + ","+ roi[7]);

		float[] map = new float[2];
		for (int i = 0; i < 4; i++) {
			map[0] = roi[2*i];
			map[1] = roi[2*i+1];
			this.coordinateSpace.mapPoints(map);
			roi[2 * i] = map[0];
			roi[2 * i + 1] = map[1];
		}

		Log.i("Setaylzer", "transformation mtrx is " + roi[0] +"," + roi[1] + " -> " + roi[2] +","+roi[3] + " -> " + roi[4]+","+roi[5]+" ->" + roi[6] + ","+ roi[7]);
		Matrix transform = new Matrix();
		transform.setPolyToPoly(roi, 0, dest, 0, 4);
		
		return Bitmap.createBitmap(image, 0, 0, 100, 150, transform, true);
	}
/*	
	public Bitmap getTransformedImage(float[] roi) {
		// Rotate 90 degrees if needed.
		double first = Math.sqrt(Math.pow(roi[0] - roi[2], 2) + Math.pow(roi[1] - roi[3], 2));
		double second = Math.sqrt(Math.pow(roi[2] - roi[4], 2) + Math.pow(roi[3] - roi[5], 2));
		if (second < first) {
			float f = roi[0];
			float g = roi[1];
			for (int i = 2; i < 8; i++) {
				roi[i - 2] = roi[i];
			}
			roi[6] = f;
			roi[7] = g;
		}

		
		float[] map = new float[2];
		for (int i = 0; i < 4; i++) {
			map[0] = roi[2*i];
			map[1] = roi[2*i+1];
			this.coordinateSpace.mapPoints(map);
			roi[2 * i] = map[0];
			roi[2 * i + 1] = map[1];
		}
		
		Log.i("Setaylzer", "transformation mtrx is " + roi[0] +"," + roi[1] + " -> " + roi[2] +","+roi[3] + " -> " + roi[4]+","+roi[5]+" ->" + roi[6] + ","+ roi[7]);
		Matrix transform = new Matrix();
		transform.setPolyToPoly(dest, 0, roi, 0, 4);
		
		Bitmap subImage = Bitmap.createBitmap(100, 150, Bitmap.Config.ARGB_8888);
		for (int x = 0; x < 100; x++) {
			for (int y = 0; y < 150; y++) {
				map[0] = x;
				map[1] = y;
				transform.mapPoints(map);
				if (map[0] >= image.getWidth())
					map[0] = image.getWidth() - 1;
				if (map[1] >= image.getHeight())
					map[1] = image.getHeight() -1;
				subImage.setPixel(x, y, image.getPixel((int)map[0], (int)map[1]));
			}
		}

		return subImage;
	}
*/
}
