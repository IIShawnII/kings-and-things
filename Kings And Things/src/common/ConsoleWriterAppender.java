package common;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import common.event.EventMonitor;

public class ConsoleWriterAppender extends AppenderSkeleton
{
	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(LoggingEvent msg) {
		EventMonitor.fireEvent(Constants.CONSOLE, msg.getRenderedMessage(), getLevelFromLog4j(msg.getLevel()));
	}
	
	private static Constants.Level getLevelFromLog4j(org.apache.log4j.Level lvl)
	{
		switch(lvl.toInt())
		{
			case org.apache.log4j.Level.FATAL_INT:
			case org.apache.log4j.Level.ERROR_INT: return Constants.Level.Error;
			case org.apache.log4j.Level.WARN_INT: return Constants.Level.Warning;
			case org.apache.log4j.Level.INFO_INT: return Constants.Level.Notice;
			default: return Constants.Level.Plain;
		}
	}
}
