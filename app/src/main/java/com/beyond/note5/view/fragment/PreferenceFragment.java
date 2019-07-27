package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.ViewGroup;
import android.widget.Button;

import com.beyond.note5.R;

/**
 * @author: beyond
 * @date: 2019/7/14
 */

public class PreferenceFragment extends PreferenceFragmentCompat {

    private ViewGroup rootView;

    private Button configAccountButton;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//        getPreferenceManager().setSharedPreferencesName(MyApplication.SHARED_PREFERENCES_NAME);
        addPreferencesFromResource(R.xml.app_preferences);
    }

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_preference_page, container, false);
//        initView();
//        initEvent();
//        return rootView;
//    }
//
//    private void initEvent() {
//        configAccountButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getContext(), DavLoginActivity.class);
//                startActivity(intent);
//            }
//        });
//    }
//
//    private void initView() {
//        configAccountButton = rootView.findViewById(R.id.config_account_button);
//    }
}
