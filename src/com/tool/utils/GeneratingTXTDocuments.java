package com.tool.utils;

import java.io.*;

/**
 * @author TOAOK
 * @version 1.0  2017/12/30.
 */
public class GeneratingTXTDocuments {
    private RandomAccessFile raf;

    public GeneratingTXTDocuments(String filePath, String fileName) throws IOException {
        String fullPath;
        if (fileName.endsWith(".txt")) {
            fullPath = filePath + File.separator + fileName;
        } else {
            fullPath = filePath + File.separator + fileName + ".txt";
        }
        if (filePath != null && !filePath.equals("")) {
            {
                File file = new File(fullPath);
                if (file.exists()) {
                    file.delete();//如果文件存在则删除原文件
                }
                while (!file.exists()) {
                    file.createNewFile();
                }
                this.raf = new RandomAccessFile(file, "rw");
            }
        } else {
            System.out.println("文件路径出问题了。。。");
        }
    }

    public void writeDate(String content) {
        try {
            raf.seek(raf.length());
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
