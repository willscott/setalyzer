package com.quimian.setalyzer;

import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

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

		detectCount(image);
	}

	double getConfidence() {
		return confidence;
	}
	
	SetCard getCard() {
		return this.sc;
	}
	
	private void detectCount(Bitmap image) {
		sc.count = 1;
	}
}
