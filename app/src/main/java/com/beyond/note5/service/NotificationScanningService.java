package com.beyond.note5.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.beyond.note5.R;
import com.beyond.note5.bean.Note;
import com.beyond.note5.model.NoteModel;
import com.beyond.note5.model.NoteModelImpl;
import com.beyond.note5.utils.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.beyond.note5.view.MainActivity.NOTIFICATION_REDIRECT_REQUEST_CODE;

public class NotificationScanningService extends Service {

    private static final String ACTION = "com.beyond.note5.intent.action.NOTIFICATION_CLICK";

    private static final int SCAN_PERIOD = 1;// minute

    private static final int[] NOTIFICATION_POINTS = new int[]{
            19, 63, 525, 24 * 60, 24 * 60 * 2, 24 * 60 * 6, 24 * 60 * 31
    }; // minute

    private static final int SILENCE_HOUR_START = 23;
    private static final int SILENCE_HOUR_PERIOD = 10;

    private NoteModel noteModel;

    private Handler handler;

    private Timer timer;

    @Override
    public void onCreate() {
        super.onCreate();
        noteModel = new NoteModelImpl();
        handler = new Handler();
        timer = new Timer(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer.schedule(new Scanner(), 0, SCAN_PERIOD * 60 * 1000);
        return super.onStartCommand(intent, flags, startId);
    }

    private void buildNotification(Note note) {
        Intent intent = new Intent(getApplicationContext(), NotificationClickReceiver.class);
        intent.setAction(ACTION);
        intent.putExtra("id", note.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), NOTIFICATION_REDIRECT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "noteChannel");
        builder.setContentTitle(note.getTitle())
                .setContentText(note.getContent())
                .setSmallIcon(R.drawable.ic_done_blue_24dp)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent);

        if (!DateUtil.in(new Date(), SILENCE_HOUR_START, SILENCE_HOUR_PERIOD)) {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        }

        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(note.getId().hashCode(), notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class Scanner extends TimerTask {

        @Override
        public void run() {
            List<Note> notifyNotes = new ArrayList<>();
            List<Note> highlightNotes = noteModel.findByPriority(5);
            for (Note highlightNote : highlightNotes) {
                for (int notificationPoint : NOTIFICATION_POINTS) {
                    if (DateUtil.between(new Date(), highlightNote.getLastModifyTime()) <= (notificationPoint + SCAN_PERIOD) * 1000 * 60 + 1000
                            && DateUtil.between(new Date(), highlightNote.getLastModifyTime()) > notificationPoint * 1000 * 60) {
                        notifyNotes.add(highlightNote);
                    }
                }
            }
            for (Note notifyNote : notifyNotes) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        buildNotification(notifyNote);
                    }
                });
            }
        }
    }
}
