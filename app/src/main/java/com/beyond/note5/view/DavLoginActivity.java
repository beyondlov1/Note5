package com.beyond.note5.view;

import com.beyond.note5.bean.User;
import com.beyond.note5.utils.OkWebDavUtil;
import com.beyond.note5.utils.PreferenceUtil;

import static com.beyond.note5.MyApplication.SYNC_REMOTE_URL;

public class DavLoginActivity extends LoginActivity {
    @Override
    public User login(String username, String password) {
        if (OkWebDavUtil.isAvailable(PreferenceUtil.getString(SYNC_REMOTE_URL),username,password)) {
            return new User(username, password);
        }
        return null;
    }
}
