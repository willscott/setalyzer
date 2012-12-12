package com.quimian.setalyzer;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

import com.quimian.setalyzer.util.SetCard;

public class Trainer extends JApplet {
	private static final long serialVersionUID = 3527853849015964123L;
	File currentImage = null;
	SelectableLabel picLabel;
	ArrayList<SetCard> labeledCards = new ArrayList<SetCard>();

	public void init() {
		JButton chooser  = new JButton("Select file");
		chooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				if(jfc.showOpenDialog(Trainer.this) == JFileChooser.APPROVE_OPTION) {
					Trainer.this.currentImage = jfc.getSelectedFile();
					Trainer.this.picLabel.update();
				}
			}
		});
		JButton saver = new JButton("Save");
		saver.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
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
					} else if (key.getKeyChar() == '2') {
						SelectableLabel.this.cardClass.count = 2;	
					} else if (key.getKeyChar() == '3') {
						SelectableLabel.this.cardClass.count = 3;
					}
					else if (key.getKeyChar() == 'e') {
						SelectableLabel.this.cardClass.shade = SetCard.Shade.EMPTY;
					} else if (key.getKeyChar() == 'f') {
						SelectableLabel.this.cardClass.shade = SetCard.Shade.FULL;
					} else if (key.getKeyChar() == 'v') {
						SelectableLabel.this.cardClass.shade = SetCard.Shade.SHADED;
					}
					else if (key.getKeyChar() == 'r') {
						SelectableLabel.this.cardClass.color = SetCard.Color.RED;
					} else if (key.getKeyChar() == 'g') {
						SelectableLabel.this.cardClass.color = SetCard.Color.GREEN;
					} else if (key.getKeyChar() == 'b') {
						SelectableLabel.this.cardClass.color = SetCard.Color.BLUE;
					}
					else if (key.getKeyChar() == 'w') {
						SelectableLabel.this.cardClass.shape = SetCard.Shape.OVAL;
					} else if (key.getKeyChar() == 'd') {
						SelectableLabel.this.cardClass.shape = SetCard.Shape.DIAMOND;
					} else if (key.getKeyChar() == 'c') {
						SelectableLabel.this.cardClass.shape = SetCard.Shape.SQUIGGLE;
					}
					else if (key.getKeyCode() == KeyEvent.VK_ENTER) {
						System.out.println("Recorded!");
						trainer.append();
						SelectableLabel.this.setIcon(null);
					}
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
		}
		
		public void update() {
			this.requestFocus();
			this.cardClass = new SetCard();
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
