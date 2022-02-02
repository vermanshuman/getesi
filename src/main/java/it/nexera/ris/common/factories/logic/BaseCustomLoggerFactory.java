package it.nexera.ris.common.factories.logic;

import org.apache.log4j.*;

import java.io.File;

public class BaseCustomLoggerFactory {
    protected final static Long DEFFAULT_MAX_FILE_SIZE = 5000000L;

    protected final static Integer DEFFAULT_MAX_BACKUP_INDEX = 5;

    protected final static Level DEFFAULT_LOG_LEVEL = Level.INFO;

    protected final static Boolean DEFFAUL_USE_CATALINA_LOG_DIRECTORY = Boolean.TRUE;

    protected final static String CATALINA_LOG_DIRECTORY = System.getProperty("catalina.home")
            + File.separator
            + "logs"
            + File.separator;

    protected final static String DEFFAULT_LOG_PATH = "";

    private static void baseInitLogger(Logger logger, String fileName,
                                       String path, Boolean useCatalinaLogDirectory, Long maxFileSize,
                                       Integer maxBackupIndex, Level logLevel) {
        try {
            if (logger.getAppender("fileAppender_" + fileName) == null) {
                SimpleLayout layout = new SimpleLayout();
                RollingFileAppender fileAppender = null;

                String filePath = "";
                if (useCatalinaLogDirectory) {
                    filePath += CATALINA_LOG_DIRECTORY;
                }
                filePath += path;

                fileAppender = new RollingFileAppender(layout, filePath
                        + fileName, true);

                fileAppender.setMaximumFileSize(maxFileSize);
                fileAppender.setMaxBackupIndex(maxBackupIndex);

                fileAppender.setName("fileAppender_" + fileName);
                fileAppender
                        .setLayout(new PatternLayout("%d{ISO8601} [%5p] %m"));

                logger.addAppender(fileAppender);
                logger.setLevel(logLevel);
            }
        } catch (Exception e) {
            //It can be only in case of incorrect logger setting
            e.printStackTrace();
        }
    }

    protected static void initLogger(Logger logger, String fileName,
                                     String path, Long maxFileSize, Integer maxBackupIndex,
                                     Boolean useCatalinaLogDirectory, Level logLevel) {
        baseInitLogger(logger, fileName, path, useCatalinaLogDirectory,
                maxFileSize, maxBackupIndex, logLevel);
    }

    protected static void initLogger(Logger logger, String fileName,
                                     String path, Long maxFileSize, Integer maxBackupIndex,
                                     Level logLevel) {
        baseInitLogger(logger, fileName, path,
                DEFFAUL_USE_CATALINA_LOG_DIRECTORY, maxFileSize,
                maxBackupIndex, logLevel);
    }

    protected static void initLogger(Logger logger, String fileName,
                                     Level logLevel) {
        baseInitLogger(logger, fileName, DEFFAULT_LOG_PATH,
                DEFFAUL_USE_CATALINA_LOG_DIRECTORY, DEFFAULT_MAX_FILE_SIZE,
                DEFFAULT_MAX_BACKUP_INDEX, logLevel);
    }

    public static Logger getCustomLogger(String fileName, String path,
                                         Long maxFileSize, Integer maxBackupIndex,
                                         Boolean useCatalinaLogDirectory, Level logLevel) {
        Logger logger = Logger.getLogger(fileName);

        initLogger(logger, fileName, path, maxFileSize, maxBackupIndex,
                useCatalinaLogDirectory, logLevel);

        return logger;
    }

    public static Logger getCustomLogger(String fileName, String path,
                                         Long maxFileSize, Integer maxBackupIndex, Level logLevel) {
        Logger logger = Logger.getLogger(fileName);

        initLogger(logger, fileName, path, maxFileSize, maxBackupIndex,
                logLevel);

        return logger;
    }

    public static Logger getCustomLogger(String loggerFileName, Level logLevel) {
        Logger logger = Logger.getLogger(loggerFileName);

        initLogger(logger, loggerFileName, logLevel);

        return logger;
    }

    public static Logger getCustomLogger(String loggerFileName) {
        Logger logger = Logger.getLogger(loggerFileName);

        initLogger(logger, loggerFileName, DEFFAULT_LOG_LEVEL);

        return logger;
    }
}
