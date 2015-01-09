/**
 * 
 */
package br.com.sedentary.mouseless.main;

/**
 *
 * @author Rodrigo Gomes da Silva
 */
public class Preferences {
    
    // Preference references
    public static final String NETWORK_INTERFACE = "__networkinterface__";
    
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
        return (String) get(p, String.class);
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public static Boolean getBoolean(String p) {
        return (Boolean) get(p, Boolean.class);
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public static byte[] getByteArray(String p) {
        return (byte[]) get(p, byte[].class);
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public static Double getDouble(String p) {
        return (Double) get(p, Double.class);
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public static Float getFloat(String p) {
        return (Float) get(p, Float.class);
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public static Integer getInt(String p) {
        return (Integer) get(p, Integer.class);
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public static Long getLong(String p) {
        return (Long) get(p, Long.class);
    }
    
    /**
     * 
     * @param p 
     * @param c 
     * @return
     */
    private static Object get(String p, Class c) {
        Object pr = null;
        
        if (c.isInstance(new String())) {
            pr = prefs.get(p, null);
        } else if (c.isInstance(Boolean.FALSE)) {
            pr = prefs.getBoolean(p, false);
        } else if (c.isInstance(new byte[] {})) {
            pr = prefs.getByteArray(p, null);
        } else if (c.isInstance((double) 0)) {
            pr = prefs.getDouble(p, 0);
        } else if (c.isInstance((float) 0)) {
            pr = prefs.getFloat(p, 0);
        } else if (c.isInstance((int) 0)) {
            pr = prefs.getInt(p, 0);
        } else if (c.isInstance((long) 0)) {
            pr = prefs.getLong(p, 0);
        }
        
        return pr;
    }
}
