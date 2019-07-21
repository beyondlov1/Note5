package com.beyond.note5.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.SuggestionSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.model.dao.NoteDao;
import com.beyond.note5.utils.BitmapUtil;
import com.beyond.note5.utils.PhotoUtil;
import com.beyond.note5.utils.ViewUtil;
import com.beyond.note5.view.markdown.render.MarkdownRender;
import com.beyond.note5.view.markdown.render.DefaultMarkdownRender;

import java.io.File;
import java.util.List;

import static android.graphics.Typeface.BOLD_ITALIC;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
//        View test_container = findViewById(R.id.test_container);
//        Drawable drawable = test_container.getBackground();
//        if (drawable instanceof Animatable) {
//            ((Animatable) drawable).start();
//        }

//        NoteMultiDetailStage detailStage = findViewById(R.id.test_detail_stage);
//        List<Note> data = new ArrayList<>();
//        Note note1 = Note.create();
//        Note note2 = Note.create();
//        Note note3 = Note.create();
//        note1.setContent("ab1");
//        note2.setContent("ab2");
//        note3.setContent("ab3");
//        data.add(note1);
//        data.add(note2);
//        data.add(note3);
//        detailStage.setData(data);
//        detailStage.setCurrentIndex(1);
//        detailStage.refresh(LoadType.CONTENT);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 2000);


        NoteDao noteDao = MyApplication.getInstance().getDaoSession().getNoteDao();
        List<Note> list = noteDao.loadAll();
        Attachment attachment = null;
        for (Note note : list) {
            if (!note.getAttachments().isEmpty()){
                attachment = note.getAttachments().get(0);
                if (new File(attachment.getPath()).exists()){
                    break;
                }
            }
        }

        if (attachment!=null){
            ImageView imageView = findViewById(R.id.test_imageView);
            double factor = (double) BitmapUtil.getOptions(attachment.getPath()).outHeight/(double) BitmapUtil.getOptions(attachment.getPath()).outWidth;
//            imageView.setImageBitmap(BitmapUtil.decodeSampledBitmapFromResource(getResources(),R.drawable.ic_link_green_600_24dp,200,200));
            Bitmap bm = BitmapUtil.decodeSampledBitmapFromFile(attachment.getPath(),ViewUtil.getScreenSize().x,
                    (int) (ViewUtil.getScreenSize().x * factor));
            imageView.setImageBitmap(BitmapUtil.scale(bm,(double) ViewUtil.getScreenSize().x/(double) bm.getWidth()));
        }


        testSpanned();
    }

    private void testSpanned() {

        EditText editText = findViewById(R.id.test_editText);
        TextView textView = findViewById(R.id.test_textView);
        Spannable text = new SpannableString("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
        Object span = new SuggestionSpan(
                this,
                new String[] {"one", "two", "three", "four"},
                SuggestionSpan.FLAG_EASY_CORRECT
        );
        text.setSpan(span, 0, 23 + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        editText.setText(text);

        Spannable redText = new SpannableString("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
        redText.setSpan(new ForegroundColorSpan(Color.RED), 12, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  //设置前景色为洋红色
        textView.setText(redText);

        String source = "dfaeognjoad\n# Lorem ipsum dolor sit amet, \n- consectetur adipiscing elit, sed do eiusmod \n- tempor incididunt ut \n1. labore et dolore \n1. magna aliqua.";
        String hash3Key = "# ";
        int start = source.indexOf(hash3Key);
        String lineEndStr = "\n";
        int end = source.indexOf(lineEndStr) - hash3Key.length();
        if (end<start){
            end = source.substring(start+hash3Key.length()).indexOf(lineEndStr)+start;
        }

        Spannable h3Text = new SpannableString(source.replaceFirst(hash3Key,""));
        h3Text.setSpan(new TextAppearanceSpan("monospace",BOLD_ITALIC, 60,  ColorStateList.valueOf(Color.GREEN), ColorStateList.valueOf(Color.RED)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  //设置前景色为洋红色
        textView.setText(h3Text);

        MarkdownRender markdownRender = new DefaultMarkdownRender();
        SpannableStringBuilder renderedText = markdownRender.render(source);
        textView.setText(renderedText);
    }

    private void takePhoto() {
        PhotoUtil.takePhoto(this, TAKE_PHOTO_REQUEST_CODE);
    }

    private static final int TAKE_PHOTO_REQUEST_CODE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO_REQUEST_CODE) {
            Log.d("test", "hahah");
        } else if (resultCode == RESULT_CANCELED && requestCode == TAKE_PHOTO_REQUEST_CODE) {
            boolean delete = PhotoUtil.getLastPhotoFile().delete();
            Log.d(this.getClass().getSimpleName(), "" + delete);
        }

    }
}
