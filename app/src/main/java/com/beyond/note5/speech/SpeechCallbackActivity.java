package com.beyond.note5.speech;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;

import com.beyond.note5.event.SpeechRecognizedEvent;

import org.apache.commons.collections.CollectionUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class SpeechCallbackActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //通过Intent传递语音识别的模式，开启语音
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //语言模式和自由模式的语音识别
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //提示语音开始
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "开始语音");

        startActivityForResult(intent,77);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 77){
            //取得语音的字符
            ArrayList<String> results=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //谷歌可能有许多语音类似的返回，越往上优先级越高，这里列出所有的返回并拼接成字符串
            if (!CollectionUtils.isEmpty(results)){
                String text = results.get(0);
                EventBus.getDefault().postSticky(new SpeechRecognizedEvent(text));
            }
        }
        finish();
    }
}
