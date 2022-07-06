package it.nexera.ris.common.helpers;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogHelper {
    public static String readStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    public static void log(Log log, Exception e) {
        log.error(e);
        log.error(LogHelper.readStackTrace(e));
    }

    public static void log(Log log, Throwable e) {
        log.error(e);
        log.error(LogHelper.readStackTrace(e));
    }

    public static void log(Log log, String msg) {
        log.error(msg);
    }

    public static void debugInfo(Log log, String msg) {
        log.info(msg);
    }

    //Logger section

    public static void log(Logger logger, Exception e) {
        logger.log(logger.getLevel(), readStackTrace(e));
    }

    public static void log(Logger logger, String msg) {
        logger.log(logger.getLevel(), msg);
    }

    public static void log(Logger logger, Throwable e) {
        logger.log(logger.getLevel(), readStackTrace(e));
    }

    public static void debugInfo(Logger logger, String msg) {
        logger.info(msg.concat("\r\n"));
        System.out.println(msg);
    }
}
