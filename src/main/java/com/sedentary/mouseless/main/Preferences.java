/**
 * 
 */
package com.sedentary.mouseless.main;

/**
 *
 * @author Rodrigo Gomes da Silva
 */
public class Preferences {
    
    // Preference references
    public static final String NETWORK_INTERFACE = "__networkinterface__";
    public static final String SERVER_PORT = "__serverport__";
    
    private static java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot().node(Preferences.class.getName());
    
    /**
     * 
     * @param p
     * @param value
     */
    public static void set(String p, Object value) {
        if (value instanceof String) {
            prefs.put(p, (String) value);
        } else if (value instanceof Boolean) {
            prefs.putBoolean(p, (Boolean) value);
        } else if (value instanceof byte[]) {
            prefs.putByteArray(p, (byte[]) value);
        } else if (value instanceof Double) {
            prefs.putDouble(p, (Double) value);
        } else if (value instanceof Float) {
            prefs.putFloat(p, (Float) value);
        } else if (value instanceof Integer) {
            prefs.putInt(p, (Integer) value);
        } else if (value instanceof Long) {
            prefs.putLong(p, (Long) value);
        }
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public static String get(String p) {
        return (String) get(p, String.class, "");
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public static Boolean getBoolean(String p) {
        return (Boolean) get(p, Boolean.class, false);
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public static byte[] getByteArray(String p) {
        return (byte[]) get(p, byte[].class, new byte[] {});
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public static Double getDouble(String p) {
        return (Double) get(p, Double.class, 0);
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public static Float getFloat(String p) {
        return (Float) get(p, Float.class, 0);
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public static Integer getInt(String p) {
        return (Integer) get(p, Integer.class, 0);
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public static Long getLong(String p) {
        return (Long) get(p, Long.class, 0);
    }
    
    /**
     * Return null in case of exception
     * 
     * @param p 
     * @param c 
     * @return
     */
    public static Object get(String p, Class c, Object defaultValue) {
        try {
            Object pr = null;

            if (c.isInstance(new String())) {
                pr = prefs.get(p, (String) defaultValue);
            } else if (c.isInstance(Boolean.FALSE)) {
                pr = prefs.getBoolean(p, (Boolean) defaultValue);
            } else if (c.isInstance(new byte[] {})) {
                pr = prefs.getByteArray(p, (byte[]) defaultValue);
            } else if (c.isInstance((double) 0)) {
                pr = prefs.getDouble(p, (double) defaultValue);
            } else if (c.isInstance((float) defaultValue)) {
                pr = prefs.getFloat(p, 0);
            } else if (c.isInstance((int) defaultValue)) {
                pr = prefs.getInt(p, 0);
            } else if (c.isInstance((long) defaultValue)) {
                pr = prefs.getLong(p, 0);
            }

            return pr;
        } catch (Exception ex) {
            return null;
        }
    }
}
