package com.quimian.setalyzer.util;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class SetCard implements Serializable {
	private static final long serialVersionUID = 5824129235680346935L;

	public SetCard() {
		this.location = null;
	}
	
	public SetCard(Object roi) {
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
	
	// Note: source is optional - used for training.
	public File source;
	public Object location;
	public Color color;
	public short count;
	public Shape shape;
	public Shade shade;

}
