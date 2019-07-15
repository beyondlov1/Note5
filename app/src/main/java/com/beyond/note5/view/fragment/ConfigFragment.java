package com.beyond.note5.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.beyond.note5.R;
import com.beyond.note5.view.DavLoginActivity;

/**
 * @author: beyond
 * @date: 2019/7/14
 */

public class ConfigFragment extends Fragment {

    private ViewGroup rootView;

    private Button configAccountButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_config_list, container, false);
        initView();
        initEvent();
        return rootView;
    }

    private void initEvent() {
        configAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DavLoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        configAccountButton = rootView.findViewById(R.id.config_account_button);
    }
}
