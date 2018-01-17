package com.tool.utils;

import java.io.*;

/**
 * @author TOAOK
 * @version 1.0  2017/12/30.
 */
public class GeneratingTXTDocuments {
    private RandomAccessFile raf;

    public GeneratingTXTDocuments(String filePath, String fileName) throws IOException {
        String fullPath = filePath + File.separator + fileName;
        if (filePath != null && !filePath.equals("")) {
            {
                File file = new File(fullPath);
                while (!file.exists()) {
                    file.createNewFile();
                }
                this.raf = new RandomAccessFile(file, "rw");
            }
        }else {
            System.out.println("文件路径出问题了。。。");
        }
    }

    public void writeDate(String content) {
        try {
            raf.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readDate() {
        return "";
    }

    public void close() throws IOException {
        raf.close();
    }
}
