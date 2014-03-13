package client.gui.die;

import java.util.Random;

import javax.swing.Timer;
import javax.swing.JPanel;

import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class Die extends JPanel implements ActionListener{
	
	private static final int WIDTH = 60;
	private static final int HEIGHT = 60;
	private static final int MAX_ROLLS = 10;
	private static final int SPOT_DIAM = 12;
	private static final Random rand = new Random();
	private static final Image[] FACES = new Image[6];
	
	static{
		System.out.println("Setup");
		for( int i=0; i<FACES.length; i++){
			FACES[i] = draw( i+1);
		}
	}
	
	private static Image draw( int face){
		BufferedImage image = new BufferedImage( WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = (Graphics2D) image.getGraphics();
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		g2d.setColor( Color.BLUE);
		g2d.fillRect( 0, 0, WIDTH, HEIGHT);
		g2d.setColor( Color.GREEN);
		g2d.drawRect( 0, 0, WIDTH - 1, HEIGHT - 1);

		if( face==1 || face==3 || face==5){
			face--;
			drawSpot( g2d, 7);
		}
		if( face>0){
			drawSpot( g2d, 1);
			drawSpot( g2d, 6);
			
			switch( face) {
				case 6:
					drawSpot( g2d, 3);
					drawSpot( g2d, 4);
				case 4:
					drawSpot( g2d, 2);
					drawSpot( g2d, 5);
					break;
			}
		}
		return image;
	}

	private static void drawSpot( Graphics2D g2d, int spot) {
		int x=WIDTH/4, y=HEIGHT/4;
		switch( spot){
			case 2: x*=3; break;
			case 3: y*=2; break;
			case 4: x*=3; y*=2; break;
			case 5: y*=3; break;
			case 6: x*=3; y*=3; break;
			case 7: x*=2; y*=2; break;
		}
		g2d.fillOval( x-(SPOT_DIAM/2), y-(SPOT_DIAM/2), SPOT_DIAM, SPOT_DIAM);
	}

	private Timer timer;
	private Parent parent;
	private int roll, target, faceValue;

	public Die( Parent parent) {
		super( true);
		this.parent = parent;
	}
	
	public Die init(){
		setFace();
		timer = new Timer( 125, this);
		setPreferredSize( new Dimension( WIDTH, HEIGHT));
		return this;
	}
	
	public void setResult( int face){
		target = face-1;
	}
	
	public void roll(){
		if( !timer.isRunning()){
			roll = 0;
			timer.start();
		}
	}
	
	private void setFace(){
		faceValue = rand.nextInt(6);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		setFace();
		roll++;
		if( roll>=MAX_ROLLS && faceValue==target){
			timer.stop();
			parent.doneRolling();
		}
		repaint();
	}

	@Override
	public void paintComponent( Graphics g) {
		super.paintComponent( g);
		g.drawImage( FACES[faceValue], 0, 0, getWidth(), getHeight(), null);
	}
}