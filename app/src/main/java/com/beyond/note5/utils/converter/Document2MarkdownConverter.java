package com.beyond.note5.utils.converter;

import com.beyond.note5.bean.Document;

import org.apache.commons.lang3.StringUtils;

/**
 * @author: beyond
 * @date: 2019/2/3
 */

public class Document2MarkdownConverter implements Converter<Document,String> {

    @Override
    public String convert(Document document) {
        String result;
        if (!StringUtils.isBlank(document.getTitle())){
            result = String.format("### %s  \n%s", document.getTitle(),document.getContent());
        }else {
            result = String.format("%s",document.getContent());
        }
        return result;
    }
}
