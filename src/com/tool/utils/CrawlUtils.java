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


    public static String getRootUrl(String site){
        StringBuffer buffer=new StringBuffer();
        if(!"".equals(site)){
            try {
                URL url=new URL(site);
                buffer.append(url.getProtocol());
                buffer.append("://");
                buffer.append(url.getHost());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return buffer.toString();
    }

    public static String getPath(String site){
        String path="";
        if(!"".equals(site)){
            try {
                URL url=new URL(site);
                path=url.getPath();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    public static Map getHeader() {
        Map<String,String> map=new HashMap();
        map.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        map.put("Accept-Encoding"," gzip, deflate");
        map.put("Accept-Language", "zh,zh-CN;q=0.9,en;q=0.8");
        map.put("Cache-Control","max-age=0");
        map.put("Connection","keep-alive");
        map.put("Cookie","Hm_lvt_913379d9cb2e5d673397376689e75b12=1523805110; width=85%25; Hm_lpvt_913379d9cb2e5d673397376689e75b12=1523805588");
        map.put("Host","www.b5200.net");
        map.put("Upgrade-Insecure-Requests","1");
        map.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");

        return map;
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
