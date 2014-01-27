package client.gui;

import java.util.ArrayList;

import javax.swing.Timer;
import javax.swing.JPanel;
import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;

import client.gui.tiles.Hex;
import static common.Constants.HEX_SIZE;
import static common.Constants.MAX_HEXES;
import static common.Constants.LOCK_SIZE;
import static common.Constants.SPIRAL_DELAY;
import static common.Constants.HEX_BOARD_SIZE;
import static common.Constants.BOARD_LOAD_ROW;
import static common.Constants.BOARD_LOAD_COL;
import static common.Constants.BOARD_TOP_PADDING;
import static common.Constants.HEX_MOVE_DISTANCE;
import static common.Constants.BOARD_WIDTH_SEGMENT;
import static common.Constants.BOARD_HEIGHT_SEGMENT;

@SuppressWarnings("serial")
public class Board extends JPanel{
	
	private static Image image;
	private static final Polygon HEX_OUTLINE;
	static{
		int w = (int) (HEX_SIZE.getWidth()/4)+1;
		int h = (int) (HEX_SIZE.getHeight()/2)+2;
		HEX_OUTLINE = new Polygon( new int[]{w,0,w,w*3,w*4,w*3}, new int[]{h*2,h,0,0,h,h*2}, 6);
		File file = new File( "Resources\\Extra\\-n Woodboard.jpg");
		try {
			image = ImageIO.read( file);
		} catch ( IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean interactWithHexes = false;
	
	private Timer timer;
	private MouseInput mouseInput;
	private ArrayList< Rectangle> lockList;
	private Rectangle hexLock;
	int heightSegment = (int) ((HEX_BOARD_SIZE.getHeight())/BOARD_HEIGHT_SEGMENT);
	int widthSegment = (int) ((HEX_BOARD_SIZE.getWidth())/BOARD_WIDTH_SEGMENT);
	
	public Board( LayoutManager layout, boolean isDoubleBuffered){
		super( layout, isDoubleBuffered);
	}
	
	protected void init(){
		mouseInput = new MouseInput();
		addMouseListener( mouseInput);
		addMouseMotionListener( mouseInput);
		addMouseWheelListener( mouseInput);
		lockList = new ArrayList<>();
		hexLock = new Rectangle( (int)(8+HEX_SIZE.getWidth()/2-LOCK_SIZE/2), (int)(8+HEX_SIZE.getHeight()/2-LOCK_SIZE/2), LOCK_SIZE, LOCK_SIZE);
		for( int i=0; i<MAX_HEXES; i++){
			addHex( 8, 8);
		}
	}
	
	public void addHex( int x, int y){
		Hex hex = new Hex();
		hex.addMouseListener( mouseInput);
		hex.addMouseMotionListener( mouseInput);
		hex.setBounds( x, y, HEX_SIZE.width, HEX_SIZE.height);
		hex.setLockArea( hexLock);
		hex.init();
		add( hex);
		revalidate();
		repaint();
	}
	
	@Override
	public void paintComponent( Graphics g){
		super.paintComponent( g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.drawImage( image, 0, 0, getWidth(), getHeight(), null);
		int x=0, y=0;
		Stroke old = g2d.getStroke();
		g2d.setStroke( new BasicStroke( 5));
		g2d.setColor( Color.BLACK);
		HEX_OUTLINE.translate( 8-2, 8-3);
		g2d.drawPolygon( HEX_OUTLINE);
		HEX_OUTLINE.translate( -(8-2), -(8-3));
		for( int ring=0; ring<BOARD_LOAD_ROW.length; ring++){
			for( int count=0; count<BOARD_LOAD_ROW[ring].length; count++){
				x = (widthSegment*BOARD_LOAD_COL[ring][count]);
				y = (heightSegment*BOARD_LOAD_ROW[ring][count])+BOARD_TOP_PADDING;
				HEX_OUTLINE.translate( ((int) (x-HEX_SIZE.getWidth()/2))-2, ((int) (y-HEX_SIZE.getHeight()/2)-3));
				g2d.drawPolygon( HEX_OUTLINE);
				HEX_OUTLINE.translate( -((int) (x-HEX_SIZE.getWidth()/2)-2), -((int) (y-HEX_SIZE.getHeight()/2)-3));
			}
		}
		g2d.setStroke( old);
	}
	
	private class SpiralPlacement implements ActionListener{

		private Hex hex = null;
		private int ring = 0, count = 0;
		private double slope = 0, intercept = 0;
		private int hexCount = getComponentCount()-1;
		private int x=0, y=0, xStart=0, yStart=0, xTemp=-1;
		
		@Override
		public void actionPerformed( ActionEvent e) {
			if( xTemp>=0){
				hex.setLocation( xTemp, (int)(slope*xTemp+intercept));
				xTemp+=HEX_MOVE_DISTANCE;
				if( xTemp>=x-HEX_SIZE.width/2){
					xTemp=-1;
					hex.setLocation( x-HEX_SIZE.width/2, y-HEX_SIZE.height/2);
				}
				repaint();
			}else if( ring<BOARD_LOAD_ROW.length){
				if( count<BOARD_LOAD_ROW[ring].length){
					hex = (Hex) getComponent( hexCount); 
					x = (widthSegment*BOARD_LOAD_COL[ring][count]);
					y = (heightSegment*BOARD_LOAD_ROW[ring][count])+BOARD_TOP_PADDING;
					hex.setLockArea( x-LOCK_SIZE/2, y-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
					xTemp = xStart = hex.getX();
					yStart = hex.getY();
					slope = (y-yStart)/(double)(x-xStart);
					intercept = yStart-slope*xStart;
					hexCount--;
					count++;
				}else{
					count = 0;
					ring++;
				}
			}else{
				timer.stop();
				interactWithHexes = true;
			}
		}
	}
	
	private class MouseInput extends MouseAdapter{

		private Rectangle bound, boardBound;
		private int xDiff, yDiff;
		private int xPressed, yPressed;

		@Override
	    public void mouseDragged(MouseEvent e){
			if(	e.getSource() instanceof Hex && interactWithHexes){
				Hex hex = (Hex)e.getSource();
				boardBound = getBounds();
				bound = new Rectangle( hex.getBounds());
				xDiff = e.getX() - xPressed;
				yDiff = e.getY() - yPressed;
				if( !hex.canLock( xDiff, yDiff)){
					bound.translate( xDiff, 0);
					if( !boardBound.contains( bound)){
						bound.translate( -xDiff, 0);
					}
					bound.translate( 0, yDiff);
					if( !boardBound.contains( bound)){
						bound.translate( 0, -yDiff);
					}
					if( checkLock( hexLock, bound)){
						hex.setLockArea( hexLock);
					}else{
						Rectangle lock = null;
						for( int i=0; i<lockList.size(); i++){
							lock = lockList.get( i);
							if( checkLock( lock, bound)){
								hex.setLockArea( lockList.remove( i));
							}
						}
					}
					if( hex.hasLock() && !hex.canLock( xDiff, yDiff)){
						Rectangle temp = hex.removeLock();
						if( !temp.equals( hexLock)){
							lockList.add( temp);
						}
					}
					hex.setBounds( bound);
				}
			}
		}
		
		private boolean checkLock( Rectangle lock, Rectangle bound){
			if( lock.contains( bound.getX()+bound.getWidth()/2, bound.getY()+bound.getHeight()/2)){
				bound.x = (int) ((lock.getX()+lock.getWidth()/2)-bound.getWidth()/2);
				bound.y = (int) ((lock.getY()+lock.getHeight()/2)-bound.getHeight()/2);
				return true;
			}
			return false;
		}

		@Override
		public void mousePressed( MouseEvent e){
			Object source = e.getSource();
			xPressed = e.getX();
			yPressed = e.getY();
			if( interactWithHexes && source instanceof Hex){
				remove( (Hex)source);
				add( (Hex)source, 0);
				revalidate();
				repaint();
			}
		}

		@Override
		public void mouseClicked( MouseEvent e){
			Object source = e.getSource();
			if( interactWithHexes && source instanceof Hex && e.getButton()==MouseEvent.BUTTON3){
				((Hex)source).flip();
			}else if( interactWithHexes && !(source instanceof Hex) && e.getButton()==MouseEvent.BUTTON2){
				for( Component hex : getComponents()){
					((Hex)hex).flip();
				}
			}else if( !(source instanceof Hex) && e.getButton()==MouseEvent.BUTTON1){
				if( timer == null){
					timer = new Timer( SPIRAL_DELAY, new SpiralPlacement());
					timer.setInitialDelay( 0);
					timer.start();
				}
			}
		}
	}
}
