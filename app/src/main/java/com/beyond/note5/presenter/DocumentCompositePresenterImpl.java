package com.beyond.note5.presenter;

import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Note;
import com.beyond.note5.bean.Reminder;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.TimeNLPUtil;
import com.beyond.note5.view.DocumentCompositeView;

import java.util.Date;

public class DocumentCompositePresenterImpl implements DocumentCompositePresenter {

    private NotePresenter notePresenter;
    private TodoPresenter todoPresenter;
    private CalendarPresenter calendarPresenter;
    private PredictPresenter predictPresenter;

    public DocumentCompositePresenterImpl(DocumentCompositeView documentCompositeView) {
        notePresenter = new NotePresenterImpl(documentCompositeView.getNoteView());
        todoPresenter = new TodoPresenterImpl(documentCompositeView.getTodoView());
        calendarPresenter = new CalendarPresenterImpl(documentCompositeView.getActivity(),
                documentCompositeView.getCalendarView());
        predictPresenter = new PredictPresenterImpl(documentCompositeView.getPredictView());
    }

    @Override
    public void add(Document document) {
        try {

            if (Document.NOTE.equals(this.getDocumentType())) {
                Note note = new Note();
                copyProperties(note, document);
                notePresenter.add(note);
            }
            if (Document.TODO.equals(this.getDocumentType())) {
                Todo todo = new Todo();
                copyProperties(todo, document);
                Date reminderStart = TimeNLPUtil.parse(todo.getContent());
                if (reminderStart!=null){
                    Reminder reminder = new Reminder();
                    reminder.setId(IDUtil.uuid());
                    reminder.setStart(reminderStart);
                    todo.setReminder(reminder);
                    todo.setReminderId(reminder.getId());
                }
                todoPresenter.add(todo);
                if (todo.getReminder()!=null) {
                    calendarPresenter.add(todo);
                }
                predictPresenter.train(todo.getContent());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        //Todo
        return Document.TODO;
    }

}
