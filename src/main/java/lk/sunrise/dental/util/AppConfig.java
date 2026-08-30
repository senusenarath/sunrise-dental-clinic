package lk.sunrise.dental.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ================================================================
 * AppConfig.java
 * Singleton Pattern - centralized runtime configuration
 *
 * Loads database and SMTP settings from app.properties on the
 * classpath so credentials live in one external file instead of
 * being hardcoded/duplicated across Java classes.
 * Package : lk.sunrise.dental.util
 * ================================================================
 */
public final class AppConfig {

    private static final String CONFIG_FILE = "app.properties";
    private static AppConfig instance;
    private final Properties props = new Properties();

    private AppConfig() {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Missing " + CONFIG_FILE + " on the classpath. " +
                        "Add src/main/resources/app.properties with db.* and smtp.* settings.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public String get(String key) {
        return props.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
