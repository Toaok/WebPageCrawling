package com.tool.main;

import com.tool.crawl.CrawlNovelFromHtml;
import com.tool.crawl.biquge.impl.CrawlBiQuGeImpl;
import com.tool.crawl.dingdian.impl.CrawlDingDianImpl;
import com.tool.vo.Site;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author TOAOK
 * @version 1.0  2018/1/8.
 */
public class MainFrame extends JFrame {

    private JComboBox mSite;
    private JTextField mUrl;
    private JButton mDownloadButton;

    private CrawlNovelFromHtml crawlNovelFromHtml;

    private final static int DEFAULT_WIDTH = 400;
    private final static int DEFAULT_HEIGHT = 80;

    public MainFrame() throws HeadlessException {
        init();
    }

    private void init() {


        //设置窗口属性
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.setLayout(new BorderLayout());
        this.setResizable(false);

        //使窗口居中
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();

        this.setBounds((int) ((dimension.width - DEFAULT_WIDTH) * 0.5), (int) ((dimension.height - DEFAULT_HEIGHT) * 0.4), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        //实例化控件
        mSite = new JComboBox();
        mSite.addItem(Site.DINGDIAN_SITE);
        mSite.addItem(Site.BIQUGE_SITE);

        mUrl = new JTextField();

        mDownloadButton = new JButton();
        mDownloadButton.setText("下载");

        this.add(new Panel(), BorderLayout.NORTH);
        this.add(mSite, BorderLayout.WEST);
        this.add(mUrl, BorderLayout.CENTER);
        this.add(mDownloadButton, BorderLayout.EAST);
        this.add(new Panel(), BorderLayout.SOUTH);

        mDownloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String url = mUrl.getText();
                if (url != null && !url.equals("")) {
                    System.out.println("website: " + mUrl.getText());
                    String selectSite = mSite.getSelectedItem().toString();
                    switch (selectSite) {
                        case Site.DINGDIAN_SITE:
                            crawlNovelFromHtml = new CrawlDingDianImpl(url);
                            crawlNovelFromHtml.download("F:" + File.separator + "Downloads" + File.separator + "小说");
                            break;
                        case Site.BIQUGE_SITE:
                            crawlNovelFromHtml = new CrawlBiQuGeImpl(url);
                            crawlNovelFromHtml.download("F:" + File.separator + "Downloads" + File.separator + "小说");
                            break;
                    }
                } else {
                    System.out.println("请输入小说的主页url!");
                }
            }
        });
    }
}
