package com.beyond.note5.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Attachment;
import com.beyond.note5.bean.Note;
import com.beyond.note5.model.dao.NoteDao;
import com.beyond.note5.utils.BitmapUtil;
import com.beyond.note5.utils.PhotoUtil;
import com.beyond.note5.utils.ViewUtil;

import java.io.File;
import java.util.List;

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
