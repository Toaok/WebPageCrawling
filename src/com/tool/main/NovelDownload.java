package com.tool.main;

import javax.swing.*;
import java.awt.*;

/**
 * @author TOAOK
 * @version 1.0  2017/12/28.
 */
public class NovelDownload {

    public static void main(String[] args) {


//        CrawlNovelFromHtmlLBase crawlContentFromHTML = new CrawlDingDianImpl("http://www.booktxt.net/2_2219/");
//        crawlContentFromHTML.download("F:" + File.separator + "Downloads" + File.separator + "小说");


        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame=new MainFrame();
                frame.setTitle("Download Novel");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });

    }
}
