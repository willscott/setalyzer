package com.quimian.setalyzer;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;

import com.quimian.setalyzer.util.SetCard;

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
				try {
					SetCard sc = (SetCard)in.readObject();
					Log.i("Setalyzer feature trainer", "card gotten");
					Long length = (Long)in.readObject();
					Log.i("Setalyzer feature trainer", "reading card data of length " + length);
					byte[] image = new byte[length.intValue()];
					in.readFully(image);
					Log.i("setalyzer feature trainer", "Card read.");
				} catch (EOFException e) {
					continue;
				}
				Log.i("Setalyzer feature trainer", "got card to process!");

				ArrayList<Float> features = new ArrayList<Float>();
				features.add(new Float(42));
				out.writeObject(features);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
