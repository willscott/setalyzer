package com.quimian.setalyzer;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.print.attribute.standard.JobHoldUntil;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.quimian.setalyzer.util.SetCard;

public class Trainer extends JApplet {
	private static final long serialVersionUID = 3527853849015964123L;
	File currentImage = null;
	SelectableLabel picLabel;

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
		getContentPane().setLayout(new FlowLayout());
		getContentPane().add(chooser);
		this.picLabel = new SelectableLabel(this);
		getContentPane().add(this.picLabel);
	}
	
	private class SelectableLabel extends JLabel {
		private static final long serialVersionUID = -3508520925542779390L;
		private Trainer trainer;
		public SetCard cardClass;

		public SelectableLabel(Trainer t) {
			this.trainer = t;
			this.cardClass = new SetCard();
			this.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent key) {
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
						System.out.println("ENter!");
					}
				}
				
				@Override
				public void keyReleased(KeyEvent arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					
				}
			});
		}
		
		public void update() {
			try {
				BufferedImage myPicture = ImageIO.read(trainer.currentImage);
				this.setIcon(new ImageIcon(myPicture));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
