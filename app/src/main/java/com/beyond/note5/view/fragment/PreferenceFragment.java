package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;

/**
 * @author: beyond
 * @date: 2019/7/14
 */

public class PreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(MyApplication.SHARED_PREFERENCES_NAME);
        addPreferencesFromResource(R.xml.app_preferences);
    }

}
