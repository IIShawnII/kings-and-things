package server.logic.game;

import java.io.Serializable;

import java.awt.Point;

import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import common.Logger;
import common.game.Roll;
import common.game.Player;
import common.game.HexState;
import common.game.PlayerInfo;
import common.game.ITileProperties;
import common.Constants.Building;
import common.Constants.SetupPhase;
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.event.network.GameStateProgress;

import server.logic.exceptions.NoMoreTilesException;

import static common.Constants.ALL_PLAYERS_ID;

/**
 * GameState can be described by the board and player info
 */
public class GameState implements Serializable
{
	private static final long serialVersionUID = 5359708831592253851L;
	
	private CupManager cup;
	private HexTileManager bank;
	private BoardGenerator boardGenerator;
	private SpecialCharacterManager bankHeroes;
	private HexBoard board;
	private final HashSet<Player> players;
	private final ArrayList<Integer> playerOrder;
	private SetupPhase currentSetupPhase;
	private RegularPhase currentRegularPhase;
	private int activeTurnPlayerNumber;
	private CombatPhase currentCombatPhase;
	private int defenderPlayerNumber;
	private Point combatLocation;
	private final ArrayList<Roll> rolls;
	private final transient HashSet<Integer> confirmedRolls;
	private final ArrayList<RollModification> rollModifications;
	private final HashMap<Integer,Integer> hitsToApply;
	private final HashSet<HexState> hexesContainingBuiltObjects;
	private Roll recordedRollForSpecialCharacter;
	private final HashMap<HexState,Integer> hexesThatNeedThingsRemoved;
	private final HashMap<Integer,Integer> playerTargets;

	/**
	 * Creates a new GameState object
	 * @param demoMode Set to true to start a game in demo mode
	 * @param players The set of players playing the game
	 * @param playerOrder A list of player ids in the order in which players will take turns
	 * @param currentSetupPhase The current setup phase
	 * @param activeTurnPlayerNumber The player id of the player who's turn it is
	 * @param activePhasePlayerNumber The player id of the next player to act in the current phase
	 * @param currentCombatPhase The current phase of combat
	 * @param defenderPlayerNumber The player who is acting as the defender
	 * @param combatLocation The (x,y) coordinates of the hex where combat is taking place
	 */
	public GameState(boolean demoMode, Set<Player> players, List<Integer> playerOrder, SetupPhase currentSetupPhase, RegularPhase currentRegularPhase,
			int activeTurnPlayerNumber, int activePhasePlayerNumber, CombatPhase currentCombatPhase, int defenderPlayerNumber, Point combatLocation)
	{
		this.players = new HashSet<Player>(players);
		this.playerOrder = new ArrayList<Integer>(playerOrder);
		this.currentSetupPhase = currentSetupPhase;
		this.currentRegularPhase = currentRegularPhase;
		this.activeTurnPlayerNumber = activeTurnPlayerNumber;
		this.currentCombatPhase = currentCombatPhase;
		this.defenderPlayerNumber = defenderPlayerNumber;
		this.combatLocation = combatLocation;
		rolls = new ArrayList<>();
		confirmedRolls = new HashSet<>();
		rollModifications = new ArrayList<RollModification>();
		
		hitsToApply = new HashMap<>();
		this.hexesContainingBuiltObjects = new HashSet<HexState>();
		playerTargets = new HashMap<>();
		for(Player p : players)
		{
			hitsToApply.put(p.getID(), 0);
			playerTargets.put(p.getID(), null);
		}
		this.setActivePhasePlayer(activePhasePlayerNumber);
		recordedRollForSpecialCharacter = null;
		this.hexesThatNeedThingsRemoved = new HashMap<> ();
		

		cup = new CupManager(demoMode);
		bank = new HexTileManager(demoMode);
		boardGenerator = new BoardGenerator(players.size(),bank);
		bankHeroes = new SpecialCharacterManager(demoMode);
		try
		{
			board = boardGenerator.createNewBoard();
		}
		catch (NoMoreTilesException e)
		{
			Logger.getErrorLogger().error("Unable to start game due to: ", e);
		}
	}
	
