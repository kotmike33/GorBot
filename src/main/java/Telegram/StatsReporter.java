package Telegram;

import Config.ConfigurationManager;
import DEBUG.Debug;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


public class StatsReporter extends MyBot {
    public void reportSubscribing(){
        int num = 0;
        Debug debug = new Debug();
        ConfigurationManager configurationManager = new ConfigurationManager("config");
        try {
            GetChatMemberCount count = new GetChatMemberCount();
            count.setChatId(configurationManager.getConfigValue("channel_chat_id"));
            num = execute(count);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        if(num!=0){
            if(!configurationManager.isRecorded("subscribersNum")){
                configurationManager.setConfigValue("subscribersNum", String.valueOf(num));
            }else {
                int oldNum = Integer.parseInt(configurationManager.getConfigValue("subscribersNum"));
                if(oldNum<num){
                    configurationManager.setConfigValue("subscribersNum", String.valueOf(num));
                    sendMessageToUser("New subscribers gained: +" + (num-oldNum),MyBot.getAdminID());
                }
                if(oldNum>num){
                    configurationManager.setConfigValue("subscribersNum", String.valueOf(num));
                }
            }
        }
    }
}
