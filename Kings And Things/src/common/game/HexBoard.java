package common.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableBiMap;

import common.TileProperties;

/**
 * This class provides a convenient mechanism for representing the hex board inside out code.
 */
public class HexBoard
{
	private final ImmutableBiMap<Point,HexState> board;
	private final List<HexState> boardList;
	
	/**
	 * Create new HexBoard, the entered list is assumed to be ordered according to the spiral layout pattern
	 * @param tiles The list of tiles to make a board out of, this must be ordered according to the spiral
	 * layout pattern and can contain any number of rings, but must not contain an incomplete ring.
	 * @throws IllegalArgumentException if tiles is null, empty, or contains non-hex tiles
	 */
	public HexBoard(List<? extends TileProperties> tiles)
	{
		if(tiles==null || tiles.size()<1)
		{
			throw new IllegalArgumentException("Can not create a board with no tiles");
		}
		HashMap<Point,HexState> tempBoard = new HashMap<Point,HexState>();
		ArrayList<HexState> tempBoardList = new ArrayList<HexState>();
		
		int numRings = 0;
		int i = 0;
		int nextRingLength = 1;
		for(TileProperties tp : tiles)
		{
			if(!tp.isHexTile())
			{
				throw new IllegalArgumentException("The entered board has non-hex tiles");
			}
			i++;
			if(i - nextRingLength == 0)
			{
				numRings++;
				i = 0;
				nextRingLength = numRings * 6;
			}
		}
		
		//place middle hex first
		HexState nextHex = new HexState(tiles.remove(0));
		tempBoard.put(new Point(numRings-1,2*(numRings-1)), nextHex);
		tempBoardList.add(nextHex);
		
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
			nextHex = new HexState(tiles.remove(0));
			tempBoard.put(new Point(lastRingPiece.x,lastRingPiece.y-1), nextHex);
			tempBoardList.add(nextHex);
			lastRingPiece.y--;
			
			//place hexes until you are at the top of the ring
			int numAwayFromTop = nextRingNumber - 2;
			for(int j=0; j<numAwayFromTop; j++)
			{
				nextHex = new HexState(tiles.remove(0));
				tempBoard.put(new Point(lastRingPiece.x+1,lastRingPiece.y-1), nextHex);
				tempBoardList.add(nextHex);
				lastRingPiece.x++;
				lastRingPiece.y--;
			}
			
			//num of hexes before switching directions
			int rowLength = nextRingNumber-1;
				
			//top right row
			for(int j=0; j<rowLength; j++)
			{
				nextHex = new HexState(tiles.remove(0));
				tempBoard.put(new Point(lastRingPiece.x+1,lastRingPiece.y+1), nextHex);
				tempBoardList.add(nextHex);
				lastRingPiece.x++;
				lastRingPiece.y++;
			}

			//right row
			for(int j=0; j<rowLength; j++)
			{
				nextHex = new HexState(tiles.remove(0));
				tempBoard.put(new Point(lastRingPiece.x,lastRingPiece.y+1), nextHex);
				tempBoardList.add(nextHex);
				lastRingPiece.y++;
			}

			//bot right row
			for(int j=0; j<rowLength; j++)
			{
				nextHex = new HexState(tiles.remove(0));
				tempBoard.put(new Point(lastRingPiece.x-1,lastRingPiece.y+1), nextHex);
				tempBoardList.add(nextHex);
				lastRingPiece.x--;
				lastRingPiece.y++;
			}

			//bot left row
			for(int j=0; j<rowLength; j++)
			{
				nextHex = new HexState(tiles.remove(0));
				tempBoard.put(new Point(lastRingPiece.x-1,lastRingPiece.y-1), nextHex);
				tempBoardList.add(nextHex);
				lastRingPiece.x--;
				lastRingPiece.y--;
			}

			//left row
			for(int j=0; j<rowLength; j++)
			{
				nextHex = new HexState(tiles.remove(0));
				tempBoard.put(new Point(lastRingPiece.x,lastRingPiece.y-1), nextHex);
				tempBoardList.add(nextHex);
				lastRingPiece.y--;
			}
		}
		
