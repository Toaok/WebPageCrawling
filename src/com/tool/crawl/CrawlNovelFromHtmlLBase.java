package com.tool.crawl;

import com.tool.crawl.dingdian.CrawlDingDian;
import com.tool.utils.CrawlUtils;
import com.tool.vo.Chapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

/**
 * Created by TOAOK on 2017/9/20.
 */

public abstract class CrawlNovelFromHtmlLBase implements CrawlNovelFromHtml {

    protected static final String USER_AGENT[] = {
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; .NET4.0E; rv:11.0) like Gecko"};
    protected static final int TIME_OUT = 1000 * 30;
    protected static final int ONCE_SIZE = 200;
    protected  int chapterTaskCount;
    protected  Document main;
    protected Chapter[] chapters;
    protected String url;//访问小说主页面的url
    protected Map<String, String> cookies;//请求cookie


    public interface Callback {
        //通过回调的方式来检查是否下载完成
        void callback(int i);
    }

    protected abstract void dispatcher(int number) throws IOException;

    public CrawlNovelFromHtmlLBase(String url) {
        if (url != null && !url.equals("")) {
            this.url = url;
            try {
                cookies = CrawlUtils.getCookies(url);
                main = Jsoup.connect(this.url).userAgent(USER_AGENT[(int) Math.random() * 10 % 2]).cookies(cookies).timeout(TIME_OUT).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        chapterTaskCount = 0;
    }


    protected class Task implements Runnable {

        private CrawlDingDian.Callback callback;
        private int number;

        public Task(int number, CrawlDingDian.Callback callback) {
            this.number = number;
            this.callback = callback;
        }

        @Override
        public synchronized void run() {

            try {
                dispatcher(number);
            } catch (IOException e) {
                e.printStackTrace();
            }
            chapterTaskCount++;
            callback.callback(number);
        }
    }
}
