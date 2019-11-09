package com.beyond.note5.speech;

import android.content.Context;

public interface SpeechService {
    void speak(Context context,SpeakListener listener);

    interface SpeakListener{
        void onRecognized(String result);
    }
}
