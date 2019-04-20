package com.beyond.note5.view.listener;

import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.beyond.note5.MyApplication;
import com.beyond.note5.utils.HtmlUtil;
import com.beyond.note5.utils.TimeNLPUtil;
import com.time.nlp.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutorService;

/**
 * @author beyondlov1
 * @date 2019/03/29
 */
@SuppressWarnings("WeakerAccess")
public class TimeExpressionDetectiveTextWatcher implements TextWatcher {

    private Handler handler;
    private EditText target;
    private ExecutorService executorService;

    private String lastStr = null;
    private int lastSelectionEnd;
    private int timeExpressionStartIndex;
    private int timeExpressionEndIndex;
    private OnTimeExpressionChangedHandler onTimeExpressionChangedHandler;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, final int start, final int before, int count) {
        final String source = s.toString();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (before > 0) { // 删除字符， 直接返回
                    lastStr = null;
                    return;
                }
                if (StringUtils.equals(lastStr, source)) {
                    lastStr = null;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            target.setSelection(lastSelectionEnd);
                        }
                    });
                    return;
                }

                final TimeUnit timeUnit = TimeNLPUtil.parseForTimeUnit(StringUtils.trim(source));
                if (timeUnit == null){
                    return;
                }
                final String html = highlightTimeExpression(source,timeUnit);
                if (start < timeExpressionStartIndex
                        || start > timeExpressionEndIndex) { // 未更改时间
                    lastStr = null;
                    return;
                }
                if (html == null) {
                    lastStr = null;
                    return;
                }
                lastStr = source;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        lastSelectionEnd = target.getSelectionEnd(); // 光标定位到原来的位置
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            target.setText(HtmlUtil.fromHtml(html));
                        }else {
                            target.setText(source);
                        }
                        int length = target.getText().length();
                        target.setSelection(lastSelectionEnd< length ?lastSelectionEnd: length); // 两个空格挨在一起在html化的过程中会变成一个
                        if (onTimeExpressionChangedHandler!=null){
                            onTimeExpressionChangedHandler.handle(timeUnit);
                        }
                    }
                });

            }
        });
    }

    private String highlightTimeExpression(String source, TimeUnit timeUnit) {
        String timeExpression = StringUtils.trim(timeUnit.Origin_Time_Expression);
        if (StringUtils.isNotBlank(timeExpression)) {
            timeExpressionStartIndex = source.indexOf(timeExpression);
            timeExpressionEndIndex = timeExpressionStartIndex + timeExpression.length();
            return source.replace(timeExpression, "<span style='" +
                    "background:lightgray;'>" +
                    timeExpression + "</span>");
        }
        return null;
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public EditText getTarget() {
        return target;
    }

    public void setTarget(EditText target) {
        this.target = target;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setOnTimeExpressionChangedHandler(OnTimeExpressionChangedHandler onTimeExpressionChangedHandler) {
        this.onTimeExpressionChangedHandler = onTimeExpressionChangedHandler;
    }

    public OnTimeExpressionChangedHandler getOnTimeExpressionChangedHandler() {
        return onTimeExpressionChangedHandler;
    }

    public static class Builder{

        private TimeExpressionDetectiveTextWatcher timeExpressionDetectiveTextWatcher;

        public Builder(EditText target){
            if (timeExpressionDetectiveTextWatcher == null){
                timeExpressionDetectiveTextWatcher = new TimeExpressionDetectiveTextWatcher();
            }
            timeExpressionDetectiveTextWatcher.setTarget(target);
        }

        public Builder executorService( @NonNull ExecutorService executorService){
            timeExpressionDetectiveTextWatcher.setExecutorService(executorService);
            return this;
        }

        public Builder handler( @NonNull Handler handler){
            timeExpressionDetectiveTextWatcher.setHandler(handler);
            return this;
        }

        public Builder timeExpressionChangedHandler(OnTimeExpressionChangedHandler onTimeExpressionChangedHandler){
            timeExpressionDetectiveTextWatcher.setOnTimeExpressionChangedHandler(onTimeExpressionChangedHandler);
            return this;
        }

        public TimeExpressionDetectiveTextWatcher build(){
            if (timeExpressionDetectiveTextWatcher.getTarget() == null){
                throw new RuntimeException("目标不能为空");
            }
            if (timeExpressionDetectiveTextWatcher.getHandler() == null){
                timeExpressionDetectiveTextWatcher.setHandler(new Handler());
            }
            if (timeExpressionDetectiveTextWatcher.getExecutorService() == null){
                timeExpressionDetectiveTextWatcher.setExecutorService(MyApplication.getInstance().getExecutorService());
            }
            return timeExpressionDetectiveTextWatcher;
        }

    }


    public interface OnTimeExpressionChangedHandler {
        void handle(TimeUnit timeUnit);
    }
}
