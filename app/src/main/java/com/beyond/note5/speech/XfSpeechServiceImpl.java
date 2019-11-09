package com.beyond.note5.speech;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.beyond.note5.event.SpeechBeginEvent;
import com.beyond.note5.event.SpeechEndEvent;
import com.beyond.note5.permission.RequirePermission;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import org.greenrobot.eventbus.EventBus;

public class XfSpeechServiceImpl implements SpeechService {

    public static final String XF_APP_ID = "xxxxx";

    private SpeechRecognizer mIat;

    public XfSpeechServiceImpl() {
    }

    public XfSpeechServiceImpl(SpeechRecognizer mIat) {
        this.mIat = mIat;
    }

    @Override
    @RequirePermission(Manifest.permission.RECORD_AUDIO)
    public void speak(Context context, SpeakListener listener) {
        //使用SpeechRecognizer对象，可根据回调消息自定义界面；

        //设置参数
        mIat.setParameter(SpeechConstant.PARAMS, "iat");      //应用领域
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn"); //语音
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin"); //普通话
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);//引擎
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");//返回结果格式
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS,"1000");
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        //mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");

        //开始听写
        mIat.startListening(new RecognizerListener() {
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {
            }

            @Override
            public void onBeginOfSpeech() {
                EventBus.getDefault().post(new SpeechBeginEvent(null));
            }

            @Override
            public void onEndOfSpeech() {
                EventBus.getDefault().post(new SpeechEndEvent(null));
            }

            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String text = JsonParser.parseIatResult(recognizerResult.getResultString());
                Log.d("onResult", text);
                listener.onRecognized(text);
            }

            @Override
            public void onError(SpeechError speechError) {

            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {
            }
        });
    }


    public SpeechRecognizer getmIat() {
        return mIat;
    }

    public void setmIat(SpeechRecognizer mIat) {
        this.mIat = mIat;
    }
}
