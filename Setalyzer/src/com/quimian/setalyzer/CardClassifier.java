package com.quimian.setalyzer;

import weka.classifiers.bayes.BayesNet;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;
import boofcv.alg.feature.detect.grid.IntensityHistogram;

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
		((Region)sc.location).setPath(roiPath, boundRegion);

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
		int stride = 3;
		FastVector<Attribute> attributes = new FastVector<Attribute>(3);
		attributes.add(new Attribute("hue"));
		attributes.add(new Attribute("saturation"));
		attributes.add(new Attribute("value"));
		Instances inst = new Instances("colors", attributes, image.getWidth() * image.getHeight() / 9);
		float[] hsv = new float[3];
		for (int x = 0; x < image.getWidth(); x += stride) {
			for (int y = 0; y < image.getHeight(); y += stride) {
				int color = image.getPixel(x, y);
				Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
				double[] vals = new double[3];
				vals[0] = (double)hsv[0];
				vals[1] = (double)hsv[1];
				vals[2] = (double)hsv[2];
				Instance i = new DenseInstance(1, vals);
				inst.add(i);
			}
		}
	}
	
	private void detectCount(Bitmap image) {
		sc.count = 1;
	}
}
