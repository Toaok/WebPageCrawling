package com.tool.crawl;

import com.tool.utils.CrawlUtils;
import com.tool.utils.GeneratingTXTDocuments;
import com.tool.utils.ThreadPoolManager;
import com.tool.vo.Chapter;
import com.tool.vo.Site;
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
    private List<Chapter> mChapters;//章节信息对象数组
    private List<Condition> mConditions;//线程锁
    private int mCountChapter;//总章节数
    private int additionalChapter;//无效章节
    private Map<String, String> cookies;//请求cookie

    private GeneratingTXTDocuments mDocuments;//生成txt文件


    CrawlNovelFromHtmlImp(Builder builder) {

        this.url = builder.url;
        this.additionalChapter = builder.additionalChapter;

        try {
            cookies = CrawlUtils.getCookies(url);
            main = Jsoup.connect(this.url)
                    .userAgent(USER_AGENT[(int) Math.random() * 10 % 2])
                    .cookies(cookies)
                    .timeout(TIME_OUT)
                    .validateTLSCertificates(false)//javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
                    .get();
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
            if (url == null || url.equals("")) {
                throw new NoSuchElementException();
            }
            this.url = url;
            return this;
        }

        public Builder additionalChapter(int additionalChapter) {
            this.additionalChapter = additionalChapter;
            return this;
        }

        public CrawlNovelFromHtml builder() {
            return new CrawlNovelFromHtmlImp(this);
        }
    }


    /**
     * 解析html页面，获取章节目录和每个章节的url
     *
     * @throws IOException
     */
    private void parseHtml() {

        Elements chapters = null;
        for (String cssQuery : Site.CHAPTER_LIST_CSSQUERY) {
            chapters = main.select(cssQuery);
            if (chapters != null && chapters.size() > 0) {
                break;
            }
        }
        if (additionalChapter > chapters.size()) {
            return;
        }
        //实际章节数
        mCountChapter = chapters.size() - additionalChapter;

        for (int i = 0; i < mCountChapter; ++i) {
            Element e = chapters.get(i + additionalChapter);
            String chapterNmae = e.text();

            //为每个章节创建一个锁
            Condition condition = mLock.newCondition();
            mConditions.add(condition);
            //创建一个章节对象
            Chapter chapter = new Chapter();
            //章节名
            chapter.setChapterName(chapterNmae);
            //获取章节的url
            String chapterUrl = e.attr("href");
            chapter.setUrl(getChapterUrl(chapterUrl));
            //加入到章节信息列表
            mChapters.add(chapter);
            //获取章节类容
            ThreadPoolManager.getInstance().execute(new Task(i));
        }

    }

    /**
     * 根据章节信息数组，重定向到每章获取每章内容
     *
     * @param number
     * @throws IOException
     */
    public void dispatcher(int number) throws IOException {

        StringBuffer chapterBuffer = new StringBuffer();
        //添加章节名
        chapterBuffer.append(mChapters.get(number).getChapterName());
        chapterBuffer.append(System.getProperty("line.separator"));

        //爬取章节信息
        Document chapterDoc = Jsoup.connect(mChapters.get(number).getUrl())
                .method(Connection.Method.GET)
                .headers(CrawlUtils.getHeader())
                .userAgent(USER_AGENT[(int) Math.random() * 10 % 2])
                .cookies(cookies)
                .timeout(TIME_OUT)
                .validateTLSCertificates(false)//javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
                .get();
        Elements e = chapterDoc.select("div#content");
        //获取章节正文，并格式化。
        String chapterContent = CrawlUtils.contentFormat(e.text());
        chapterBuffer.append(chapterContent);
        chapterBuffer.append(System.getProperty("line.separator"));

        //获取章节信息对象
        Chapter chapter = mChapters.get(number);
        //将章节正文添加到章节信对象中
        chapter.setContent(chapterBuffer.toString());
    }

    @Override
    public String getNovelName() {
        String novelName = null;

        Elements elements = null;
        for (String cssQuery : Site.NAME_CSSQUERY) {
            elements = main.select(cssQuery);
            if (elements != null && elements.size() > 0) {
                break;
            }
        }
        if (elements != null && elements.size() > 0) {
            novelName = elements.first().text();
        }
        return novelName;
    }

    @Override
    public String download(String filePath) {
        if (filePath == null || filePath.equals("")) {
            filePath = CrawlUtils.getDefaultPath();
        }

        System.out.println("文件路径：" + filePath);

        String result = "";
        try {
            mDocuments = new GeneratingTXTDocuments(filePath, getNovelName() + ".txt");
            parseHtml();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String download() {
        return download(CrawlUtils.getDefaultPath());
    }

    private String getChapterUrl(String chapterUrl) {
        return (chapterUrl.startsWith("http") || chapterUrl.startsWith("https")) ? chapterUrl : (getRootUrl(url) + (chapterUrl.contains(getPath(url)) ? chapterUrl : getPath(url) + chapterUrl));
    }


    /**
     * 在该线程中执行的任务是：
     * ->首先通过重定向获取章节内容（这里要进行网络请求，比较耗时）
     * ->然后判断该章节的上一章是否已存储 [是]->开始存储本章节 [否]->等待上一章存储完成
     * 这里好像没什么用
     */
    private class Task implements Runnable {

        private int number;

        public Task(int number) {
            this.number = number;
        }

        @Override
        public void run() {
            try {
                //重定向到章节内容，获取章节正文
                dispatcher(number);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //获取到内容后进行存储
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
                } else {//number==0,存储第一章不需要等待
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
            float progress = chapterTaskCount / (float) (mCountChapter) * 100;
            if (progress < 100) {
                System.out.printf("已下载: %5.2f%%\r", progress);
            } else if (progress == 100) {
                System.out.printf("已下载: %5.2f%%\n", progress);
            }

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
