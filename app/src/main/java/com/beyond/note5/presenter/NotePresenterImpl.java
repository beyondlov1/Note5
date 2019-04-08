package com.beyond.note5.presenter;

import android.os.Handler;
import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Note;
import com.beyond.note5.model.NoteModel;
import com.beyond.note5.model.NoteModelImpl;
import com.beyond.note5.utils.HtmlUtil;
import com.beyond.note5.view.NoteView;

import org.apache.commons.lang3.StringUtils;

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

    public NotePresenterImpl(NoteView noteView) {
        this.noteView = noteView;
        this.noteModel = new NoteModelImpl();
        this.executorService = MyApplication.getInstance().getExecutorService();
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        httpBuilder.connectTimeout(3000, TimeUnit.MILLISECONDS);
        httpBuilder.readTimeout(3000, TimeUnit.MILLISECONDS);
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
        noteView.onAddSuccess(note);
    }

    @Override
    public void addFail(Note note) {
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
        noteView.onUpdateSuccess(note);
    }

    @Override
    public void updateFail(Note note) {
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
    public void deleteSuccess(Note note) {
        noteView.onDeleteSuccess(note);
    }

    @Override
    public void deleteFail(Note note) {
        noteView.onDeleteFail(note);
    }

    @Override
    public void findAll() {
        try {
            List<Note> allNote = noteModel.findAll();
            this.findAllSuccess(allNote);
        } catch (Exception e) {
            e.printStackTrace();
            this.findAllFail();
        }
    }

    @Override
    public void findAllSuccess(List<Note> allNote) {
        noteView.onFindAllSuccess(allNote);
    }

    @Override
    public void findAllFail() {
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

        String url = HtmlUtil.getUrl(note.getContent());
        if (StringUtils.isBlank(url)) {
            return false;
        }

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .build();
        Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        if (!response.isSuccessful()) {
            return false;
        }
        if (response.body() == null) {
            return false;
        }
        String titleFromHtml = HtmlUtil.getTitleFromHtml(response.body().string());
        Log.d("NotePresenterImpl", titleFromHtml + "");
        if (StringUtils.isNotBlank(titleFromHtml)) {
            if (StringUtils.equals(titleFromHtml, note.getTitle())) {
                return false;
            }
            note.setTitle(titleFromHtml);
            return true;
        }
        return false;
    }
}
