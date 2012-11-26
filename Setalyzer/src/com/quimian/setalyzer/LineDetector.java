package com.quimian.setalyzer;

import georegression.struct.line.LineParametric2D_F32;

import java.util.List;

import android.widget.ImageView;
import android.util.Log;
import boofcv.abst.feature.detect.line.DetectLineHoughPolar;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.factory.feature.detect.line.FactoryDetectLineAlgs;
import boofcv.gui.feature.ImageLinePanel;
import boofcv.gui.image.ShowImages;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;


public class LineDetector {
	// adjusts edge threshold for identifying pixels belonging to a line
	private static final float edgeThreshold = 25;
	// adjust the maximum number of found lines in the image
	private static final int maxLines = 10;

	/**
	 * Detects lines inside the image using different types of Hough detectors
	 *
	 * @param image Input image.
	 * @param imageType Type of image processed by line detector.
	 * @param derivType Type of image derivative.
	 */
	public static<T extends ImageSingleBand, D extends ImageSingleBand>
	List<LineParametric2D_F32> detectLines( T image, Class<T> imageType, Class<D> derivType)	{

		// Comment/uncomment to try a different type of line detector
		DetectLineHoughPolar<T,D> detector = FactoryDetectLineAlgs.houghPolar(3, 30, 2, Math.PI / 180,
				edgeThreshold, maxLines, imageType, derivType);
		//			DetectLineHoughFoot<T,D> detector = FactoryDetectLineAlgs.houghFoot(3, 8, 5, edgeThreshold,
		//					maxLines, imageType, derivType);
		//			DetectLineHoughFootSubimage<T,D> detector = FactoryDetectLineAlgs.houghFootSub(3, 8, 5, edgeThreshold,
		//					maxLines, 2, 2, imageType, derivType);

		List<LineParametric2D_F32> found = detector.detect(image);
//		D derivX = detector.getDerivX();
//		D derivY = detector.getDerivY();
		
		for (int i=0; i<found.size(); i++) {
			LineParametric2D_F32 line = found.get(i);
			Log.i("setalyzer", "p: " + line.p + ", slope: " + line.slope + ", angle: " + line.getAngle());
		}
		return found;
		
		// display the results
		/*ImageLinePanel gui = new ImageLinePanel();
		gui.setBackground(image);
		gui.setLines(found);
		gui.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));

		ShowImages.showWindow(gui,"Found Lines");*/
	}

	public static void overlayLines(ImageUInt8 image, List<LineParametric2D_F32> lines) {
		for (int i=0; i<lines.size(); i++) {
			LineParametric2D_F32 line = lines.get(i);
			for (int t = -50; t < 50; t++) {
				int x = Math.round(line.getX() + (t * line.getSlopeX()));
				int y = Math.round(line.getY() + (t * line.getSlopeY()));
				image.set(x, y, 255);
			}
		}
	}
}
