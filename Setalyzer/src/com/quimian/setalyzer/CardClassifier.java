package com.quimian.setalyzer;

import android.graphics.Region;
import boofcv.struct.image.ImageUInt8;

import com.quimian.setalyzer.util.SetCard;

public class CardClassifier {
	SetCard sc = new SetCard();

	public CardClassifier(ImageUInt8 linesImage, Region card) {
		sc.color = SetCard.Color.RED;
		sc.count = 1;
		sc.shade = SetCard.Shade.EMPTY;
		sc.shape = SetCard.Shape.OVAL;
	}

	SetCard getCard() {
		return this.sc;
	}
}