	/**
	 * Standard copy constructor
	 * @param other State to clone
	 */
	public GameState(GameState other)
	{
		cup = other.cup.clone();
		bank = other.bank.clone();
		boardGenerator = other.boardGenerator.clone();
		bankHeroes = other.bankHeroes.clone();
		board = other.board.clone();
		players = new HashSet<>(other.players.size());
		for(Player p : other.players)
		{
			players.add(p.clone());
		}
		playerOrder = new ArrayList<>(other.playerOrder);
		currentSetupPhase = other.currentSetupPhase;
		currentRegularPhase = other.currentRegularPhase;
		activeTurnPlayerNumber = other.activeTurnPlayerNumber;
		currentCombatPhase = other.currentCombatPhase;
		defenderPlayerNumber = other.defenderPlayerNumber;
		combatLocation = new Point(other.combatLocation.x,other.combatLocation.y);
		rolls = new ArrayList<>(other.rolls.size());
		for(Roll r : other.rolls)
		{
			rolls.add(r.clone());
		}
		confirmedRolls = new HashSet<>( other.confirmedRolls.size());
		for( Integer ID : other.confirmedRolls)
		{
			confirmedRolls.add( new Integer( ID));
		}
		rollModifications = new ArrayList<>(other.rollModifications.size());
		for(RollModification rm : other.rollModifications)
		{
			rollModifications.add(rm.clone());
		}
		hitsToApply = new HashMap<>(other.hitsToApply);
		hexesContainingBuiltObjects = new HashSet<>(other.hexesContainingBuiltObjects.size());
		for(HexState hs : other.hexesContainingBuiltObjects)
		{
			hexesContainingBuiltObjects.add(hs.clone());
		}
		recordedRollForSpecialCharacter = other.recordedRollForSpecialCharacter.clone();
		hexesThatNeedThingsRemoved = new HashMap<>(other.hexesThatNeedThingsRemoved.size());
		for(Entry<HexState,Integer> e : other.hexesThatNeedThingsRemoved.entrySet())
		{
			hexesThatNeedThingsRemoved.put(e.getKey().clone(), e.getValue());
		}
		playerTargets = new HashMap<>(other.playerTargets);
	}
	
	@Override
	public GameState clone()
	{
		return new GameState(this);
	}
	
	/**
	 * convert Players set to PlayerInfo array
	 */
	public PlayerInfo[] getPlayerInfoArray(){
		PlayerInfo[] infos = new PlayerInfo[ players.size()];
		int index=0;
		for( Player player:players){
			infos[index] = player.getPlayerInfo();
			index++;
		}
		return infos;
	}
	
	/**
	 * Gets the current board
	 * @return The game board
	 */
	public HexBoard getBoard()
	{
		return board;
	}
	
	/**
	 * Get the set of players currently playing the game
	 * @return The players of the game
	 */
	public Set<Player> getPlayers()
	{
		return Collections.unmodifiableSet(players);
	}
	
	/**
	 * Get a list of player ids indicating the player order
	 * @return List indicating player order
	 */
	public List<Integer> getPlayerOrder()
	{
		return Collections.unmodifiableList(playerOrder);
	}
	
	/**
	 * Get the current setup phase of the game
	 * @return The setup phase of the game
	 */
	public SetupPhase getCurrentSetupPhase()
	{
		return currentSetupPhase;
	}
	
	public CupManager getCup()
	{
		return cup;
	}
	
	public HexTileManager getBank()
	{
		return bank;
	}
	
	public BoardGenerator getBoardGenerator()
	{
		return boardGenerator;
	}
	
	public SpecialCharacterManager getBankHeroes()
	{
		return bankHeroes;
	}
	
	/**
	 * Get the current regular phase of the game
	 * @return The regular phase of the game
	 */
	public RegularPhase getCurrentRegularPhase()
	{
		return currentRegularPhase;
	}
	
	public boolean hasRecordedRollForSpecialCharacter()
	{
		return recordedRollForSpecialCharacter != null;
	}
	
	//checks if tile is owned by player
	public Player getOwningPlayer(ITileProperties tile) {
		for (Player p : players) {
			if (p.ownsThingOnBoard(tile)) {
				return p;
			}
		}
		return null;
	}
	
