package org.socialhistoryservices.delivery.reproduction.util;

import org.socialhistoryservices.delivery.config.DeliveryProperties;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Various date utility functions.
 */
public class DateUtils {
    /**
     * Determine if the given time is between today->open and today->close of the readingroom.
     *
     * @param properties The properties.
     * @param time       The given time.
     * @return Whether the given time is between opening and closing time.
     */
    public static boolean isBetweenOpeningAndClosingTime(DeliveryProperties properties, Date time) {
        Date open, close;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        format.setLenient(false);

        try {
            open = format.parse(properties.getRequestAutoPrintStartTime());
            close = format.parse(properties.getRequestLatestTime());
        }
        catch (ParseException e) {
            throw new RuntimeException("Failed parsing necessary open and " +
                    "closing dates for auto printing. Are they in the " +
                    "correct HH:mm format in your properties file?");
        }

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(time);

        int createH = cal.get(Calendar.HOUR_OF_DAY);
        int createM = cal.get(Calendar.MINUTE);
        cal.setTime(open);

        int openH = cal.get(Calendar.HOUR_OF_DAY);
        int openM = cal.get(Calendar.MINUTE);
        cal.setTime(close);

        int closeH = cal.get(Calendar.HOUR_OF_DAY);
        int closeM = cal.get(Calendar.MINUTE);

        boolean afterOpen = createH > openH || (createH == openH && createM >= openM);
        boolean beforeClose = createH < closeH || (createH == closeH && createM < closeM);

        return (afterOpen && beforeClose);
    }
}
