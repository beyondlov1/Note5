package com.beyond.note5.view.adapter.component.header;

import com.beyond.note5.bean.Document;
import com.beyond.note5.constant.ReadFlagEnum;

import java.util.List;
import java.util.Objects;

public class ReadFlagItemDataGenerator<T extends Document> extends AbstractItemDataGenerator<T,Header> {

    public ReadFlagItemDataGenerator(List<T> contentData) {
        super(contentData);
    }

    @Override
    protected void init() {
        Integer lastReadFlag = null;
        int index = 0;
        for (T document : contentData) {
            Integer readFlag = document.getReadFlag();
            if (lastReadFlag == null || !lastReadFlag.equals(readFlag)) {
                Header header = new Header(index + headerData.size(), ReadFlagEnum.getName(readFlag));
                headerData.add(header);
                itemData.add(header);
            }
            itemData.add(document);
            lastReadFlag = readFlag;
            index++;
        }
    }

    @Override
    public int getInsertIndex(T t) {
        int index = 0;
        for (T document : contentData) {
            if (Objects.equals(document.getReadFlag(),t.getReadFlag())){
                return index;
            }
            index++;
        }

        //所需的header不存在, 放在合适位置
        if (index == contentData.size()){
            index = 0;
            for (T document : contentData) {
                if (t.getReadFlag() < document.getReadFlag()){
                    return index;
                }
                index++;
            }
        }
        return contentData.size();
    }
}
