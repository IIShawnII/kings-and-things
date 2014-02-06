package common.event;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import server.event.commands.Command;
import common.Logger;

public class NotificationMarshaller
{
	private static final JAXBContext JC = createJaxbContext();
	
	/**
	 * Use this method to convert any notification to a String to be sent from server to client
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
			return JAXBContext.newInstance();
		}
		catch (JAXBException e)
		{
			Logger.getErrorLogger().error("Unable to create JAXBContext due to: ", e);
			return null;
		}
	}
}
