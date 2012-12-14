package com.quimian.setalyzer;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.quimian.setalyzer.util.SetCard;

public class Trainer extends JApplet {
	private static final long serialVersionUID = 3527853849015964123L;
	public static final String directoryName = "/Users/adam/Dropbox/Camera Uploads/setalyzer_images/board";
	
	File currentImage = null;
	File imageDirectory = null;
	ArrayList<File> images = null;
	ListIterator<File> imagesIterator = null;
	short imageIndex = 0;
	SelectableLabel picLabel;
	ArrayList<SetCard> labeledCards = new ArrayList<SetCard>();

	public void init() {
		this.resize(1000, 1000);
		JButton chooser  = new JButton("Select Directory");
		chooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser(directoryName);
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(jfc.showOpenDialog(Trainer.this) == JFileChooser.APPROVE_OPTION) {
					Trainer.this.imageDirectory = jfc.getSelectedFile();
					System.out.println("selected " + Trainer.this.imageDirectory.getAbsolutePath());
					FilenameFilter filter = new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png");
						}
					};
					Trainer.this.images = new ArrayList<File>(Arrays.asList(Trainer.this.imageDirectory.listFiles(filter)));
					Trainer.this.imagesIterator = Trainer.this.images.listIterator();
					if (Trainer.this.imagesIterator.hasNext()) {
						Trainer.this.currentImage = Trainer.this.imagesIterator.next();
						Trainer.this.picLabel.update();
					}
				}
			}
		});
		JButton saver = new JButton("Save");
		saver.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser(Trainer.directoryName);
				if(jfc.showSaveDialog(Trainer.this) == JFileChooser.APPROVE_OPTION) {
					try {
						FileOutputStream fos = new FileOutputStream(jfc.getSelectedFile());
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						oos.writeObject(labeledCards);
						oos.close();
						fos.close();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		getContentPane().setLayout(new FlowLayout());
		getContentPane().add(chooser);
		getContentPane().add(saver);
		this.picLabel = new SelectableLabel(this);
		getContentPane().add(this.picLabel);
	}
	
	public void append() {
		labeledCards.add(this.picLabel.cardClass);
		
	}
	
	private class SelectableLabel extends JLabel {
		private static final long serialVersionUID = -3508520925542779390L;
		private Trainer trainer;
		public SetCard cardClass;
		private boolean recorded = false;

		public SelectableLabel(Trainer t) {
			this.trainer = t;
			this.cardClass = new SetCard();
			this.setEnabled(true);
			this.setFocusable(true);
			this.addKeyListener(new KeyListener() {
				
				@Override
				public void keyReleased(KeyEvent key) {
					if (key.getKeyChar() == '1') {
						SelectableLabel.this.cardClass.count = 1;
						recorded = false;
					} else if (key.getKeyChar() == '2') {
						SelectableLabel.this.cardClass.count = 2;	
						recorded = false;
					} else if (key.getKeyChar() == '3') {
						SelectableLabel.this.cardClass.count = 3;
						recorded = false;
					}
					else if (key.getKeyChar() == 'e') {
						SelectableLabel.this.cardClass.shade = SetCard.Shade.EMPTY;
						recorded = false;
					} else if (key.getKeyChar() == 'f') {
						SelectableLabel.this.cardClass.shade = SetCard.Shade.FULL;
						recorded = false;
					} else if (key.getKeyChar() == 's') {
						SelectableLabel.this.cardClass.shade = SetCard.Shade.SHADED;
						recorded = false;
					}
					else if (key.getKeyChar() == 'r') {
						SelectableLabel.this.cardClass.color = SetCard.Color.RED;
						recorded = false;
					} else if (key.getKeyChar() == 'g') {
						SelectableLabel.this.cardClass.color = SetCard.Color.GREEN;
						recorded = false;
					} else if (key.getKeyChar() == 'b') {
						SelectableLabel.this.cardClass.color = SetCard.Color.BLUE;
						recorded = false;
					}
					else if (key.getKeyChar() == 'o') {
						SelectableLabel.this.cardClass.shape = SetCard.Shape.OVAL;
						recorded = false;
					} else if (key.getKeyChar() == 'd') {
						SelectableLabel.this.cardClass.shape = SetCard.Shape.DIAMOND;
						recorded = false;
					} else if (key.getKeyChar() == 'q') {
						SelectableLabel.this.cardClass.shape = SetCard.Shape.SQUIGGLE;
						recorded = false;
					}
					else if (key.getKeyCode() == KeyEvent.VK_RIGHT) {
						if (!recorded && SelectableLabel.this.cardClass.allAttributesSet()) {
							System.out.println("Recorded!");
							trainer.append();
						}
						else {
							System.out.println("Not recorded because attributes not yet set");
						}
						SelectableLabel.this.setIcon(null);
						if (SelectableLabel.this.trainer.imagesIterator.hasNext()) {
							SelectableLabel.this.trainer.currentImage = SelectableLabel.this.trainer.imagesIterator.next();
							SelectableLabel.this.update();
						}
						else {
							setText("End of images");
						}
					} else if (key.getKeyCode() == KeyEvent.VK_ENTER) {
						if (!recorded && SelectableLabel.this.cardClass.allAttributesSet()) {
							System.out.println("Recorded!");
							trainer.append();
							SelectableLabel.this.update();
							SelectableLabel.this.repaint();
							recorded = true;
						}
						else {
							System.out.println("Not recorded because attributes not yet set");
						}
						
					} else if (key.getKeyCode() == KeyEvent.VK_ESCAPE) {
						System.out.println("Cleared!");
						SelectableLabel.this.cardClass.location = new ArrayList<Float>();
						SelectableLabel.this.repaint();
					}
					System.out.println(SelectableLabel.this.cardClass);
				}
				
				@Override
				public void keyTyped(KeyEvent arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					
				}
			});
			this.addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public void mouseClicked(MouseEvent e) {
					((ArrayList<Float>)SelectableLabel.this.cardClass.location).add(new Float(e.getX()));
					((ArrayList<Float>)SelectableLabel.this.cardClass.location).add(new Float(e.getY()));
					System.out.println(SelectableLabel.this.cardClass.location);
					recorded = false;
					SelectableLabel.this.repaint();
				}
			});
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			ArrayList<Float> location = (ArrayList<Float>)SelectableLabel.this.cardClass.location;
			for (int i=0; i<location.size(); i+=2) {
				int x = Math.round(location.get(i));
				int y = Math.round(location.get(i+1));
				g.setColor(Color.RED);
				g.fillRect(x-2, y-2, 4, 4);
			}
		}
		
		public void update() {
			this.requestFocus();
			this.cardClass = new SetCard();
			this.cardClass.location = new ArrayList<Float>();
			if (this.getIcon() == null) {
				try {
					BufferedImage myPicture = ImageIO.read(trainer.currentImage);
					this.setIcon(new ImageIcon(myPicture));
					this.cardClass.source = trainer.currentImage;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
