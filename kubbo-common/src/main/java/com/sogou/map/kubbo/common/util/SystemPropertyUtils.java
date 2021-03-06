package com.sogou.map.kubbo.common.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.regex.Pattern;

import com.sogou.map.kubbo.common.logger.Logger;
import com.sogou.map.kubbo.common.logger.LoggerFactory;

/**
 * A collection of utility methods to retrieve and parse the values of the Java system properties.
 * notice: system env will override system property
 * eg. KUBBO_CONFIGURATION will override kubbo.configuration 
 */
public final class SystemPropertyUtils {

    private static final Logger logger = LoggerFactory.getLogger(SystemPropertyUtils.class);

    /**
     * Returns {@code true} if and only if the system property with the specified {@code key}
     * exists.
     */
    public static boolean contains(String key) {
        return get(key) != null;
    }

    /**
     * Returns the value of the Java system property with the specified
     * {@code key}, while falling back to {@code null} if the property access fails.
     *
     * @return the property value or {@code null}
     */
    public static String get(String key) {
        return get(key, null);
    }
    
    
    public static void set(final String key, final String value){
        //system proprerty
        try {
            if (System.getSecurityManager() == null) {
                System.setProperty(key, value);
            } else {
                AccessController.doPrivileged(new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return System.setProperty(key, value);
                    }
                });
            }
        } catch (Exception e) {
            logger.warn(String.format("Unable to set system property '{}'.", key), e);
        }
    }

    /**
     * Returns the value of the Java system property with the specified
     * {@code key}, while falling back to the specified default value if
     * the property access fails.
     * @param key property
     * @param def default property value
     * @return the property value.
     *         {@code def} if there's no such property or if an access to the
     *         specified property is not allowed.
     */
    public static String get(final String key, String def) {
        if (key == null) {
            throw new IllegalArgumentException("key == NULL");
        }
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key must not be empty.");
        }

        //env
        String envKey = key.toUpperCase().replaceAll("[^0-9A-Z]", "_");
        String value = System.getenv(envKey);
        if(value != null){
            return value;
        }
        //system proprerty
        try {
            if (System.getSecurityManager() == null) {
                value = System.getProperty(key);
            } else {
                value = AccessController.doPrivileged(new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return System.getProperty(key);
                    }
                });
            }
        } catch (Exception e) {
            logger.warn(String.format("Unable to retrieve a system property '{}'; default values will be used.", key), e);
        }

        if (value == null) {
            return def;
        }

        return value;
    }

    /**
     * Returns the value of the Java system property with the specified
     * {@code key}, while falling back to the specified default value if
     * the property access fails.
     *
     * @return the property value.
     *         {@code def} if there's no such property or if an access to the
     *         specified property is not allowed.
     */
    public static boolean getBoolean(String key, boolean def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim().toLowerCase();
        if (value.isEmpty()) {
            return true;
        }

        if ("true".equals(value) || "yes".equals(value) || "1".equals(value)) {
            return true;
        }

        if ("false".equals(value) || "no".equals(value) || "0".equals(value)) {
            return false;
        }

        logger.warn(
                String.format("Unable to parse the boolean system property '{}':{} - using the default value: {}",
                key, value, def)
        );

        return def;
    }

    private static final Pattern INTEGER_PATTERN = Pattern.compile("-?[0-9]+");

    /**
     * Returns the value of the Java system property with the specified
     * {@code key}, while falling back to the specified default value if
     * the property access fails.
     *
     * @return the property value.
     *         {@code def} if there's no such property or if an access to the
     *         specified property is not allowed.
     */
    public static int getInt(String key, int def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim().toLowerCase();
        if (INTEGER_PATTERN.matcher(value).matches()) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                // Ignore
            }
        }

        logger.warn(
                String.format("Unable to parse the integer system property '{}':{} - using the default value: {}",
                key, value, def)
        );

        return def;
    }

    /**
     * Returns the value of the Java system property with the specified
     * {@code key}, while falling back to the specified default value if
     * the property access fails.
     *
     * @return the property value.
     *         {@code def} if there's no such property or if an access to the
     *         specified property is not allowed.
     */
    public static long getLong(String key, long def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim().toLowerCase();
        if (INTEGER_PATTERN.matcher(value).matches()) {
            try {
                return Long.parseLong(value);
            } catch (Exception e) {
                // Ignore
            }
        }

        logger.warn(
                String.format("Unable to parse the long integer system property '{}':{} - using the default value: {}",
                key, value, def)
        );

        return def;
    }
    
    /**
     * NaV (not a value)
     * @param value
     * @return not a value
     */
    public static boolean isNaV(String value) {
        return value == null || value.length() == 0 
                || "null".equalsIgnoreCase(value) 
                || "N/A".equalsIgnoreCase(value);
    }

    private SystemPropertyUtils() {
        // Unused
    }
}

