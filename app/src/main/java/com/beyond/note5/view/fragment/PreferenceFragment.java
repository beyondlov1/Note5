package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.v7.preference.Preference;
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

        findPreference(MyApplication.SYNC_STRATEGY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                    MyApplication.getInstance().refreshSynchronizers(newValue.toString());
                    return true;
            }
        });

    }
}
