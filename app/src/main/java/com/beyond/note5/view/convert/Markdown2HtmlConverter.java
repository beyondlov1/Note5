package com.beyond.note5.view.convert;

import org.markdownj.MarkdownProcessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by beyond on 2019/2/3.
 */

public class Markdown2HtmlConverter implements Converter<String,String> {

    private MarkdownProcessor markdownProcessor = new MarkdownProcessor();

    @Override
    public String convert(String markdown) {
        markdown = replaceUrlsToMarkDownStyle(markdown);
        return markdownProcessor.markdown(markdown);
    }

    private String replaceUrlsToMarkDownStyle(String source) {
        Pattern pattern = Pattern.compile("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            String url = matcher.group();
            String stringBuilder = "[" +
                    url +
                    "]" +
                    "(" +
                    url +
                    ")";
            source = source.replace(url, stringBuilder);
        }
        return source;
    }
}