	public boolean isOwnedByPlayer(ITileProperties tile) {
		return getOwningPlayer(tile) != null;
	}
	
	public Roll getRecordedRollForSpecialCharacter()
	{
		return recordedRollForSpecialCharacter;
	}
	
	public void recordRollForSpecialCharacter(Roll r)
	{
		recordedRollForSpecialCharacter = r;
	}
	
	/**
	 * Get the player who needs to move next for the current
	 * phase
	 * @return The player who needs to move next for this phase
	 */
	public Player getActivePhasePlayer()
	{
		for(Player p : getPlayers())
		{
			if(p.getPlayerInfo().isActive())
			{
				return p;
			}
		}
		throw new IllegalStateException("No active player found");
	}
	
	public Player getPlayersTarget(int id)
	{
		Integer targetId = playerTargets.get(id);
		if(targetId == null)
		{
			return null;
		}
		for(Player p : players)
		{
			if(p.getID() == targetId.intValue())
			{
				return p;
			}
		}
		
		return null;
	}
	
	public HashSet<Player> getPlayersStillFightingInCombatHex()
	{
		HashSet<Player> fightingPlayers = new HashSet<>();
		for(ITileProperties thing : getCombatHex().getFightingThingsInHex())
		{
			for(Player p : players)
			{
				if(p.ownsThingOnBoard(thing))
				{
					fightingPlayers.add(p);
				}
			}
		}
		return fightingPlayers;
	}

	/**
	 * Get the player who's turn it is
	 * @return The player who's turn it is
	 */
	public Player getActiveTurnPlayer()
	{
		return getPlayerByPlayerNumber(activeTurnPlayerNumber);
	}
	
	/**
	 * Given a player id, find the player with that id
	 * @param playerNumber The player id to find
	 * @return The player with the specified id
	 * @throws IllegalArgumentException if playerNumber can
	 * not be found
	 */
	public Player getPlayerByPlayerNumber(int playerNumber)
	{
		for(Player p : getPlayers())
		{
			if(p.getID() == playerNumber)
			{
				return p;
			}
		}
		
		throw new IllegalArgumentException("There is no player with number: " + playerNumber);
	}
	
	/**
	 * The current combat phase
	 * @return The combat phase
	 */
	public CombatPhase getCurrentCombatPhase()
	{
		return currentCombatPhase;
	}
	
	/**
	 * The hex where combat is taking place
	 * @return The combat hex
	 */
	public HexState getCombatHex()
	{
		return board.getHexByXY(combatLocation.x, combatLocation.y);
	}

	/**
	 * The coordinates of the hex where combat is taking place,
	 * or null if no combat is happening
	 * @return The location of the combat hex, or null if
	 * there is not combat
	 */
	public Point getCombatLocation()
	{
		return combatLocation;
	}
	
	/**
	 * The player that is acting as the defender
	 * @return Defending player
	 */
	public Player getDefendingPlayer()
	{
		return getPlayerByPlayerNumber(defenderPlayerNumber);
	}

	
	/**
	 * The ID of the player that is acting as the defender
	 * @return Defending player id
	 */
	public int getDefendingPlayerNumber()
	{
		return defenderPlayerNumber;
	}
	
