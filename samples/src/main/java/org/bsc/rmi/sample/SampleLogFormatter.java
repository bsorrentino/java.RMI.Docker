package org.bsc.rmi.sample;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import static java.util.Optional.ofNullable;

public class SampleLogFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {

        final String st = ofNullable(record.getThrown()).map( ex -> {
            final StringWriter sw = new StringWriter();
            ex.printStackTrace( new PrintWriter(sw));
            return sw.toString();
        } ).orElse("");

        return String.format( "%s %s.%s\n%d - %s%s\n",
                record.getLevel(),
                record.getLoggerName(),
                record.getSourceMethodName(),
                //record.getMillis(),
                record.getThreadID(),
                record.getMessage(),
                st
                );

    }
}
