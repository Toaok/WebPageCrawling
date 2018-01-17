package com.tool.crawl.biquge.impl;

import com.tool.crawl.CrawlNovelFromHtmlLBase;
import com.tool.crawl.biquge.CrawlBiQuGe;
import com.tool.utils.CrawlUtils;
import com.tool.utils.GeneratingTXTDocuments;
import com.tool.vo.Chapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * @author TOAOK
 * @version 1.0  2018/1/14.
 */
public class CrawlBiQuGeImpl extends CrawlBiQuGe implements CrawlNovelFromHtmlLBase.Callback {

    private GeneratingTXTDocuments documents;

    public CrawlBiQuGeImpl(String url) {
        super(url);
    }

    /**
     * 解析html页面，获取章节目录和每个章节的url
     *
     * @throws IOException
     */

    protected void parseHtml() throws IOException {

        Elements catalog = main.select("div#wrapper>div.box_con>div#list>dl");
        Elements chapter = catalog.select("a");

        int countChapter = chapter.size() - ADDITIONAL_CHAPTER;//实际章节数


        chapters = new Chapter[countChapter];//创建一个章节数组，来存储所有章节

        for (int i = 0; i < countChapter; ++i) {
            Element e = chapter.get(i + ADDITIONAL_CHAPTER);
            String chapterNmae = e.text();

            chapters[i] = new Chapter();//创建一个章节对象

            //章节名
            chapters[i].setChapterName(chapterNmae);
            //获取章节的url
            String chapterUrl = e.attr("href");
            chapters[i].setUrl(chapterUrl);
            //请求
            new Thread(new Task(i, this)).start();
        }

    }

    @Override
    public void dispatcher(int number) throws IOException {

        StringBuffer chapterBuffer = new StringBuffer();

        Document chapterDoc = Jsoup.connect(ROOTURL + chapters[number].getUrl()).userAgent(USER_AGENT[(int) Math.random() * 10 % 2]).cookies(cookies).timeout(TIME_OUT).get();
        Elements e = chapterDoc.select("div#content");
        //获取章节正文，并格式化。
        String chapterContent = CrawlUtils.contentFormat(e.text());
        chapterBuffer.append(chapterContent);
        //换行
        chapterBuffer.append(System.getProperty("line.separator"));
        chapters[number].setContent(chapterBuffer.toString());
    }

    @Override
    public String download(String filePath) {
        String result = "";
        try {
            documents = new GeneratingTXTDocuments(filePath, getNovelName() + ".txt");
            parseHtml();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String getNovelName() {
        String novelName = main.select("div#maininfo>div#info>h1").first().text();
        return novelName;
    }

    @Override
    public synchronized void callback(int i) {
        int countChapter = chapters.length;
        if (chapterTaskCount >= countChapter) {
            for (Chapter chapter : chapters) {
                documents.writeDate(chapter.getChapterName() +
                        System.getProperty("line.separator") + chapter.getContent());
            }
        }
        System.out.printf("已下载 %.2f%%\n", (chapterTaskCount / (float) (countChapter) * 100));
        if (chapterTaskCount >= countChapter) {
            System.out.println("小说下载成功！！");
            try {
                documents.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
