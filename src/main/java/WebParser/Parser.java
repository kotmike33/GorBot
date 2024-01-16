package WebParser;

import Config.ConfigurationManager;
import DEBUG.Debug;
import Telegram.MyBot;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private List <String> selectorsList = new ArrayList<>();
    public Parser() {
        selectorsList.add("div.cat-row1 a");
        selectorsList.add("div.cat-row2 a:nth-child(1)");
        selectorsList.add("div.cat-row2 a:nth-child(2)");
        selectorsList.add("div.cat-row2 a:nth-child(3)");
        selectorsList.add("div.cat-row3 a:nth-child(1)");
        selectorsList.add("div.cat-row3 a:nth-child(2)");
        selectorsList.add("div.cat-row3 a:nth-child(3)");
    }
    public void parseFreshPosts() {
        Debug debug = new Debug();
        ConfigurationManager configurationManager = new ConfigurationManager("config");
        try {
            LastPostedMessageTracker tracker = new LastPostedMessageTracker();
            Document doc = Jsoup.connect(configurationManager.getConfigValue("posts_source_url")).get();

            String selector;
            for (int i = 0; i<selectorsList.size(); i++) {
                selector = selectorsList.get(i);
                Element postElement = doc.selectFirst(selector);
                assert postElement != null;
                String postTitle = postElement.getElementsByClass("title").get(0).text();
                String href = postElement.attr("href");
                Debug.functionDebug("Current Title: " + postTitle + "\n" + "href = " + href);
                if (postTitle.length() > 1 && href.length() > 1 && HashGenerator.getHash(postTitle).equals(tracker.getLastSentPostTitle())) {
                    Debug.functionDebug("Last parsed Title found !!!");
                    if (i == 0) {
                        break;
                    }

                    for (int x = i - 1; x >= 0; x--) {
                        postElement = doc.selectFirst(selectorsList.get(x));
                        assert postElement != null;
                        postTitle = postElement.getElementsByClass("title").get(0).text();
                        href = postElement.attr("href");

                        Document postPageDoc = null;
                        try {
                            postPageDoc = Jsoup.connect(href).get();
                        } catch (Exception e) {
                            MyBot bot = new MyBot();
                            bot.reportErrorCode(e.getMessage());
                        }
                        if (postPageDoc != null) {
                            PostPage postPage = new PostPage(postPageDoc);
                            postPage.savePostToXML();
                        }
                        tracker.recordParsedTitle(postTitle, "lastPost");
                    }
                    break;
                }
            }
        } catch (IOException e) {
            MyBot bot = new MyBot();
            bot.reportErrorCode(e.getMessage());
            Debug.functionDebug(e.getMessage());
        }
    }
    public void parseFreshHoroscopeLink() {
        Debug debug = new Debug();
        ConfigurationManager configurationManager = new ConfigurationManager("config");
        try {
            LastPostedMessageTracker tracker = new LastPostedMessageTracker();
            Document doc = Jsoup.connect(configurationManager.getConfigValue("source_url")).get();
            Elements postElements = doc.select("div.cat-row1 a");
            Element postElement = postElements.get(0);
            if (postElement != null) {
                String href = postElement.attr("href");
                String postTitle = postElement.getElementsByClass("title").get(0).text();
                Debug.functionDebug("Current Title: " + postTitle + "\n" + "href = " + href);
                if (postTitle.length() > 1 && href.length() > 1 && !HashGenerator.getHash(postTitle).equals(tracker.getLastSentHoroscopeTitle())) {
                    Debug.functionDebug("NEW Title found !!!");

                    Document postPageDoc = null;
                    try {
                        postPageDoc = Jsoup.connect(href).get();
                    }catch (Exception e){
                        MyBot bot =new MyBot();
                        bot.reportErrorCode(e.getMessage());
                    }
                    if(postPageDoc!=null) {
                        PostPage postPage = new PostPage(postPageDoc);
                        postPage.setPostPictureUrl(null);
                        generateHoroscopeMessage(postPage);
                    }
                    tracker.recordParsedTitle(postTitle,"horoscope");
                }
            }
        } catch (IOException e) {
            MyBot bot = new MyBot();
            bot.reportErrorCode(e.getMessage());
            Debug.functionDebug(e.getMessage());
        }
    }
    private static boolean isXMLPostAvailable() {
        try {
            Document doc = Jsoup.parse(new File(PostPage.xmlFilePath), "UTF-8");

            return !doc.select("post").isEmpty();
        } catch (IOException e) {
            MyBot bot = new MyBot();
            bot.reportErrorCode(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public void generateHoroscopeMessage(PostPage postPage){
        ConfigurationManager configurationManager = new ConfigurationManager("config");
        MyBot bot = new MyBot();
        if(configurationManager.getConfigValue("is_alive").equals("1")){
            try {
                StringBuilder message = new StringBuilder();

                message.append("<b> " + " ☪\uFE0F" + postPage.getPostTitle() + " </b>" + "\n\n");

                for (String text : postPage.getMainTextBlock()){
                    if (text.contains("<b>")){
                        message.append(addEmojisToHoroscope(text)).append("\n");
                    }else {
                        message.append(text).append("\n\n");
                    }
                }
                bot.sendPostToChannel(String.valueOf(message),postPage.getPostPictureUrl());
            } catch (Exception e) {
                bot.reportErrorCode(e.getMessage());
                e.printStackTrace();
            }
        }else {
            bot.sendMessageToUser("No posts available or the sleep mode is on",MyBot.getAdminID());
        }
    }
    public static String addEmojisToHoroscope(String horoscopeText) {
        Map<String, String> zodiacEmojis = new HashMap<>();
        zodiacEmojis.put("Овен", "\u2648");
        zodiacEmojis.put("Телец", "\u2649");
        zodiacEmojis.put("Близнецы", "\u264A");
        zodiacEmojis.put("Рак", "\u264B");
        zodiacEmojis.put("Лев", "\u264C");
        zodiacEmojis.put("Дева", "\u264D");
        zodiacEmojis.put("Весы", "\u264E");
        zodiacEmojis.put("Скорпион", "\u264F");
        zodiacEmojis.put("Стрелец", "\u2650");
        zodiacEmojis.put("Козерог", "\u2651");
        zodiacEmojis.put("Водолей", "\u2652");
        zodiacEmojis.put("Рыбы", "\u2653");

        Map<String, String> zodiacEmojis2 = new HashMap<>();
        zodiacEmojis2.put("Овны", "\u2648");
        zodiacEmojis2.put("Тельцы", "\u2649");
        zodiacEmojis2.put("Близнецы", "\u264A");
        zodiacEmojis2.put("Раки", "\u264B");
        zodiacEmojis2.put("Львы", "\u264C");
        zodiacEmojis2.put("Девы", "\u264D");
        zodiacEmojis2.put("Весы", "\u264E");
        zodiacEmojis2.put("Скорпионы", "\u264F");
        zodiacEmojis2.put("Стрельцы", "\u2650");
        zodiacEmojis2.put("Козероги", "\u2651");
        zodiacEmojis2.put("Водолеи", "\u2652");
        zodiacEmojis2.put("Рыбы", "\u2653");


        boolean modified = false;
        for (Map.Entry<String, String> entry : zodiacEmojis.entrySet()) {
            if(horoscopeText.contains(entry.getKey())) {
                String[] words = horoscopeText.split("\\s+");
                for (String word : words) {
                    if (word.contains(entry.getKey())) {
                        int wordEndIndex = horoscopeText.indexOf(word) + word.length();
                        StringBuilder modifiedString = new StringBuilder(horoscopeText);
                        horoscopeText = String.valueOf(modifiedString.insert(wordEndIndex, entry.getValue()));
                        modified = true;
                        break;
                    }
                }
            }
        }
        if(!modified) {
            for (Map.Entry<String, String> entry : zodiacEmojis2.entrySet()) {
                if (horoscopeText.contains(entry.getKey())) {
                    String[] words = horoscopeText.split("\\s+");
                    for (String word : words) {
                        if (word.contains(entry.getKey())) {
                            int wordEndIndex = horoscopeText.indexOf(word) + word.length();
                            StringBuilder modifiedString = new StringBuilder(horoscopeText);
                            horoscopeText = String.valueOf(modifiedString.insert(wordEndIndex, entry.getValue()));
                            break;
                        }
                    }
                }
            }
        }
        horoscopeText = horoscopeText.replaceAll("Лунное оповещение","Лунное оповещение \uD83C\uDF1A\uD83C\uDF1A\uD83C\uDF1A");

        return horoscopeText;
    }
    public void generateMessageFromXML(int index, boolean isAutomated) {
        ConfigurationManager configurationManager = new ConfigurationManager("config");
        MyBot bot = new MyBot();

        if(Parser.isXMLPostAvailable() && configurationManager.getConfigValue("is_alive").equals("1")) {
            try {
                StringBuilder message = new StringBuilder();
                Document doc = Jsoup.parse(new File(PostPage.xmlFilePath), "UTF-8");

                Element firstPostElement = doc.select("post").get(index);

                if (firstPostElement != null) {
                    Element postTitleElement = firstPostElement.select("post-title").first();
                    if (postTitleElement != null) {
                        String postTitle = postTitleElement.text();
                        message.append("<b> ").append(postTitle).append(" </b>").append("\n\n");
                    }

                    Elements mainTextBlockElements = firstPostElement.select("main-text-block");
                    for (Element element : mainTextBlockElements) {
                        String text = element.text();
                        if (text.contains("<b>")){
                            message.append(addEmojisToHoroscope(text)).append("\n");
                        }else {
                            message.append(text).append("\n\n");
                        }
                    }

                    String resultMessage = String.valueOf(message);
                    resultMessage = resultMessage.replaceAll("<br>","\n");

                    String mainPictureUrl = "";
                    Elements mainPictureElements = firstPostElement.select("post-picture-url");
                    for (Element mainPictureElement : mainPictureElements) {
                        mainPictureUrl = mainPictureElement.text();
                    }

                    bot.sendPostToChannel(resultMessage,mainPictureUrl);

                    firstPostElement.remove();
                    FileWriter writer = new FileWriter(PostPage.xmlFilePath);
                    writer.write(doc.outerHtml());
                    writer.close();
                }else {
                    bot.sendMessageToUser("The requested Post is null",MyBot.getAdminID());
                }
            } catch (Exception e) {
                bot.reportErrorCode(e.getMessage());
                e.printStackTrace();
            }
        }else {
            if(!isAutomated){
                bot.sendMessageToUser("No posts available or the sleep mode is on",MyBot.getAdminID());
            }
        }
    }
    public static String escapeHtmlForTelegram(String htmlText) {
        String escapedHtml = StringEscapeUtils.escapeHtml4(htmlText);
        return escapedHtml;
    }
    public boolean emptyXMLFile(){
        try {
            File xmlFile = new File(PostPage.xmlFilePath);
            Document doc = Jsoup.parse(xmlFile, "UTF-8");

            doc.select("posts").empty();

            FileWriter writer = new FileWriter(xmlFile);
            writer.write(doc.outerHtml());
            writer.close();
            return true;
        } catch (IOException e) {
            MyBot bot = new MyBot();
            bot.reportErrorCode(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public static int getNumberOfXMLPosts() {
        try {
            Document doc = Jsoup.parse(new File(PostPage.xmlFilePath), "UTF-8");
            Elements objectElements = doc.select("posts post");
            return objectElements.size();
        }catch (Exception e){
            MyBot bot = new MyBot();
            bot.reportErrorCode(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    public List<String> listPosts(){
        List<String> result = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(new File(PostPage.xmlFilePath), "UTF-8");
            Elements objectElements = doc.select("posts post");
            int i = 0;
            for(Element postElement : objectElements){
                result.add( i +" " + postElement.select("post-title").text());
                i++;
            }
        }catch (Exception e){
            MyBot bot = new MyBot();
            bot.reportErrorCode(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
    @Test
    public void testVoid(){
        Debug debug = new Debug();
        LastPostedMessageTracker tracker = new LastPostedMessageTracker();

        ConfigurationManager configurationManager = new ConfigurationManager("config");
        MyBot.setChannelChatID(Long.valueOf(configurationManager.getConfigValue("dev_channel_chat_id")));
        Parser parser = new Parser();
        parser.parseFreshPosts();
        parser.generateMessageFromXML(0,false);
    }
}
