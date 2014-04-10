package server.logic.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import server.event.GameStarted;
import server.event.internal.ApplyHitsCommand;
import server.event.internal.ConstructBuildingCommand;
import server.event.internal.DiscardThingsCommand;
import server.event.internal.EndPlayerTurnCommand;
import server.event.internal.ExchangeSeaHexCommand;
import server.event.internal.ExchangeThingsCommand;
import server.event.internal.GiveHexToPlayerCommand;
import server.event.internal.ModifyRollForSpecialCharacterCommand;
import server.event.internal.MoveThingsCommand;
import server.event.internal.PlaceThingOnBoardCommand;
import server.event.internal.RecruitThingsCommand;
import server.event.internal.RemoveThingsFromHexCommand;
import server.event.internal.ResolveCombatCommand;
import server.event.internal.RetreatCommand;
import server.event.internal.RollDiceCommand;
import server.event.internal.TargetPlayerCommand;
import server.logic.game.GameState;
import server.logic.game.handlers.CombatCommandHandler;
import server.logic.game.handlers.ConstructBuildingCommandHandler;
import server.logic.game.handlers.MovementCommandHandler;
import server.logic.game.handlers.RecruitSpecialCharacterCommandHandler;
import server.logic.game.handlers.RecruitingThingsCommandHandler;
import server.logic.game.handlers.SetupPhaseCommandHandler;
import common.Constants;
import common.Constants.BuildableBuilding;
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.RollReason;
import common.Constants.SetupPhase;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Player;
import common.game.Roll;

