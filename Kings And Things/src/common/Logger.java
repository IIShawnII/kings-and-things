package common;

/**
 * See server.Logic.java for an example on how to use this class. The main difference
 * between the standard logger and the error logger, is the amount of information
 * collected in the log message. To change the formatting, information collected, or
 * logging threshold of the log messages, change the values in the LogSettings/*log4j.properties
 * files, note that this can be done without the need to re-compile.
 */
public abstract class Logger
{
	public static org.apache.log4j.Logger getStandardLogger()
	{
		return org.apache.log4j.Logger.getLogger("standardLogger");
	}

	public static org.apache.log4j.Logger getErrorLogger()
	{
		return org.apache.log4j.Logger.getLogger("errorLogger");
	}
}
