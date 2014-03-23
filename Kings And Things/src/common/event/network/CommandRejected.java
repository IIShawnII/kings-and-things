package common.event.network;

import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.event.AbstractInternalEvent;
import common.game.PlayerInfo;

public class CommandRejected extends AbstractInternalEvent
{
	private final RegularPhase currentRegularPhase;
	private final SetupPhase currentSetupPhase;
	private final PlayerInfo currentActivePlayer;
	private final String message;
	
	public CommandRejected(RegularPhase regularPhase, SetupPhase setupPhase, PlayerInfo player, String msg)
	{
		currentRegularPhase = regularPhase;
		currentSetupPhase = setupPhase;
		currentActivePlayer = player;
		message = msg;
	}

	public RegularPhase getCurrentRegularPhase()
	{
		return currentRegularPhase;
	}

	public SetupPhase getCurrentSetupPhase()
	{
		return currentSetupPhase;
	}

	public PlayerInfo getCurrentActivePlayer()
	{
		return currentActivePlayer;
	}
	
	public String getErrorMessage()
	{
		return message;
	}
}
