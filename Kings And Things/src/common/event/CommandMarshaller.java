package common.event;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import server.event.commands.AbstractCommand;
import server.event.commands.ConstructBuildingCommand;
import server.event.commands.EndPlayerTurnCommand;
import server.event.commands.ExchangeSeaHexCommand;
import server.event.commands.ExchangeThingsCommand;
import server.event.commands.GiveHexToPlayerCommand;
import server.event.commands.MovementCommand;
import server.event.commands.PaidRecruitsCommand;
import server.event.commands.PlaceThingOnBoardCommand;
import server.event.commands.RequestStartCommand;
import server.event.commands.StartGameCommand;
import common.Logger;

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
	public static String marshalCommand(AbstractCommand command)
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
	public static AbstractCommand unmarshalCommand(String command)
	{
		try
		{
			Unmarshaller um = JC.createUnmarshaller();
			return (AbstractCommand) um.unmarshal(new StringReader(command));
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
											ExchangeThingsCommand.class, GiveHexToPlayerCommand.class, MovementCommand.class,
											PaidRecruitsCommand.class, PlaceThingOnBoardCommand.class, StartGameCommand.class,
											RequestStartCommand.class);
		}
		catch (JAXBException e)
		{
			Logger.getErrorLogger().error("Unable to create JAXBContext due to: ", e);
			return null;
		}
	}
}
