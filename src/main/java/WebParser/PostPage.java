package WebParser;

import DEBUG.Debug;
import Telegram.MyBot;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class PostPage {
    public static String xmlFilePath = Debug.PROJECT_PATH + "resources/posts.xml";
    public PostPage(Document document) {
        Debug.functionDebug("!!!!!!------Filling the PostPage Object------------------------------------------!!!!!!");
        Elements titles = document.select("div.post-content h1");
        setPostTitle(titles.get(0).text());
        Debug.functionDebug("PostPage_Title = " + getPostTitle());

        Elements postPicturesMain = document.select("div.post-content figure a");
        setPostPictureUrl(postPicturesMain.get(0).attr("href"));
        Debug.functionDebug("PostPage_PostPicture = " + getPostPictureUrl());

        Elements postContentTextElements = document.select(".post-content-inner p, .post-content-inner h1, .post-content-inner h2");
        for (Element element : postContentTextElements) {
            if(element.tagName().equals("p") || element.tagName().equals("h1") || element.tagName().equals("h2")) {
                if(!element.tagName().equals("p")){

                    setMainTextBlock("<b> a" + element.text() + " </b>");
                } else {
                    String text = element.outerHtml();
                    text = text.replaceAll("<p>","")
                            .replaceAll("</p>","")
                            .replaceAll("<em>","")
                            .replaceAll("</em>","")
                            .replaceAll("&nbsp","")
                            .replaceAll("</strong><br>"," </b>")
                            .replaceAll("<br>","\n")
                            .replaceAll("<strong>","<b> ")
                            .replaceAll("</strong>","</b>");
                    Debug.functionDebug("\n\n\n<        OUTERHTML      >");
                    Debug.functionDebug(text);
                    Debug.functionDebug("<       /OUTERHTML       >\n\n\n");

                    if(text.contains("<b>")){
                        Document doc = Jsoup.parse(text);

                        Element boldElement = doc.select("b").first();
                        String bold = boldElement.toString();
                        String notBold = doc.text();
                        setMainTextBlock(bold);
                        setMainTextBlock(notBold);
                    }else {
                        setMainTextBlock(text);
                    }
                }
            }
        }
        Debug.functionDebug("PostPage_PostContentText = " + "\n" + getMainTextBlock());

        Debug.functionDebug("!!!!!!------The PostPage Object Filled------------------------------------------!!!!!!!");
    }

    private String postTitle;
    private String postPictureUrl;
    private List <String> mainTextBlock = new ArrayList<>();
    public void setPostTitle(String title) {
        if (title != null && title.length() > 1) {
            this.postTitle = title;
        }
    }
    public String getPostTitle() {
        return postTitle;
    }
    public void setPostPictureUrl(String url) {
        this.postPictureUrl = url;
    }
    public String getPostPictureUrl() {
        return postPictureUrl;
    }
    public void setMainTextBlock(String textBlock){
        if(textBlock.length()>1) {
            this.mainTextBlock.add(textBlock);
        }
    }
    public List<String> getMainTextBlock(){
        return this.mainTextBlock;
    }
    public void savePostToXML(){
        Debug.functionDebug("XML file WIP: " + xmlFilePath);
        try {
            String xmlFilePath = Debug.PROJECT_PATH + "resources/posts.xml";
            File xmlFile = new File(xmlFilePath);

            Document doc;

            doc = Jsoup.parse(xmlFile, "UTF-8");

            Element postsElement = doc.selectFirst("posts");

            if (postsElement == null) {
                postsElement = doc.appendElement("posts");
            }

            Element postElement = postsElement.appendElement("post");

            Element postTitleElement = postElement.appendElement("post-title");
            postTitleElement.appendChild(new TextNode(getPostTitle()));

            Element postPictureUrlElement = postElement.appendElement("post-picture-url");
            postPictureUrlElement.appendChild(new TextNode(getPostPictureUrl()));

            Element mainTextBlocksElement = postElement.appendElement("main-text-blocks");
            for (String textBlock : getMainTextBlock()) {
                Element mainTextBlockElement = mainTextBlocksElement.appendElement("main-text-block");
                mainTextBlockElement.appendChild(new TextNode(textBlock));
            }

            if(!PostFilter.isRestricted(getMainTextBlock().toString())) {
                Debug.functionDebug("\n\n");
                Debug.functionDebug(doc.outerHtml());
                Debug.functionDebug("\n\n");

                FileWriter writer = new FileWriter(xmlFile);
                writer.write(doc.outerHtml());
                writer.close();
                Debug.functionDebug("XML file updated: " + xmlFilePath);
            }else {
                Debug.functionDebug("Restricted post found. Declined.");
            }
        } catch (Exception e) {
            MyBot bot = new MyBot();
            bot.reportErrorCode(e.getMessage());
            e.printStackTrace();
        }
    }
}
