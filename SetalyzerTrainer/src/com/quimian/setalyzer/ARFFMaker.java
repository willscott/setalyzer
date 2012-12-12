package com.quimian.setalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.quimian.setalyzer.util.SetCard;

/**
 * Take a serialized set card data set created by the trainer,
 * and combine it with the feature extractor from Setalyzer to create
 * a weka compatible ARFF file for use in generating a classifier.
 * @author willscott
 *
 */
public class ARFFMaker implements Runnable {
	private Lock lock = new ReentrantLock();
	private Condition signal = lock.newCondition();
	private SetCard card;
	private boolean done = false;
	private ArrayList<Float> feat;
	
	public static void main(String[] argv) {
		if (argv.length == 0) {
			System.err.println("Missing Argument: labeled data");
		}
		File labeledData = new File(argv[0]);
		try {
			FileInputStream fis = new FileInputStream(labeledData);
			ObjectInputStream ois = new ObjectInputStream(fis);
			@SuppressWarnings("unchecked")
			ArrayList<SetCard> data = (ArrayList<SetCard>)ois.readObject();
			ois.close();
			fis.close();
			ARFFMaker maker = new ARFFMaker(data);
			FileOutputStream fos = new FileOutputStream(new File(argv[0] + ".arff"));
			maker.write(fos);
			fos.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private ArrayList<SetCard> cards;
	private ArrayList<String> features;
	public ARFFMaker(ArrayList<SetCard> data) {
		// Set up server for android.
		System.out.println("Setting up web thread.");
		Thread server = new Thread(this);
		server.start();
		
		this.cards = data;
		features = new ArrayList<String>();
		if (this.cards.size() > 0) {
			try {
				ArrayList<Float> test = remoteGetFeatures(this.cards.get(0));
			
				for (int i = 0; i < test.size(); i++) {
					features.add("Feature"+i);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Float> remoteGetFeatures(SetCard c) throws InterruptedException {
		ArrayList<Float> result = null;
		lock.lock();
			while(card != null) {
				signal.await();
			}
			card = c;
			done = false;
			signal.signalAll();
			System.out.println("Waiting for card");
			while (feat == null) {
				signal.await();
			}
			System.out.println("Got Result.");
			result = (ArrayList<Float>)feat.clone();
			card = null;
			feat = null;
			signal.signalAll();
		lock.unlock();
		return result;
	}
	
	public void write(OutputStream os) {
		PrintStream ps = new PrintStream(os);
		ps.println("@relation set-card");

		for (String f : features) {
			ps.println("@attribute " + f + " real");
		}
		ps.println("@attribute color {red, green, blue}");
		ps.println("@attribute shade {full, empty, shaded}");
		ps.println("@attribute count {1, 2, 3}");
		ps.println("@attribute shape {diamond, oval, squiggle}");
		ps.println("@data");
		for (SetCard c : cards) {
			// Print features.
			try {
				ArrayList<Float> data = remoteGetFeatures(c);
				for (Float f : data) {
					ps.print(f);
					ps.print(",");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Print class
			if(c.color == SetCard.Color.RED) ps.print("red");
			else if(c.color == SetCard.Color.GREEN) ps.print("green");
			else ps.print("blue");
			ps.print(",");
			if(c.shade == SetCard.Shade.FULL) ps.print("full");
			else if(c.shade == SetCard.Shade.EMPTY) ps.print("empty");
			else ps.print("shaded");
			ps.print("," + c.count);
			ps.print(",");
			if(c.shape == SetCard.Shape.DIAMOND) ps.print("diamond");
			else if (c.shape == SetCard.Shape.OVAL) ps.print("oval");
			else ps.print("squiggle");
			ps.println();
		}
		ps.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		while (true) {
		try {
			ServerSocket ss = new ServerSocket(9090);
			System.out.println("Waiting for connection");
			Socket client = ss.accept();
			System.out.println("Got Connection from " + client.getRemoteSocketAddress());
			ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(client.getInputStream());
			while (true) {
				// Get Client
				lock.lock();
				while(card == null || done) {
					System.out.println("waiting for card");
					signal.await();
				}
				System.out.println("Alerted!");

				//Process
				out.writeObject(card);
				done = true;
				System.out.println("Wrote out a card!");
				//Write the file.
				
				feat = (ArrayList<Float>)in.readObject();
				System.out.println(feat.get(0));

				signal.signalAll();
				lock.unlock();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		}
	}
}
