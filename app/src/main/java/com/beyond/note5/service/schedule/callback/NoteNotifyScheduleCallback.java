package com.beyond.note5.service.schedule.callback;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.model.NoteModel;
import com.beyond.note5.model.NoteModelImpl;
import com.beyond.note5.service.NotificationClickReceiver;
import com.beyond.note5.service.schedule.utils.ScheduleUtil;
import com.beyond.note5.utils.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.beyond.note5.view.MainActivity.NOTIFICATION_REDIRECT_REQUEST_CODE;

public class NoteNotifyScheduleCallback implements ScheduleCallback {

    private static final String ACTION = "com.beyond.note5.intent.action.NOTIFICATION_CLICK";

    private static final int DEFAULT_SCAN_PERIOD = 60 * 1000;

    public static final long[] NOTIFICATION_POINTS = new long[]{
            1, 19, 63, 525, 24 * 60, 24 * 60 * 2, 24 * 60 * 6, 24 * 60 * 31
    }; // minute

    protected static final int SILENCE_HOUR_START = 23;
    protected static final int SILENCE_HOUR_PERIOD = 10;

    protected NoteModel noteModel;

    protected Handler handler;

    public NoteNotifyScheduleCallback() {
        noteModel = new NoteModelImpl();
        handler = new Handler();
    }

    @Override
    public void onCall(Context context, Intent intent) throws ClassNotFoundException {
        long scanPeriod = intent.getLongExtra("delay", DEFAULT_SCAN_PERIOD);
        List<Note> notifyNotes = new ArrayList<>();
        List<Note> highlightNotes = noteModel.findByPriority(5);
        long currentTimeMillis = System.currentTimeMillis();
        for (Note highlightNote : highlightNotes) {
            for (long notificationPoint : NOTIFICATION_POINTS) {
                if (DateUtil.between(currentTimeMillis, highlightNote.getLastModifyTime()) <= notificationPoint * 1000 * 60 + scanPeriod/2 - 1000
                        && DateUtil.between(currentTimeMillis, highlightNote.getLastModifyTime()) > notificationPoint * 1000 * 60) {
                    notifyNotes.add(highlightNote);
                }
            }
        }
        for (Note notifyNote : notifyNotes) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    buildNotification(context, notifyNote, 0);
                }
            });
        }
    }

    protected void buildNotification(Context context, Note note, Integer index) {
        Intent intent = new Intent(context, NotificationClickReceiver.class);
        intent.setData(Uri.parse(ScheduleUtil.getScheduleId(note,index)));
        intent.setAction(ACTION);
        intent.putExtra("id", note.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_REDIRECT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "noteChannel");
        builder.setContentTitle(note.getTitle())
                .setContentText(note.getContent())
                .setStyle(new NotificationCompat.BigTextStyle())
                .setSmallIcon(R.drawable.ic_done_blue_24dp)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (!DateUtil.in(new Date(), SILENCE_HOUR_START, SILENCE_HOUR_PERIOD)) {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        }

        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(note.getId().hashCode(), notification);
    }


}
