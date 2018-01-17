package com.tool.utils;


import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TOAOK
 * @version 1.0  2017/9/26.
 */
public class CrawlUtils {
    public static final int TIME_OUT = 1000 * 5;
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    public static String HttpGet(String path) {
        try {
            return HttpGet(new URL(path));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String HttpGet(URL url) {

        HttpURLConnection connection = null;
        String response = "";
        try {
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            connection = (HttpURLConnection) url.openConnection();

            //设置连接参数
            connection.setReadTimeout(TIME_OUT);
            connection.setConnectTimeout(TIME_OUT);
            connection.setDoInput(true);
            connection.setRequestMethod(METHOD_GET);
            //设置请求消息头
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-ALive");
            connection.setRequestProperty("charset", "utf-8");
            connection.connect();

            switch (connection.getResponseCode()) {

                case HttpURLConnection.HTTP_NOT_FOUND:
                    Thread.sleep(TIME_OUT);
                    break;
                case HttpURLConnection.HTTP_OK:
                    InputStream inputStream = null;
                    BufferedReader reader = null;
                    try {
                        inputStream = connection.getInputStream();

                        StringBuffer buffer = new StringBuffer();

                        reader = new BufferedReader(new InputStreamReader(inputStream));
                        String readLine = null;

                        while ((readLine = reader.readLine()) != null) {
                            buffer.append(readLine);
                        }
                        response = buffer.toString();
                    } finally {
                        if (reader != null) ;
                        {
                            reader.close();
                        }
                        connection.disconnect();
                    }
                    break;
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static Map<String, String> getCookies(String path) throws IOException {
        Map<String, String> cookies = new HashMap<>();

        CookieManager manager = new CookieManager();
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.getHeaderFields();
        CookieStore store = manager.getCookieStore();
        List<HttpCookie> cookiesList = store.getCookies();
        for (HttpCookie cookie : cookiesList) {
            cookies.put(cookie.getName(), cookie.getValue());
        }
        return cookies;
    }


    /**
     * 格式化章节内容
     *
     * @param content
     * @return contentFormat
     * @throws UnsupportedEncodingException
     */
    public static String contentFormat(String content) throws UnsupportedEncodingException {
        return new String(content.getBytes("UTF-8")).replaceAll("     ", "\n\t");
    }

    public static String getValue(String value, String startStr, int endIndex, String defaultValue) {
        int startIndex = value.indexOf(startStr);
        defaultValue = getValue(value, startIndex + startStr.length(), endIndex, defaultValue);
        return defaultValue;
    }


    public static String getValue(String value, int startIndex, int endIndex, String defaultValue) {
        if (value != null && startIndex >= 0 && endIndex >= 0 && startIndex <= endIndex) {
            defaultValue = value.substring(startIndex, endIndex);
        }
        return defaultValue;
    }

    public static String getValue(String value, int startIndex, String endStr, String defaultValue) {
        int endIndex = value.indexOf(endStr, startIndex);
        defaultValue = getValue(value, startIndex, endIndex, defaultValue);
        return defaultValue;
    }

    public static String getValue(String value, String startStr, String endStr, String defaultValue) {
        int startIndex = value.indexOf(startStr);
        int endIndex = value.indexOf(endStr, startIndex);
        defaultValue = getValue(value, startIndex + startStr.length(), endIndex, defaultValue);
        return defaultValue;
    }
}
