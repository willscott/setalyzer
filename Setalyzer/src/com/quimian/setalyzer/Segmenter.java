package com.quimian.setalyzer;

import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import boofcv.abst.feature.detect.edge.DetectEdgeContour;
import boofcv.alg.misc.GPixelMath;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.factory.feature.detect.edge.FactoryDetectEdgeContour;
import boofcv.struct.image.FactoryImage;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageUInt8;

import android.graphics.Path;
import android.graphics.Region;
import android.util.Log;

public class Segmenter {
	public static ImageUInt8 test;
	
	private static final double ASPECT_RATIO_TOLERANCE = 0.80;    // aspect ratios within this percent of the target are card shaped
	private	static final double TARGET_ASPECT_RATIO = 0.64;		 // aspect ratio of a set card
	private static final double SLOPE_PARALLEL_TOLERANCE = 0.70;  // slopes within this percent are parallel
	private static final int 	EDGE_SIZE_THRESHOLD = 100;
	private static final int 	BLOB_SIZE_THRESHOLD = 10;
	private static final double CARD_AREA_TOLERANCE = 0.80;

	private static final int BLUR_RADIUS = 5;

	public static List<List<Point2D_F64>> segment(ImageUInt8 image) {
		ArrayList<float[]> thresholdRegionList = new ArrayList<float[]>();
		ArrayList<float[]> cannyRegionList = new ArrayList<float[]>();

		Segmenter s = new Segmenter(image);

		List<List<Point2D_F64>> regions = s.getBlobRegions();
		if (regions == null) {
			return null;
		}
		
		// Convert bounding quads into regions.
		for (List<Point2D_F64> quad : regions) {
			if (quad == null) {
				continue;
			}
			thresholdRegionList.add(convertQuadToRegion(quad, image.getWidth(), image.getHeight()));
		}
		
		return regions;
//		return thresholdRegionList;
//		return cannyRegionList;
	}
	
	// convert a blob image into quadrilaterals bounding each of the blobs
	public static List<List<Point2D_F64>> quadsFromBlobs(ImageSInt32 blobImage, int numBlobs) {
		@SuppressWarnings("unchecked")
		List<List<Point2D_F64>> points = new ArrayList(); 
		for (int i = 1; i < numBlobs; i++) {
			//TODO: make fast
			ArrayList<Point2D_F64> inClass = new ArrayList<Point2D_F64>();
			for (int x = 0; x < blobImage.getWidth(); x++) {
				for (int y = 0; y < blobImage.getHeight(); y++) {
					if(blobImage.get(x, y) == i) {
						inClass.add(new Point2D_F64(x, y));
					}
				}
			}
			// Transform blobs into quadrilaterals, ignoring blobs of very small size
			if (inClass.size() > BLOB_SIZE_THRESHOLD) {
				points.add(boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.findCorners(inClass));
			}
		}
		return points;
	}
	
	// Assumes that the points are ordered going around the quadrilateral
	private static double areaOfQuad(List<Point2D_F64> quad) {
		double area1 = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.area(quad.get(0), quad.get(2), quad.get(1));
		double area2 = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.area(quad.get(0), quad.get(2), quad.get(3));
		
		return area1 + area2;
	}
	
	public static float[] convertQuadToRegion(List<Point2D_F64> quad, int width, int height) {
		if (quad == null)
			return null;
		float[] f = new float[2 * quad.size()];
		for (int i = 0; i < quad.size(); i++) {
			f[2*i] = (float)quad.get(i).x;
			f[2*i + 1] = (float)quad.get(i).y;
		}
		return f;
	}

	private ImageUInt8 sample;
	private double mean;

	private ImageUInt8 blurred;
	private List<List<Point2D_F64>> blobQuads;
	private int numBlobs;

	private List<List<Point2D_I32>> prunedCannyEdgeList;

	public Segmenter(ImageUInt8 gray) {
		
		// Downsample image to smaller size
		int sampleWidth = 300;
		double scale = (1.0 * gray.getWidth()) / sampleWidth;
		int sampleHeight = (int)(gray.getHeight() * scale);
		ImageUInt8 sample = new ImageUInt8(sampleWidth, sampleHeight);
		boofcv.alg.distort.DistortImageOps.scale(gray, sample, boofcv.alg.interpolate.TypeInterpolate.NEAREST_NEIGHBOR);
		
		// Calculate mean
		this.mean = GPixelMath.sum(gray)/(gray.width*gray.height);
		
		// Blur image and store blurred image
		this.blurred = boofcv.alg.filter.blur.BlurImageOps.mean(sample, null, BLUR_RADIUS, null);
		
		// Create binary image and label blobs for threshold-based segmentation
//		Log.i("Setalyzer", "THreshold value is " + this.mean);
		// Threshold to generate binary image
		ImageUInt8 binary = boofcv.alg.filter.binary.ThresholdImageOps.threshold(this.blurred, null, (int)this.mean, false);
		ImageSInt32 blobImage = FactoryImage.create(ImageSInt32.class, sample.getWidth(), sample.getHeight());
		this.numBlobs = boofcv.alg.filter.binary.BinaryImageOps.labelBlobs4(binary, blobImage);
		this.blobQuads = quadsFromBlobs(blobImage, numBlobs);
		
		// Canny detect edges and store them
		// Dynamic canny edge, which sets the threshold as a function of the image's edge intensity
		DetectEdgeContour<ImageUInt8> cannyD =
				FactoryDetectEdgeContour.canny(0.05,0.15,true,ImageUInt8.class,ImageSInt16.class);
		cannyD.process(blurred);
		List<List<Point2D_I32>> edges = cannyD.getContours();
		
		// Prune edges which are obviously not cards
		List<List<Point2D_I32>> prunedEdgeList = new ArrayList<List<Point2D_I32>>();
		for (List<Point2D_I32> edge: edges) {
			// Disregard tiny contours
			if (edge.size() < EDGE_SIZE_THRESHOLD) {
				continue;
			}
			// Disregard shapes with blatantly non-card shapes
			if (!isCardShaped(edge)) {
				continue;
			}
			prunedEdgeList.add(edge);
		}	
		this.prunedCannyEdgeList = prunedEdgeList;
		this.sample = sample;
	}
	
