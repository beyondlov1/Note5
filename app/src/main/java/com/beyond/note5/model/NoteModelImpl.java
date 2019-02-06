package com.beyond.note5.model;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Note;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.model.dao.NoteDao;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public class NoteModelImpl implements NoteModel {

    private NoteDao noteDao;

    public NoteModelImpl(){
        DaoSession daoSession = MyApplication.getInstance().getDaoSession();
        noteDao = daoSession.getNoteDao();
    }

    @Override
    public void add(Note note){
        noteDao.insert(note);
    }

    @Override
    public void update(Note note) {
        noteDao.update(note);
    }

    @Override
    public void delete(Note note) {
        noteDao.delete(note);
    }

    @Override
    public List<Note> findAll() {
        return noteDao.queryBuilder()
                .where(NoteDao.Properties.Type.eq(Document.NOTE))
                .orderDesc(NoteDao.Properties.LastModifyTime)
                .list();
    }
}
