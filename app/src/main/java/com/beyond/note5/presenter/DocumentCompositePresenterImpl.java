package com.beyond.note5.presenter;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Note;
import com.beyond.note5.bean.Reminder;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.model.CalendarModel;
import com.beyond.note5.model.CalendarModelImpl;
import com.beyond.note5.model.NoteModel;
import com.beyond.note5.model.NoteModelImpl;
import com.beyond.note5.model.PredictModel;
import com.beyond.note5.model.TodoModel;
import com.beyond.note5.model.TodoModelImpl;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.TimeNLPUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.DocumentCompositeView;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class DocumentCompositePresenterImpl implements DocumentCompositePresenter {

    private NoteModel noteModel;
    private TodoModel todoModel;
    private CalendarModel calendarModel;
    private PredictModel predictModel;
    private DocumentCompositeView documentCompositeView;

    public DocumentCompositePresenterImpl(DocumentCompositeView documentCompositeView) {
        noteModel = new NoteModelImpl();
        todoModel = new TodoModelImpl();
        calendarModel = new CalendarModelImpl(documentCompositeView.getActivity());
        predictModel = MyApplication.getInstance().getPredictModel();
        this.documentCompositeView = documentCompositeView;
    }

    @Override
    public void add(Document document) {
        try {

            if (Document.NOTE.equals(this.getDocumentType())) {
                Note note = new Note();
                copyProperties(note, document);
                noteModel.add(note);
                onNoteAddSuccess(note);
            }
            if (Document.TODO.equals(this.getDocumentType())) {
                Todo todo = new Todo();
                copyProperties(todo, document);

                Date reminderStart = TimeNLPUtil.parse(todo.getContent());
                if (reminderStart != null) {
                    Reminder reminder = new Reminder();
                    reminder.setId(IDUtil.uuid());
                    reminder.setStart(reminderStart);
                    todo.setReminder(reminder);
                    todo.setReminderId(reminder.getId());
                }
                this.fillContentWithoutTime(todo);
                todoModel.add(todo);
                if (todo.getReminder() != null) {
                    calendarModel.add(todo);
                }
                MyApplication.getInstance().getExecutorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            predictModel.train(todo.getContent());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                onTodoAddSuccess(todo);
            }
        }catch (Exception e){
            e.printStackTrace();
            onAddFail();
        }
    }

    /**
     * 计算无时间内容
     * 要不要改成异步， 看情况吧
     * @param todo 待办
     */
    private void fillContentWithoutTime(Todo todo) {
        String contentWithoutTime = StringUtils.trim(TimeNLPUtil.getOriginExpressionWithoutTime(StringUtils.trim(todo.getContent())));
        if (StringUtils.isBlank(contentWithoutTime)) {
            contentWithoutTime = StringUtils.trim(todo.getContent());
        }
        todo.setContentWithoutTime(contentWithoutTime);
    }

    private void copyProperties(Document dest, Document orgi) {
        dest.setId(orgi.getId());
        dest.setTitle(orgi.getTitle());
        dest.setContent(orgi.getContent());
        dest.setPriority(orgi.getPriority());
        dest.setVersion(orgi.getVersion());
        dest.setCreateTime(orgi.getCreateTime());
        dest.setReadFlag(orgi.getReadFlag());
        dest.setLastModifyTime(orgi.getLastModifyTime());
    }

    private String getDocumentType() {
        return Document.TODO;
    }


    private void onNoteAddSuccess(Note document) {
        documentCompositeView.onNoteAddSuccess(document);
        ToastUtil.toast(documentCompositeView.getActivity(),"添加记录成功");
    }

    private void onTodoAddSuccess(Todo document) {
        documentCompositeView.onTodoAddSuccess(document);
        ToastUtil.toast(documentCompositeView.getActivity(),"添加待办事项成功");

    }

    private void onAddFail() {
        ToastUtil.toast(documentCompositeView.getActivity(),"添加失败");
    }

}
