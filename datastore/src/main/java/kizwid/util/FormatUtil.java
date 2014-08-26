package kizwid.util;

/**
 * User: sandkev
 * Date: 2011-09-16
 */

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

public class FormatUtil {
    public static final String PATTERN_YYYYMMDD_NO_DELIM = "yyyyMMdd";
    public static final String PATTERN_YYYYMMDD_DASH_DELIM = "yyyy-MM-dd";
    public static final String PATTERN_HHMMSS_COLON_DELIM = "HH:mm:ss";
    public static final String PATTERN_HHMM_NO_DASH_DELIM = "HHmm";
    public static final String PATTERN_HHMM_SS_MS_DASH_DELIM = "HHmm-ss-SSS";
    public static final String PATTERN_MILLIS_SUFFIX = ".SSS";
    public static final String PATTERN_SQL_DATETIME = PATTERN_YYYYMMDD_DASH_DELIM + " " + PATTERN_HHMMSS_COLON_DELIM + PATTERN_MILLIS_SUFFIX;
    public static final String PATTERN_YYYYMMDD_HHMM = PATTERN_YYYYMMDD_NO_DELIM + "_HHmm";

    public static final DecimalFormatThreadLocal VALUE_FORMAT_2DP = new DecimalFormatThreadLocal("0.##");


    private static class DecimalFormatThreadLocal extends ThreadLocal {

        private final String decimalPattern;

        DecimalFormatThreadLocal(String decimalPattern) {
            this.decimalPattern = decimalPattern;
        }

        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat(decimalPattern);
        }

        public DecimalFormat getDecimalFormat() {
            return (DecimalFormat) super.get();
        }

    }

    private static class DateFormatSupplier {
        private static final ConcurrentHashMap<String, ThreadLocal<DateFormat>> localFormatsByPattern = new ConcurrentHashMap<String, ThreadLocal<DateFormat>>();

        public static DateFormat getFormat(final String pattern) {
            return getFormat(pattern, TimeZone.getDefault());
        }

        public static DateFormat getFormat(final String pattern, final TimeZone timeZone) {
            String key = String.format("%s [%s]", pattern, timeZone.getID());
            ThreadLocal<DateFormat> localFormat;
            localFormat = localFormatsByPattern.get(key);
            if (localFormat == null) {
                localFormat = new ThreadLocal<DateFormat>() {
                    @Override
                    protected DateFormat initialValue() {
                        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                        simpleDateFormat.setTimeZone(timeZone);
                        simpleDateFormat.setLenient(false);
                        return simpleDateFormat;
                    }
                };
                localFormatsByPattern.putIfAbsent(key, localFormat);
            }
            return localFormat.get();
        }
    }


    public FormatUtil() {
    }

    public static String formatSqlDateTime(Date date) {
        return formatSqlDateTime(date, TimeZone.getDefault());
    }

    public static String formatSqlDateTime(Date date, TimeZone timeZone) {
        return DateFormatSupplier.getFormat(PATTERN_SQL_DATETIME, timeZone).format(date);
    }

    public static String yyyymmdd_hhmm(long time) {
        return yyyymmdd_hhmm(time, TimeZone.getDefault());
    }

    public static String yyyymmdd_hhmm(long time, TimeZone timeZone) {
        return DateFormatSupplier.getFormat(PATTERN_YYYYMMDD_HHMM, timeZone).format(new Date(time));
    }

    public static String formatHoursMinutes(long time) {
        return DateFormatSupplier.getFormat(PATTERN_HHMM_NO_DASH_DELIM).format(new Date(time));
    }

    public static String formatHoursMinutesSecondsMillis(long time) {
        return DateFormatSupplier.getFormat(PATTERN_HHMM_SS_MS_DASH_DELIM).format(new Date(time));
    }

    public static Date yyyymmddToDate(String date) throws ParseException {
        return yyyymmddToDate(date, TimeZone.getDefault());
    }

    public static Date yyyymmddToDate(String date, TimeZone timeZone) throws ParseException {
        return DateFormatSupplier.getFormat(PATTERN_YYYYMMDD_NO_DELIM, timeZone).parse(date);
    }

    public static String yyyymmdd(Date date) {
        return yyyymmdd(date, TimeZone.getDefault());
    }

    public static String yyyymmdd(Date date, TimeZone timeZone) {
        return DateFormatSupplier.getFormat(PATTERN_YYYYMMDD_NO_DELIM, timeZone).format(date);
    }

    public static Date yyyymmdd_hhmmToDate(String date) throws ParseException {
        return yyyymmdd_hhmmToDate(date, TimeZone.getDefault());
    }

    public static Date yyyymmdd_hhmmToDate(String date, TimeZone timeZone) throws ParseException {
        return DateFormatSupplier.getFormat(PATTERN_YYYYMMDD_HHMM, timeZone).parse(date);
    }

    public static String formatCustomPattern(Date date, String pattern) {
        return formatCustomPattern(date, pattern, TimeZone.getDefault());
    }

    public static String formatCustomPattern(Date date, String pattern, TimeZone timeZone) {
        return DateFormatSupplier.getFormat(pattern, timeZone).format(date);
    }

    public static Date parseCustomPattern(String date, String pattern) throws ParseException {
        return parseCustomPattern(date, pattern, TimeZone.getDefault());
    }

    public static Date parseCustomPattern(String date, String pattern, TimeZone timeZone) throws ParseException {
        return DateFormatSupplier.getFormat(pattern, timeZone).parse(date);
    }

    public static String formatValue(Number number) {
        return VALUE_FORMAT_2DP.getDecimalFormat().format(number);
    }

    /**
     * 123 -> "123rd"<BR>
     * 41 -> "41st"<BR>
     * 494 -> "494th"<BR>
     */
    public static String int2ordinal(int number) {
        // ..1st ..2nd ..3rd ..th conversion
        String sWk = String.valueOf(number);
        if (sWk.length() >= 2) {
            String sLast2 = sWk.substring(sWk.length() - 2, sWk.length());
            if (sLast2.equals("11") || sLast2.equals("12") || sLast2.equals("13"))
                return sWk + "th";
        }
        ;
        char cWk = sWk.charAt(sWk.length() - 1);
        if (cWk == '1') return sWk + "st";
        else if (cWk == '2') return sWk + "nd";
        else if (cWk == '3') return sWk + "rd";
        return sWk + "th";
    }
}
