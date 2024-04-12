package global;

import java.io.Serializable;

public class Config implements Serializable {
    public static final boolean COLOR = true;
    public static final boolean DEBUG = true;
    public static final int DEFAULT_PORT = 12345;
    public static final int ACCESS_LEVEL = 0;
    public static final String JDK_VERSION = System.getProperty("java.version");
    public static final String DATE = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
    public static final String TIME = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
    public static final String VERSION = "alpha";
    public static final long MAX_MEMORY = Runtime.getRuntime().maxMemory();
    public static final long TOTAL_MEMORY = Runtime.getRuntime().totalMemory();
    public static final long FREE_MEMORY = Runtime.getRuntime().freeMemory();
    public static final long USED_MEMORY = TOTAL_MEMORY - FREE_MEMORY;
}
