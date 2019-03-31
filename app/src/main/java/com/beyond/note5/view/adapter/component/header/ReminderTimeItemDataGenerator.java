package com.beyond.note5.view.adapter.component.header;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.model.dao.TodoDao;
import com.beyond.note5.utils.DateUtil;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author beyondlov1
 * @date 2019/03/10
 */
public class ReminderTimeItemDataGenerator extends AbstractItemDataGenerator<Todo, TodoHeader> {

    private TodoDao todoDao;

    public ReminderTimeItemDataGenerator(List<Todo> contentData) {
        super(contentData);
        todoDao = getTodoDao();
    }

    private TodoDao getTodoDao() {
        DaoSession daoSession = MyApplication.getInstance().getDaoSession();
        return daoSession.getTodoDao();
    }

    @Override
    protected void init() {
        Date lastReminderTime = null;
        int index = 0;
        for (Todo todo : contentData) {
            Date reminderTime = todo.getReminder() == null ? new Date() : todo.getReminder().getStart();
            if (lastReminderTime == null || !DateUtils.truncatedEquals(reminderTime, lastReminderTime, Calendar.DATE)) {
                TodoHeader header = new TodoHeader(index + headerData.size(), DateFormatUtils.format(reminderTime, "yyyy-MM-dd"));
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
        final Date TODAY_MAX_TIME = new Date(DateUtils.ceiling(new Date(), Calendar.DATE).getTime() - 1);
        //插入普通数据
        int index = 0;
        for (Todo todo1 : contentData) {
            Date reminderTime = todo.getReminder() == null ? TODAY_MAX_TIME : todo.getReminder().getStart();
            Date reminderTime1 = todo1.getReminder() == null ? TODAY_MAX_TIME : todo1.getReminder().getStart();
            if (DateUtils.truncatedEquals(reminderTime1, reminderTime, Calendar.DATE) //判断reminderTime
                    && todo.getReadFlag() <= todo1.getReadFlag()    //判断readFlag
                    && DateUtils.truncatedCompareTo(todo.getLastModifyTime(), todo1.getLastModifyTime(), Calendar.MILLISECOND) >= 0) {
                if (DateUtils.truncatedCompareTo(reminderTime1, reminderTime, Calendar.MILLISECOND) >= 0) {
                    return index;
                }
            }
            //如果上边没拦住，说明是readFlag的最后一个，用下面的直接拦住
            if (DateUtils.truncatedEquals(reminderTime1, reminderTime, Calendar.DATE) //判断reminderTime
                    && todo.getReadFlag() < (todo1.getReadFlag())) {
                if (DateUtils.truncatedCompareTo(reminderTime1, reminderTime, Calendar.MILLISECOND) >= 0) {
                    return index;
                }
            }
            index++;
        }

        //如果所需的header不存在, 放在合适位置
        if (index == contentData.size()) {
            index = 0;
            for (Todo todo1 : contentData) {
                Date reminderTime = todo.getReminder() == null ? TODAY_MAX_TIME : todo.getReminder().getStart();
                Date reminderTime1 = todo1.getReminder() == null ? TODAY_MAX_TIME : todo1.getReminder().getStart();
                if (DateUtils.truncatedCompareTo(reminderTime, reminderTime1, Calendar.DATE) < 0) {
                    return index;
                }
                index++;
            }
        }
        return contentData.size();
    }

    @Override
    public void refresh() {
        super.refresh();
        for (TodoHeader headerDatum : headerData) {
            if (DateUtil.isToday(headerDatum.getContent())) {
                headerDatum.setDoneTodoCount(getTodayDoneCount());
                headerDatum.setTotalTodoCount(getTodayTotalCount());
            }
        }
    }

    private long getTodayTotalCount() {
        Date start = DateUtils.truncate(new Date(), Calendar.DATE);
        Date end = DateUtils.addDays(start, 1);
        return countToday("WHERE T.TYPE = 'todo' AND " +
                "(T0.START >= " + start.getTime() + " AND T0.START <" + end.getTime() +
                " OR T0.START IS NULL)");
    }

    private long getTodayDoneCount() {
        Date start = DateUtils.truncate(new Date(), Calendar.DATE);
        Date end = DateUtils.addDays(start, 1);
        return countToday("WHERE T.TYPE = 'todo' AND T.READ_FLAG = '1' AND " +
                "(T0.START >= " + start.getTime() + " AND T0.START <" + end.getTime() +
                " OR T0.START IS NULL)");
    }

    private long countToday(String where) {
        List<Todo> todos = todoDao.queryDeep(where);
        if (todos == null || todos.isEmpty()) {
            return 0;
        }
        return todos.size();
    }
}
