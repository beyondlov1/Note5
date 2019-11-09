package com.beyond.note5.speech;

import android.content.Context;
import android.content.Intent;

public class SpeechServiceImpl implements SpeechService {

    @Override
    public void speak(Context context,SpeakListener listener) {
        try{
            Intent internalIntent = new Intent(context,SpeechCallbackActivity.class);
            context.startActivity(internalIntent);
        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
