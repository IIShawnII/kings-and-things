package client.gui;

import java.util.ArrayList;

import java.io.File;
import java.io.IOException;

import javax.swing.Timer;
import javax.swing.JPanel;
import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.Polygon;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import client.gui.tiles.Hex;
import client.gui.tiles.Tile;
import static common.Constants.HEX_SIZE;
import static common.Constants.LOCK_SIZE;
import static common.Constants.BOARD_SIZE;
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
	
	private static final BufferedImage image;
	private static final Polygon HEX_OUTLINE;
	private static final int heightSegment = (int) ((HEX_BOARD_SIZE.getHeight())/BOARD_HEIGHT_SEGMENT);
	private static final int widthSegment = (int) ((HEX_BOARD_SIZE.getWidth())/BOARD_WIDTH_SEGMENT);
	static{
		int w = (int) (HEX_SIZE.getWidth()/4)+1;
		int h = (int) (HEX_SIZE.getHeight()/2)+2;
		HEX_OUTLINE = new Polygon( new int[]{w,0,w,w*3,w*4,w*3}, new int[]{h*2,h,0,0,h,h*2}, 6);
		File file = new File( "Resources\\Extra\\-n Woodboard.jpg");
		Image background = null;
		try {
			background = ImageIO.read( file);
		} catch ( IOException e) {
			e.printStackTrace();
			System.err.println("Board");
		}
		
		image = new BufferedImage( BOARD_SIZE.width, BOARD_SIZE.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.drawImage( background, 0, 0, BOARD_SIZE.width, BOARD_SIZE.height, null);
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
		g2d.dispose();
	}
	
	private boolean interactWithHexes = false;
	
	private Timer timer;
	private MouseInput mouseInput;
	private ArrayList< Rectangle> lockList;
	private Rectangle hexLock;
	
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
		addHex( 8, 8);
	}
	
	public Tile addHex( int x, int y){
		Tile hex = new Hex();
		hex.addMouseListener( mouseInput);
		hex.addMouseMotionListener( mouseInput);
		hex.setBounds( x, y, HEX_SIZE.width, HEX_SIZE.height);
		hex.setLockArea( hexLock);
		hex.init();
		add(hex,0);
		return hex;
	}
	
	@Override
	public void paintComponent( Graphics g){
		super.paintComponent( g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.drawImage( image, 0, 0, getWidth(), getHeight(), null);
	}
	
	private class SpiralPlacement implements ActionListener{

		private Rectangle oldBound = null;
		private Tile tile = null;
		private int ring = 0, count = 0;
		private double slope = 0, intercept = 0;
		private int x=0, y=0, xStart=0, yStart=0, xTemp=-1, yTemp=-1;
		
		@Override
		public void actionPerformed( ActionEvent e) {
			if( xTemp>=0){
				oldBound = tile.getBounds();
				yTemp = (int)(slope*xTemp+intercept);
				tile.setLocation( xTemp, yTemp);
				xTemp+=HEX_MOVE_DISTANCE;
				//hex has passed its final location
				if( xTemp>=x-HEX_SIZE.width/2){
					xTemp=-1;
					tile.setLocation( x-HEX_SIZE.width/2, y-HEX_SIZE.height/2);
				}
				oldBound.add(tile.getBounds());
				repaint( oldBound);
			}else if( ring<BOARD_LOAD_ROW.length){
				if( count<BOARD_LOAD_ROW[ring].length){
					tile = addHex( 8, 8);
					x = (widthSegment*BOARD_LOAD_COL[ring][count]);
					y = (heightSegment*BOARD_LOAD_ROW[ring][count])+BOARD_TOP_PADDING;
					tile.setLockArea( x-LOCK_SIZE/2, y-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
					xTemp = xStart = tile.getX();
					yStart = tile.getY();
					slope = (y-yStart)/(double)(x-xStart);
					intercept = yStart-slope*xStart;
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
		private boolean ignore = false;

		@Override
	    public void mouseDragged(MouseEvent e){
			if(	!ignore && e.getSource() instanceof Tile && interactWithHexes){
				Tile tile = (Tile)e.getSource();
				boardBound = getBounds();
				bound = new Rectangle( tile.getBounds());
				xDiff = e.getX() - xPressed;
				yDiff = e.getY() - yPressed;
				if( !tile.canLock( xDiff, yDiff)){
					bound.translate( xDiff, 0);
					if( !boardBound.contains( bound)){
						bound.translate( -xDiff, 0);
					}
					bound.translate( 0, yDiff);
					if( !boardBound.contains( bound)){
						bound.translate( 0, -yDiff);
					}
					if( checkLock( hexLock, bound)){
						tile.setLockArea( hexLock);
					}else{
						Rectangle lock = null;
						for( int i=0; i<lockList.size(); i++){
							lock = lockList.get( i);
							if( checkLock( lock, bound)){
								tile.setLockArea( lockList.remove( i));
							}
						}
					}
					if( tile.hasLock() && !tile.canLock( xDiff, yDiff)){
						Rectangle temp = tile.removeLock();
						if( !temp.equals( hexLock)){
							lockList.add( temp);
						}
					}
					tile.setBounds( bound);
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
			if( interactWithHexes && source instanceof Tile){
				xPressed = e.getX();
				yPressed = e.getY();
				Tile tile = (Tile)source;
				if( tile.isInside( xPressed, yPressed)){
					remove( tile);
					add( tile, 0);
					revalidate();
					repaint( tile.getBounds());
					ignore = false;
				}else{
					ignore = true;
				}
			}
		}

		@Override
		public void mouseClicked( MouseEvent e){
			Object source = e.getSource();
			if( interactWithHexes && source instanceof Tile && e.getButton()==MouseEvent.BUTTON3){
				if( ((Tile)source).isInside( xPressed, yPressed)){
					((Tile)source).flip();
				}
			}else if( interactWithHexes && !(source instanceof Tile) && e.getButton()==MouseEvent.BUTTON2){
				for( Component tile : getComponents()){
					((Tile)tile).flip();
				}
			}else if( !(source instanceof Tile) && e.getButton()==MouseEvent.BUTTON1){
				if( timer == null){
					timer = new Timer( SPIRAL_DELAY, new SpiralPlacement());
					timer.setInitialDelay( 0);
					timer.start();
				}
			}
		}
	}
}