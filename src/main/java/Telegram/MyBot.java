package Telegram;

import Config.ConfigurationManager;
import DEBUG.Debug;
import WebParser.Parser;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class MyBot extends TelegramLongPollingBot {
    private static Long adminID;
    public static void setAdminID(Long id){
        adminID = id;
    }
    public static Long getAdminID(){
        return adminID;
    };
    private static Long channelChatID;
    public static Long getChannelChatID(){
        return channelChatID;
    }
    public static void setChannelChatID(Long chatID){
        channelChatID = chatID;
    }
    public void reportErrorCode(String errorCode){
        ConfigurationManager configurationManager = new ConfigurationManager("config");
        SendMessage message = new SendMessage();
        message.setChatId(configurationManager.getConfigValue("dev_channel_chat_id"));
        message.setText(errorCode);
        try {
            execute(message);
        } catch (TelegramApiException ee) {
            ee.printStackTrace();
        }
    }
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()) {
            Debug.functionDebug("update received. the text is: " + update.getMessage().getText());
            if(!Objects.equals(update.getMessage().getChatId(), getAdminID())){
                sendMessageToUser("GO AWAY! YOU ARE NOT ADMIN \uD83D\uDE21",update.getMessage().getChatId());
            }else {
                String updateText = update.getMessage().getText();
                UpdateHandler updateHandler = new UpdateHandler(updateText, "");
                if (updateHandler.isCommandGetInfo()) {
                    updateHandler.handleCommandGetInfo();
                }else if (updateHandler.isCommandAddPostFromLink()) {
                    updateHandler.handleCommandAddPostFromLink();
                } else if (updateHandler.isWaitingForLinksFromAdminMode()) {
                    updateHandler.handleIsWaitingForLinksFromAdminMode();
                } else if(updateHandler.isCommandAddFilter()){
                    updateHandler.handleCommandAddFilter();
                } else if(updateHandler.isCommandRemoveFilter()){
                    updateHandler.handleCommandRemoveFilter();
                } else if(updateHandler.isCommandListFilters()){
                    updateHandler.handleCommandListFilters();
                } else if(UpdateHandler.isWaitingForAddFilter){
                    updateHandler.handleCommandAddFilter();
                } else if(UpdateHandler.isWaitingForRemoveFilter){
                    updateHandler.handleCommandRemoveFilter();
                }else if (updateHandler.isCommandSleep()) {
                    updateHandler.handleCommandSleep();
                } else if (updateHandler.isCommandUpdateHashInternal()) {
                    updateHandler.handleCommandUpdateHashInternal();
                } else if (updateHandler.isCommandGetHashInternal()) {
                    updateHandler.handleCommandGetHashInternal();
                } else if (updateHandler.isCommandForceRunParser()) {
                    updateHandler.handleCommandForceRunParser();
                } else if (updateHandler.isCommandNewPost()) {
                    updateHandler.handleCommandNewPost();
                } else if (updateHandler.isCommandListPosts()) {
                    updateHandler.handleCommandListPosts();
                }else if (updateHandler.isCommandUpdateHash()) {
                    updateHandler.handleCommandUpdateHash();
                }else if (updateHandler.isCommandGetHash()) {
                    updateHandler.handleCommandGetHash();
                }else if(updateHandler.isCommandRemoveAllPostsFromQueue()){
                    updateHandler.handleCommandRemoveAllPostsFromQueue();
                }
            }
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            Debug.functionDebug("update received. the CALLBACK is: " + callbackData);
            UpdateHandler updateHandler = new UpdateHandler("",callbackData);
            if (updateHandler.isPostsListCallBack()){
                updateHandler.handleCallBackForPostsList();
            } else if(updateHandler.isListFilterCallBack()){
                updateHandler.handleListFiltersCallBack();
            }
        }
    }
    public Message sendMessageToUser(String text, Long chatID){
        SendMessage message = new SendMessage();
        message.setChatId(chatID);
        message.setText(text);
        message.setParseMode(ParseMode.HTML);
        try {
            return execute(message);
        } catch (TelegramApiException e) {
            reportErrorCode(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    public void sendPostToChannel(String text, String photoLink) {
        StringBuilder debugContentPictures = new StringBuilder();

        boolean isHoroscope = photoLink==null;

        List<String> messages = splitMessage(text, isHoroscope);
        SendMessage message = new SendMessage();
        message.setChatId(getChannelChatID());
        message.setParseMode(ParseMode.HTML);

        SendPhoto sendPhoto = new SendPhoto();
        if(!isHoroscope) {
            sendPhoto.setChatId(getChannelChatID());
            sendPhoto.setPhoto(new InputFile(photoLink));
            sendPhoto.setParseMode(ParseMode.HTML);
        }

        try{
            boolean counter = false;
            for (String textPart : messages) {
                if(!counter && !isHoroscope){
                    sendPhoto.setCaption(textPart);
                    execute(sendPhoto);
                    counter = true;
                }else {
                    message.setText(textPart);
                    execute(message);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private List<String> splitMessage(String message, boolean isHoroscope) {
        final int maxLength = 4095;
        final int maxCaptionLength = 1024;
        List<String> result = new ArrayList<>();
        String[] paragraphs = message.split("\\n\\s*\\n");

        StringBuilder currentMessage = new StringBuilder();
        int currentLength = 0;

        int tempLength;
        if(!isHoroscope) {
            tempLength = maxCaptionLength;
        }else {
            tempLength = maxLength;
        }
        for (String paragraph : paragraphs) {
            if (currentLength + paragraph.length() > tempLength) {
                result.add(currentMessage.toString());
                currentMessage = new StringBuilder();
                currentLength = 0;
                tempLength = maxLength;
            }

            currentMessage.append(paragraph).append("\n").append("\n");
            currentLength += paragraph.length();
        }

        if (currentMessage.length() > 0) {
            result.add(currentMessage.toString());
        }

        return result;
    }
    public void sendTelegramMessageWithButtons(Long chatId, List<String> buttonLabels) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Select a post to publish:");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (String buttonLabel : buttonLabels) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonLabel);
            button.setCallbackData("postsListReply_" + buttonLabel.charAt(0));

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public void sendFiltersMessageWithButtons(List<String>filters){
        if(filters.size()>0) {
            SendMessage message = new SendMessage();
            message.setChatId(getAdminID());
            message.setText("Select a filter to delete:");

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            for (String filter : filters) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(filter);
                button.setCallbackData("filtersListReply_" + filter);

                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(button);
                keyboard.add(row);
            }
            keyboardMarkup.setKeyboard(keyboard);
            message.setReplyMarkup(keyboardMarkup);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }else {
            sendMessageToUser("No Filters found",MyBot.getAdminID());
        }
    }
    public void pinMessageInAdminChat(Message sentMessage){
        try {
            PinChatMessage pinChatMessage = new PinChatMessage();
            pinChatMessage.setChatId(MyBot.getAdminID());
            pinChatMessage.setMessageId(sentMessage.getMessageId());
            execute(pinChatMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public void unpinMessageInAdminChat(){
        try {
            UnpinChatMessage unpinChatMessage = new UnpinChatMessage();
            unpinChatMessage.setChatId(MyBot.getAdminID());
            execute(unpinChatMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    @Override
    public String getBotUsername() {
        return "TelegramChannelBot";
    }
    @Override
    public String getBotToken() {
        ConfigurationManager config = new ConfigurationManager("config");
        if(Debug.isDevBuild){
            return config.getConfigValue("bot_token_dev");
        }
        return config.getConfigValue("bot_token");
    }
    private final Object updateLock = new Object();
    private final int maxUpdatesPerFetch = 100;
    private long lastUpdateId = 0;
    private void startFetchingTelegramUpdates() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    GetUpdates getUpdates = new GetUpdates();
                    getUpdates.setOffset((int) (lastUpdateId + 1));
                    List<Update> updates = execute(getUpdates);

                    int updatesProcessed = 0;
                    for (Update update : updates) {
                        synchronized (updateLock) {
                            if (update.getUpdateId() > lastUpdateId) {
                                onUpdateReceived(update);
                                lastUpdateId = update.getUpdateId();
                            }
                        }
                        updatesProcessed++;
                        if (updatesProcessed >= maxUpdatesPerFetch) {
                            break;
                        }
                    }
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 300);
    }
    private void startHoroscopeUpdates() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Parser parser = new Parser();
                parser.parseFreshHoroscopeLink();
            }
        }, 500, getRandomUpdatePeriod(2));
    }
    private void startPostsUpdates() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Parser parser = new Parser();
                parser.parseFreshPosts();
            }
        }, 300, getRandomUpdatePeriod(1));
    }
    private long getRandomUpdatePeriod(int multiplier) {
        Random random = new Random();
        long min = 800000L*multiplier;
        long max = 1200000L*multiplier;

        return min + (long) (random.nextDouble() * (max - min + 1));
    }
    private void startCollectingStats() {
        if(!Debug.isDevBuild){
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    StatsReporter statsReporter = new StatsReporter();
                    statsReporter.reportSubscribing();
                }
            }, 0, 300000);
        }
    }
    public static void main(String[] args) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            Debug.isDevBuild = true;
            Debug.PROJECT_PATH = "C:/Users/kotmi/Documents/GitHub/GorBot/src/main/";
            System.out.println("Dev environment is set up");
        }else {
            Debug.isDevBuild = false;
            Debug.PROJECT_PATH = "/root/GorBot/";
        }
        Debug debug = new Debug();
        debug.cleanLogs();

        System.out.println("ManagerBot Started !!!");

        MyBot myBot = new MyBot();
        Debug.functionDebug(myBot.getBotToken());

        ConfigurationManager configurationManager = new ConfigurationManager("config");
        setAdminID(Long.valueOf(configurationManager.getConfigValue("adminID")));
        if(Debug.isDevBuild){
            setChannelChatID(Long.valueOf(configurationManager.getConfigValue("dev_channel_chat_id")));
        }else {
            setChannelChatID(Long.valueOf(configurationManager.getConfigValue("channel_chat_id")));
        }

        myBot.startFetchingTelegramUpdates();
        myBot.startHoroscopeUpdates();
        myBot.startPostsUpdates();
        myBot.startCollectingStats();

        ScheduledPostSender executor = new ScheduledPostSender();
        executor.startScheduledPostSending();
    }
}