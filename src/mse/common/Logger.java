package mse.common;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by michael on 28/09/2015.
 */
public class Logger {

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    LogLevel logLevel;

    public Logger(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public void log(LogLevel logLevel, String message) {
        if (logLevel.value <= this.logLevel.value) {
            Date date = new Date();
            System.out.printf("%s [%s] - %s\n", logLevel.tag, dateFormat.format(date), message);
        }
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

}