		board = new ImmutableBiMap.Builder<Point, HexState>().putAll(tempBoard).build();
		boardList = Collections.unmodifiableList(tempBoardList);
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
	public HexState getHexByRowColumn(int row, int col)
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
	public HexState getHexByXY(int x, int y)
	{
		if(!hexExistsAtXY(x,y))
		{
			throw new IllegalArgumentException("No hex exists at position (" + x + "," + y + ")");
		}
		return board.get(new Point(x,y));
	}

	/**
	 * Given a hex, return the row,column numbers of that hex in this board
	 * as a Point object.
	 * @param hex The hex to find
	 * @return The row,column coordinates of the hex
	 * @throws IllegalArgumentException if the entered tile is null, is
	 * not a hex, or doesn't exist in this board
	 */
	public Point getRowColumnOfHex(TileProperties hex)
	{
		Point xy = getXYCoordinatesOfHex(hex);
		return new Point(xy.y,xy.x);
	}
	
	/**
	 * Given a hex, return the x,y position of that hex in this board
	 * as a Point object.
	 * @param hex The hex to find
	 * @return The x,y coordinates of the hex
	 * @throws IllegalArgumentException if the entered tile is null, is
	 * not a hex, or doesn't exist in this board
	 */
	public Point getXYCoordinatesOfHex(TileProperties hex)
	{
		if(hex==null)
		{
			throw new IllegalArgumentException("The entered hex must not be null.");
		}
		if(!hex.isHexTile())
		{
			throw new IllegalArgumentException("The entered tile must be a hex tile.");
		}
		for(Entry<Point,HexState> entry : board.entrySet())
		{
			if(entry.getValue().getHex().equals(hex))
			{
				return new Point(entry.getKey().x, entry.getKey().y);
			}
		}
		
		throw new IllegalArgumentException("The entered hex could not be found");
	}
	
	/**
	 * Given a hex from this board, find a list of all adjacent hexes.
	 * The list may be empty but is guaranteed not to be null. The given
	 * hex is not returned inside the generated list.
	 * @param hex The hex to find adjacent hexes for
	 * @return A list of all hexes that are adjacent to the entered hex
	 * @throws IllegalArgumentException if the entered tile is null, is
	 * not a hex, or doesn't exist in this board
	 */
	public List<HexState> getAdjacentHexesTo(TileProperties hex)
	{
		Point coords = getXYCoordinatesOfHex(hex);
		int x = coords.x;
		int y = coords.y;
		
		ArrayList<Point> coordsToTest = new ArrayList<Point>();
		coordsToTest.add(new Point(x,y-1));
		coordsToTest.add(new Point(x,y+1));
		coordsToTest.add(new Point(x-1,y-1));
		coordsToTest.add(new Point(x-1,y+1));
		coordsToTest.add(new Point(x+1,y-1));
		coordsToTest.add(new Point(x+1,y+1));
		
		Iterator<Point> it = coordsToTest.iterator();
		while(it.hasNext())
		{
			Point nextPoint = it.next();
			if(!hexExistsAtXY(nextPoint.x, nextPoint.y))
			{
				it.remove();
			}
		}
		
		ArrayList<HexState> adjacentHexes = new ArrayList<HexState>();
		for(Point p : coordsToTest)
		{
			adjacentHexes.add(getHexByXY(p.x, p.y));
		}
		
		return Collections.unmodifiableList(adjacentHexes);
	}
	
	/**
	 * Use this method if you want to traverse all hexes on the board.
	 * The returned list is non-modifiable and references the same objects
	 * returned by the other methods in this class. The list is also in
	 * 'spiral placement' order
	 * @return The list of hexes
	 */
	public List<HexState> getHexesAsList()
	{
		return boardList;
	}
}
