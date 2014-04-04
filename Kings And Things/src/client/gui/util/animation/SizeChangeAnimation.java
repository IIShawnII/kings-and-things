package client.gui.util.animation;

import java.awt.Image;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;

public class SizeChangeAnimation{

	private int size;
	private JPanel panel;
	private Image shrinkImage;
	private boolean expanded = false;
	private boolean changing = false;
	private Dimension originalSize = null;
	
	public SizeChangeAnimation( Image shrinkImage, int size, JPanel panel){
		this.shrinkImage = shrinkImage;
		this.size = size;
		this.panel = panel;
		this.originalSize = panel.getBounds().getSize();
	}
	
	public boolean isChanging(){
		return changing;
	}
	
	public boolean isExpanded(){
		return expanded;
	}

	public void expandTo( final int width, final int height) {
		panel.setBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED));
		new Animation( new Dimension( width, height), true).start();
	}

	public void shrinkToOriginal() {
		if( isExpanded()){
			new Animation( originalSize, false).start();
		}
	}
	
	public boolean paint( Graphics g){
		if( !expanded && !changing){
			g.drawImage( shrinkImage, 0, 0, size, size, null);
			return true;
		}
		return false;
	}
	
	private class Animation implements ActionListener{
		
		public static final int EXPANDING_RATE = 10;
		
		private Timer timer;
		private Dimension target;
		private boolean expand;
		private int yOffSet, sizeOffSet;
		
		public Animation( Dimension target, boolean expand){
			this.target = target;
			this.expand = expand;
			yOffSet = expand? -EXPANDING_RATE : EXPANDING_RATE;
			sizeOffSet = expand? EXPANDING_RATE : -EXPANDING_RATE;
			timer = new Timer( 10, this);
			timer.setInitialDelay( 0);
		}
		
		public void start(){
			changing = true;
			timer.start();
		}

		@Override
		public void actionPerformed( ActionEvent e) {
			boolean changed = false;
			Rectangle bound = panel.getBounds();
			if( expand){
				changed = adjustWidth( bound, bound.width, target.width);
				changed = changed || adjustHeight( bound, bound.height, target.height);
			}else{
				changed = adjustWidth( bound, target.width, bound.width);
				changed = changed || adjustHeight( bound, target.height, bound.height);
			}
			if( changed){
				panel.setBounds( bound);
			}else{
				timer.stop();
				changing = false;
				expanded = expand;
				if(!expand){
					panel.setBorder( null);
					panel.setOpaque( false);
				}else{
					panel.setOpaque( true);
					panel.revalidate();
				}
			}
		}
		
		private boolean adjustHeight( Rectangle bound, int height1, int height2){
			if( height1 < height2){
				bound.translate( 0, yOffSet);
				bound.grow( 0, sizeOffSet);
				return true;
			}
			return false;
		}
		
		private boolean adjustWidth( Rectangle bound, int width1, int width2){
			if( width1 < width2){
				bound.grow( sizeOffSet, 0);
				return true;
			}
			return false;
		}
	}
}