	/**
	 * Find out if the game is currently waiting for a 
	 * player to roll for something
	 * @return True if someone needs to roll a die,
	 * false otherwise
	 */
	public boolean isWaitingForRolls()
	{
		for(Roll r : rolls)
		{
			if(r.needsRoll())
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Given a roll, find out if someone has elected to modify
	 * that roll, through paying gold or using a magic card.
	 * @param r The roll to check for
	 * @return True if the given roll ought to be modified,
	 * false otherwise
	 */
	public boolean hasRollModificationFor(Roll r)
	{
		for(RollModification rm : rollModifications)
		{
			if(Roll.rollSatisfiesParameters(rm.getRollToModify(), r.getRollReason(), r.getRollingPlayerID(), r.getRollTarget(), r.getDiceCount()))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Given a roll, find all recorded modifications that need to be
	 * applied to it.
	 * @param r The roll to check for
	 * @return List of all roll modifications that need to be applied
	 * to the given roll
	 */
	public List<RollModification> getRollModificationsFor(Roll r)
	{
		ArrayList<RollModification> modifications = new ArrayList<RollModification>();
		for(RollModification rm : rollModifications)
		{
			if(Roll.rollSatisfiesParameters(rm.getRollToModify(), r.getRollReason(), r.getRollingPlayerID(), r.getRollTarget(), r.getDiceCount()))
			{
				modifications.add(rm);
			}
		}
		return Collections.unmodifiableList(modifications);
	}
	
	/**
	 * Checks if there are any players who still have to apply
	 * hits to their creatures
	 * @return True if some players still need to apply hits,
	 * false otherwise
	 */
	public boolean hitsToApply()
	{
		for (int hitNumber : hitsToApply.values())
		{
			if(hitNumber > 0)
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Retrieves the current number of hits that need to be
	 * applied to a particular player
	 * @param id The player id of the player
	 * @return The number of hits they have taken
	 */
	public int getHitsOnPlayer(int id)
	{
		return hitsToApply.get(id);
	}
	
	/**
	 * Gets a list of all rolls recently made that we might not
	 * need to wait for
	 * @return List of completed rolls
	 */
	public List<Roll> getFinishedRolls()
	{
		ArrayList<Roll> finishedRolls = new ArrayList<Roll>();
		for(Roll r : rolls)
		{
			if(!r.needsRoll())
			{
				finishedRolls.add(r);
			}
		}
		
		return Collections.unmodifiableList(finishedRolls);
	}
	
	/**
	 * Get all of the rolls the game has recently recorded
	 * @return List of recent rolls
	 */
	public List<Roll> getRecordedRolls()
	{
		return Collections.unmodifiableList(rolls);
	}
	
	/*
	 * Retrieves the set of hexes that have been built during the construction phase
	 */
	public Set<HexState> getHexesContainingBuiltObjects() 
	{
		return Collections.unmodifiableSet(hexesContainingBuiltObjects);
	}
	
	
	/** setters **/
	

	/**
	 * Keeps track of a roll that needs to be
	 * made
	 * @param roll The roll that must be made
	 */
	public void addNeededRoll(Roll roll)
	{
		rolls.add(roll);
	}
	
	/**
	 * Remove a roll from the list of rolls that
	 * need to be made
	 * @param roll The roll to remove
	 */
	public void removeRoll(Roll roll)
	{
		rolls.remove(roll);
	}

	/**
	 * Given a roll modification, at it to this object's list
	 * of recorded roll modifications that need to be performed
	 * @param modification The modification to record
	 * @return True if the item was added successfully, false otherwise
	 */
	public boolean addRollModification(RollModification modification)
	{
		return rollModifications.add(modification);
	}
	
	/**
	 * Given a list of roll modifications, remove all elements from the list
	 * that occur in this game state.
	 * @param modifications The list of roll modifications to remove
	 * @return True if at least one element from the supplied list was
	 * removed from this object
	 */
	public boolean removeRollModifications(Collection<RollModification> modifications)
	{
		boolean oneRemoved = false;
		for(RollModification rm : modifications)
		{
			oneRemoved = removeRollModification(rm) || oneRemoved;
		}
		
		return oneRemoved;
	}

	/**
	 * Given a roll modification, remove it from the list of roll modifications
	 *  that occur in this game state.
	 * @param modifications The roll modification to remove
	 * @return True if the supplied modification was removed from this object
	 */
	public boolean removeRollModification(RollModification modification)
	{
		return rollModifications.remove(modification);
	}
	
	/**
	 * Add a number to the amount of hits that a player
	 * has currently taken
	 * @param playerNumber The player that has been hit
	 * @param hitCount The number of hits to add
	 */
	public void addHitsToPlayer(int playerNumber, int hitCount)
	{
		int totalHits = hitsToApply.get(playerNumber) + hitCount;
		hitsToApply.put(playerNumber, totalHits);
	}
	
	/**
	 * Remove a number of hits from the amount of hits that a
	 * player has currently taken
	 * @param playerNumber The player that is applying hits
	 * @param hitCount The number to remove
	 */
	public void removeHitsFromPlayer(int playerNumber, int hitCount)
	{
		int totalHits = hitsToApply.get(playerNumber) - hitCount;
		hitsToApply.put(playerNumber, totalHits);
	}

	/**
	 * Remove all recorded combat hits against all players
	 */
	public void removeAllHitsFromAllPlayers()
	{
		for(Player p : players)
		{
			hitsToApply.put(p.getID(), 0);
		}
	}
	public void setPlayersTarget(int id, int targetId)
	{
		playerTargets.put(id, targetId);
	}
	
	public void clearAllPlayerTargets()
	{
		for(Player p : players)
		{
			playerTargets.put(p.getID(),null);
		}
	}

	/**
	 * Clears the list of rolls that need to
	 * be made
	 */
	public void removeAllRecordedRolls()
	{
		rolls.clear();
	}
	
	public void removeAllRecordedRollModifications()
	{
		rollModifications.clear();
	}
	
	/**
	 * Records that a hex needs to have a certain number of creatures removed from
	 * it, this could happen from the dark plague, or retreating in combat to an
	 * overcrowded hex, etc
	 * @param hex The hex that things need to be removed from
	 * @param numberOfCreturesToRemove The number of creatures that must be removed
	 * from that hex
	 */
	public void addHexThatNeedsThingsRemoved(HexState hex, int numberOfCreaturesToRemove) 
	{
		if(numberOfCreaturesToRemove <= 0)
		{
			hexesThatNeedThingsRemoved.remove(hex);
		}
		else
		{
			hexesThatNeedThingsRemoved.put(hex,numberOfCreaturesToRemove);
		}
	}

	/**
	 * Call this method to update the recorded number of things that need to be removed
	 * from a hex.
	 * @param hex The hex that things need to be removed from
	 * @param numberOfCreturesToRemove The number of creatures that must still be removed
	 * from the hex
	 */
	public void updateHexThatNeedsThingsRemoved(HexState hex, int numberOfCreaturesToRemove)
	{
		addHexThatNeedsThingsRemoved(hex,numberOfCreaturesToRemove);
	}
	
	/**
	 * Returns number of things to be removed from hexes
	 */
	public int getThingsToRemoveFromHex(HexState hex)
	{
		Integer val = hexesThatNeedThingsRemoved.get(hex);
		return val==null? 0 : val;
	}
	
	/**
	 * Call this to check if there are any hexes that players still need to remove
	 * things from
	 * @return True if some hexes need to have things removed from them, false otherwise
	 */
	public boolean hasHexesThatNeedThingsRemoved()
	{
		return hexesThatNeedThingsRemoved.size()>0;
	}
	
	/**
	 * Sets the current board
	 * @param board The game board
	 */
	public void setBoard(HexBoard board)
	{
		this.board = board;
	}
	
	/**
	 * Set the set of players currently playing the game
	 * @param players The players of the game
	 */
	public void setPlayers(Set<Player> players)
	{
		players.addAll(players);
	}
	
	/**
	 * Set a list of player ids indicating the player order
	 * @param playerOrder List indicating player order
	 */
	public void setPlayerOrder(List<Integer> playerOrder)
	{
		this.playerOrder.addAll(playerOrder);
	}
	
	/**
	 * Set the current setup phase of the game
	 * @param phase The setup phase of the game
	 */
	public void setCurrentSetupPhase(SetupPhase phase)
	{
		currentSetupPhase = phase;
	}
	
	/**
	 * Set the current regular phase of the game
	 * @param The regular phase of the game
	 */
	public void setCurrentRegularPhase(RegularPhase phase)
	{
		currentRegularPhase = phase;
	}
	
	/**
	 * Set the player who needs to move next for the current
	 * phase
	 * @param id The player who needs to move next for this phase
	 */
	public void setActivePhasePlayer(int id)
	{
		for(Player p : getPlayers())
		{
			p.getPlayerInfo().setIsActive(p.getID() == id);
		}
	}
	
	public Player getAttackerByIndex(int index)
	{
		int defenderIndex = playerOrder.indexOf(defenderPlayerNumber);
		Player nextPlayer = getPlayerByPlayerNumber(playerOrder.get((defenderIndex + index)%playerOrder.size()));
		return getPlayersStillFightingInCombatHex().contains(nextPlayer)? nextPlayer : null;
	}

	/**
	 * Set the player who's turn it is
	 * @param id The player who's turn it is
	 */
	public void setActiveTurnPlayer(int id)
	{
		activeTurnPlayerNumber = id;
	}
	
	/**
	 * The current combat phase
	 * @param combatPhase The combat phase
	 */
	public void setCurrentCombatPhase(CombatPhase combatPhase)
	{
		currentCombatPhase = combatPhase;
	}

	/**
	 * The coordinates of the hex where combat is taking place,
	 * or null if no combat is happening
	 * @param location The location of the combat hex, or null if
	 * there is not combat
	 */
	public void setCombatLocation(Point location)
	{
		combatLocation = location;
	}
	
	/**
	 * @param id The ID of the player that is acting as the defender
	 */
	public void setDefendingPlayerNumber(int id)
	{
		defenderPlayerNumber = id;
	}
	
	/**
	 * Adds a hex with a built-in object to hexes with built-in objects
	 */
	public void addHexToListOfConstructedHexes(HexState newHex) 
	{
		hexesContainingBuiltObjects.add(newHex);
	}
	
	/**
	 * Removes all the hexes with built in objects
	 */
	public void removeAllHexesWithBuiltInObjects() 
	{
		hexesContainingBuiltObjects.clear();
	}
	
	/**
	 * Checks victory conditions to see if someone has won the game yet,
	 * @return The player who won the game, or null if there is still no winner.
	 */
	public Player getWinningPlayer()
	{
		HashSet<HexState> hexesWithCitadels = new HashSet<HexState>();
		
		for(HexState hs : board.getHexesAsList())
		{
			ITileProperties building = hs.getBuilding();
			if(building!=null && building.getName().equals(Building.Citadel.name()))
			{
				hexesWithCitadels.add(hs);
			}
		}
		if(hexesWithCitadels.size()==1 && !hexesContainingBuiltObjects.contains(hexesWithCitadels.iterator().next()))
		{
			for(Player p : players)
			{
				if(p.ownsThingOnBoard(hexesWithCitadels.iterator().next().getBuilding()))
				{
					return currentRegularPhase == RegularPhase.SPECIAL_POWERS? p : null;
				}
			}
			throw new IllegalStateException("Hex has building with no owner: " + hexesWithCitadels.iterator().next());
		}
		else if(hexesWithCitadels.size()>1)
		{
			for(Player p : players)
			{
				int numCitadels = 0;
				for(ITileProperties tp : p.getOwnedThingsOnBoard())
				{
					if(tp.getName().equals(Building.Citadel.name()))
					{
						numCitadels++;
					}
				}
				if(numCitadels >= 2)
				{
					return p;
				}
			}
		}
		
		return null;
	}

	public void addDoneRolling( int id) {
		confirmedRolls.add( id);
	}

	public void removeDoneRollTie( int id) {
		confirmedRolls.remove( id);
	}
	
	public boolean allRolled(){
		for( Player player : players){
			if( !confirmedRolls.contains( player.getID())){
				return false;
			}
		}
		return true;
	}
	
	public int getPlayersInCombatIDMask()
	{
		int mask = 0;
		for(Player p : getPlayersStillFightingInCombatHex())
		{
			mask |= p.getID();
		}
		return mask;
	}

	public void notifyClientsOfState(){
		GameStateProgress state = new GameStateProgress();
		state.setPhases( currentSetupPhase, currentRegularPhase, currentCombatPhase);
		state.setFlipped( currentSetupPhase.ordinal() > SetupPhase.PICK_FIRST_HEX.ordinal());
		bankHeroes.getAvailableHeroes().toArray( state.getSpecial(bankHeroes.getAvailableHeroes().size()));
		board.fillArray( state.getHexes( board.getBoardSize()));
		state.setPlayersAndRacks( players);
		state.postNetworkEvent( ALL_PLAYERS_ID);
	}
}
