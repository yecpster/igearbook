package com.igearbook.util;

import net.jforum.entities.Post;

import org.apache.commons.lang3.StringUtils;

public class PostUtils {
    private PostUtils() {

    }

    public static String shortPostText(final Post post, final int length) {
        String text = HtmlUtil.removeAllHTML(post.getText());
        if (StringUtils.isNotBlank(text)) {
            text = text.trim();
            text = text.replaceAll("\\s+", " ");
            if (text.length() > length) {
                text = text.substring(0, length);
            }
        } else {
            text = "";
        }
        return text;
    }
}
