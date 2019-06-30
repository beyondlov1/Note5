package com.beyond.note5.view;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.beyond.note5.R;
import com.beyond.note5.bean.Account;
import com.beyond.note5.model.AccountModelImpl;
import com.beyond.note5.presenter.AccountPresenter;
import com.beyond.note5.presenter.AccountPresenterImpl;
import com.beyond.note5.utils.OkWebDavUtil;
import com.beyond.note5.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

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

    private AccountPresenter accountPresenter;

    @BindView(R.id.login_server)
    EditText server;
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
    @BindView(R.id.login_account_list)
    RecyclerView accountRecyclerView;

    private MyRecyclerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        accountPresenter = new AccountPresenterImpl(new AccountModelImpl(),new MyAccountView());

        adapter = new MyRecyclerAdapter();
        accountRecyclerView.setAdapter(adapter);
        //设置显示格式
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        accountRecyclerView.setLayoutManager(staggeredGridLayoutManager);

        accountPresenter.findAll();
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

    public abstract Account login(String server, String username, String password);

    @OnClick(R.id.loginButton)
    public void onLoginButtonClicked() {

        if (username.getText() == null || password.getText() == null) {
            return;
        }
        Account account = new Account();
        account.setServer(OkWebDavUtil.concat(server.getText().toString(),""));
        account.setUsername(username.getText().toString());
        account.setPassword(password.getText().toString());
        accountPresenter.login(account);
    }

    private class MyAccountView implements AccountView {
        @Override
        public void onLoginSuccess(Account account) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    accountPresenter.findAll();
//                    EventBus.getDefault().post(new SyncNoteListEvent(null));
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    startActivity(intent);
//                    finish();
                }
            });

        }

        @Override
        public void onLoginFail(Exception e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.toast(LoginActivity.this, "用户名密码错误");
                }
            });
        }

        @Override
        public void onFindAllSuccess(List<Account> all) {
            adapter.setData(all);
        }

        @Override
        public void onFindAllFail(Exception e) {

        }
    }

    private class MyRecyclerAdapter extends RecyclerView.Adapter {

        private List<Account> data = new ArrayList<>();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.item_account, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            myViewHolder.username.setText(data.get(position).getUsername());
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        private class MyViewHolder extends RecyclerView.ViewHolder{

            TextView username;

            public MyViewHolder(View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.item_account_username);
            }
        }

        public void setData(List<Account> data){
            notifyItemRangeRemoved(0,this.data.size());
            this.data.clear();
            this.data.addAll(data);
            notifyItemRangeInserted(0,this.data.size());
        }

    }
}
