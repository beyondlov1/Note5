package com.beyond.note5.view.fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Reminder;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.DeleteTodoEvent;
import com.beyond.note5.event.FillTodoModifyEvent;
import com.beyond.note5.event.HideTodoEditEvent;
import com.beyond.note5.event.UpdateTodoEvent;
import com.beyond.note5.predict.bean.Callback;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.utils.TimeNLPUtil;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;
import com.zhy.view.flowlayout.TagView;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TodoModifySuperFragment extends TodoEditSuperFragment {

    private ImageButton clearButton;
    private ImageButton saveButton;
    private TagFlowLayout flowLayout;
    private TagAdapter<String> tagAdapter;

    private List<String> tagData = new ArrayList<>();

    private Handler handler = new Handler();

    //回显
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onEventMainThread(FillTodoModifyEvent fillTodoModifyEvent){
        Todo todo = fillTodoModifyEvent.get();
        createdDocument = ObjectUtils.clone(todo);
        contentEditText.setText(createdDocument.getContent());
        contentEditText.setSelection(createdDocument.getContent().length());
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        ViewStub toolsContainer = view.findViewById(R.id.fragment_edit_todo_view_stub);
        toolsContainer.inflate();
        clearButton = view.findViewById(R.id.fragment_edit_todo_clear);
        saveButton = view.findViewById(R.id.fragment_edit_todo_save);

        ViewStub tagsContainer = view.findViewById(R.id.fragment_edit_todo_tag_view_stub);
        tagsContainer.inflate();
        flowLayout = view.findViewById(R.id.fragment_edit_todo_tags);
        tagAdapter = new TagAdapter<String>(tagData) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                TextView tv = new TextView(getContext());
                tv.setText(s);
                return tv;
            }
        };
        flowLayout.setAdapter(tagAdapter);
    }

    @Override
    protected void initEvent(View view) {
        super.initEvent(view);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditText.setText(null);
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = contentEditText.getText().toString();
                if (StringUtils.isBlank(content)){
                    EventBus.getDefault().post(new DeleteTodoEvent(createdDocument));
                }else {
                    createdDocument.setTitle(content.length() > 10 ? content.substring(1, 10) : content);
                    createdDocument.setContent(content);
                    createdDocument.setLastModifyTime(new Date());
                    createdDocument.setVersion(createdDocument.getVersion() == null?0:createdDocument.getVersion()+1);
                    Reminder reminder = createdDocument.getReminder();
                    Date reminderStart = TimeNLPUtil.parse(content);
                    if (reminderStart !=null){
                        if (reminder!=null){
                            reminder.setStart(reminderStart);
                        }else{
                            reminder = new Reminder();
                            reminder.setId(IDUtil.uuid());
                            reminder.setStart(reminderStart);
                            createdDocument.setReminderId(reminder.getId());
                        }
                        createdDocument.setReminder(reminder);
                    }
                    EventBus.getDefault().post(new UpdateTodoEvent(createdDocument));
                }
                EventBus.getDefault().post(new HideTodoEditEvent(null));
                InputMethodUtil.hideKeyboard(contentEditText);
            }
        });
        contentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, final int start, int before, int count) {
                MyApplication.getInstance().getTagPredictor().predict(s.toString(), new Callback<List<Tag>>() {
                    @Override
                    public void onSuccess(List<Tag> tags) {
                        tagData.clear();
                        for (Tag tag : tags) {
                            tagData.add(tag.getContent());
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                tagAdapter.notifyDataChanged();
                            }
                        });
                    }

                    @Override
                    public void onFail() {

                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        flowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                if (view instanceof TagView){
                    TagView tagView = (TagView) view;
                    View childView = tagView.getTagView();
                    if (childView instanceof TextView) {
                        TextView textView = (TextView) childView;
                        CharSequence text = textView.getText();
                        appendToEditText(contentEditText, text);
                    }
                }
                return true;
            }

            private void appendToEditText(EditText editText, CharSequence source) {
                int start = editText.getSelectionStart();
                int end = editText.getSelectionEnd();
                Editable edit = editText.getEditableText();//获取EditText的文字
                if (start < 0 || start >= edit.length()) {
                    edit.append(source);
                } else {
                    edit.replace(start, end, source);//光标所在位置插入文字
                }
            }
        });
    }
}
