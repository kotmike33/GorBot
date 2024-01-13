package Config;

import DEBUG.Debug;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConfigurationManager {
	public ConfigurationManager(String filePath){
		this.configFile = Debug.PROJECT_PATH + "resources/" + filePath + ".properties";
		properties = new Properties();
		loadConfig();
	}
	private Properties properties;
	private String configFile;

	private void loadConfig() {
		try (InputStream inputStream = new FileInputStream(configFile)) {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveConfig() {
		try (OutputStream outputStream = new FileOutputStream(configFile)) {
			properties.store(outputStream, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getConfigValue(String key) {
		return properties.getProperty(key);
	}

	public String getConfigKey(String value) {
		for (String key : properties.stringPropertyNames()) {
			if (properties.getProperty(key).equals(value)) {
				return key;
			}
		}
		return null;
	}

	public void setConfigValue(String key, String value) {
		properties.setProperty(key, value);
		saveConfig();
	}
	public boolean isRecorded(String text){
		if(properties.containsKey(text)){
			return true;
		}
		if(properties.contains(text)){
			return true;
		}
		return false;
	}
	public List<String> getAllKeys(){
		return new ArrayList<>(properties.stringPropertyNames());
	}
	public List<String> getAllValues() {
		List<String> values = new ArrayList<>();
		for (String key : properties.stringPropertyNames()) {
			values.add(properties.getProperty(key));
		}
		return values;
	}
	public void cleanConfig(){
		properties.clear();
		saveConfig();
	}
	public String getLastKey(){
		return getAllKeys().get(getAllKeys().size()-1);
	}
	public void deleteKey(String key){
		properties.remove(key);
		saveConfig();
	}
	public void deleteValue(String value){
		properties.remove(getConfigKey(value));
		saveConfig();
	}
}