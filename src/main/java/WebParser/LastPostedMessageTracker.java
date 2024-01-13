package WebParser;

import DEBUG.Debug;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class LastPostedMessageTracker {
    private Properties properties;
    private final String configFile = Debug.PROJECT_PATH + "resources/tracker.properties";

    public LastPostedMessageTracker() {
        properties = new Properties();
        loadConfig();
    }

    private void loadConfig() {
        try (InputStream inputStream = new FileInputStream(configFile)) {
            properties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        try (OutputStream outputStream = new FileOutputStream(configFile)) {
            properties.store(outputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getConfigValue(String key) {
        return properties.getProperty(key);
    }

    private void setConfigValue(String key, String value) {
        properties.setProperty(key, value);
        saveConfig();
    }
    public void recordParsedTitle(String title, String key){
        //setConfigValue(key,HashGenerator.getHash(title));
        Debug.functionDebug("Recorded to the tracker: " + title);
    }
    public boolean updateHash(String hash){
        if(HashGenerator.isSHA256Hash(hash)) {
            setConfigValue("lastPost", hash);
            return true;
        }
        return false;
    }
    public String getLastSentHoroscopeTitle(){
        return getConfigValue("horoscope");
    }
    public String getLastSentPostTitle(){
        return getConfigValue("lastPost");
    }
}
