package Telegram;

import Config.ConfigurationManager;
import WebParser.HashGenerator;
import WebParser.LastPostedMessageTracker;
import WebParser.Parser;
import WebParser.PostFilter;

public class UpdateHandler extends MyBot{
    public UpdateHandler(String updateText,String callBackData){
        this.text = updateText;
        this.callBackData = callBackData;
    }
    private final String text;
    private final String callBackData;
    private static boolean isWaitingForLinksFromAdmin = false;
    public static boolean isWaitingForAddFilter = false;
    public static boolean isWaitingForRemoveFilter = false;

    public boolean isCommandGetInfo(){
        return text.equals("/get_info");
    }
    public void handleCommandGetInfo(){
        sendMessageToUser(
                "Number of posts in queue: " + Parser.getNumberOfXMLPosts() + "\n" +
                        "Next planned post: " + ScheduledPostSender.getNextPostTime(),
                getAdminID()
        );
    }
    public boolean isCommandNewPost() {
        return text.equals("/post_now");
    }
    public void handleCommandNewPost() {
        Parser parser = new Parser();
        parser.generateMessageFromXML(0,false);
    }
    public boolean isCommandSleep() {
        return text.equals("/sleep");
    }
    public void handleCommandSleep() {
        ConfigurationManager configurationManager = new ConfigurationManager("config");
        if (configurationManager.getConfigValue("is_alive").equals("1")) {
            configurationManager.setConfigValue("is_alive", "0");
            pinMessageInAdminChat(sendMessageToUser("Sleep Mode Activated", getAdminID()));
        } else {
            configurationManager.setConfigValue("is_alive", "1");
            sendMessageToUser("Waking Up!", getAdminID());
            unpinMessageInAdminChat();
        }

    }
    public boolean isCommandUpdateHash(){
        return text.equals("/update_hash");
    }
    public void handleCommandUpdateHash(){
        sendMessageToUser(
                "Use the <code>updateHash:</code><i>your_new_hash</i> syntax to update the last saved hash",
                MyBot.getAdminID()
        );
    }
    public boolean isCommandUpdateHashInternal() {
        return text.startsWith("updateHash:") && HashGenerator.isSHA256Hash(text.replaceAll("updateHash:", ""));
    }
    public void handleCommandUpdateHashInternal() {
        LastPostedMessageTracker tracker = new LastPostedMessageTracker();
        if (tracker.updateHash(text.replaceAll("updateHash:",""))) {
            sendMessageToUser("Updated", getAdminID());
        } else {
            sendMessageToUser("Failed", getAdminID());
        }
    }
    public boolean isCommandGetHash() {
        return text.equals("/get_hash_from_title");
    }
    public void handleCommandGetHash() {
        sendMessageToUser(
                "Use the <code>toHash:</code><i>your_title</i> syntax to get the hash of it",
                getAdminID()
        );
    }
    public boolean isCommandGetHashInternal() {
        return text.startsWith("toHash:");
    }
    public void handleCommandGetHashInternal() {
        sendMessageToUser(
                HashGenerator.getHash(text.replace("toHash:", "")),
                getAdminID()
        );
    }
    public boolean isCommandForceRunParser() {
        return text.equals("/run_parser");
    }
    public void handleCommandForceRunParser() {
        Parser parser = new Parser();
        parser.parseFreshHoroscopeLink();
        parser.parseFreshPosts();
    }
    public boolean isCommandAddPostFromLink() {
        return text.equals("/add_post_from_link");
    }
    public void handleCommandAddPostFromLink() {
        if(!isWaitingForLinksFromAdmin) {
            isWaitingForLinksFromAdmin = true;
            sendMessageToUser(
                    "Waiting for links. Feel free to send 1 link per message. Don't forget to turn this mode off!",
                    getAdminID()
            );
        }else {
            isWaitingForLinksFromAdmin = false;
            sendMessageToUser(
                    "Admin links parsing mode is OFF.",
                    getAdminID()
            );
        }
    }
    public boolean isWaitingForLinksFromAdminMode(){
        return isWaitingForLinksFromAdmin;
    }
    public void handleIsWaitingForLinksFromAdminMode(){
        Parser parser = new Parser();
        /*
        if(parser.parsePostPage(text)){
            sendMessageToUser("Success",getAdminID());
        }else {
            sendMessageToUser("Waiting for links mode is ON. Your message is not link or it was failed to parse.",getAdminID());
        }
        */
    }
    public boolean isCommandListPosts(){
        return text.equals("/list_posts");
    }
    public void handleCommandListPosts(){
        Parser parser = new Parser();
        sendTelegramMessageWithButtons(getAdminID(),parser.listPosts());
    }
    public boolean isPostsListCallBack(){
        return callBackData.contains("postsListReply_");
    }
    public void handleCallBackForPostsList(){
        String index = callBackData.replaceAll("postsListReply_","");
        Parser parser = new Parser();
        parser.generateMessageFromXML(Integer.parseInt(index),false);
    }
    public boolean isCommandRemoveAllPostsFromQueue(){return text.equals("/remove_all_posts_from_xml");}
    public void handleCommandRemoveAllPostsFromQueue(){
        Parser parser = new Parser();
        if(parser.emptyXMLFile()){
            sendMessageToUser("Cleaned",MyBot.getAdminID());
        }else {
            sendMessageToUser("Failed",MyBot.getAdminID());
        }
    }
    public boolean isCommandAddFilter(){
        return text.equals("/add_filter");
    }
    public boolean isCommandRemoveFilter(){
        return text.equals("/remove_filter");
    }
    public boolean isCommandListFilters(){
        return text.equals("/list_filters");
    }
    public void handleCommandAddFilter(){
        if(isWaitingForAddFilter){
            PostFilter.addFilter(text);
            isWaitingForAddFilter=false;
            sendMessageToUser("Done!",MyBot.getAdminID());
        }else {
            sendMessageToUser("Send me the new Filter!",MyBot.getAdminID());
            isWaitingForAddFilter = true;
        }
    }
    public void handleCommandRemoveFilter(){
        if(isWaitingForRemoveFilter){
            PostFilter.deleteFilter(text);
            isWaitingForRemoveFilter=false;
            sendMessageToUser("Done!",MyBot.getAdminID());
        }else {
            sendMessageToUser("Send me the Filter to delete!",MyBot.getAdminID());
            isWaitingForRemoveFilter = true;
        }
    }
    public void handleCommandListFilters(){
        ConfigurationManager configurationManager = new ConfigurationManager("filter");
        sendFiltersMessageWithButtons(configurationManager.getAllValues());
    }
    public boolean isListFilterCallBack(){
        return callBackData.startsWith("filtersListReply_");
    }
    public void handleListFiltersCallBack(){
        try {
            PostFilter.deleteFilter(callBackData.replaceAll("filtersListReply_", ""));
            sendMessageToUser("Done!",MyBot.getAdminID());
        }catch (Exception e){
            sendMessageToUser("Failed to delete.",MyBot.getAdminID());
        }
    }
}
