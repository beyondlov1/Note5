package com.beyond.note5.view.adapter.list.header;

/**
 * @author beyondlov1
 * @date 2019/03/29
 */
public class TodoHeader extends Header {

    private long totalTodoCount;
    private long doneTodoCount;

    public TodoHeader(int position, String content) {
        super(position, content);
    }

    public long getTotalTodoCount() {
        return totalTodoCount;
    }

    public void setTotalTodoCount(long totalTodoCount) {
        this.totalTodoCount = totalTodoCount;
    }

    public long getDoneTodoCount() {
        return doneTodoCount;
    }

    public void setDoneTodoCount(long doneTodCount) {
        this.doneTodoCount = doneTodCount;
    }
}
