package com.beyond.note5.view.convert;

import android.util.Log;

import com.beyond.note5.bean.Document;
import com.beyond.note5.utils.ResourceUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Created by beyond on 2019/2/3.
 */

public class Document2HtmlConverter implements Converter<Document,String> {

    private String templatePath = "tpl/detail_template.html";

    @Override
    public String convert(Document document) {
        String html = "%s<br>%s";
        try {
             html = ResourceUtil.getAssetFile(templatePath);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("loadWebContent","读取文件失败");
        }
        return String.format(html,StringUtils.isBlank(document.getTitle())?"":document.getTitle(),document.getContent());
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }
}
