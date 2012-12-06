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
		// Map through coordinate space transform.
		float[] map = new float[2];
		for (int i = 0; i < 4; i++) {
			map[0] = roi[2*i];
			map[1] = roi[2*i+1];
			this.coordinateSpace.mapPoints(map);
			roi[2 * i] = map[0];
			roi[2 * i + 1] = map[1];
		}

		// Rotate the upper left corner first.
		int min = Integer.MAX_VALUE;
		int min_idx = 0;
		for (int i = 0; i < 4; i++) {
			if (roi[2*i] + roi[2*i+1] < min) {
				min = (int)(roi[2*i] + roi[2*i+1]);
				min_idx = i;
			}
		}
		float[] mappedROI = new float[8];
		for (int i = 0; i < 8; i++) {
			mappedROI[i] = roi[(i + 2*min_idx) % 8];
		}

		Matrix transform = new Matrix();
		transform.setPolyToPoly(mappedROI, 0, dest, 0, 4);
		
		return Bitmap.createBitmap(image, 0, 0, 100, 150, transform, true);
	}
}
