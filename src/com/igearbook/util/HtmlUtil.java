package com.igearbook.util;

import java.util.regex.Pattern;

public class HtmlUtil {
    private static Pattern PT_HTML = Pattern.compile("<.+?>");
    private static Pattern PT_HTML_ENCODE = Pattern.compile("<.+?>|&.+?;");

    /**
     * Remove HTML tags
     */
    public static String removeHTML(String str) {
        if (str == null)
            return "";
        return PT_HTML.matcher(str).replaceAll("");
    }

    /**
     * Remove HTML tags and encoded character,e.g. &nbsp;
     */
    public static String removeAllHTML(String str) {
        if (str == null)
            return "";
        return PT_HTML_ENCODE.matcher(str).replaceAll("");
    }
}