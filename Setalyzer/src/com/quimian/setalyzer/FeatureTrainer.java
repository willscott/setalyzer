package com.quimian.setalyzer;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import boofcv.android.ConvertBitmap;
import boofcv.io.wrapper.images.JpegByteImageSequence;
import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageUInt8;

import com.quimian.setalyzer.util.SetCard;
import com.quimian.setalyzer.util.SubImage;

public class FeatureTrainer extends AsyncTask<String, Void, Void> {
	@Override
	protected Void doInBackground(String... params) {
		try {
			Log.i("Setalyzer Featuer Trainer", "Connecting to " + params[0]);
			Socket s = new Socket(params[0], 9090);
			Log.i("Setalyzer feature trainer", "Connected!");
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			while (true) {
				ArrayList<Float> features = new ArrayList<Float>();

				try {
					SetCard sc = (SetCard)in.readObject();
					Log.i("Setalyzer feature trainer", "card gotten");
					Long length = (Long)in.readObject();
					Log.i("Setalyzer feature trainer", "reading card data of length " + length);
					byte[] image = new byte[length.intValue()];
					in.readFully(image);
					Log.i("setalyzer feature trainer", "Card read.");
					Bitmap img = BitmapFactory.decodeByteArray(image, 0, length.intValue());
					SubImage si = new SubImage(img, new Matrix());
					ArrayList<Float> locs = (ArrayList<Float>)sc.location;
					float[] floc = new float[8];
					for (int i = 0; i < 8; i++) {
						floc[i] = locs.get(i).floatValue();
					}
					Bitmap subimg = si.getSubImage(floc);
					ImageUInt8 boofimg = ConvertBitmap.bitmapToGray(subimg, (ImageUInt8)null, null);
					features.addAll(FeatureExtractor.getFeatures(boofimg, subimg));
					Log.i("setalyzer feature trainer", "Returning " + features.size() + " features for card");
				} catch (EOFException e) {
					continue;
				}
				Log.i("Setalyzer feature trainer", "got card to process!");

				out.writeObject(features);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
