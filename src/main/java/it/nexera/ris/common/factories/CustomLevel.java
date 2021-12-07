package it.nexera.ris.common.factories;

import org.apache.log4j.Level;

public class CustomLevel extends Level {
    private static final long serialVersionUID = 4641115434060788463L;

    public static final int LIB_ERROR_INT = ERROR_INT - 1;

    public static final Level LIB_ERROR = new CustomLevel(LIB_ERROR_INT,
            "LIB_ERROR", 10);

    public static final int LIB_INFO_INT = INFO_INT + 1;

    public static final Level LIB_INFO = new CustomLevel(LIB_INFO_INT,
            "LIB_INFO", 10);

    public static final int LIB_WARN_INT = WARN_INT - 1;

    public static final Level LIB_WARN = new CustomLevel(LIB_WARN_INT,
            "LIB_WARN", 10);

    public static final int HL7_INFO_INT = INFO_INT + 2;

    public static final Level HL7_INFO = new CustomLevel(HL7_INFO_INT,
            "HL7_INFO", 10);

    public static final int ACTIVITY_INFO_INT = INFO_INT + 2;

    public static final Level ACTIVITY_INFO = new CustomLevel(ACTIVITY_INFO_INT,
            "ACTIVITY_INFO", 10);

    protected CustomLevel(int levelInt, String levelName, int syslogEquivalent) {
        super(levelInt, levelName, syslogEquivalent);
    }
}
