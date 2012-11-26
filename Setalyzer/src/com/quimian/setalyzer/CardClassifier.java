package com.quimian.setalyzer;

import georegression.struct.point.Point2D_F64;

import java.util.List;

import com.quimian.setalyzer.util.SetCard;

import android.graphics.Bitmap;

public class CardClassifier {
	SetCard sc = new SetCard();

	CardClassifier(Bitmap image, List<Point2D_F64> regionOfInterest) {
		sc.color = SetCard.Color.RED;
		sc.count = 1;
		sc.shade = SetCard.Shade.EMPTY;
		sc.shape = SetCard.Shape.OVAL;
	}

	SetCard getCard() {
		return this.sc;
	}
}
