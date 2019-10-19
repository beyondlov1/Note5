package com.beyond.note5.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.beyond.note5.R;
import com.beyond.note5.view.component.EditorListenerAdapter;
import com.beyond.note5.view.fragment.NoteModifyFragment;

public class NoteModifyEditorActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_container);
        NoteModifyFragment fragment = new NoteModifyFragment();
        fragment.setEditorListener(new EditorListenerAdapter(){
            @Override
            public void afterSave(CharSequence cs) {
                super.afterSave(cs);
                fragment.getActivity().finish();
            }
        });
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.activity_note_edit_fragment_container, fragment);
        fragmentTransaction.commit();
    }

}
