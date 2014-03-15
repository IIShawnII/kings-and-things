package server.event;

import server.logic.game.GameState;

import common.event.AbstractInternalEvent;

public class SetupPhaseComplete extends AbstractInternalEvent
{
	private final boolean isDemoMode;
	private final GameState currentState;

	public SetupPhaseComplete(boolean isDemoMode, GameState currentState, final Object OWNER){
		super( OWNER);
		this.isDemoMode = isDemoMode;
		this.currentState = currentState;
	}

	public boolean isDemoMode()
	{
		return isDemoMode;
	}

	public GameState getCurrentState()
	{
		return currentState;
	}
}
