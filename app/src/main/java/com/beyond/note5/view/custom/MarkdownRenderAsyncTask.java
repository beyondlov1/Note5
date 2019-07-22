package com.beyond.note5.view.custom;

import android.os.AsyncTask;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

import com.beyond.note5.view.markdown.render.MarkdownRenders;

import java.lang.ref.WeakReference;

/**
 * @author: beyond
 * @date: 2019/7/22
 */

public class MarkdownRenderAsyncTask extends AsyncTask<Object, Void, SpannableStringBuilder> {

    private WeakReference<TextView> textViewWeakReference;

    private int key;

    public MarkdownRenderAsyncTask(TextView textView) {
        this.textViewWeakReference = new WeakReference<>(textView);
    }

    @Override
    protected SpannableStringBuilder doInBackground(Object... objects) {
        return MarkdownRenders.render((String) objects[0], (int) objects[1]);
    }

    @Override
    protected void onPostExecute(SpannableStringBuilder spannableStringBuilder) {
        TextView textView = textViewWeakReference.get();
        if (textView== null){
            return;
        }
        if (textView.getText().toString().hashCode() == key) {
            textView.setText(spannableStringBuilder);
        }
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}