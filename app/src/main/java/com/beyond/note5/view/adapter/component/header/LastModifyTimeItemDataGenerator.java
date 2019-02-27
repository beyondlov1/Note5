package com.beyond.note5.view.adapter.component.header;

import com.beyond.note5.bean.Document;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LastModifyTimeItemDataGenerator<T extends Document> extends AbstractItemDataGenerator<T> {

    public LastModifyTimeItemDataGenerator(List<T> contentData) {
        super(contentData);
    }

    @Override
    protected void init() {
        Date lastLastModifyTime= null;
        int index = 0;
        for (T document : contentData) {
            Date lastModifyTime = document.getLastModifyTime();
            if (lastLastModifyTime == null || !DateUtils.truncatedEquals(lastModifyTime,lastLastModifyTime,Calendar.DATE)) {
                Header header = new Header(index + headerData.size(),DateFormatUtils.format(lastModifyTime,"yyyy-MM-dd"));
                headerData.add(header);
                itemData.add(header);
            }
            itemData.add(document);
            lastLastModifyTime = lastModifyTime;
            index++;
        }
    }

    @Override
    public int getInsertIndex(T t) {
        //插入普通数据
        int index = 0;
        for (T document : contentData) {
            if (DateUtils.truncatedEquals(document.getLastModifyTime(),t.getLastModifyTime(),Calendar.DATE) //判断lastModifyTime
                    && t.getReadFlag()<=document.getReadFlag()){ //判断readFlag
                return index;
            }
            index++;
        }

        //如果所需的header不存在, 放在合适位置
        if (index == contentData.size()){
            index = 0;
            for (T document : contentData) {
                if (DateUtils.truncatedCompareTo(t.getLastModifyTime(),document.getLastModifyTime(),Calendar.DATE)>0){
                    return index;
                }
                index++;
            }
        }
        return contentData.size();
    }
}
