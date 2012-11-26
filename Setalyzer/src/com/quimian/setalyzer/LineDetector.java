package com.quimian.setalyzer;

import georegression.struct.line.LineParametric2D_F32;
import georegression.struct.line.LineSegment2D_F32;

import java.util.List;

import android.widget.ImageView;
import android.util.Log;
import boofcv.abst.feature.detect.line.DetectLineHoughFoot;
import boofcv.abst.feature.detect.line.DetectLineHoughFootSubimage;
import boofcv.abst.feature.detect.line.DetectLineHoughPolar;
import boofcv.abst.feature.detect.line.DetectLineSegmentsGridRansac;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.factory.feature.detect.line.FactoryDetectLineAlgs;
import boofcv.gui.feature.ImageLinePanel;
import boofcv.gui.image.ShowImages;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;


public class LineDetector {
	// adjusts edge threshold for identifying pixels belonging to a line
	private static final float edgeThreshold = 35;
	// adjust the maximum number of found lines in the image
	private static final int maxLines = 20;  // 0 -> all

	// adjust the blur radius to be applied before line finding
	private static final int blurRadius = 7;
	
	/**
	 * Detects lines inside the image using different types of Hough detectors
	 *
	 * @param image Input image.
	 * @param imageType Type of image processed by line detector.
	 * @param derivType Type of image derivative.
	 */
	public static<T extends ImageSingleBand, D extends ImageSingleBand>
	List<LineParametric2D_F32> detectLines( T image, Class<T> imageType, Class<D> derivType)	{

		
		T blurred = GeneralizedImageOps.createSingleBand(imageType, image.width, image.height);
		GBlurImageOps.gaussian(image, blurred, -1, blurRadius, null);
		// Comment/uncomment to try a different type of line detector
		DetectLineHoughPolar<T,D> detector = FactoryDetectLineAlgs.houghPolar(3, 30, 2, Math.PI / 180,
				edgeThreshold, maxLines, imageType, derivType);
//		DetectLineHoughFoot<T,D> detector = FactoryDetectLineAlgs.houghFoot(3, 8, 5, edgeThreshold,
//							maxLines, imageType, derivType);
//					DetectLineHoughFootSubimage<T,D> detector = FactoryDetectLineAlgs.houghFootSub(3, 8, 5, edgeThreshold,
//							maxLines, 2, 2, imageType, derivType);

		List<LineParametric2D_F32> found = detector.detect(blurred);
		
		return found;
		
	}
	
	/**
	 * Detects segments inside the image
	 *
	 * @param image Input image.
	 * @param imageType Type of image processed by line detector.
	 * @param derivType Type of image derivative.
	 */
	public static<T extends ImageSingleBand, D extends ImageSingleBand>
	List<LineSegment2D_F32> detectLineSegments( T image,
							 Class<T> imageType ,
							 Class<D> derivType )
	{
 
		// Comment/uncomment to try a different type of line detector
		DetectLineSegmentsGridRansac<T,D> detector = FactoryDetectLineAlgs.lineRansac(40, 30, 2.36, true, imageType, derivType);
 
		List<LineSegment2D_F32> found = detector.detect(image);

		return found;
	}

	public static void overlayLines(ImageUInt8 image, List<LineParametric2D_F32> lines) {
		for (int i=0; i<lines.size(); i++) {
			LineParametric2D_F32 line = lines.get(i);
			for (int t = -1000; t < 1000; t++) {
				int x = Math.round(line.getX() + (t * line.getSlopeX()));
				int y = Math.round(line.getY() + (t * line.getSlopeY()));
				if (image.isInBounds(x, y)) {
					image.set(x, y, 255);
				}
			}
		}
	}
	public static void overlayLineSegments(ImageFloat32 image, List<LineSegment2D_F32> lines) {
		for (int i=0; i<lines.size(); i++) {
			LineSegment2D_F32 line = lines.get(i);
			int x1 = Math.round(line.getA().x);
			int y1 = Math.round(line.getA().y);
			int x2 = Math.round(line.getB().x);
			int y2 = Math.round(line.getB().y);

			int x = x1;
			int y = y1;
			int t = 0;
			
			while ( ((x1 < x2) && x < x2) ||
					((y1 < y2) && y < y2)) {
				x = Math.round(x1 + (t * line.slopeX()));
				y = Math.round(y1 + (t * line.slopeY()));
				if (image.isInBounds(x, y)) {
					image.set(x, y, Integer.MAX_VALUE);
				}
				t++;
			}
		}
	}
}