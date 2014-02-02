package common;

import java.awt.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides a convenient mechanism for representing the hex board inside out code.
 */
public class HexBoard
{
	private final Map<Point,TileProperties> board;
	
	/**
	 * Create new HexBoard, the entered list is assumed to be ordered according to the spiral layout pattern
	 * @param tiles The list of tiles to make a board out of, this must be ordered according to the spiral
	 * layout pattern and can contain any number of rings, but must not contain an incomplete ring.
	 */
	public HexBoard(List<? extends TileProperties> tiles)
	{
		if(tiles==null || tiles.size()<1)
		{
			throw new IllegalArgumentException("Can not create a board with no tiles");
		}
		HashMap<Point,TileProperties> tempBoard = new HashMap<Point,TileProperties>();
		
		int numRings = 0;
		int i = 0;
		int nextRingLength = 1;
		for(int x=0; x<tiles.size(); x++)
		{
			i++;
			if(i - nextRingLength == 0)
			{
				numRings++;
				i = 0;
				nextRingLength = numRings * 6;
			}
		}
		
		//place middle hex first
		tempBoard.put(new Point(numRings-1,2*(numRings-1)), tiles.remove(0));
		
		//every other hex is placed relative to the last placed hex
		Point lastRingPiece = new Point(numRings-1,2*(numRings-1));
		int nextRingNumber = 2;
		while(!tiles.isEmpty())
		{
			if(tiles.size() < (nextRingNumber-1)*6)
			{
				throw new IllegalArgumentException("The entered board has an incomplete ring of hexes.");
			}
			//start by placing one piece above the last placed piece of the last ring
			tempBoard.put(new Point(lastRingPiece.x,lastRingPiece.y-1), tiles.remove(0));
			lastRingPiece.y--;
			
			//place hexes until you are at the top of the ring
			int numAwayFromTop = nextRingNumber - 2;
			for(int j=0; j<numAwayFromTop; j++)
			{
				tempBoard.put(new Point(lastRingPiece.x+1,lastRingPiece.y-1), tiles.remove(0));
				lastRingPiece.x++;
				lastRingPiece.y--;
			}
			
			//num of hexes before switching directions
			int rowLength = nextRingNumber-1;
				
			//top right row
			for(int j=0; j<rowLength; j++)
			{
				tempBoard.put(new Point(lastRingPiece.x+1,lastRingPiece.y+1), tiles.remove(0));
				lastRingPiece.x++;
				lastRingPiece.y++;
			}

			//right row
			for(int j=0; j<rowLength; j++)
			{
				tempBoard.put(new Point(lastRingPiece.x,lastRingPiece.y+1), tiles.remove(0));
				lastRingPiece.y++;
			}

			//bot right row
			for(int j=0; j<rowLength; j++)
			{
				tempBoard.put(new Point(lastRingPiece.x-1,lastRingPiece.y+1), tiles.remove(0));
				lastRingPiece.x--;
				lastRingPiece.y++;
			}

			//bot left row
			for(int j=0; j<rowLength; j++)
			{
				tempBoard.put(new Point(lastRingPiece.x-1,lastRingPiece.y-1), tiles.remove(0));
				lastRingPiece.x--;
				lastRingPiece.y--;
			}

			//left row
			for(int j=0; j<rowLength; j++)
			{
				tempBoard.put(new Point(lastRingPiece.x,lastRingPiece.y-1), tiles.remove(0));
				lastRingPiece.y--;
			}
		}
		
		board = Collections.unmodifiableMap(tempBoard);
	}
	
	/**
	 * Checks whether or not any hex exists at the specified row and column,
	 * with top left being (0,0)
	 * @param row The row to check
	 * @param col The column to check
	 * @return True if hex exists, false otherwise
	 */
	public boolean hexExistsAtRowColumn(int row, int col)
	{
		return hexExistsAtXY(col,row);
	}

	/**
	 * Checks whether or not any hex exists at the specified x,y coordinates,
	 * with top left being (0,0)
	 * @param x The x coordinate to check
	 * @param y The y coordinate to check
	 * @return True if hex exists, false otherwise
	 */
	public boolean hexExistsAtXY(int x, int y)
	{
		return board.containsKey(new Point(x,y));
	}
	
	/**
	 * Retrieves a hex by row,column number with the top left being (0,0)
	 * @param row The row of the hex to return
	 * @param col The column of the hex to return
	 * @return The hex at the requested position
	 * @throws IllegalArgumentException if no hex exists at the specified
	 * row,column
	 */
	public TileProperties getHexByRowColumn(int row, int col)
	{
		return getHexByXY(col,row);
	}
	
	/**
	 * Retrieves a hex by x,y coordinates with the top left being (0,0)
	 * @param x The x coordinate of the hex to return
	 * @param y The y coordinate of the hex to return
	 * @return The hex at the requested position
	 * @throws IllegalArgumentException if no hex exists at the specified
	 * coordinates
	 */
	public TileProperties getHexByXY(int x, int y)
	{
		if(!hexExistsAtXY(x,y))
		{
			throw new IllegalArgumentException("No hex exists at position (" + x + "," + y + ")");
		}
		return board.get(new Point(x,y));
	}
}
