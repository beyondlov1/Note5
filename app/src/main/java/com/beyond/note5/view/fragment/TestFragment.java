package com.beyond.note5.view.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.note5.R;

public class TestFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_detail, container, false);
        View operationContainer = view.findViewById(R.id.fragment_note_detail_operation_container);
        operationContainer.setBackgroundColor(Color.RED);
        return view;
    }
}
