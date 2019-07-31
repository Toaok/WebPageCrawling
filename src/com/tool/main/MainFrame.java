package com.tool.main;

import com.tool.crawl.CrawlNovelFromHtml;
import com.tool.crawl.CrawlNovelFromHtmlImp;
import com.tool.vo.Site;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

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
        mSite.addItem(Site.BIQUGE_SITE);
        mSite.addItem(Site.DINGDIAN_SITE);
        mSite.addItem(Site.OTHER_SITE);
        ComboBoxEditor editor = mSite.getEditor();
        JTextField textField = (JTextField) editor.getEditorComponent();
        textField.setDocument(new IntegerDocument());

        mUrl = new JTextField();

        mDownloadButton = new JButton();
        mDownloadButton.setText("下载");

        this.add(new Panel(), BorderLayout.NORTH);
        this.add(mSite, BorderLayout.WEST);
        this.add(mUrl, BorderLayout.CENTER);
        this.add(mDownloadButton, BorderLayout.EAST);
        this.add(new Panel(), BorderLayout.SOUTH);


        mSite.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getItem().toString().equals(Site.OTHER_SITE)) {
                    mSite.setEditable(true);
                } else {
                    mSite.setEditable(false);
                }
            }
        });


        mDownloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String url = mUrl.getText();
                if (url != null && !url.equals("")) {
                    System.out.println("website: " + mUrl.getText());
                    String selectSite = mSite.getSelectedItem().toString();
                    switch (selectSite) {
                        case Site.DINGDIAN_SITE:
                            crawlNovelFromHtml = new CrawlNovelFromHtmlImp.Builder()
                                    .url(url)
                                    .additionalChapter(8)
                                    .builder();
                            System.out.println("additionalChapter:" + 8);
                            crawlNovelFromHtml.download();
                            break;
                        case Site.BIQUGE_SITE:
                            crawlNovelFromHtml = new CrawlNovelFromHtmlImp.Builder()
                                    .url(url)
                                    .builder();
                            System.out.println("additionalChapter:" + 0);
                            crawlNovelFromHtml.download();
                            break;
                        default:
                            crawlNovelFromHtml = new CrawlNovelFromHtmlImp.Builder()
                                    .url(url)
                                    .additionalChapter(Integer.parseInt(selectSite))
                                    .builder();
                            System.out.println("additionalChapter:" + selectSite);
                            crawlNovelFromHtml.download();
                            break;
                    }
                } else {
                    System.out.println("请输入小说的主页url!");
                }
            }
        });
    }
}
