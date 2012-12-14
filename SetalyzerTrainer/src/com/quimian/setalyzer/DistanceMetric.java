/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quimian.setalyzer;

import boofcv.abst.feature.associate.GeneralAssociation;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.describe.DescribeRegionPoint;
import boofcv.abst.feature.detect.interest.InterestPointDetector;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.describe.FactoryDescribeRegionPoint;
import boofcv.factory.feature.detect.interest.FactoryInterestPoint;
import boofcv.gui.feature.AssociationPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.FastQueue;
import boofcv.struct.feature.AssociatedIndex;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.feature.TupleDescQueue;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import georegression.struct.point.Point2D_F64;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * After interest points have been detected in two images the next step is to associate the two
 * sets of images so that the relationship can be found.  This is done by computing descriptors for
 * each detected feature and associating them together.  In the code below abstracted interfaces are
 * used to allow different algorithms to be easily used.  The cost of this abstraction is that detector/descriptor
 * specific information is thrown away, potentially slowing down or degrading performance.
 *
 * @author Peter Abeles
 */
public class DistanceMetric<T extends ImageSingleBand, FD extends TupleDesc> {

	// algorithm used to detect interest points
	InterestPointDetector<T> detector;
	// algorithm used to describe each interest point based on local pixels
	DescribeRegionPoint<T, FD> describe;
	// Associated descriptions together by minimizing an error metric
	GeneralAssociation<FD> associate;

	// location of interest points
	List<Point2D_F64> pointsA;
	List<Point2D_F64> pointsB;

	Class<T> imageType;

	public DistanceMetric(InterestPointDetector<T> detector,
								  DescribeRegionPoint<T, FD> describe,
								  GeneralAssociation<FD> associate,
								  Class<T> imageType) {
		this.detector = detector;
		this.describe = describe;
		this.associate = associate;
		this.imageType = imageType;
	}

	/**
	 * Detect and associate point features in the two images.  Display the results.
	 * @return 
	 */
	public double associate( T inputA, T inputB )
	{
		// stores the location of detected interest points
		pointsA = new ArrayList<Point2D_F64>();
		pointsB = new ArrayList<Point2D_F64>();

		// stores the description of detected interest points
		FastQueue<FD> descA = new TupleDescQueue<FD>(describe,true);
		FastQueue<FD> descB = new TupleDescQueue<FD>(describe,true);

		// describe each image using interest points
		describeImage(inputA,pointsA,descA);
		describeImage(inputB,pointsB,descB);

		// Associate  features between the two images
//		associate.setSource(descA);
//		associate.setDestination(descB);
		associate.associate(descA, descB);

		// display the results
//		AssociationPanel panel = new AssociationPanel(20);
//		panel.setAssociation(pointsA,pointsB,associate.getMatches());
//		panel.setImages(imageA,imageB);

//		ShowImages.showWindow(panel,"Associated Features");
		FastQueue<AssociatedIndex> matches = associate.getMatches();
		double mean = 0;
		for (int i=0; i< matches.size(); i++) {
			AssociatedIndex index = matches.get(i);
			mean += index.fitScore;
		}
		mean = mean/matches.size();
		
		return matches.size();
	}

	/**
	 * Detects features inside the two images and computes descriptions at those points.
	 */
	private void describeImage(T input, List<Point2D_F64> points, FastQueue<FD> descs )
	{
		detector.detect(input);
		describe.setImage(input);

		descs.reset();

		for( int i = 0; i < detector.getNumberOfFeatures(); i++ ) {
			// get the feature location info
			Point2D_F64 p = detector.getLocation(i);
			double yaw = detector.getOrientation(i);
			double scale = detector.getScale(i);

			// extract the description and save the results into the provided description
			if( describe.isInBounds(p.x,p.y,yaw,scale)) {
			   describe.process(p.x, p.y, yaw, scale, descs.grow());
				points.add(p.copy());
			}
		}
	}

	public static double distance( ImageFloat32 imageA, ImageFloat32 imageB ) {

		Class imageType = ImageFloat32.class;

		// select which algorithms to use
		InterestPointDetector detector = FactoryInterestPoint.fastHessian(1, 2, 200, 1, 9, 4, 4);
		DescribeRegionPoint describe = FactoryDescribeRegionPoint.surf(true, imageType);
//		DescribeRegionPoint describe = FactoryDescribeRegionPoint.brief(16,512,-1,4,true, imageType);

		ScoreAssociation scorer = FactoryAssociation.defaultScore(describe.getDescriptorType());
		GeneralAssociation associate = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, -1, true);

		// load and match images
		DistanceMetric app = new DistanceMetric(detector,describe,associate,imageType);

//		BufferedImage imageA = UtilImageIO.loadImage("data/evaluation/stitch/kayak_01.jpg");
//		BufferedImage imageB = UtilImageIO.loadImage("data/evaluation/stitch/kayak_03.jpg");
//		BufferedImage imageB = UtilImageIO.loadImage("data/evaluation/particles01.jpg");

		double distance = app.associate(imageA,imageB);
		return distance;
	}
	
	public static void main( String[] argv ) {
		Class imageType = ImageFloat32.class;
	
		BufferedImage imageA = UtilImageIO.loadImage("/Users/adam/android_opencv_workspace/BoofCV/data/evaluation/stitch/kayak_01.jpg");
		BufferedImage imageB = UtilImageIO.loadImage("/Users/adam/android_opencv_workspace/BoofCV/data/evaluation/stitch/kayak_03.jpg");
//		BufferedImage imageB = UtilImageIO.loadImage("/Users/adam/android_opencv_workspace/BoofCV/data/evaluation/particles01.jpg");

		ImageFloat32 inputA = ConvertBufferedImage.convertFromSingle(imageA, null, imageType);
		ImageFloat32 inputB = ConvertBufferedImage.convertFromSingle(imageB, null, imageType);	
		
//		System.out.println(distance(inputA, inputB));
		
		
	}
}
