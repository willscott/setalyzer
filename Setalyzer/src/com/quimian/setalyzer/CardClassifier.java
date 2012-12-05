package com.quimian.setalyzer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;

import com.quimian.setalyzer.util.SetCard;

public class CardClassifier {
	SetCard sc = new SetCard();
	double confidence;

	public CardClassifier(Bitmap image, float[] roi) {
		/* Bound Calculation. */
		sc.location = new Region();
		Path roiPath = new Path();
		roiPath.moveTo(roi[0], roi[1]);
		roiPath.lineTo(roi[2], roi[3]);
		roiPath.lineTo(roi[4], roi[5]);
		roiPath.lineTo(roi[6], roi[7]);
		roiPath.close();
		RectF bounds = new RectF();
		roiPath.computeBounds(bounds, false);
		Rect roundBounds = new Rect();
		roundBounds.set((int)bounds.left, (int)bounds.top, (int)Math.ceil(bounds.right), (int)Math.ceil(bounds.bottom));
		Region boundRegion = new Region(roundBounds);
		sc.location.setPath(roiPath, boundRegion);

		/* Card Creation. */
		sc.color = SetCard.Color.RED;
		sc.count = 1;
		sc.shade = SetCard.Shade.EMPTY;
		sc.shape = SetCard.Shape.OVAL;
		confidence = 0.0;

		detectColor(image);
		detectCount(image);
	}

	double getConfidence() {
		return confidence;
	}
	
	SetCard getCard() {
		return this.sc;
	}
	
	/**
	 * Check a diagonal to search for a 'lightest' pixel as reference.
	 * then scan a sample of the center of the image, look at offset from reference..
	 * 
	 * @param image
	 */
	private void detectColor(Bitmap image) {
		int light = 0;
		int refLight = 0;
		int dark = Integer.MAX_VALUE;
		int refDark = Integer.MAX_VALUE;
		int padding = image.getHeight() / 10;
		double aspect = image.getWidth() / (1.0 * image.getHeight());
		int stride = 3;
		for (int d = padding; d < image.getHeight() - padding; d += stride) {
			int x = (int)(d * aspect);
			int px = image.getPixel(x, d);
			int val = Color.red(px) + Color.green(px) + Color.blue(px);
			if (val > refLight) {
				light = px;
				refLight = val;
			}
			if (val < refDark) {
				dark = px;
				refDark = val;
			}
		}
		
		int red = 0, green = 0, blue = 0;
		int white_threshold = (int)(light - (light - dark)*0.95);
		int r, g, b;
		int x_0 = image.getWidth() / 3;
		int width = x_0 + image.getWidth() / 3;
		int y_0 = image.getHeight() / 3;
		int height = y_0 + image.getHeight() / 3;

		for (int x = x_0; x < width; x += stride) {
			for (int y = y_0; y < height; y += stride) {
				int px = image.getPixel(x, y);
				if (px > white_threshold)
					continue;
				px += (Color.WHITE - light);
				r = Color.red(px);
				g = Color.green(px);
				b = Color.blue(px);
				if (r > g && r > b) {
					red++;
				} else if (g > r && g > b) {
					green++;
				} else {
					blue++;
				}
			}
		}
		Log.i("Setalyzer", "Histogram winners were red=" + red + " green=" + green + " blue=" + blue);
		if (red > green && red > blue) {
			sc.color = SetCard.Color.RED;
			Log.i("setalyzer", "region is red");
		} else if (green > red && green > blue) {
			sc.color = SetCard.Color.GREEN;
			Log.i("setalyzer", "region is green");
		} else {
			sc.color = SetCard.Color.BLUE;
			Log.i("setalyzer", "region is blue");
		}
	}
	
	private void detectCount(Bitmap image) {
		sc.count = 1;
	}
}
