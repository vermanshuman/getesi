package it.nexera.ris.common.helpers;

import it.nexera.ris.web.converters.BaseConverter;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateTimeHelper extends BaseHelper {

    public static TimeZone defaultTimeZone;

    static {
        defaultTimeZone = TimeZone.getTimeZone("Europe/Rome").inDaylightTime(new Date())
                ? TimeZone.getTimeZone("GMT+2") : TimeZone.getTimeZone("Europe/Rome");
    }

    public static final Locale defaultLocale = Locale.ITALY;

    private static String datePattern = "dd/MM/yyyy";

    private static String datePatternWithoutCentury = "dd/MM/yy";

    private static String dateTimePdfPattern = "dd/MM/yyyy - HH:mm";

    private static String timePattern = "HH:mm";

    private static String timePatternWithSeconds = "HH:mm:ss";

    private static String mySQLDatePattern = "yyyy-MM-dd";

    private static String mySQLDateTimePattern = "yyyy-MM-dd HH:mm:ss";

    private static String pathDatePattern = "dd-MM-yyyy";

    private static String pathDateTimePattern = "dd-MM-yyyy HH:mm";

    private static String dotsDatePattern = "dd.MM.yyyy";

    private static String datePatternWithMinutes = "dd/MM/yyyy HH:mm";

    private static String datePatternWithSeconds = "dd/MM/yyyy HH:mm:ss";

    private static String datePatternWithMinutesAndSuffix = "dd/MM/yyyy HH:mm a";

    private static String datePatternWithTimezone = "MMM dd yyyy HH:mm Z";

    private static String datePatternForSessionId = "yyyy_MM_dd_HH_mm_ss_SSS";

    private static String datePatternForFileEntity = "_ddMMMyy_HHmmss_SSSSSSSSS_";
    
    private static String datePatternForFileWithMinutes = "ddMMyyyy_HHmm";

    private static String datePatternForFilePath = "\\yyy\\MM\\dd\\";

    private static String datePatternForXlsx = "yyyy/MM/dd";

    private static String xmlDatePattert = "ddMMyyyy";

    private static String xmlDateRegex = "^(((0[1-9]|[12][0-9]|30)[-/]?(0[13-9]|1[012])|31[-/]?(0[13578]|1[02])|(0[1-9]|1[0-9]|2[0-8])[-/]?02)[-/]?[0-9]{4}|29[-/]?02[-/]?([0-9]{2}(([2468][048]|[02468][48])|[13579][26])|([13579][26]|[02468][048]|0[0-9]|1[0-6])00))$";

    private static String xmlSecondDatePattert = "yyyyMMdd";

    private static String xmlSecondDatePattertYear = "yyyy";

    private static String xmlSecondDateRegex = "^([0-9]{4}[-/]?((0[13-9]|1[012])[-/]?(0[1-9]|[12][0-9]|30)|(0[13578]|1[02])[-/]?31|02[-/]?(0[1-9]|1[0-9]|2[0-8]))|([0-9]{2}(([2468][048]|[02468][48])|[13579][26])|([13579][26]|[02468][048]|0[0-9]|1[0-6])00)[-/]?02[-/]?29)$";

    private static String monthWordDatePattert = "dd MMMM yyyy";

    private static DateFormat createDateFormat(String pattern) {
        return createDateFormat(pattern, null);
    }

    private static DateFormat createDateFormat(String pattern, Locale locale) {
        return new SimpleDateFormat(pattern, locale == null ? defaultLocale : locale);
    }

    private static DateFormat fromCalendar = createDateFormat("EEE dd MMM HH:mm:ss z yyyy", Locale.ITALY);

    private static DateFormat dfm = createDateFormat(DateTimeHelper.getDatePatternWithoutCentury());

    private static DateFormat dotsFormatter = createDateFormat(dotsDatePattern, Locale.ITALY);

    public static Date getNow() {
        return new Date();
    }

    public static Date getNinetyNineYearsBefore() {
        return new DateTime(new Date()).minusYears(99).toDate();
    }
    
    public static Date getHundredFiveBefore() {
        return new DateTime(new Date()).minusYears(105).toDate();
    }

    public static Date getDayStart(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    public static Date getDayEnd(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);

        return c.getTime();
    }

    public static Calendar getDayStart(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c;
    }

    public static Calendar getDayEnd(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);

        return c;
    }

    public static Date getDate(Date time) {
        long millisInDay = 60 * 60 * 24 * 1000;
        long currentTime = time.getTime();
        long dateOnly = (currentTime / millisInDay) * millisInDay;
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT"));
        return new Date(dateOnly);
    }

    @SuppressWarnings("deprecation")
    public static boolean DateLessThenMaxDate(Date date, Date maxDate) {
        if (date.getYear() < maxDate.getYear()) {
            return true;
        } else if (date.getYear() == maxDate.getYear()
                && date.getMonth() < maxDate.getMonth()) {
            return true;
        } else if (date.getYear() == maxDate.getYear()
                && date.getMonth() == maxDate.getMonth()
                && date.getDate() <= maxDate.getDate()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean between(Date compareDate, Date startDate, Date endDate) {
        return (compareDate.after(startDate) || compareDate.getTime() == startDate.getTime())
                && (compareDate.before(endDate) || compareDate.getTime() == endDate.getTime());
    }

    public static boolean between(Calendar compareDate, Calendar startDate, Calendar endDate) {
        return (compareDate.after(startDate) || compareDate.getTime().equals(startDate.getTime()))
                && (compareDate.before(endDate) || compareDate.getTime().equals(endDate.getTime()));
    }

    public static Date fromString(String str, String format) {
        return fromString(str, format, null);
    }

    public static Date fromXMLString(String str) {
        if(ValidationHelper.isNullOrEmpty(str))
            return null;
        if (ValidationHelper.checkCorrectFormatByExpression(xmlSecondDateRegex,
                str)) {
            return fromString(str, getXmlSecondDatePattert(), null);
        } else if (ValidationHelper.checkCorrectFormatByExpression(xmlDateRegex,
                str)) {
            Date date = fromString(str, getXmlDatePattert(), null);
            if(date == null)
                date = fromString(str, getDatePattern(), null);
            return date;
        } else {
            return null;
        }
    }


    public static Date fromXMLStringDate(String str) {
        if (!ValidationHelper.isNullOrEmpty(str)) {
            return fromString(str, getXmlSecondDatePattert(), null);
        } else {
            return null;
        }
    }

    public static Date fromXMLStringYear(String str) {
        if (!ValidationHelper.isNullOrEmpty(str)) {
            return fromString(str, getXmlSecondDatePattertYear(), null);
        } else {
            return null;
        }
    }

    public static String fromStringFormater(String date, String format, Locale locale) {
        if (!ValidationHelper.isNullOrEmpty(date)) {
            DateFormat dateFormat = createDateFormat(format, locale);
            return dateFormat.format(new Date(date));
        }
        return null;
    }

    public static Date fromString(String str, String format, Locale locale) {
        if (!ValidationHelper.isNullOrEmpty(str)) {
            DateFormat dateFormat = createDateFormat(format, locale);
            try {
                return dateFormat.parse(str);
            } catch (ParseException e2) {

            }
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    public static Date fromAMPMString(String str) {
        return new Date(str);
    }

    public static synchronized Date fromString(String str) {
        if (!ValidationHelper.isNullOrEmpty(str)) {
            try {
                return dfm.parse(str);
            } catch (ParseException e2) {
                try {
                    return dotsFormatter.parse(str);
                } catch (ParseException e) {

                }
            }
            try {
                return fromCalendar.parse(str);
            } catch (ParseException e1) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String toXMLPatern(Date value) {
        return toFormatedString(value, getXmlDatePattert());
    }

    public static String toStringTime(Date value) {
        return toFormatedString(value, getTimePattern());
    }

    public static String toFileNameString(Date value) {
        return toFormatedString(value, getPathDateTimePattern());
    }

    public static String toString(Date value) {
        return toFormatedString(value, getDatePattern());
    }

    public static String getCurrentDate() {
        return toFormatedString(getNow(), getDatePattern());
    }

    public static String toStringDateWithDots(Date value) {
        return toFormatedString(value, getDotsDatePattern());
    }

    public static String toStringWithMinutes(Date value) {
        return toFormatedString(value, getDatePatternWithMinutes());
    }

    public static String ToStringWithSeconds(Date value) {
        return toFormatedString(value, getDatePatternWithSeconds());
    }

    public static String ToPathString(Date value) {
        return toFormatedString(value, getPathDatePattern());
    }

    public static String toFormatedString(Date value, String format) {
        return toFormatedString(value, format, null);
    }

    public static String ToStringTimeWithSeconds(Date value) {
        return toFormatedString(value, getTimePatternWithSeconds());
    }

    public static String ToDatePatternWithMinutesAndSuffix(Date value) {
        return toFormatedString(value, getDatePatternWithMinutesAndSuffix());
    }

    public static String toSessionTime(Date value) {
        return toFormatedString(value, getDatePatternForSessionId());
    }

    public static String toFileEntityDate(Date value) {
        return toFormatedString(value, getDatePatternForFileEntity());
    }

    public static String toFormatedString(Date value, String format, Locale local) {
        if (ValidationHelper.isNullOrEmpty(value)) return "";
        DateFormat df = createDateFormat(format, local);
        df.setTimeZone(TimeZone.getTimeZone("Europe/Rome").inDaylightTime(value)
                ? TimeZone.getTimeZone("GMT+2") : TimeZone.getTimeZone("Europe/Rome"));
        return df.format(value);
    }

    public static String toFormatedStringLocal(Date value, String format, Locale local) {
        DateFormat df = createDateFormat(format, local);
        if (value != null) {
            return df.format(value);
        }
        return "";
    }

    public static String ToMySqlString(Date value) {
        return toFormatedString(value, getMySQLDatePattern());
    }

    public static String ToFilePathString(Date value) {
        return toFormatedString(value, getDatePatternForFilePath());
    }

    public static String ToMySqlStringWithSeconds(Date value) {
        return toFormatedString(value, getMySQLDateTimePattern());
    }

    public static void setDatePattern(String datePattern) {
        DateTimeHelper.datePattern = datePattern;
    }

    public static String getDatePattern() {
        return datePattern;
    }

    public static String getDatePatternWithoutCentury() {
        return datePatternWithoutCentury;
    }

    public static void setDatePatternWithoutCentury(String datePatternWithoutCentury) {
        DateTimeHelper.datePatternWithoutCentury = datePatternWithoutCentury;
    }

    public static void setMySQLDatePattern(String mySQLDatePattern) {
        DateTimeHelper.mySQLDatePattern = mySQLDatePattern;
    }

    public static String getMySQLDatePattern() {
        return mySQLDatePattern;
    }

    public static void setPathDatePattern(String pathDatePattern) {
        DateTimeHelper.pathDatePattern = pathDatePattern;
    }

    public static String getPathDatePattern() {
        return pathDatePattern;
    }

    public static void setDatePatternWithMinutes(String datePatternWithMinutes) {
        DateTimeHelper.datePatternWithMinutes = datePatternWithMinutes;
    }

    public static String getDatePatternWithMinutes() {
        return datePatternWithMinutes;
    }

    public static String getDateTimePdfPattern() {
        return dateTimePdfPattern;
    }

    public static void setDateTimePdfPattern(String dateTimePdfPattern) {
        DateTimeHelper.dateTimePdfPattern = dateTimePdfPattern;
    }

    public static void setDatePatternWithSeconds(String datePatternWithSeconds) {
        DateTimeHelper.datePatternWithSeconds = datePatternWithSeconds;
    }

    public static String getDatePatternWithSeconds() {
        return datePatternWithSeconds;
    }

    public static void setPathDateTimePattern(String pathDateTimePattern) {
        DateTimeHelper.pathDateTimePattern = pathDateTimePattern;
    }

    public static String getPathDateTimePattern() {
        return pathDateTimePattern;
    }

    public static void setMySQLDateTimePattern(String mySQLDateTimePattern) {
        DateTimeHelper.mySQLDateTimePattern = mySQLDateTimePattern;
    }

    public static String getMySQLDateTimePattern() {
        return mySQLDateTimePattern;
    }

    public static String getTimePattern() {
        return timePattern;
    }

    public static void setTimePattern(String timePattern) {
        DateTimeHelper.timePattern = timePattern;
    }

    public static String getDatePatternForFilePath() {
        return datePatternForFilePath;
    }

    public static void setDatePatternForFilePath(String datePatternForFilePath) {
        DateTimeHelper.datePatternForFilePath = datePatternForFilePath;
    }

    public static String getTimeString(Calendar c) {
        return String.format("%s:%s",
                BaseConverter.convertToHourString(c.get(Calendar.HOUR_OF_DAY)),
                BaseConverter.convertToHourString(c.get(Calendar.MINUTE)));
    }

    public static String getTimePatternWithSeconds() {
        return timePatternWithSeconds;
    }

    public static void setTimePatternWithSeconds(String timePatternWithSeconds) {
        DateTimeHelper.timePatternWithSeconds = timePatternWithSeconds;
    }

    public static String getDotsDatePattern() {
        return dotsDatePattern;
    }

    public static void setDotsDatePattern(String dotsDatePattern) {
        DateTimeHelper.dotsDatePattern = dotsDatePattern;
    }

    public static String getDatePatternWithMinutesAndSuffix() {
        return datePatternWithMinutesAndSuffix;
    }

    public static void setDatePatternWithMinutesAndSuffix(
            String datePatternWithMinutesAndSuffix) {
        DateTimeHelper.datePatternWithMinutesAndSuffix = datePatternWithMinutesAndSuffix;
    }

    public static String getXmlDatePattert() {
        return xmlDatePattert;
    }

    public static void setXmlDatePattert(String xmlDatePattert) {
        DateTimeHelper.xmlDatePattert = xmlDatePattert;
    }

    public static String getXmlSecondDatePattertYear() {
        return xmlSecondDatePattertYear;
    }

    public static void setXmlSecondDatePattertYear(String xmlSecondDatePattertYear) {
        DateTimeHelper.xmlSecondDatePattertYear = xmlSecondDatePattertYear;
    }

    public static String getXmlSecondDatePattert() {
        return xmlSecondDatePattert;
    }

    public static void setXmlSecondDatePattert(String xmlSecondDatePattert) {
        DateTimeHelper.xmlSecondDatePattert = xmlSecondDatePattert;
    }

    public static String getDatePatternWithTimezone() {
        return datePatternWithTimezone;
    }

    public static String getDatePatternForSessionId() {
        return datePatternForSessionId;
    }

    public static void setDatePatternForSessionId(String datePatternForSessionId) {
        DateTimeHelper.datePatternForSessionId = datePatternForSessionId;
    }

    public static String getDatePatternForFileEntity() {
        return datePatternForFileEntity;
    }

    public static void setDatePatternForFileEntity(String datePatternForFileEntity) {
        DateTimeHelper.datePatternForFileEntity = datePatternForFileEntity;
    }

    public static void setDatePatternWithTimezone(String datePatternWithTimezone) {
        DateTimeHelper.datePatternWithTimezone = datePatternWithTimezone;
    }

    public static String getMonthWordDatePattert() {
        return monthWordDatePattert;
    }

    public static void setMonthWordDatePattert(String monthWordDatePattert) {
        DateTimeHelper.monthWordDatePattert = monthWordDatePattert;
    }

    public static Date convertTimeZones(Date date, TimeZone from, TimeZone to) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());

        calendar.setTimeZone(from);
        calendar.add(Calendar.MILLISECOND, from.getRawOffset() * -1);
        if (from.inDaylightTime(calendar.getTime())) {
            calendar.add(Calendar.MILLISECOND, calendar.getTimeZone().getDSTSavings() * -1);
        }

        calendar.add(Calendar.MILLISECOND, to.getRawOffset());
        if (to.inDaylightTime(calendar.getTime())) {
            calendar.add(Calendar.MILLISECOND, to.getDSTSavings());
        }

        calendar.setTimeZone(to);

        return calendar.getTime();
    }

    public static String normalizeTimezone(String str) {
        StringBuilder sb = new StringBuilder(str.substring(0, str.length() - 5));
        sb.append("UTC");

        if (Integer.parseInt(str.substring(str.length() - 4)) != 0) {
            sb.append(str.substring(str.length() - 5, str.length() - 2));
            sb.append(":");
            sb.append(str.substring(str.length() - 2));
        }
        return sb.toString();
    }

    public static TimeZone getTimeZone() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        return c.getTimeZone();
    }

    public static int getHour(String time) {
        return Integer.parseInt(time.substring(0, 2));
    }

    public static int getMinute(String time) {
        return Integer.parseInt(time.substring(3, 5));
    }

    public static int getDateDiffInMin(Date date1, Date date2) {
        return (int) ((date1.getTime() - date2.getTime()) / (1000 * 60));
    }

    public static long getDateDiffInDay(Date date1, Date date2) {
        return (date1.getTime() - date2.getTime()) / (1000 * 60 * 60 * 24);
    }

    public static int getTimeDiff(String time1, String time2) {
        int h1 = getHour(time1);
        int h2 = getHour(time2);

        int m1 = getMinute(time1);
        int m2 = getMinute(time2);

        return ((h2 - h1) * 60 + (m2 - m1));
    }

    public static void setTimeToCalendar(Date date, Calendar calendar) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
    }

    public static void setTimeToCalendar(String time, Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, getHour(time));
        calendar.set(Calendar.MINUTE, getMinute(time));
    }

    public static boolean isTheSameDay(Date dateAssignment, Date currentDate) {
        Date dateAssig = getDayStart(dateAssignment);
        Date dateCurr = getDayStart(currentDate);
        /*This method need for compare two dates without time.
        For example (Fri Mar 20 09:00:00 EET 2015).equals(Fri Mar 20 13:00:00 EET 2015) return false, 
        but (Fri Mar 20 00:00:00 EET 2015).equals(Fri Mar 20 00:00:00 EET 2015) return true */
        return dateAssig.equals(dateCurr);
    }

    public static boolean timeBetweenTwoDate(Date dateFrom, Date dateTo,
                                             String timeFrom, String timeTo, Date dateAssignment) {
        if (isTheSameDay(dateAssignment, dateFrom)) {
            Calendar dateFromTime = Calendar.getInstance();
            Calendar dateToTime = Calendar.getInstance();
            Calendar timeFromTime = Calendar.getInstance();
            Calendar timeToTime = Calendar.getInstance();

            setTimeToCalendar(dateFrom, dateFromTime);
            setTimeToCalendar(dateTo, dateToTime);
            setTimeToCalendar(timeFrom, timeFromTime);
            setTimeToCalendar(timeTo, timeToTime);

            if (between(timeFromTime, dateFromTime, dateToTime)) {
                return true;
            } else if (between(timeToTime, dateFromTime, dateToTime)) {
                return true;
            } else if (between(dateFromTime, timeFromTime, timeToTime)) {
                return true;
            } else if (between(dateToTime, timeFromTime, timeToTime)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean timeLessThanAvailableTime(String startTime,
                                                    String endTime, Long maxTime, Long minTime, Long averageDuration) {
        long availableTime = (maxTime - minTime) * 60;
        long selectedTime = (getHour(endTime) * 60 + getMinute(endTime))
                - (getHour(startTime) * 60 + getMinute(startTime))
                + averageDuration;

        return selectedTime <= availableTime;
    }

    public static long getAvailableAverageDuration(String startTime,
                                                   String endTime, Long maxTime, Long minTime) {
        long availableTime = (maxTime - minTime) * 60;
        long selectedTime = (getHour(endTime) * 60 + getMinute(endTime))
                - (getHour(startTime) * 60 + getMinute(startTime));

        return availableTime - selectedTime;
    }

    public static Integer differenceInMinutes(Date date1, Date date2) {
        Integer difference = null;

        if (date1 != null && date2 != null) {
            if (datesEqual(date1, date2)) {
                return 0;
            } else {
                long time1 = date1.getTime();
                long time2 = date2.getTime();

                return ((int) (time2 - time1) / 1000 / 60);
            }
        }

        return difference;
    }

    public static boolean datesEqual(Date date1, Date date2) {
        if (date1 == null && date2 == null) {
            return true;
        }

        if (date1 == null || date2 == null) {
            return false;
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        if (cal1.getTime().equals(cal2.getTime())) {
            return true;
        }

        return false;
    }

    public static boolean areDatesEqual(Date date1, Date date2) {
        if (date1 == null && date2 == null) {
            return true;
        }

        if (date1 == null || date2 == null) {
            return false;
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        cal1.set(Calendar.HOUR, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        cal1.set(Calendar.HOUR, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        if (cal1.getTime().equals(cal2.getTime())) {
            return true;
        }

        return false;
    }

    public static String getDatePatternForXlsx() {
        return datePatternForXlsx;
    }

    public static void setDatePatternForXlsx(String datePatternForXlsx) {
        DateTimeHelper.datePatternForXlsx = datePatternForXlsx;
    }

    public static Date getTomorrowDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        return cal.getTime();
    }
    
    public static String getFromattedDate(Date deathDate) {
        return DateTimeHelper.toFormatedString(deathDate,
                DateTimeHelper.getMonthWordDatePattert(), Locale.ITALY);
    }
    
    public static Date addDays(Date date, int days) {
        return new DateTime(date).plusDays(days).toDate();
    }

    public static Date minusYears(Date date, int years) {
        return new DateTime(date).minusYears(years).toDate();
    }

    public static String toFileDateWithMinutes(Date value) {
        return toFormatedString(value, datePatternForFileWithMinutes);
    }


    public static String getMonth(int month) {
        DateFormat formatter = new SimpleDateFormat("MMMM", defaultLocale);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, month-1);
        return formatter.format(calendar.getTime());
    }

    public static Date getMonthStart(int month) {
        GregorianCalendar gc = new GregorianCalendar();

        gc.set(GregorianCalendar.MONTH, month-1);
        gc.set(GregorianCalendar.YEAR, getYearOfNow());
        gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
        gc.set(GregorianCalendar.MINUTE, 0);
        gc.set(GregorianCalendar.SECOND, 0);
        gc.set(GregorianCalendar.MILLISECOND, 0);
        return gc.getTime();
    }

    public static Date getMonthEnd(int month) {
        GregorianCalendar gc = new GregorianCalendar();

        gc.set(GregorianCalendar.MONTH, month-1);
        gc.set(GregorianCalendar.YEAR, getYearOfNow());
        gc.set(GregorianCalendar.DAY_OF_MONTH, 31);
        gc.set(GregorianCalendar.HOUR_OF_DAY, 23);
        gc.set(GregorianCalendar.MINUTE, 59);
        gc.set(GregorianCalendar.SECOND, 59);
        gc.set(GregorianCalendar.MILLISECOND, 59);
        return gc.getTime();
    }

    public static Integer getYearOfNow() {
        return getFieldOfNow(GregorianCalendar.YEAR);
    }

    public static Integer getFieldOfNow(Integer field) {
        GregorianCalendar gc = new GregorianCalendar();
        return gc.get(field);
    }

    public static int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return (cal.get(Calendar.MONTH) + 1);
    }
}
