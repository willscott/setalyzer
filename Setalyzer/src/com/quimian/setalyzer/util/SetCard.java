package com.quimian.setalyzer.util;

import android.graphics.Region;

public class SetCard {
	public SetCard() {
		this.location = null;
	}
	
	public SetCard(Region roi) {
		this.location = roi;
	}

	public enum Color {
		RED,
		BLUE,
		GREEN
	}
	public enum Shape {
		DIAMOND,
		OVAL,
		SQUIGGLE
	}
	public enum Shade {
		EMPTY,
		SHADED,
		FULL
	}
	
	public Region location;
	public Color color;
	public short count;
	public Shape shape;
	public Shade shade;
}
