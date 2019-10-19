package com.beyond.note5.presenter;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Note;
import com.beyond.note5.event.AddNoteAllSuccessEvent;
import com.beyond.note5.event.UpdateNoteAllSuccessEvent;
import com.beyond.note5.event.note.AddNoteSuccessEvent;
import com.beyond.note5.event.note.DeleteNoteSuccessEvent;
import com.beyond.note5.event.note.UpdateNoteSuccessEvent;
import com.beyond.note5.model.NoteModel;
import com.beyond.note5.component.DaggerCommonComponent;
import com.beyond.note5.service.schedule.ScheduleReceiver;
import com.beyond.note5.service.schedule.callback.NoteExactNotifyScheduleCallback;
import com.beyond.note5.utils.HtmlUtil;
import com.beyond.note5.utils.PreferenceUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.NoteView;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.beyond.note5.MyApplication.NOTE_NOTIFICATION_SHOULD_SCHEDULE;
import static com.beyond.note5.service.schedule.ScheduleReceiver.NOTIFICATION_EXACT_REQUEST_CODE;
import static com.beyond.note5.service.schedule.callback.NoteNotifyScheduleCallback.NOTIFICATION_POINTS;

/**
 * @author: beyond
 * @date: 2019/1/31
 */

public class NotePresenterImpl implements NotePresenter {

    private NoteView noteView;
    @Inject
    NoteModel noteModel;
    @Inject
    ThreadPoolExecutor executorService;
    @Inject
    OkHttpClient okHttpClient;
    @Inject
    Handler handler;

    public NotePresenterImpl(@Nullable NoteView noteView) {
        this.noteView = noteView;
        DaggerCommonComponent.builder().build().inject(this);
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
            note.setVersion((note.getVersion() == null ? 0 : note.getVersion()) + 1);
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
            cancelSchedule(note);
        } catch (Exception e) {
            e.printStackTrace();
            this.deleteFail(note);
        }
    }

    @Override
    public void deleteLogic(Note note) {
        try {
            note.setVersion((note.getVersion() == null ? 0 : note.getVersion()) + 1);
            noteModel.deleteLogic(note);
            this.deleteSuccess(note);
            cancelSchedule(note);
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
            note.setVersion((note.getVersion() == null ? 0 : note.getVersion()) + 1);
            noteModel.deleteDeepLogic(note);
            this.deleteSuccess(note);
            cancelSchedule(note);
        } catch (Exception e) {
            e.printStackTrace();
            this.deleteFail(note);
        }
    }

    @Override
    public void updatePriority(Note note) {
        try {
            note.setVersion((note.getVersion() == null ? 0 : note.getVersion()) + 1);
            noteModel.update(note);
            updatePrioritySuccess(note);
            scheduleNotificationFromNow(note);
        } catch (Exception e) {
            e.printStackTrace();
            updatePriorityFail(note);
        }
    }

    private void scheduleNotificationFromNow(Note note) {
        scheduleNotificationFrom(note, System.currentTimeMillis());
    }

    private void scheduleNotificationFrom(Note note, long timeMillis) {
        try {
            boolean shouldSchedule = PreferenceUtil.getBoolean(NOTE_NOTIFICATION_SHOULD_SCHEDULE, false);
            if (!shouldSchedule) {
                return;
            }
            if (note.getPriority() == 5) {
                startScheduleFrom(note, timeMillis);
            } else {
                cancelSchedule(note);
            }
        }catch (Exception e){
            Log.e(getClass().getSimpleName(),"定时任务设置失败");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.toast(MyApplication.getInstance(),"定时任务设置失败");
                }
            });
        }
    }

    private void startScheduleFrom(Note note, long timeMillis) {
        try {
            Map<String, String> data = new HashMap<>(2);
            data.put("id", note.getId());
            data.put("index", String.valueOf(0));
            List<String> scheduleIds = getScheduleIds(note);
            for (int i = 0; i < scheduleIds.size(); i++) {
                ScheduleReceiver.scheduleOnce(MyApplication.getInstance(), NOTIFICATION_EXACT_REQUEST_CODE,
                        timeMillis + NOTIFICATION_POINTS[i] * 60 * 1000,
                        NoteExactNotifyScheduleCallback.class,
                        data, scheduleIds.get(i));
            }
        }catch (Exception e){
            Log.e(getClass().getSimpleName(),"定时任务设置失败");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.toast(MyApplication.getInstance(),"定时任务设置失败");
                }
            });
        }
    }

    private void cancelSchedule(Note note) {
        try {
            List<String> scheduleIds = getScheduleIds(note);
            for (String scheduleId : scheduleIds) {
                ScheduleReceiver.cancel(MyApplication.getInstance(), NOTIFICATION_EXACT_REQUEST_CODE, scheduleId);
            }
        }catch (Exception e){
            Log.e(getClass().getSimpleName(),"定时任务取消失败");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.toast(MyApplication.getInstance(),"定时任务取消失败");
                }
            });
        }

    }

    private List<String> getScheduleIds(Note note) {
        List<String> scheduleIs = new ArrayList<>(NOTIFICATION_POINTS.length);
        for (long notificationPoint : NOTIFICATION_POINTS) {
            scheduleIs.add(note.getId() + "/" + notificationPoint);
        }
        return scheduleIs;
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
    public void addAllForSync(List<Note> addList, String[] oppositeKeys) {
        try {
            noteModel.addAll(addList);
            addAllSuccess(addList);
        } catch (Exception e) {
            e.printStackTrace();
            addAllFail(e);
        }
    }

    @Override
    public void addAllForSync(List<Note> addList, String source) {
        try {
            noteModel.addAll(addList, source);
            addAllSuccess(addList);
            for (Note note : addList) {
                if (note.getLastModifyTime() == null){
                    continue;
                }
                scheduleNotificationFrom(note, note.getLastModifyTime().getTime());
            }
        } catch (Exception e) {
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
    public void updateAllForSync(List<Note> updateList, String[] oppositeKeys) {
        try {
            noteModel.updateAll(updateList);
            updateAllSuccess(updateList);
        } catch (Exception e) {
            e.printStackTrace();
            updateAllFail(e);
        }
    }

    @Override
    public void updateAllForSync(List<Note> updateList, String source) {
        try {
            noteModel.updateAll(updateList, source);
            updateAllSuccess(updateList);
            for (Note note : updateList) {
                if (note.getLastModifyTime() == null){
                    continue;
                }
                scheduleNotificationFrom(note, note.getLastModifyTime().getTime());
            }
        } catch (Exception e) {
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
    public List<Note> selectAllAfterLastModifyTime(Date date) {
        return noteModel.findAllAfterLastModifyTime(date);
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
                .addHeader("User-Agent", "PostmanRuntime/7.15.0")
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


    @Override
    public void search(String searchKey) {
        try {
            List<Note> allNote = noteModel.searchByContent(searchKey);
            searchSuccess(allNote);
        } catch (Exception e) {
            e.printStackTrace();
            this.searchFail(e);
        }
    }

    private void searchSuccess(List<Note> notes) {
        if (noteView!=null){
            noteView.onSearchSuccess(notes);
        }
    }

    private void searchFail(Exception e) {
        if (noteView!=null){
            noteView.onSearchFail(e);
        }
    }
}