	private double getMean() {
		return mean;
	}
	
	public static boolean topNSimilarSized(int[] areas, int n) {
		int sum = 0;
		int lastIndex = areas.length - 1;
		for (int i=0; i<n; i++) {
			sum += areas[lastIndex-i];
		}
		double average = (double)sum/(double)n;
		
		for (int i=0; i<n; i++) {
			if (Math.min(areas[lastIndex-i], average) / Math.max(areas[lastIndex-i],  average) < CARD_AREA_TOLERANCE) {
				return false;
			}
		}
		return true;
	}

	public static int areaOfRegion(List<Point2D_F64> list) {
		List<Point2D_F64> quad;
		try {
			quad = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.findCorners(list);
		}
		catch (Exception e) {
			System.out.println("finding corners of bounding quadrilateral failed");
			return -1;
		}
		
		double triangleArea0 = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.area(quad.get(0), quad.get(1), quad.get(2));
		double triangleArea1 = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.area(quad.get(0), quad.get(3), quad.get(2));
		
		return (int) Math.round(triangleArea1 + triangleArea0);
		
	}

	public static boolean isCardShaped(List<Point2D_I32> contour) {
		// turn it into a quad
		List<Point2D_F64> f64Contour = new ArrayList<Point2D_F64>();
		for (Point2D_I32 point: contour) {
			f64Contour.add(new Point2D_F64(point.x, point.y));
		}
		
		List<Point2D_F64> quad;
		try {
			quad = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.findCorners(f64Contour);
		}
		catch (Exception e) {
			System.out.println("finding corners of bounding quadrilateral failed");
			return false;
		}
	
		Double ratio = aspectRatio(quad);
		if (ratio == null) {
			return false;
		}
		if (Math.min(ratio, TARGET_ASPECT_RATIO) / Math.max(ratio, TARGET_ASPECT_RATIO) < ASPECT_RATIO_TOLERANCE ) {
			return true;
		}
		return false;
	}

	private static Double aspectRatio(List<Point2D_F64> quad) {
		Double d0 = euclidianDistance(quad.get(0), quad.get(1));
		Double d1 = euclidianDistance(quad.get(0), quad.get(2));
		Double d2 = euclidianDistance(quad.get(1), quad.get(3));
		Double d3 = euclidianDistance(quad.get(3), quad.get(0));
		
		Double s0 = slope(quad.get(0), quad.get(1));
		Double s1 = slope(quad.get(0), quad.get(2));
		Double s2 = slope(quad.get(1), quad.get(3));
		Double s3 = slope(quad.get(3), quad.get(0));
		
		// ensure that things are roughly parallel - slopes of opposing sides are similar
		if (Math.min(s0,s2) / Math.max(s0, s2) > SLOPE_PARALLEL_TOLERANCE) {
			return null;
		}
		if (Math.min(s1,s3) / Math.max(s1, s3) > SLOPE_PARALLEL_TOLERANCE) {
			return null;
		}
		
		// average the lengths of opposing sides
		Double meanSide0 = (d0+d2)/2;
		Double meanSide1 = (d1+d3)/2;
		
		// return the ratio of the (averaged) sides as the aspect ratio
		return Math.max(meanSide0, meanSide1) / Math.min(meanSide0, meanSide1);
	}

	private static Double slope(Point2D_F64 p0, Point2D_F64 p1) {
		Double rise = p0.y - p1.y;
		Double run = p0.x - p1.x;
		return rise/run;
	}

	private static double euclidianDistance(Point2D_F64 p0, Point2D_F64 p1) {
		return Math.sqrt(Math.pow(p0.x - p1.x, 2) + Math.pow(p0.y - p1.y, 2));
	}

	public List<List<Point2D_F64>> getBlobRegions() {
		return blobQuads;
//		if (this.blobQuads.size() < 9) {
//			System.out.println("blobs can't find enough things that might be cards");
//		}
//		else {
//		
//			// See whether the top 9/12/15 ROIs by size are roughly the same size 
//			// Calculate areas and sort
//			int[] areas = new int[blobQuads.size()];
//			for (int i=0; i<blobQuads.size(); i++) {
//				areas[i] = Segmenter.areaOfRegion(blobQuads.get(i));
//			}
//			Arrays.sort(areas);
//
//			// From the top, see if they're all within tolerance from the mean
//			int similarSizedLargest = -1;
//			if (blobQuads.size() >= 15 && Segmenter.topNSimilarSized(areas, 15)) {
//				similarSizedLargest = 15;
//			}
//			else if (blobQuads.size() >= 12 && Segmenter.topNSimilarSized(areas, 12)) {
//				similarSizedLargest = 12;
//			}
//			else if (Segmenter.topNSimilarSized(areas, 9)) {
//				similarSizedLargest = 9;
//			}
//
//			if (similarSizedLargest != -1) {
//				List<List<Point2D_F64>> finalEdgeList = blobQuads.subList(blobQuads.size()-similarSizedLargest, blobQuads.size()-1);
//				return finalEdgeList;
//			}
//		}
//		return null;
	}

}
