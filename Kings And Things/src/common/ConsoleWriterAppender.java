package common;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class ConsoleWriterAppender extends AppenderSkeleton
{
        @Override
        public void close() {}

        @Override
        public boolean requiresLayout() {
                return false;
        }

        @Override
        protected void append(LoggingEvent msg) {}
}
