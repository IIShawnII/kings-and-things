package server.event.commands;

import common.event.AbstractInternalEvent;
import server.logic.game.BoardGenerator;
import server.logic.game.CupManager;
import server.logic.game.GameState;
import server.logic.game.HexTileManager;
import server.logic.game.SpecialCharacterManager;

public class SetupPhaseComplete extends AbstractInternalEvent
{
	private final boolean isDemoMode;
	private final CupManager cup;
	private final HexTileManager bank;
	private final BoardGenerator boardGenerator;
	private final GameState currentState;
	private final SpecialCharacterManager bankHeroManager;

	public SetupPhaseComplete(boolean isDemoMode, CupManager cup, HexTileManager bank,	BoardGenerator boardGenerator, GameState currentState, SpecialCharacterManager bankHeroManager, final Object OWNER){
		super( OWNER);
		this.isDemoMode = isDemoMode;
		this.cup = cup;
		this.bank = bank;
		this.boardGenerator = boardGenerator;
		this.currentState = currentState;
		this.bankHeroManager = bankHeroManager;
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
	
	public SpecialCharacterManager getBankHeroManager()
	{
		return bankHeroManager;
	}
}
