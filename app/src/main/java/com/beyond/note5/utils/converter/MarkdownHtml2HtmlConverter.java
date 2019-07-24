package com.beyond.note5.utils.converter;

import android.util.Log;

import com.beyond.note5.utils.ResourceUtil;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author: beyond
 * @date: 2019/2/3
 */

public class MarkdownHtml2HtmlConverter implements Converter<String, String> {

    @Override
    public String convert(String markdownHtml) {
        String html = "%s";
        try {
            String templatePath = "tpl/detail_markdown_template.html";
            html = ResourceUtil.getAssetFile(templatePath);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("loadWebContent", "读取文件失败");
        }
        String processedHtml = String.format(html, markdownHtml);
        return chooseCss(processedHtml,markdownHtml);
    }

    private String chooseCss(String processedHtml,String markdownHtml) {
        markdownHtml = RegExUtils.replaceAll(markdownHtml, "\n", "");
        markdownHtml = StringUtils.trim(markdownHtml);
        boolean startsWithImage = markdownHtml.startsWith("<img");
        if (startsWithImage){
            processedHtml = RegExUtils.replaceFirst(processedHtml,
                    "<link rel=\"stylesheet\" href=\"file:///android_asset/css/detail.css\" type=\"text/css\"/>",
                    "<link rel=\"stylesheet\" href=\"file:///android_asset/css/detail_image_first.css\" type=\"text/css\"/>");
        }else {
            processedHtml = RegExUtils.replaceFirst(processedHtml,
                    "<link rel=\"stylesheet\" href=\"file:///android_asset/css/detail.css\" type=\"text/css\"/>",
                    "<link rel=\"stylesheet\" href=\"file:///android_asset/css/detail_normal.css\" type=\"text/css\"/>");
        }
        return processedHtml;
    }
}
