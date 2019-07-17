package com.beyond.note5.presenter;

import android.os.Handler;
import android.support.annotation.Nullable;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Note;
import com.beyond.note5.event.AddNoteAllSuccessEvent;
import com.beyond.note5.event.UpdateNoteAllSuccessEvent;
import com.beyond.note5.event.note.AddNoteSuccessEvent;
import com.beyond.note5.event.note.DeleteNoteSuccessEvent;
import com.beyond.note5.event.note.UpdateNoteSuccessEvent;
import com.beyond.note5.model.NoteModel;
import com.beyond.note5.model.NoteModelImpl;
import com.beyond.note5.utils.HtmlUtil;
import com.beyond.note5.view.NoteView;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public class NotePresenterImpl implements NotePresenter {

    private NoteView noteView;
    private NoteModel noteModel;
    private ExecutorService executorService;
    private final OkHttpClient okHttpClient;
    private final Handler handler;

    public NotePresenterImpl(@Nullable NoteView noteView) {
        this.noteView = noteView;
        this.noteModel = NoteModelImpl.getSingletonInstance();
        this.executorService = MyApplication.getInstance().getExecutorService();
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        httpBuilder.connectTimeout(10000, TimeUnit.MILLISECONDS);
        httpBuilder.readTimeout(10000, TimeUnit.MILLISECONDS);
        this.okHttpClient = httpBuilder.build();
        this.handler = new Handler();
    }

    @Override
    public void add(final Note note) {
        try {
            noteModel.add(note);
            addSuccess(note);
            updateTitleAsync(note);
        } catch (Exception e) {
            e.printStackTrace();
            addFail(note);
        }
    }

    @Override
    public void addSuccess(Note note) {
        if (noteView == null) {
            return;
        }
        EventBus.getDefault().post(new AddNoteSuccessEvent(note));
        noteView.onAddSuccess(note);
    }

    @Override
    public void addFail(Note note) {
        if (noteView == null) {
            return;
        }
        noteView.onAddFail(note);
    }

    @Override
    public void update(final Note note) {
        try {
            noteModel.update(note);
            updateSuccess(note);
            updateTitleAsync(note);
        } catch (Exception e) {
            e.printStackTrace();
            updateFail(note);
        }

    }

    @Override
    public void updateSuccess(Note note) {
        if (noteView == null) {
            return;
        }
        EventBus.getDefault().post(new UpdateNoteSuccessEvent(note));
        noteView.onUpdateSuccess(note);
    }

    @Override
    public void updateFail(Note note) {
        if (noteView == null) {
            return;
        }
        noteView.onUpdateFail(note);
    }

    @Override
    public void delete(Note note) {
        try {
            noteModel.delete(note);
            this.deleteSuccess(note);
        } catch (Exception e) {
            e.printStackTrace();
            this.deleteFail(note);
        }
    }

    @Override
    public void deleteLogic(Note note) {
        try {
            noteModel.deleteLogic(note);
            this.deleteSuccess(note);
        } catch (Exception e) {
            e.printStackTrace();
            this.deleteFail(note);
        }
    }

    @Override
    public void deleteDeep(Note note) {
        try {
            noteModel.deleteDeep(note);
            this.deleteSuccess(note);
        } catch (Exception e) {
            e.printStackTrace();
            this.deleteFail(note);
        }
    }

    @Override
    public void deleteDeepLogic(Note note) {
        try {
            noteModel.deleteDeepLogic(note);
            this.deleteSuccess(note);
        } catch (Exception e) {
            e.printStackTrace();
            this.deleteFail(note);
        }
    }

    @Override
    public void updatePriority(Note note) {
        try {
            noteModel.update(note);
            updatePrioritySuccess(note);
        } catch (Exception e) {
            e.printStackTrace();
            updatePriorityFail(note);
        }
    }

    @Override
    public void updatePrioritySuccess(Note note) {
        if (noteView == null) {
            return;
        }
        noteView.onUpdatePrioritySuccess(note);
    }

    @Override
    public void updatePriorityFail(Note note) {
        if (noteView == null) {
            return;
        }
        noteView.onUpdatePriorityFail(note);
    }

    @Override
    public void deleteSuccess(Note note) {
        if (noteView == null) {
            return;
        }
        EventBus.getDefault().post(new DeleteNoteSuccessEvent(note));
        noteView.onDeleteSuccess(note);
    }

    @Override
    public void deleteFail(Note note) {
        if (noteView == null) {
            return;
        }
        noteView.onDeleteFail(note);
    }

    @Override
    public void findAll() {
        try {
            List<Note> allNote = noteModel.findAll();
            findAllSuccess(allNote);
        } catch (Exception e) {
            e.printStackTrace();
            this.findAllFail();
        }
    }

    @Override
    public List<Note> selectAllInAll() {
        return noteModel.findAllInAll();
    }

    @Override
    public Note selectById(String id) {
        return noteModel.findById(id);
    }

    @Override
    public List<Note> selectByIds(Collection<String> ids) {
        return noteModel.findByIds(ids);
    }

    @Override
    public void addAll(List<Note> addList) {
        try {
            noteModel.addAll(addList);
            addAllSuccess(addList);
        }catch (Exception e){
            e.printStackTrace();
            addAllFail(e);
        }
    }

    private void addAllSuccess(List<Note> addList) {
        if (noteView == null) {
            return;
        }
        EventBus.getDefault().post(new AddNoteAllSuccessEvent(addList));
        noteView.onAddAllSuccess(addList);
    }

    private void addAllFail(Exception e) {
        if (noteView == null) {
            return;
        }
        noteView.onAddAllFail(e);
    }

    @Override
    public void updateAll(List<Note> updateList) {
        try {
            noteModel.updateAll(updateList);
            updateAllSuccess(updateList);
        }catch (Exception e){
            e.printStackTrace();
            updateAllFail(e);
        }
    }

    private void updateAllSuccess(List<Note> updateList) {
        if (noteView == null) {
            return;
        }
        EventBus.getDefault().post(new UpdateNoteAllSuccessEvent(updateList));
        noteView.onUpdateAllSuccess(updateList);
    }

    private void updateAllFail(Exception e) {
        if (noteView == null) {
            return;
        }
        noteView.onUpdateAllFail(e);
    }

    @Override
    public List<Note> selectByModifiedDate(Date date) {
        return noteModel.findByModifiedDate(date);
    }

    @Override
    public void findAllSuccess(List<Note> allNote) {
        if (noteView == null) {
            return;
        }
        noteView.onFindAllSuccess(allNote);
    }

    @Override
    public void findAllFail() {
        if (noteView == null) {
            return;
        }
        noteView.onFindAllFail();
    }

    private void updateTitleAsync(final Note note) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                boolean isNeedUpdate = false;
                try {
                    isNeedUpdate = processTitle(note);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (isNeedUpdate) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            update(note);
                        }
                    });
                }
            }
        });
    }


    /**
     * 生曾title
     *
     * @param note note
     * @return 是否需要更新
     * @throws Exception 异常
     */
    private boolean processTitle(Note note) throws Exception {

        String url = HtmlUtil.getUrl2(note.getContent());
        if (StringUtils.isBlank(url)) {
            return false;
        }

        String titleFromUrl = this.getTitleFromUrl(url);
        if (StringUtils.isNotBlank(titleFromUrl)) {
            if (StringUtils.equals(titleFromUrl, note.getTitle())) {
                return false;
            }
            note.setTitle(titleFromUrl);
            return true;
        }
        return false;
    }

    private String getTitleFromUrl(String url) throws IOException {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .build();
        Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        if (!response.isSuccessful()) {
            return null;
        }
        if (response.body() == null) {
            return null;
        }
        String titleFromHtml = HtmlUtil.getTitleFromHtml(response.body().string());
        if (StringUtils.isNotBlank(titleFromHtml)) {
            return titleFromHtml;
        }
        return null;
    }
}
