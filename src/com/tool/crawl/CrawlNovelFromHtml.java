package com.tool.crawl;

/**
 * @author TOAOK
 * @version 1.0  2018/1/3.
 */
public interface CrawlNovelFromHtml {
    /**
     * 根据给出的filePath将获取的内容下载到指定目录
     * @param filePath
     */
    String download(String filePath);
    String download();

    /**
     * 获取小说名
     * @return novelName
     */
    String getNovelName();
}
