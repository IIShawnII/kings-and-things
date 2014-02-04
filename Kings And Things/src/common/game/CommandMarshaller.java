package common.game;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import common.Logger;
import common.game.commands.Command;
import common.game.commands.ConstructBuildingCommand;
import common.game.commands.EndPlayerTurnCommand;
import common.game.commands.ExchangeSeaHexCommand;
import common.game.commands.ExchangeThingsCommand;
import common.game.commands.GiveHexToPlayerCommand;
import common.game.commands.PlaceThingOnBoardCommand;
import common.game.commands.RequestStartCommand;
import common.game.commands.StartGameCommand;

/**
 * This class handles converting commands back and forth to Strings so they
 * can be sent across the network
 */
public abstract class CommandMarshaller
{
	private static final JAXBContext JC = createJaxbContext();
	
	/**
	 * Use this method to convert any command to a String to be sent from client to server
	 * or vice versa
	 * @param command The command to send
	 * @return The entered command in String format
	 */
	public static String marshalCommand(Command command)
	{
		try
		{
			StringWriter sw = new StringWriter();
			Marshaller m = JC.createMarshaller();
			m.marshal(command, sw);
			
			return sw.toString();
		}
		catch (JAXBException e)
		{
			Logger.getErrorLogger().error("Unable to marshal command due to: ", e);
			return null;
		}
	}

	/**
	 * 
	 * Use this method to convert any command from a String format back into the
	 * original command
	 * @param command The command in String format
	 * @return The original command
	 */
	public static Command unmarshalCommand(String command)
	{
		try
		{
			Unmarshaller um = JC.createUnmarshaller();
			return (Command) um.unmarshal(new StringReader(command));
		}
		catch (JAXBException e)
		{
			Logger.getErrorLogger().error("Unable to unmarshal command due to: ", e);
			return null;
		}
	}
	
	private static JAXBContext createJaxbContext()
	{
		try
		{
			return JAXBContext.newInstance(ConstructBuildingCommand.class, EndPlayerTurnCommand.class, ExchangeSeaHexCommand.class,
											ExchangeThingsCommand.class, GiveHexToPlayerCommand.class, PlaceThingOnBoardCommand.class,
											StartGameCommand.class, RequestStartCommand.class);
		}
		catch (JAXBException e)
		{
			Logger.getErrorLogger().error("Unable to create JAXBContext due to: ", e);
			return null;
		}
	}
}
