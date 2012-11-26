package com.quimian.setalyzer.util;

public class SetCard {
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
	
	public Color color;
	public short count;
	public Shape shape;
	public Shade shade;
}
