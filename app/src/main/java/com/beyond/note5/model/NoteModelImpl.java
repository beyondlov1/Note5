package com.beyond.note5.model;

import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Note;
import com.beyond.note5.model.dao.AttachmentDao;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.model.dao.NoteDao;
import com.beyond.note5.utils.PhotoUtil;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public class NoteModelImpl implements NoteModel {

    private NoteDao noteDao;

    private AttachmentDao attachmentDao;

    public static NoteModel getSingletonInstance(){
        return NoteModelHolder.noteModel;
    }

    private static class NoteModelHolder {
        private static final NoteModel noteModel = new NoteModelImpl();
    }

    public NoteModelImpl() {
        DaoSession daoSession = MyApplication.getInstance().getDaoSession();
        noteDao = daoSession.getNoteDao();
        attachmentDao = daoSession.getAttachmentDao();
    }

    @Override
    public void add(Note note) {
        noteDao.insert(note);
        List<Attachment> attachments = note.getAttachments();
        if (CollectionUtils.isNotEmpty(attachments)) {
            try {
                attachmentDao.insertInTx(attachments);
                //压缩图片
                for (Attachment attachment : attachments) {
                    PhotoUtil.compressImage(attachment.getPath());
                }
            }catch (SQLiteConstraintException e){
                e.printStackTrace();
                Log.i("NoteModelImpl","attachment主键重复");
            }
        }

    }

    @Override
    public void update(Note note) {
        noteDao.update(note);
        List<Attachment> attachments = note.getAttachments();
        if (CollectionUtils.isNotEmpty(attachments)) {
            attachmentDao.updateInTx(attachments);
        }
    }

    @Override
    public void deleteLogic(Note note) {
        note.setLastModifyTime(new Date());
        note.setValid(false);
        noteDao.update(note);
    }

    @Override
    public void delete(Note note) {
        noteDao.delete(note);
    }

    @Override
    public void deleteDeep(Note note) {
        this.delete(note);
        List<Attachment> attachments = note.getAttachments();
        if (CollectionUtils.isNotEmpty(attachments)) {
            for (Attachment attachment : attachments) {
                String path = attachment.getPath();
                File file = new File(path);
                FileUtils.deleteQuietly(file);
            }
            attachmentDao.deleteInTx(attachments);
        }
    }

    @Override
    public void deleteDeepLogic(Note note) {
        this.deleteLogic(note);
        List<Attachment> attachments = note.getAttachments();
        if (CollectionUtils.isNotEmpty(attachments)) {
            for (Attachment attachment : attachments) {
                String path = attachment.getPath();
                File file = new File(path);
                FileUtils.deleteQuietly(file);
            }
            attachmentDao.deleteInTx(attachments);
        }
    }

    @Override
    public List<Note> findAll() {
        return noteDao.queryBuilder()
                .where(NoteDao.Properties.Type.eq(Document.NOTE))
                .where(NoteDao.Properties.Valid.eq(true))
                .orderAsc(NoteDao.Properties.ReadFlag)
                .orderDesc(NoteDao.Properties.LastModifyTime)
                .list();
    }

    @Override
    public List<Note> findAllInAll() {
        return noteDao.queryBuilder()
                .where(NoteDao.Properties.Type.eq(Document.NOTE))
                .orderAsc(NoteDao.Properties.ReadFlag)
                .orderDesc(NoteDao.Properties.LastModifyTime)
                .list();
    }


}
