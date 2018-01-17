package com.tool.crawl.biquge;

import com.tool.crawl.CrawlNovelFromHtmlLBase;

/**
 * @author TOAOK
 * @version 1.0  2018/1/12.
 */
public abstract class CrawlBiQuGe extends CrawlNovelFromHtmlLBase {
    protected static final int ADDITIONAL_CHAPTER = 0;
    protected static final String ROOTURL = "http://www.biquge.com.tw/";
    public CrawlBiQuGe(String url) {
        super(url);
    }
}
