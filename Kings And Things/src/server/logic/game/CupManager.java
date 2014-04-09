package server.logic.game;

import java.util.ArrayList;
import java.util.Iterator;

import server.logic.exceptions.NoMoreTilesException;

import common.Constants;
import common.Constants.Ability;
import common.Constants.Building;
import common.game.ITileProperties;
import common.game.TileProperties;
import common.game.TwoSidedTileProperties;

/**
 * this class encapsulates the logic of drawing tiles from the cup, and placing
 * previously drawn ones back.
 */
public class CupManager extends AbstractTileManager
{
	private static final long serialVersionUID = 1998393393444664606L;
	private static int id = 2;
	
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
		generateBuildingTilesInCup(Building.Village);
		generateBuildingTilesInCup(Building.City);
	}
	
	public CupManager(CupManager other)
	{
		super(Constants.deepCloneCollection(other.tiles,new ArrayList<ITileProperties>()),"cup");
		isDemoMode = other.isDemoMode;
		numDraws = other.numDraws;
	}
	
	@Override
	public CupManager clone()
	{
		return new CupManager(this);
	}

	/**
	 * Call this method to draw a tile from the cup.
	 * @return A cup tile.
	 * @throws NoMoreTilesException If there are no more tiles left to draw.
	 */
	@Override
	public ITileProperties drawTile() throws NoMoreTilesException
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
				numDraws++;
				return tiles.remove(0);
			}
		}
	}
	
	private void stackDeck() throws NoMoreTilesException
	{
		synchronized(tiles)
		{
			ArrayList<ITileProperties> newDeckOrder = new ArrayList<ITileProperties>();
			
			//player 1
			ITileProperties tile = removeCreatureByNameAndAttack("Old_Dragon",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Giant_Spider",1);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Elephant",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Brown_Knight",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Giant",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Dwarves",2);
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
			tile = removeCreatureByNameAndAttack("Greathunter",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Nomads",1);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Witch_Doctor",2);
			newDeckOrder.add(tile);

			//player 4
			tile = removeCreatureByNameAndAttack("Tribesmen",2);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Giant_Lizard",2);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Villains",2);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Tigers",3);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Vampire_Bat",4);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Tribesmen",2);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Dark_Wizard",1);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Black_Knight",3);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Giant_Ape",5);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Buffalo_Herd",3);
			newDeckOrder.add(tile);

			tile = removeCreatureByNameAndAttack("Cyclops",5);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Mountain_Men",1);
			newDeckOrder.add(tile);
			tile = removeCreatureByNameAndAttack("Goblins",1);
			newDeckOrder.add(tile);
			
			for(ITileProperties tp : tiles)
			{
				newDeckOrder.add(tp);
			}
			
			tiles.clear();
			tiles.addAll(newDeckOrder);
		}
	}

	private void generateBuildingTilesInCup(Building b)
	{
		ArrayList<TileProperties> faceUpBuildings = new ArrayList<>(6);
		ArrayList<TileProperties> faceDownBuildings = new ArrayList<>(6);
		
		for(TileProperties tp : Constants.BUILDING.values())
		{
			if(tp.getName().equals(b.name()))
			{
				if(tp.hasAbility(Ability.Neutralised))
				{
					faceDownBuildings.add(tp);
				}
				else
				{
					faceUpBuildings.add(tp);
				}
			}
		}
		
		for(int i=0; i<faceUpBuildings.size(); i++)
		{
			TileProperties faceUp = faceUpBuildings.get(i);
			TileProperties faceDown = faceDownBuildings.get(i);
			if(faceUp == null || faceDown == null)
			{
				throw new IllegalArgumentException("No building tiles found for type: " + b);
			}
			
			reInsertTile(new TwoSidedTileProperties(new TileProperties(faceUp,id++),new TileProperties(faceDown,id++)));
		}
	}
	
	private ITileProperties removeCreatureByNameAndAttack(String name, int attack) throws NoMoreTilesException
	{
		synchronized(tiles)
		{
			Iterator<ITileProperties> it = tiles.iterator();
			while(it.hasNext())
			{
				ITileProperties tp = it.next();
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
