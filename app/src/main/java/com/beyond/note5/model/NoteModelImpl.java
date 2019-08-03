package com.beyond.note5.model;

import android.database.sqlite.SQLiteConstraintException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Note;
import com.beyond.note5.model.dao.AttachmentDao;
import com.beyond.note5.model.dao.DaoSession;
import com.beyond.note5.model.dao.NoteDao;
import com.beyond.note5.model.dao.SyncStateDao;
import com.beyond.note5.model.dao.TraceLogDao;
import com.beyond.note5.sync.model.entity.TraceLog;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.PhotoUtil;
import com.beyond.note5.utils.PreferenceUtil;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public class NoteModelImpl implements NoteModel {

    private NoteDao noteDao;

    private AttachmentDao attachmentDao;

    private TraceLogDao traceLogDao;

    private SyncStateDao syncStateDao;

    public static NoteModel getSingletonInstance() {
        return NoteModelHolder.noteModel;
    }

    private static class NoteModelHolder {
        private static final NoteModel noteModel = new NoteModelImpl();
    }

    public NoteModelImpl() {
        DaoSession daoSession = MyApplication.getInstance().getDaoSession();
        noteDao = daoSession.getNoteDao();
        attachmentDao = daoSession.getAttachmentDao();
        traceLogDao = daoSession.getTraceLogDao();
        syncStateDao = daoSession.getSyncStateDao();
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
            } catch (SQLiteConstraintException e) {
                e.printStackTrace();
                Log.i("NoteModelImpl", "attachment主键重复");
            }
        }

        onInserted(note,PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID));
    }

    @Override
    public void update(Note note) {
        noteDao.update(note);
        List<Attachment> attachments = note.getAttachments();
        if (CollectionUtils.isNotEmpty(attachments)) {
            attachmentDao.updateInTx(attachments);
        }

        onUpdated(note,PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID));
    }

    @Override
    public void deleteLogic(Note note) {
        note.setLastModifyTime(new Date());
        note.setValid(false);
        noteDao.update(note);

        onUpdated(note,PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID));
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
        note.setLastModifyTime(new Date());
        note.setValid(false);
        noteDao.update(note);

        List<Attachment> attachments = note.getAttachments();
        if (CollectionUtils.isNotEmpty(attachments)) {
            for (Attachment attachment : attachments) {
                String path = attachment.getPath();
                File file = new File(path);
                FileUtils.deleteQuietly(file);
            }
            attachmentDao.deleteInTx(attachments);
        }

        onUpdated(note,PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID));
    }

    @Override
    public List<Note> findByPriority(int priority) {
        return noteDao.queryBuilder()
                .where(NoteDao.Properties.Type.eq(Document.NOTE))
                .where(NoteDao.Properties.Priority.eq(priority))
                .where(NoteDao.Properties.Valid.eq(true))
                .list();
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

    @Override
    public Note findById(String id) {
        return noteDao.load(id);
    }

    @Override
    public List<Note> findAllAfterLastModifyTime(Date date) {
        return noteDao.queryBuilder()
                .where(NoteDao.Properties.Type.eq(Document.NOTE))
                .where(NoteDao.Properties.LastModifyTime.gt(date))
                .orderAsc(NoteDao.Properties.ReadFlag)
                .orderDesc(NoteDao.Properties.LastModifyTime)
                .list();
    }

    @Override
    public List<Note> findByIds(Collection<String> ids) {
        return noteDao.queryBuilder()
                .where(NoteDao.Properties.Id.in(ids))
                .list();
    }

    @Override
    public void addAll(List<Note> addList) {
        addAll(addList,PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID));
    }

    @Override
    public void addAll(List<Note> addList, String source) {
        noteDao.insertInTx(addList);
        List<Attachment> allAttachments = new ArrayList<>();
        for (Note note : addList) {
            if (note.getAttachments() != null) {
                allAttachments.addAll(note.getAttachments());
            }
        }

        if (CollectionUtils.isNotEmpty(allAttachments)) {
            try {
                attachmentDao.insertInTx(allAttachments);
                //压缩图片
                for (Attachment attachment : allAttachments) {
                    PhotoUtil.compressImage(attachment.getPath());
                }
            } catch (SQLiteConstraintException e) {
                e.printStackTrace();
                Log.i("NoteModelImpl", "attachment主键重复");
            }
        }

        onInsertedAll(source,addList.toArray(new Note[0]));
    }


    @Override
    public void updateAll(List<Note> updateList) {
        updateAll(updateList,PreferenceUtil.getString(MyApplication.VIRTUAL_USER_ID));
    }

    @Override
    public void updateAll(List<Note> updateList, String source) {
        noteDao.updateInTx(updateList);

        List<Attachment> allAttachments = new ArrayList<>();
        for (Note note : updateList) {
            if (note.getAttachments() != null) {
                allAttachments.addAll(note.getAttachments());
            }
        }
        if (CollectionUtils.isNotEmpty(allAttachments)) {
            attachmentDao.updateInTx(allAttachments);
        }

        onUpdatedAll(source,updateList.toArray(new Note[0]));
    }



    private void onInserted(Note note, String source) {
        addInsertLog(note,source);
    }

    private void onUpdated(Note note, String source) {
        addUpdateLog(note,source);
        removeSyncSuccessStateInfo(note);
    }

    private void onInsertedAll(String source,Note... addList) {
        addAllInsertLog(source,addList);
    }

    private void onUpdatedAll(String source,Note... updateList) {
        addAllUpdateLog( source,updateList);
    }



    private void addAllInsertLog(String source, Note... notes) {
        List<TraceLog> traceLogs = new ArrayList<>(notes.length);
        for (Note note : notes) {
            traceLogs.add(createAddSyncLogInfo(note,source));
        }
        traceLogDao.insertInTx(traceLogs);
    }

    private void addAllUpdateLog( String source,Note... notes) {
        List<TraceLog> traceLogs = new ArrayList<>(notes.length);
        for (Note note : notes) {
            traceLogs.add(createUpdateSyncLogInfo(note,source));
        }
        traceLogDao.insertInTx(traceLogs);
    }

    private void addInsertLog(Note note, String source) {
        TraceLog traceLog = createAddSyncLogInfo(note,source);
        traceLogDao.insert(traceLog);
    }

    private void addUpdateLog(Note note,String source) {
        TraceLog traceLog = createUpdateSyncLogInfo(note,source);
        traceLogDao.insert(traceLog);
    }



    @NonNull
    private TraceLog createAddSyncLogInfo(Note note, String source) {
        TraceLog traceLog = new TraceLog();
        traceLog.setId(IDUtil.uuid());
        traceLog.setDocumentId(note.getId());
        traceLog.setOperation(TraceLog.ADD);
        traceLog.setOperationTime(note.getLastModifyTime());
        traceLog.setCreateTime(new Date());
        traceLog.setSource(source);
        traceLog.setType(Note.class.getSimpleName().toLowerCase());
        return traceLog;
    }

    @NonNull
    private TraceLog createUpdateSyncLogInfo(Note note, String source) {
        TraceLog traceLog = new TraceLog();
        traceLog.setId(IDUtil.uuid());
        traceLog.setDocumentId(note.getId());
        traceLog.setOperation(TraceLog.UPDATE);
        traceLog.setOperationTime(note.getLastModifyTime());
        traceLog.setCreateTime(new Date());
        traceLog.setSource(source);
        traceLog.setType(Note.class.getSimpleName().toLowerCase());
        return traceLog;
    }


    private void removeSyncSuccessStateInfo(Note note) {
        syncStateDao.queryBuilder()
                .where(SyncStateDao.Properties.DocumentId.eq(note.getId()))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }
}
