package kizwid.shared.util;

/**
 * User: kizwid
 * Date: 2011-09-16
 */
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatUtil
{
    private static final DateFormatThreadLocal DATE_FORMAT_YYYYMMDD = new DateFormatThreadLocal("yyyyMMdd");
    private static final DateFormatThreadLocal DATE_FORMAT_YYYYMMDD_HHMM = new DateFormatThreadLocal("yyyyMMdd_HHmm");
    private static final DateFormatThreadLocal TIME_FORMAT_HHMM = new DateFormatThreadLocal("HH-mm");
    private static final DateFormatThreadLocal TIME_FORMAT_HHMMSS = new DateFormatThreadLocal("HH:mm:ss");
    private static final DateFormatThreadLocal SQL_DATETIME_FORMAT = new DateFormatThreadLocal("yyyy-MM-dd HH:mm:ss.SSS");
    public static final DecimalFormatThreadLocal VALUE_FORMAT_2DP = new DecimalFormatThreadLocal("0.##");
    public static final DecimalFormatThreadLocal VALUE_FORMAT_0DP = new DecimalFormatThreadLocal("0");

    private static class DecimalFormatThreadLocal extends ThreadLocal
    {

        @Override
        protected DecimalFormat initialValue()
        {
            return new DecimalFormat(decimalPattern);
        }

        public DecimalFormat getDecimalFormat()
        {
            return (DecimalFormat)super.get();
        }

        private final String decimalPattern;

        DecimalFormatThreadLocal(String decimalPattern)
        {
            this.decimalPattern = decimalPattern;
        }
    }

    private static class DateFormatThreadLocal extends ThreadLocal
    {

        @Override
        protected DateFormat initialValue()
        {
            return new SimpleDateFormat(datePattern);
        }

        public DateFormat getDateFormat()
        {
            return (DateFormat)super.get();
        }

        private final String datePattern;

        DateFormatThreadLocal(String datepattern)
        {
            datePattern = datepattern;
        }
    }


    public FormatUtil()
    {
    }

    public static String formatSqlDateTime(Date date)
    {
        return date == null ?
                "1900-01-01 00:00:00":
                SQL_DATETIME_FORMAT.getDateFormat().format(date);
    }

    public static String formatValue(Number number)
    {
        return VALUE_FORMAT_2DP.getDecimalFormat().format(number);
    }

    public static String yyyymmdd_hhmm(long time) {
        return DATE_FORMAT_YYYYMMDD_HHMM.getDateFormat().format(new Date(time));
    }

    public static String formatHoursMinutes(long time) {
        return TIME_FORMAT_HHMM.getDateFormat().format(new Date(time));
    }

    public static String formatHoursMinutesSeconds(long time) {
        return TIME_FORMAT_HHMMSS.getDateFormat().format(new Date(time));
    }

    public static Date yyyymmddToDate(String date) throws ParseException {
        return DATE_FORMAT_YYYYMMDD.getDateFormat().parse(date);
    }

    public static String yyyymmdd(Date date) {
        return DATE_FORMAT_YYYYMMDD.getDateFormat().format(date);
    }

    public static Date yyyymmdd_hhmmToDate(String date) throws ParseException {
        return DATE_FORMAT_YYYYMMDD_HHMM.getDateFormat().parse(date);
    }

}

