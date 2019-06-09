package com.beyond.note5.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.User;
import com.beyond.note5.event.SyncNoteListEvent;
import com.beyond.note5.utils.PreferenceUtil;
import com.beyond.note5.utils.ToastUtil;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author: beyond
 * @date: 2019/1/29
 */

public abstract class LoginActivity extends AppCompatActivity {

    public static final String DAV_LOGIN_REMEMBER_USERNAME = "dav.login.remember.username";
    public static final String DAV_LOGIN_USERNAME = "dav.login.username";
    public static final String DAV_LOGIN_REMEMBER_PASSWORD = "dav.login.remember.password";
    public static final String DAV_LOGIN_PASSWORD = "dav.login.password";
    public static final String DAV_LOGIN = "dav.login";

    private Handler handler = new Handler();

    @BindView(R.id.username)
    EditText username;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.login_remember_username)
    CheckBox loginRememberUsername;
    @BindView(R.id.login_remember_password)
    CheckBox loginRememberPassword;
    @BindView(R.id.loginButton)
    Button loginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.login_remember_username)
    public void onLoginRememberUsernameClicked() {
    }

    @OnClick(R.id.login_remember_password)
    public void onLoginRememberPasswordClicked() {
        if (loginRememberPassword.isChecked()) {
            loginRememberUsername.setChecked(true);
        }
    }

    public abstract User login(String username, String password);

    @OnClick(R.id.loginButton)
    public void onLoginButtonClicked() {

        if (username.getText() == null || password.getText() == null) {
            return;
        }
        MyApplication.getInstance().getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                User user = login(username.getText().toString(), password.getText().toString());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (user == null) {
                            PreferenceUtil.put(DAV_LOGIN_REMEMBER_USERNAME,false);
                            PreferenceUtil.put(DAV_LOGIN_REMEMBER_PASSWORD,false);
                            ToastUtil.toast(LoginActivity.this, "用户名密码错误");
                            return;
                        }

                        if (StringUtils.isNotBlank(username.getText().toString())
                                && loginRememberUsername.isChecked()) {
                            PreferenceUtil.put(DAV_LOGIN_REMEMBER_USERNAME, true);
                            PreferenceUtil.put(DAV_LOGIN_USERNAME, user.getUsername());
                        }

                        if (StringUtils.isNotBlank(password.getText().toString())
                                && loginRememberPassword.isChecked()) {
                            PreferenceUtil.put(DAV_LOGIN_REMEMBER_PASSWORD, true);
                            PreferenceUtil.put(DAV_LOGIN_PASSWORD, user.getPassword());
                        }

                        PreferenceUtil.put(DAV_LOGIN, true);

                        EventBus.getDefault().post(new SyncNoteListEvent(null));

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }
}
