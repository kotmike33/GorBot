package WebParser;

import Config.ConfigurationManager;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PostFilter {
    public static boolean isRestricted(String textBlocks){
        ConfigurationManager configurationManager = new ConfigurationManager("filter");
        for(String filter: configurationManager.getAllValues()){
            if (textBlocks.contains(filter)){
                return true;
            }
        }
        return false;
    }
    public static void addFilter(String filterValue){
        ConfigurationManager configurationManager = new ConfigurationManager("filter");

        if(configurationManager.isRecorded(filterValue)){

        }else {
            String lastKey;
            try {
                lastKey = configurationManager.getLastKey();
            }catch (Exception e){
                lastKey = "0";
            }
            lastKey = String.valueOf(Integer.parseInt(lastKey)+1);
            configurationManager.setConfigValue(lastKey, StringEscapeUtils.unescapeJava(filterValue));
        }
    }
    public static void deleteFilter(String filterValue){
        ConfigurationManager configurationManager = new ConfigurationManager("filter");
        configurationManager.deleteKey(configurationManager.getConfigKey(filterValue));
    }
    public static List<String> listFilters(){
        List<String> filters = new ArrayList<>();
        ConfigurationManager configurationManager = new ConfigurationManager("filter");
        for(String key: configurationManager.getAllKeys()){
            filters.add(configurationManager.getConfigValue(key));
        }
        return filters;
    }
}
