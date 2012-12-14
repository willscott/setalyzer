package com.quimian.setalyzer;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Color;
import boofcv.struct.image.ImageUInt8;

public class FeatureExtractor {	
	public static ArrayList<Float> getFeatures(ImageUInt8 image, Bitmap color) {
		ArrayList<Float> features = new ArrayList<Float>();

		float[] avgs = getImageAvgs(color);
		for (int i = 0; i < avgs.length; i++) {
			features.add(Float.valueOf(avgs[i]));
		}
		// Get Features.
		return features;
	}

	private static float[] getImageAvgs(Bitmap image) {
		// avg of all color - avg of central color - avg of diagonal color
		float[] features = new float[9];
		int width = image.getWidth();
		int height = image.getHeight();
		int x_0 = (int)(width * 0.1);
		int x_f = (int)(width * 0.9); 
		int y_0 = (int)(height * 0.1);
		int y_f = (int)(height * 0.9);
		int x_33 = (int)(width * 0.33);
		int x_50 = (int)(width * 0.5);
		int x_66 = (int)(width * 0.66);
		int y_33 = (int)(height * 0.33);
		int y_50 = (int)(height * 0.5);
		int y_66 = (int)(height * 0.66);
		for (int x = x_0; x < x_f; x++) {
			for (int y = y_0; y < y_f; y++) {
				int px = image.getPixel(x, y);
				int red = Color.red(px);
				int green = Color.green(px);
				int blue = Color.blue(px);
				features[0] += red;
				features[1] += green;
				features[2] += blue;
				if (x_33 < x && x_66 > x && y_33 < y && y_66 > y) {
					features[3] += red;
					features[4] += green;
					features[5] += blue;
					if (x < x_50 == y < y_50) {
						features[6] += red;
						features[7] += green;
						features[8] += blue;
					}
				}
			}
		}
		return features;
	}
}
