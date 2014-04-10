package server.logic.game;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import server.logic.game.handlers.ApplyMagicEventsCommandHandler;
import server.logic.game.handlers.ApplyRandomEventsCommandHandler;
import server.logic.game.handlers.CombatCommandHandler;
import server.logic.game.handlers.CommandHandler;
import server.logic.game.handlers.ConstructBuildingCommandHandler;
import server.logic.game.handlers.MovementCommandHandler;
import server.logic.game.handlers.RecruitSpecialCharacterCommandHandler;
import server.logic.game.handlers.RecruitingThingsCommandHandler;
import server.logic.game.handlers.SetupPhaseCommandHandler;

/**
 * This class is used to create handlers for commands that change the state of a game
 */
public class CommandHandlerManager{
	private final HashSet<CommandHandler> commandHandlers = new HashSet<CommandHandler>();
	
	public CommandHandlerManager()
	{
		commandHandlers.add(new CombatCommandHandler());
		commandHandlers.add(new ConstructBuildingCommandHandler());
		commandHandlers.add(new MovementCommandHandler());
		commandHandlers.add(new RecruitingThingsCommandHandler());
		commandHandlers.add(new SetupPhaseCommandHandler());
		commandHandlers.add(new RecruitSpecialCharacterCommandHandler());
		commandHandlers.add(new ApplyRandomEventsCommandHandler());
		commandHandlers.add(new ApplyMagicEventsCommandHandler());
	}
	
	/**
	 * call this method to initialize this class before sending any commands
	 */
	public void initialize(){
		for(CommandHandler ch : commandHandlers)
		{
			ch.initialize();
		}
	}
	
	/**
	 * call this method when you are done with the instance
	 */
	public void dispose(){
		for(CommandHandler ch : commandHandlers)
		{
			ch.dispose();
		}
	}
	
	/**
	 * This method is useful for unit testing purposes,
	 * which does not use our event driven architecture
	 * @return The current state of the game, in it's
	 * entirety
	 */
	Set<CommandHandler> getCommandHandlers()
	{
		return Collections.unmodifiableSet(commandHandlers);
	}
}
