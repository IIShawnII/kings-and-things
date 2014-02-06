package server.logic.game;

import java.util.ArrayList;
import java.util.Iterator;

import server.logic.exceptions.NoMoreTilesException;
import common.Constants;
import common.TileProperties;

/**
 * this class encapsulates the logic of drawing tiles from the cup, and placing
 * previously drawn ones back.
 */
class CupManager extends AbstractTileManager
{
	private final boolean isDemoMode;
	private int numDraws;

	/**
	 * Create new CupManager.
	 * @param isDemoMode Set to true if we should stack the deck of cup tiles
	 * to match the demo script board.
	 */
	public CupManager(boolean isDemoMode)
	{
		super(Constants.CUP.values(),"cup");
		this.isDemoMode = isDemoMode;
		numDraws = 0;
	}

	/**
	 * Call this method to draw a tile from the cup.
	 * @return A cup tile.
	 * @throws NoMoreTilesException If there are no more tiles left to draw.
	 */
	@Override
	public TileProperties drawTile() throws NoMoreTilesException
	{
		if(!isDemoMode)
		{
			return super.drawTile();
		}
		else
		{
			synchronized(tiles)
			{
				if(numDraws == 0)
				{
					stackDeck();
				}
				return tiles.remove(numDraws++);
			}
		}
	}
	
	private void stackDeck() throws NoMoreTilesException
	{
		synchronized(tiles)
		{
			ArrayList<TileProperties> newDeckOrder = new ArrayList<TileProperties>();
			
			//player 1
			TileProperties tile = removeCreatureByNameAndAttack("Old_Dragon",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Giant_Spider",1);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Elephant",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Brown_Knight",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Giant",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Dwarves",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Skeletons",1);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Watusi",2);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Goblins",1);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Ogre",2);
			newDeckOrder.add(tile);

			//player 2
			tile = removeCreatureByNameAndAttack("Pterodactyl_Warriors",2);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Sandworm",3);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Green_Knight",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Dervish",2);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Crocodiles",2);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Nomads",1);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Druid",3);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Walking_Tree",5);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Crawling_Vines",6);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Bandits",2);
			newDeckOrder.add(tile);

			//player 3
			tile = removeCreatureByNameAndAttack("Centaur",2);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Camel_Corps",3);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Farmers",1);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Farmers",1);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Genie",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Skeletons",1);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Pygmies",2);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Great_Hunter",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Nomads",1);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Witch_Doctor",2);
			newDeckOrder.add(tile);

			//player 4
			tile = removeCreatureByNameAndAttack("Tribesman",2);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Giant_Lizard",2);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Villains",2);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Tigers",3);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Vampire_Bat",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Tribesman",2);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Dark_Wizard",1);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Black_Knight",3);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Giant_Ape",5);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Buffalo_Herd",3);
			newDeckOrder.add(tile);
			
			for(TileProperties tp : tiles)
			{
				newDeckOrder.add(tp);
			}
			
			tiles.clear();
			tiles.addAll(newDeckOrder);
		}
	}
	
	private TileProperties removeCreatureByNameAndAttack(String name, int attack) throws NoMoreTilesException
	{
		synchronized(tiles)
		{
			Iterator<TileProperties> it = tiles.iterator();
			while(it.hasNext())
			{
				TileProperties tp = it.next();
				if(tp.getName().equals(name) && tp.getValue() == attack)
				{
					it.remove();
					return tp;
				}
			}
			
			throw new NoMoreTilesException("Unable to draw cup tile named: " + name + ", with attack: " + attack + ", because there are not enough tiles of that type.");
		}
	}
}
