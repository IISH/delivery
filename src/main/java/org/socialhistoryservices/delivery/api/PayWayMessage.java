package org.socialhistoryservices.delivery.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents messages from and to PayWay.
 */
public class PayWayMessage extends TreeMap<String, Object> {
    public static final int ORDER_NOT_PAYED = 0;
    public static final int ORDER_PAYED = 1;
    public static final int ORDER_REFUND_OGONE = 2;
    public static final int ORDER_REFUND_BANK = 3;

    public static final int ORDER_OGONE_PAYMENT = 0;
    public static final int ORDER_BANK_PAYMENT = 1;
    public static final int ORDER_CASH_PAYMENT = 2;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public PayWayMessage() {
    }

    public PayWayMessage(Map<? extends String, ?> message) {
        putAll(message);
    }

    /**
     * Adds or overwrites a value for a valid parameter.
     *
     * @param parameter The parameter to add.
     * @param value     The value to add.
     */
    @Override
    public Object put(String parameter, Object value) {
        parameter = parameter.toUpperCase().trim();

        // If value is ok, put both the parameter and the value into the map
        if ((value != null) && !value.toString().trim().isEmpty()) {
            if (value instanceof Date) {
                Date date = (Date) value;
                return super.put(parameter, DATE_FORMAT.format(date));
            }
            else {
                return super.put(parameter, value.toString().trim());
            }
        }
        else {
            return null;
        }
    }

    /**
     * Returns the value for the given property.
     *
     * @param parameter The parameter to obtain the value of.
     * @return The corresponding value.
     */
    public Object get(String parameter) {
        parameter = parameter.toUpperCase().trim();

        if (this.containsKey(parameter)) {
            return super.get(parameter);
        }
        else {
            return null;
        }
    }

    /**
     * Removes the mapping for this key from this TreeMap if present.
     *
     * @param key The key for which mapping should be removed.
     * @return The previous value associated with the key.
     */
    @Override
    public Object remove(Object key) {
        String parameter = key.toString().toUpperCase().trim();
        return super.remove(parameter);
    }

    /**
     * Returns the string value for the given property.
     *
     * @param parameter The parameter to obtain the value of.
     * @return The corresponding value.
     */
    public String getString(String parameter) {
        Object value = get(parameter);
        return (value != null) ? value.toString().trim() : null;
    }

    /**
     * Returns the integer value for the given property.
     *
     * @param parameter The parameter to obtain the value of.
     * @return The corresponding value.
     */
    public Integer getInteger(String parameter) {
        String value = getString(parameter);
        return (value != null) ? Integer.parseInt(value) : null;
    }

    /**
     * Returns the long value for the given property.
     *
     * @param parameter The parameter to obtain the value of.
     * @return The corresponding value.
     */
    public Long getLong(String parameter) {
        String value = getString(parameter);
        return (value != null) ? Long.parseLong(value) : null;
    }

    /**
     * Returns the boolean value for the given property.
     *
     * @param parameter The parameter to obtain the value of.
     * @return The corresponding value.
     */
    public Boolean getBoolean(String parameter) {
        String value = getString(parameter);
        return (value != null) ? Boolean.parseBoolean(value) : null;
    }

    /**
     * Returns the date for the given property.
     *
     * @param parameter The parameter to obtain the value of.
     * @return The corresponding value.
     */
    public Date getDate(String parameter) {
        try {
            String value = getString(parameter);
            return (value != null) ? DATE_FORMAT.parse(value) : null;
        }
        catch (ParseException pe) {
            return null;
        }
    }
}