public abstract class PossibleMoveGenerator
{
	public static HashMap<Action,GameState> getAllPossibleActionsFromState(boolean isDemoMode, GameState state)
	{
		HashMap<Action,GameState> possibleMoves = new HashMap<Action,GameState>();
		
		if(state.getCombatHex() != null)
		{
			for(Player p : state.getPlayersStillFightingInCombatHex())
			{
				int hitsToApply = state.getHitsOnPlayer(p.getID());
				if(hitsToApply>0 && (state.getCurrentCombatPhase() == CombatPhase.APPLY_MAGIC_HITS || state.getCurrentCombatPhase() == CombatPhase.APPLY_MELEE_HITS
						|| state.getCurrentCombatPhase() == CombatPhase.APPLY_RANGED_HITS))
				{
					for(ITileProperties tp : state.getCombatHex().getFightingThingsInHex())
					{
						if(p.ownsThingOnBoard(tp))
						{
							try
							{
								GameState clonedState = state.clone();
								ApplyHitsCommand command = new ApplyHitsCommand(1, tp);
								command.setID(p.getID());
								CombatCommandHandler handler = new CombatCommandHandler();
								handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
								handler.receiveApplyHitsCommand(command);
								possibleMoves.put(new Action(command), clonedState);
							}
							catch(Throwable t)
							{
								//invalid move
							}
						}
					}
				}
				else if(state.getCurrentCombatPhase() == CombatPhase.ATTACKER_ONE_RETREAT || state.getCurrentCombatPhase() == CombatPhase.ATTACKER_TWO_RETREAT || state.getCurrentCombatPhase() == CombatPhase.ATTACKER_THREE_RETREAT
						|| state.getCurrentCombatPhase() == CombatPhase.DEFENDER_RETREAT)
				{
					for(HexState adjacentHex : state.getBoard().getAdjacentHexesTo(state.getCombatHex().getHex()))
					{
						try
						{
							GameState clonedState = state.clone();
							RetreatCommand command = new RetreatCommand(adjacentHex.getHex());
							command.setID(p.getID());
							CombatCommandHandler handler = new CombatCommandHandler();
							handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
							handler.receiveRetreatCommand(command);
							possibleMoves.put(new Action(command), clonedState);
						}
						catch(Throwable t)
						{
							//invalid move
						}
					}
				}
				else if(state.getCurrentCombatPhase() == CombatPhase.SELECT_TARGET_PLAYER)
				{
					for(Player otherPlayer : state.getPlayersStillFightingInCombatHex())
					{
						if(!p.equals(otherPlayer))
						{
							try
							{
								GameState clonedState = state.clone();
								TargetPlayerCommand command = new TargetPlayerCommand(otherPlayer.getID());
								command.setID(p.getID());
								CombatCommandHandler handler = new CombatCommandHandler();
								handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
								handler.receiveTargetPlayerCommand(command);
								possibleMoves.put(new Action(command), clonedState);
							}
							catch(Throwable t)
							{
								//invalid move
							}
						}
					}
				}
				else if(state.getCurrentCombatPhase() == CombatPhase.MAGIC_ATTACK || state.getCurrentCombatPhase() == CombatPhase.RANGED_ATTACK || state.getCurrentCombatPhase() == CombatPhase.MELEE_ATTACK)
				{
					for(ITileProperties tp : state.getCombatHex().getThingsInHexOwnedByPlayer(p))
					{
						try
						{
							GameState clonedState = state.clone();
							RollDiceCommand command = new RollDiceCommand(new Roll(1, tp, RollReason.ATTACK_WITH_CREATURE, p.getID()));
							command.setID(p.getID());
							CombatCommandHandler handler = new CombatCommandHandler();
							handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
							handler.receiveRollDiceCommand(command);
							possibleMoves.put(new Action(command), clonedState);
						}
						catch(Throwable t)
						{
							//invalid move
						}
					}
				}
				else if(state.getCurrentCombatPhase() == CombatPhase.DETERMINE_DAMAGE)
				{
					for(ITileProperties tp : state.getCombatHex().getThingsInHexOwnedByPlayer(p))
					{
						try
						{
							GameState clonedState = state.clone();
							RollDiceCommand command = new RollDiceCommand(new Roll(1,tp,RollReason.CALCULATE_DAMAGE_TO_TILE,p.getID()));
							command.setID(p.getID());
							CombatCommandHandler handler = new CombatCommandHandler();
							handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
							handler.receiveRollDiceCommand(command);
							possibleMoves.put(new Action(command), clonedState);
						}
						catch(Throwable t)
						{
							//invalid move
						}
					}
				}
				else if(state.getCurrentCombatPhase() == CombatPhase.DETERMINE_DEFENDERS)
				{
					try
					{
						GameState clonedState = state.clone();
						RollDiceCommand command = new RollDiceCommand(new Roll(1,state.getCombatHex().getHex(),RollReason.EXPLORE_HEX,p.getID()));
						command.setID(p.getID());
						CombatCommandHandler handler = new CombatCommandHandler();
						handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
						handler.receiveRollDiceCommand(command);
						possibleMoves.put(new Action(command), clonedState);
					}
					catch(Throwable t)
					{
						//invalid move
					}
				}
			}
		}
		Player p = state.getActivePhasePlayer();
		if(state.getCurrentSetupPhase() == SetupPhase.PLACE_FREE_TOWER || (state.getCurrentSetupPhase() == SetupPhase.SETUP_FINISHED && state.getCurrentRegularPhase() == RegularPhase.CONSTRUCTION))
		{
			for(ITileProperties tp : p.getOwnedHexes())
			{
				for(BuildableBuilding b : BuildableBuilding.values())
				{
					try
					{
						ConstructBuildingCommand command = new ConstructBuildingCommand(b, tp);
						command.setID(p.getID());
						GameState clonedState = state.clone();
						ConstructBuildingCommandHandler handler = new ConstructBuildingCommandHandler();
						handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
						handler.recieveConstructBuildingCommand(command);
						possibleMoves.put(new Action(command), clonedState);
					}
					catch(Throwable t)
					{
						//invalid move
					}
				}
			}
		}
		else if(state.getCurrentSetupPhase() == SetupPhase.DETERMINE_PLAYER_ORDER)
		{
			try
			{
				GameState clonedState = state.clone();
				RollDiceCommand command = new RollDiceCommand(new Roll(1,null,RollReason.DETERMINE_PLAYER_ORDER,p.getID()));
				command.setID(p.getID());
				CombatCommandHandler handler = new CombatCommandHandler();
				handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
				handler.receiveRollDiceCommand(command);
				possibleMoves.put(new Action(command), clonedState);
			}
			catch(Throwable t)
			{
				//invalid move
			}
		}
		if(p.hasCardsInHand() && p.getTrayThings().size() == Constants.MAX_RACK_SIZE)
		{
			HashSet<ITileProperties> discardableThings = new HashSet<ITileProperties>(p.getCardsInHand());
			discardableThings.addAll(p.getTrayThings());
			for(ITileProperties tp : discardableThings)
			{
				try
				{
					ArrayList<ITileProperties> list = new ArrayList<ITileProperties>();
					list.add(tp);
					DiscardThingsCommand command = new DiscardThingsCommand(list);
					command.setID(p.getID());
					GameState clonedState = state.clone();
					RecruitingThingsCommandHandler handler = new RecruitingThingsCommandHandler();
					handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
					handler.receiveDiscardThingsCommand(command);
					possibleMoves.put(new Action(command), clonedState);
				}
				catch(Throwable t)
				{
					//invalid move
				}
			}
		}
		try
		{
			EndPlayerTurnCommand command = new EndPlayerTurnCommand();
			command.setID(p.getID());
			GameState clonedState = state.clone();
			RecruitingThingsCommandHandler handler = new RecruitingThingsCommandHandler();
			handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
			handler.recieveEndPlayerTurnCommand(command);
			possibleMoves.put(new Action(command), clonedState);
		}
		catch(Throwable t)
		{
			//invalid move
		}
		if(state.getCurrentSetupPhase() == SetupPhase.EXCHANGE_SEA_HEXES)
		{
			ITileProperties startingHex = p.getOwnedHexes().iterator().next();
			for(HexState tp : state.getBoard().getAdjacentHexesTo(startingHex))
			{
				try
				{
					ExchangeSeaHexCommand command = new ExchangeSeaHexCommand(tp.getHex());
					command.setID(p.getID());
					GameState clonedState = state.clone();
					SetupPhaseCommandHandler handler = new SetupPhaseCommandHandler();
					handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
					handler.recieveExchangeSeaHexCommand(command);
					possibleMoves.put(new Action(command), clonedState);
				}
				catch(Throwable t)
				{
					//invalid move
				}
			}
		}
		else if(state.getCurrentSetupPhase() == SetupPhase.EXCHANGE_THINGS)
		{
			for(Set<ITileProperties> s : getAllCombinations(p.getTrayThings()))
			{
				if(!s.isEmpty())
				{
					try
					{
						ExchangeThingsCommand command = new ExchangeThingsCommand(s);
						command.setID(p.getID());
						GameState clonedState = state.clone();
						RecruitingThingsCommandHandler handler = new RecruitingThingsCommandHandler();
						handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
						handler.recieveExchangeThingsCommand(command);
						possibleMoves.put(new Action(command), clonedState);
					}
					catch(Throwable t)
					{
						//invalid move
					}
				}
			}
		}
		else if(state.getCurrentSetupPhase() == SetupPhase.SETUP_FINISHED && state.getCurrentRegularPhase() == RegularPhase.RECRUITING_THINGS)
		{
			HashSet<Set<ITileProperties>> possibleExchanges = new HashSet<Set<ITileProperties>>();
			for(Set<ITileProperties> s : getAllCombinations(p.getTrayThings()))
			{
				if(s.size() % 2 == 0)
				{
					possibleExchanges.add(s);
				}
			}
			HashSet<Integer> possiblePaymentValues = new HashSet<Integer>();
			for(int i=0; i<=p.getGold() && i<=25; i+=5)
			{
				possiblePaymentValues.add(i);
			}
			for(Set<ITileProperties> exchanges : possibleExchanges)
			{
				for(Integer goldAmount : possiblePaymentValues)
				{
					try
					{
						RecruitThingsCommand command = new RecruitThingsCommand(goldAmount, exchanges);
						command.setID(p.getID());
						GameState clonedState = state.clone();
						RecruitingThingsCommandHandler handler = new RecruitingThingsCommandHandler();
						handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
						handler.receiveRecruitThingsCommand(command);
						possibleMoves.put(new Action(command), clonedState);
					}
					catch(Throwable t)
					{
						//invalid move
					}
				}
			}
		}
		else if(state.getCurrentSetupPhase() == SetupPhase.PICK_FIRST_HEX || state.getCurrentSetupPhase() == SetupPhase.PICK_SECOND_HEX || state.getCurrentSetupPhase() == SetupPhase.PICK_THIRD_HEX)
		{
			for(HexState hs : state.getBoard().getHexesAsList())
			{
				try
				{
					GiveHexToPlayerCommand command = new GiveHexToPlayerCommand(hs.getHex());
					command.setID(p.getID());
					GameState clonedState = state.clone();
					SetupPhaseCommandHandler handler = new SetupPhaseCommandHandler();
					handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
					handler.recieveGiveHexToPlayerCommand(command);
					possibleMoves.put(new Action(command), clonedState);
				}
				catch(Throwable t)
				{
					//invalid move
				}
			}
		}
		else if(state.getCurrentSetupPhase() == SetupPhase.SETUP_FINISHED && state.getCurrentRegularPhase() == RegularPhase.RECRUITING_CHARACTERS)
		{
			if(state.hasRecordedRollForSpecialCharacter())
			{
				
			}
			else
			{
				ArrayList<Integer> possibleGoldModifications = new ArrayList<Integer>();
				for(int i=0; i<p.getGold(); i+=5)
				{
					possibleGoldModifications.add(i);
				}
				for(ITileProperties hero : state.getBankHeroes().getAvailableHeroes())
				{
					for(Integer goldAmount : possibleGoldModifications)
					{
						try
						{
							ModifyRollForSpecialCharacterCommand command = new ModifyRollForSpecialCharacterCommand(goldAmount, hero);
							command.setID(p.getID());
							GameState clonedState = state.clone();
							RecruitSpecialCharacterCommandHandler handler = new RecruitSpecialCharacterCommandHandler();
							handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
							handler.receiveModifyRollForSpecialCharacterCommand(command);
							possibleMoves.put(new Action(command), clonedState);
						}
						catch(Throwable t)
						{
							//invalid move
						}
					}
					try
					{
						RollDiceCommand command = new RollDiceCommand(new Roll(2,hero,RollReason.RECRUIT_SPECIAL_CHARACTER, p.getID()));
						command.setID(p.getID());
						GameState clonedState = state.clone();
						SetupPhaseCommandHandler handler = new SetupPhaseCommandHandler();
						handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
						handler.receiveRollDiceCommand(command);
						possibleMoves.put(new Action(command), clonedState);
					}
					catch(Throwable t)
					{
						//invalid move
					}
				}
			}
		}
		else if(state.getCurrentSetupPhase() == SetupPhase.SETUP_FINISHED && state.getCurrentRegularPhase() == RegularPhase.MOVEMENT)
		{
			for(HexState hs : state.getBoard().getHexesAsList())
			{
				Set<ITileProperties> playerThingsInHex = hs.getThingsInHexOwnedByPlayer(p);
				if(!playerThingsInHex.isEmpty())
				{
					ArrayList<ArrayList<ITileProperties>> possibleMoveHexes = new ArrayList<ArrayList<ITileProperties>>();
					ArrayList<ITileProperties> startingPoint = new ArrayList<ITileProperties>();
					startingPoint.add(hs.getHex());
					possibleMoveHexes.add(startingPoint);
					for(int i=0; i<Constants.MAX_MOVE_SPEED; i++)
					{
						ArrayList<ArrayList<ITileProperties>> movesToAdd = new ArrayList<ArrayList<ITileProperties>>();
						for(ArrayList<ITileProperties> moveChain : possibleMoveHexes)
						{
							if(moveChain.size() == i+1)
							{
								for(HexState adjacentHex : state.getBoard().getAdjacentHexesTo(moveChain.get(moveChain.size() - 1)))
								{
									ArrayList<ITileProperties> hexesToMoveThrough = new ArrayList<ITileProperties>(moveChain);
									hexesToMoveThrough.add(adjacentHex.getHex());
									movesToAdd.add(hexesToMoveThrough);
								}
							}
						}
						possibleMoveHexes.addAll(movesToAdd);
					}
					
					for(Set<ITileProperties> possibleThingsToMove : getAllCombinations(playerThingsInHex))
					{
						if(!possibleThingsToMove.isEmpty())
						{
							for(ArrayList<ITileProperties> moveHexes : possibleMoveHexes)
							{
								if(moveHexes.size()>1)
								{
									try
									{
										MoveThingsCommand command = new MoveThingsCommand(possibleThingsToMove, moveHexes);
										command.setID(p.getID());
										GameState clonedState = state.clone();
										MovementCommandHandler handler = new MovementCommandHandler();
										handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
										handler.recieveMoveThingsCommand(command);
										possibleMoves.put(new Action(command), clonedState);
									}
									catch(Throwable t)
									{
										//invalid move
									}
								}
							}
						}
					}
				}
			}
		}
		HashSet<ITileProperties> thingsToPlace = new HashSet<ITileProperties>(p.getTrayThings());
		thingsToPlace.addAll(p.getCardsInHand());
		for(ITileProperties hex : p.getOwnedHexes())
		{
			for(ITileProperties thing : thingsToPlace)
			{
				try
				{
					PlaceThingOnBoardCommand command = new PlaceThingOnBoardCommand(thing, hex);
					command.setID(p.getID());
					GameState clonedState = state.clone();
					RecruitingThingsCommandHandler handler = new RecruitingThingsCommandHandler();
					handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
					handler.recievePlaceThingOnBoardCommand(command);
					possibleMoves.put(new Action(command), clonedState);
				}
				catch(Throwable t)
				{
					//invalid move
				}
			}
			for(ITileProperties thing : state.getBoard().getHexStateForHex(hex).getThingsInHexOwnedByPlayer(p))
			{
				try
				{
					HashSet<ITileProperties> thingsToRemove = new HashSet<ITileProperties>();
					thingsToRemove.add(thing);
					RemoveThingsFromHexCommand command = new RemoveThingsFromHexCommand(hex, thingsToRemove);
					command.setID(p.getID());
					GameState clonedState = state.clone();
					RecruitingThingsCommandHandler handler = new RecruitingThingsCommandHandler();
					handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
					handler.receiveRemoveThingFromBoardCommand(command);
					possibleMoves.put(new Action(command), clonedState);
				}
				catch(Throwable t)
				{
					//invalid move
				}
			}
		}
		for(HexState contestedHex : state.getBoard().getContestedHexes(state.getPlayers()))
		{
			try
			{
				ResolveCombatCommand command = new ResolveCombatCommand(contestedHex.getHex());
				command.setID(p.getID());
				GameState clonedState = state.clone();
				CombatCommandHandler handler = new CombatCommandHandler();
				handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
				handler.receiveResolveCombatCommand(command);
				possibleMoves.put(new Action(command), clonedState);
			}
			catch(Throwable t)
			{
				//invalid move
			}
		}
		return possibleMoves;
	}
	
	private static <T> Set<Set<T>> getAllCombinations(Collection<T> list)
	{
		return com.google.common.collect.Sets.powerSet(new HashSet<T>(list));
	}
}
