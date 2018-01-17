package com.tool.crawl.dingdian;

import com.tool.crawl.CrawlNovelFromHtmlLBase;

/**
 * @author TOAOK
 * @version 1.0  2018/1/1.
 */
public abstract class CrawlDingDian extends CrawlNovelFromHtmlLBase {

    protected static final int ADDITIONAL_CHAPTER = 9;
    protected static final String ROOTURL = "http://www.booktxt.net";

    public CrawlDingDian(String url) {
        super(url);
    }
}
