package org.bsc.rmi.sample;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SampleLogFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        return String.format( "%s %s.%s\n%d - %s\n",
                record.getLevel(),
                record.getLoggerName(),
                record.getSourceMethodName(),
                //record.getMillis(),
                record.getThreadID(),
                record.getMessage()
                );

    }
}
