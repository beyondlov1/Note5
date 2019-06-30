package com.beyond.note5.view;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Account;
import com.beyond.note5.utils.OkWebDavUtil;
import com.beyond.note5.utils.PreferenceUtil;

public class DavLoginActivity extends LoginActivity {
    @Override
    public Account login(String server, String username, String password) {
        if (OkWebDavUtil.isAvailable(server,OkWebDavUtil.concat(server,PreferenceUtil.getString(MyApplication.NOTE_LOCK_PATH)),username,password)) {
            return new Account(username, password);
        }
        return null;
    }
}
