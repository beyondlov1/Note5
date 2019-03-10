package com.beyond.note5.view.adapter.component.header;

import com.beyond.note5.bean.Todo;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author beyondlov1
 * @date 2019/03/10
 */
public class ReminderTimeItemDataGenerator extends AbstractItemDataGenerator<Todo> {

    public ReminderTimeItemDataGenerator(List<Todo> contentData) {
        super(contentData);
    }

    @Override
    protected void init() {
        Date lastReminderTime= null;
        int index = 0;
        for (Todo todo : contentData) {
            Date reminderTime = todo.getReminder() == null?new Date():todo.getReminder().getStart();
            if (lastReminderTime == null || !DateUtils.truncatedEquals(reminderTime,lastReminderTime, Calendar.DATE)) {
                Header header = new Header(index + headerData.size(), DateFormatUtils.format(reminderTime,"yyyy-MM-dd"));
                headerData.add(header);
                itemData.add(header);
            }
            itemData.add(todo);
            lastReminderTime = reminderTime;
            index++;
        }
    }

    @Override
    public int getInsertIndex(Todo todo) {
        final Date TODAY_MAX_TIME = new Date(DateUtils.ceiling(new Date(),Calendar.DATE).getTime()-1);
        //插入普通数据
        int index = 0;
        for (Todo todo1 : contentData) {
            Date reminderTime =  todo.getReminder() == null? TODAY_MAX_TIME :todo.getReminder().getStart();
            Date reminderTime1 = todo1.getReminder() == null?TODAY_MAX_TIME:todo1.getReminder().getStart();
            if (DateUtils.truncatedEquals(reminderTime1,reminderTime,Calendar.DATE) //判断reminderTime
                    && todo.getReadFlag()<=todo1.getReadFlag()){ //判断readFlag
                if (DateUtils.truncatedCompareTo(reminderTime1,reminderTime,Calendar.MILLISECOND)>=0){
                    return index;
                }
            }
            index++;
        }

        //如果所需的header不存在, 放在合适位置
        if (index == contentData.size()){
            index = 0;
            for (Todo todo1 : contentData) {
                Date reminderTime =  todo.getReminder() == null?TODAY_MAX_TIME:todo.getReminder().getStart();
                Date reminderTime1 = todo1.getReminder() == null?TODAY_MAX_TIME:todo1.getReminder().getStart();
                if (DateUtils.truncatedCompareTo(reminderTime,reminderTime1,Calendar.DATE)<0){
                    return index;
                }
                index++;
            }
        }
        return contentData.size();
    }
}
