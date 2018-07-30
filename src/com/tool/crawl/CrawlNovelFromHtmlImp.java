package com.tool.crawl;

import com.tool.utils.CrawlUtils;
import com.tool.utils.GeneratingTXTDocuments;
import com.tool.utils.ThreadPoolManager;
import com.tool.vo.Chapter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.tool.utils.CrawlUtils.getPath;
import static com.tool.utils.CrawlUtils.getRootUrl;

/**
 * Created by TOAOK on 2017/9/20.
 */

public class CrawlNovelFromHtmlImp implements CrawlNovelFromHtml {

    private static final String USER_AGENT[] = {
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; .NET4.0E; rv:11.0) like Gecko"};
    private static final int TIME_OUT = 0;
    private static final int ONCE_SIZE = 200;
    private int chapterTaskCount;//已下载章节数
    private Document main;

    private String url;//访问小说主页面的url

    private Lock mLock;
    private List<Chapter> mChapters;//每章内容
    private List<Condition> mConditions;//线程锁
    private int mCountChapter;//总章节数
    private int additionalChapter;//额外章节
    private Map<String, String> cookies;//请求cookie

    private GeneratingTXTDocuments mDocuments;//生成txt文件

//    private String rootUrl = "";

    CrawlNovelFromHtmlImp(Builder builder) {

        this.url=builder.url;
        this.additionalChapter=builder.additionalChapter;

        try {
            cookies = CrawlUtils.getCookies(url);
            main = Jsoup.connect(this.url).userAgent(USER_AGENT[(int) Math.random() * 10 % 2]).cookies(cookies).timeout(TIME_OUT).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        rootUrl = getRootUrl(url);
        mLock = new ReentrantLock();
        mConditions = new ArrayList<>();//创建一个lock数组，为每个章节写入时加锁
        mChapters = new ArrayList<>();//创建一个章节数组，来存储所有章节
        chapterTaskCount = 0;//多线程访问章节
    }

    public static class Builder {

        String url;
        int additionalChapter;

        public Builder url(String url) {
            if (url == null ||url.equals("")) {
                throw new NoSuchElementException();
            }
            this.url = url;
            return this;
        }

        public Builder additionalChapter( int additionalChapter) {
            this.additionalChapter = additionalChapter;
            return this;
        }

        public CrawlNovelFromHtml builder(){
            return new CrawlNovelFromHtmlImp(this);
        }
    }


    /**
     * 解析html页面，获取章节目录和每个章节的url
     *
     * @throws IOException
     */
    private void parseHtml() {

        Elements catalog = main.select("div#wrapper>div.box_con>div#list>dl");
        Elements chapters = catalog.select("a");

        mCountChapter = chapters.size() - additionalChapter;//实际章节数

        for (int i = 0; i < mCountChapter; ++i) {
            Element e = chapters.get(i + additionalChapter);
            String chapterNmae = e.text();

            Condition condition = mLock.newCondition();//为每个章节创建一个锁
            mConditions.add(condition);
            Chapter chapter = new Chapter();//创建一个章节对象
            //章节名
            chapter.setChapterName(chapterNmae);
            //获取章节的url
            String chapterUrl = e.attr("href");
            chapter.setUrl(getChapterUrl(chapterUrl));
            mChapters.add(chapter);
            //请求
            ThreadPoolManager.getInstance().execute(new Task(i));
        }

    }

    /**
     * 根据章节内容数组，重定向到每章获取每章内容
     *
     * @param number
     * @throws IOException
     */
    public void dispatcher(int number) throws IOException {

        StringBuffer chapterBuffer = new StringBuffer();
        //添加章节名
        chapterBuffer.append(mChapters.get(number).getChapterName());
        //换行
        chapterBuffer.append(System.getProperty("line.separator"));
        Document chapterDoc = Jsoup.connect( mChapters.get(number).getUrl())
                .method(Connection.Method.GET)
                .headers(CrawlUtils.getHeader())
                .userAgent(USER_AGENT[(int) Math.random() * 10 % 2])
                .cookies(cookies)
                .timeout(TIME_OUT)
                .get();
        Elements e = chapterDoc.select("div#content");
        //获取章节正文，并格式化。
        String chapterContent = CrawlUtils.contentFormat(e.text());
        chapterBuffer.append(chapterContent);

        chapterBuffer.append(System.getProperty("line.separator"));
        mChapters.get(number).setContent(chapterBuffer.toString());
    }

    @Override
    public String getNovelName() {
        String novelName = main.select("div#maininfo>div#info>h1").first().text();
        return novelName;
    }

    @Override
    public String download(String filePath) {
        String result = "";
        try {
            mDocuments = new GeneratingTXTDocuments(filePath, getNovelName() + ".txt");
            parseHtml();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }



    private String getChapterUrl(String chapterUrl){
        return (chapterUrl.startsWith("http")||chapterUrl.startsWith("https"))?chapterUrl:(getRootUrl(url) + (chapterUrl.contains(getPath(url)) ? chapterUrl : getPath(url) + chapterUrl));
    }


    private class Task implements Runnable {

        private int number;

        public Task(int number) {
            this.number = number;
        }

        @Override
        public void run() {
            try {
                //重定向到章节内容
                dispatcher(number);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Chapter chapter = mChapters.get(number);

            mLock.lock();
            try {
                if (number > 0) {
                    if (!mChapters.get(number - 1).isSaved())
                        mConditions.get(number).await();
                    mDocuments.writeDate(chapter.getContent());
                    chapter.setSaved(true);
                    if (number < mConditions.size() - 1)
                        mConditions.get(number + 1).signal();
                } else {
                    mDocuments.writeDate(chapter.getContent());
                    chapter.setSaved(true);
                    if (number < mConditions.size() - 1)
                        mConditions.get(number + 1).signal();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mLock.unlock();
            }

            chapterTaskCount++;
            //小说下载进度
            float progress= (float)(Math.round((chapterTaskCount / (float) (mCountChapter) * 100)*10*2))/(10*2);
            System.out.print("已下载"+progress+"%\r" );
            if (chapterTaskCount >= mCountChapter) {
                System.out.println("小说下载成功！！");
                try {
                    mDocuments.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
            ThreadPoolManager.getInstance().remove(this);
        }
    }
}
