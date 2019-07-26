package com.beyond.note5.utils;

import android.os.Handler;
import android.util.Log;

import com.beyond.note5.event.PollRequest;
import com.beyond.note5.event.PollResponse;
import com.beyond.note5.view.MainActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class PollSupport {

    private static final int MAX_CHECK_COUNT = 7;

    private int checkCount = MAX_CHECK_COUNT;
    private boolean isResponse = false;
    private Object responseData;

    public void sendRequest(OnResponseListener listener) {
        EventBus.getDefault().register(this);
        poll(listener);
    }

    private void poll(OnResponseListener listener) {
        if (isResponse) {
            listener.onResponse(responseData);
            reset();
            return;
        }
        if (checkCount < 0) {
            reset();
            return;
        }
        checkCount--;
        EventBus.getDefault().post(new PollRequest(MainActivity.class));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                poll(listener);
            }
        }, 100);
    }

    private void reset() {
        checkCount = MAX_CHECK_COUNT;
        isResponse = false;
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceived(PollResponse event) {
        responseData = event.get();
        isResponse = true;
        Log.d(getClass().getSimpleName(), "polling success");
    }

    public interface OnResponseListener {
        void onResponse(Object data);
    }
}