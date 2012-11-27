package com.quimian.setalyzer;

import georegression.struct.point.Point2D_F64;

import java.util.ArrayList;
import java.util.List;

import boofcv.struct.image.FactoryImage;
import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageUInt8;

import android.graphics.Path;
import android.graphics.Region;
import android.util.Log;

public class Segmenter {
	public static ImageUInt8 test;

	public static List<Region> segment(ImageUInt8 image) {
		ArrayList<Region> list = new ArrayList<Region>();

		// Downsample
		Segmenter s = new Segmenter(image);
		double threshold = s.getMean();
		Log.i("Setalyzer", "THreshold value is " + threshold);
		ImageUInt8 blurred = boofcv.alg.filter.blur.BlurImageOps.mean(image, null, 5, null);
		ImageUInt8 binary = boofcv.alg.filter.binary.ThresholdImageOps.threshold(blurred, null, (int)threshold, false);
		
		ImageSInt32 out = FactoryImage.create(ImageSInt32.class, image.getWidth(), image.getHeight());
		int numBlobs = 
				boofcv.alg.filter.binary.BinaryImageOps.labelBlobs4(binary, out);
		Log.i("Setalyzer", "Blobs found:" + numBlobs);
		//Segmenter.test = out;
		//for (int i = 0; i < binary.data.length; i++) {
		//	if (binary.data[i] == 1) {
		//		binary.data[i] = (byte) 0xff;
		//	}
		//}
		//Segmenter.test = binary;

		@SuppressWarnings("unchecked")
		List<Point2D_F64>[] points = new ArrayList[numBlobs];
		for (int i = 1; i < numBlobs; i++) {
			//TODO: make fast
			ArrayList<Point2D_F64> inClass = new ArrayList<Point2D_F64>();
			for (int x = 0; x < out.getWidth(); x++) {
				for (int y = 0; y < out.getHeight(); y++) {
					if(out.get(x, y) == i) {
						inClass.add(new Point2D_F64(x, y));
					}
				}
			}
			if (inClass.size() > 10) {
				points[i] = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.findCorners(inClass);
			} else {
				points[i] = null;
			}
		}
		
		// Convert bounding quads into regions.
		for (List<Point2D_F64> quad : points) {
			if (quad == null)
				continue;
			Region r = new Region();
			Path p = null;
			for (Point2D_F64 po : quad) {
				if (p == null) {
					p = new Path();
					p.moveTo((float)po.x, (float)po.y);
				} else
					p.lineTo((float)po.x, (float)po.y);
			}
			p.close();
			r.setPath(p, new Region(0, 0, image.getWidth(), image.getHeight()));
			list.add(r);
		}
		
		return list;
	}

	private ImageUInt8 sample;
	private double sum;

	private Segmenter(ImageUInt8 image) {
		int sampleWidth = 300;

		double scale = sampleWidth / (1.0 * image.getWidth());
		int sampleHeight = (int)(image.getHeight() * scale);
		ImageUInt8 sample = new ImageUInt8(sampleWidth, sampleHeight);
		for (int x = 0; x < sampleWidth; x++) {
			for (int y = 0; y < sampleHeight; y++) {
				int value = image.get((int)(x * scale), (int)(y * scale));
				sum += value;
				sample.set(x, y, value);
			}
		}
		
		this.sample = sample;
	}
	
	private double getMean() {
		return sum / (1.0 * sample.getWidth() * sample.getHeight());
	}
}
