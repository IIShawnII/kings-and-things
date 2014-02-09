package server.event;

import common.event.AbstractEvent;

import server.logic.game.BoardGenerator;
import server.logic.game.CupManager;
import server.logic.game.GameState;
import server.logic.game.HexTileManager;

public class GameStarted extends AbstractEvent
{
	private final boolean isDemoMode;
	private final CupManager cup;
	private final HexTileManager bank;
	private final BoardGenerator boardGenerator;
	private final GameState currentState;

	public GameStarted(boolean isDemoMode, CupManager cup, HexTileManager bank,	BoardGenerator boardGenerator, GameState currentState)
	{
		this.isDemoMode = isDemoMode;
		this.cup = cup;
		this.bank = bank;
		this.boardGenerator = boardGenerator;
		this.currentState = currentState;
	}

	public boolean isDemoMode()
	{
		return isDemoMode;
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

	public GameState getCurrentState()
	{
		return currentState;
	}
}
