package com.beyond.note5.view.fragment;

import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html;
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
import com.beyond.note5.predict.AbstractTagCallback;
import com.beyond.note5.predict.bean.Tag;
import com.beyond.note5.utils.IDUtil;
import com.beyond.note5.utils.InputMethodUtil;
import com.beyond.note5.utils.TimeNLPUtil;
import com.beyond.note5.view.custom.SelectionListenableEditText;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;
import com.zhy.view.flowlayout.TagView;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.*;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

public class TodoModifySuperFragment extends TodoEditSuperFragment {

    private ImageButton clearButton;
    private ImageButton saveButton;
    private TagFlowLayout flowLayout;
    private TagAdapter<String> tagAdapter;

    private List<String> tagData = new ArrayList<>();

    private Handler handler = new Handler();

    //回显
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(FillTodoModifyEvent fillTodoModifyEvent) {
        Todo todo = fillTodoModifyEvent.get();
        createdDocument = ObjectUtils.clone(todo);
        String html = highlightTimeExpression(createdDocument.getContent());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (html != null) {
                contentEditText.setText(Html.fromHtml(html, FROM_HTML_MODE_COMPACT));
            }else {
                contentEditText.setText(createdDocument.getContent());
            }
        }else {
            contentEditText.setText(createdDocument.getContent());
        }
        contentEditText.setSelection(createdDocument.getContent().length());
    }

    private String highlightTimeExpression(String source) {
        String timeExpression = StringUtils.trim(TimeNLPUtil.getOriginTimeExpression(StringUtils.trim(source)));
        if (StringUtils.isNotBlank(timeExpression)) {
            return source.replace(timeExpression, "<span style='" +
                    "background:lightgray;'>" +
                    timeExpression + "</span>");
        }
        return null;
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
                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                tv.setBackground(getResources().getDrawable(R.drawable.radius_24dp_blue, null));
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
                if (StringUtils.isBlank(content)) {
                    EventBus.getDefault().post(new DeleteTodoEvent(createdDocument));
                } else {
                    createdDocument.setTitle(content.length() > 10 ? content.substring(0, 10) : content);
                    createdDocument.setContent(content);
                    createdDocument.setLastModifyTime(new Date());
                    createdDocument.setVersion(createdDocument.getVersion() == null ? 0 : createdDocument.getVersion() + 1);
                    Reminder reminder = createdDocument.getReminder();
                    Date reminderStart = TimeNLPUtil.parse(content);
                    if (reminderStart != null) {
                        if (reminder != null) {
                            reminder.setStart(reminderStart);
                        } else {
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

        if (contentEditText instanceof SelectionListenableEditText){
            ((SelectionListenableEditText) contentEditText).setOnSelectionChanged(new SelectionListenableEditText.OnSelectionChangeListener() {

                @Override
                public void onChanged(String content, int selStart, int selEnd) {
                    if (content.length()>=selStart){
                        predictTags(content.substring(0,selStart));
                    }
                }
            });
        }

        contentEditText.addTextChangedListener(new TextWatcher() {

            private String lastStr = null;
            private int lastSelectionEnd;
            private int timeExpressionStartIndex;
            private int timeExpressionEndIndex;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, final int start, int before, int count) {
                String source = s.toString();
//                predictTags(source);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (StringUtils.equals(lastStr, source)) {
                        lastStr = null;
                        contentEditText.setSelection(lastSelectionEnd);
                        return;
                    }
                    if (start < timeExpressionStartIndex
                            || start > timeExpressionEndIndex) {
                        lastStr = null;
                        return;
                    }
                    if (before > 0) {
                        lastStr = null;
                        return;
                    }
                    String html = highlightTimeExpression(source);
                    if (html == null) {
                        lastStr = null;
                        return;
                    }
                    lastStr = source;
                    lastSelectionEnd = contentEditText.getSelectionEnd();
                    contentEditText.setText(Html.fromHtml(html, FROM_HTML_MODE_COMPACT));
                }
            }

            private String highlightTimeExpression(String source) {
                String timeExpression = StringUtils.trim(TimeNLPUtil.getOriginTimeExpression(StringUtils.trim(source)));
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
        });

        flowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                if (view instanceof TagView) {
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

    private void predictTags(String source) {
        MyApplication.getInstance().getTagPredictor().predict(source, new AbstractTagCallback() {

            @Override
            protected void handleResult(List<Tag> tags) {
                tagData.clear();
                Collections.sort(tags, new Comparator<Tag>() {
                    @Override
                    public int compare(Tag o1, Tag o2) {
                        return -o1.getScore() + o2.getScore();
                    }
                });
                if (tags.size() >= 5) {
                    tags = tags.subList(0, 5);
                }
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

        });
    }
}
