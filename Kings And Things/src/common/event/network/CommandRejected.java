package common.event.network;

import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.Constants.UpdateInstruction;
import common.event.AbstractNetwrokEvent;
import common.game.PlayerInfo;

public class CommandRejected extends AbstractNetwrokEvent
{
	private static final long serialVersionUID = 4882338853179679316L;
	
	private final RegularPhase currentRegularPhase;
	private final PlayerInfo currentActivePlayer;
	private final UpdateInstruction instruction;
	private final SetupPhase currentSetupPhase;
	private final String message;
	
	public CommandRejected(RegularPhase regularPhase, SetupPhase setupPhase, PlayerInfo player, String msg, UpdateInstruction instruction)
	{
		currentRegularPhase = regularPhase;
		currentSetupPhase = setupPhase;
		this.instruction = instruction;
		currentActivePlayer = player;
		message = msg;
	}
	
	public CommandRejected(RegularPhase regularPhase, SetupPhase setupPhase, PlayerInfo player, String msg)
	{
		this( regularPhase, setupPhase, player, msg, null);
	}
	
	public CommandRejected(RegularPhase regularPhase, SetupPhase setupPhase, PlayerInfo player, UpdateInstruction instruction)
	{
		this( regularPhase, setupPhase, player, null, instruction);
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
	
	public UpdateInstruction getInstruction(){
		return instruction;
	}
	
	@Override
	public String toString(){
		return "Network/CommandRejected: ID:" + currentActivePlayer.getID() + ", Message:" + message + ", Instruction: " + instruction + ", Setup: " + currentSetupPhase + ", Regular: " + currentRegularPhase;
	}
}
