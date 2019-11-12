package com.beyond.note5.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Account;
import com.beyond.note5.bean.AccountWrapper;
import com.beyond.note5.component.DaggerLoginActivityComponent;
import com.beyond.note5.component.module.AccountModule;
import com.beyond.note5.presenter.AccountPresenter;
import com.beyond.note5.utils.OkWebDavUtil;
import com.beyond.note5.utils.StatusBarUtil;
import com.beyond.note5.utils.ToastUtil;
import com.beyond.note5.view.adapter.view.AccountViewAdapter;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author: beyond
 * @date: 2019/1/29
 */

public abstract class LoginActivity extends AppCompatActivity {

    public static final String DAV_LOGIN_USERNAME = "dav.login.username";
    public static final String DAV_LOGIN_PASSWORD = "dav.login.password";

    @Inject
    Handler handler;

    @Inject
    AccountPresenter accountPresenter;

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
        StatusBarUtil.showLightWhiteStatusBar(this);
        initInject();
        initView();
        initEvent();
    }

    protected void initInject(){
        DaggerLoginActivityComponent.builder()
                .accountModule(new AccountModule(new MyAccountView()))
                .build().inject(this);
    }

    protected void initEvent(){
        server.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    showListPopupWindow();
                }
            }
        });
    }


    private void showListPopupWindow() {
        final String[] showArray = {"坚果云", "TeraCloud"};//要填充的数据
        final String[] dataArray = {"https://dav.jianguoyun.com/dav/", "https://yura.teracloud.jp/dav/"};//要填充的数据
        final ListPopupWindow listPopupWindow;
        listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAdapter(new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1,
                showArray) {
        });//用android内置布局，或设计自己的样式
        listPopupWindow.setAnchorView(server);//以哪个控件为基准，在该处以mEditText为基准
        listPopupWindow.setModal(false);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {//设置项点击监听
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                server.setText(dataArray[i]);//把选择的选项内容展示在EditText上
                listPopupWindow.dismiss();//如果已经选择了，隐藏起来
            }
        });
        listPopupWindow.show();
    }

    protected void initView(){
        ButterKnife.bind(this);

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

    private class MyAccountView extends AccountViewAdapter {
        @Override
        public void onLoginSuccess(Account account) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    accountPresenter.findAll();
                    refreshSynchronizers();
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
        public void onDeleteSuccess(Account account) {
            accountPresenter.findAll();
        }
    }

    private void refreshSynchronizers() {
        MyApplication.getInstance().refreshSynchronizers();
    }

    private class MyRecyclerAdapter extends RecyclerView.Adapter {

        private List<AccountWrapper> data = new ArrayList<>();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.item_account, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            myViewHolder.server.setText(data.get(position).getServer());
            myViewHolder.username.setText(data.get(position).getUsername());
            String noteLastSyncTime = DateFormatUtils.format(data.get(position).getNoteLastSyncTime(), "yyyy-MM-dd HH:mm:ss");
            String todoLastSyncTime = DateFormatUtils.format(data.get(position).getTodoLastSyncTime(), "yyyy-MM-dd HH:mm:ss");
            myViewHolder.noteLastSyncTime.setText(String.format("NoteLastSyncTime:%s", noteLastSyncTime));
            myViewHolder.todoLastSyncTime.setText(String.format("TodoLastSyncTime:%s", todoLastSyncTime));
            if (data.get(position).getValid()){
                myViewHolder.enable.setChecked(true);
            }else {
                myViewHolder.enable.setChecked(false);
            }
            myViewHolder.enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        AccountWrapper account = data.get(position);
                        account.setValid(true);
                        accountPresenter.update(account.getAccount());
                    }else {
                        AccountWrapper account = data.get(position);
                        account.setValid(false);
                        accountPresenter.update(account.getAccount());
                    }
                    refreshSynchronizers();
                }
            });
            myViewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    accountPresenter.delete(data.get(position).getAccount());
                    refreshSynchronizers();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        private class MyViewHolder extends RecyclerView.ViewHolder{

            CheckBox enable;
            TextView server;
            TextView username;
            TextView noteLastSyncTime;
            TextView todoLastSyncTime;
            ImageButton delete;

            MyViewHolder(View itemView) {
                super(itemView);
                server = itemView.findViewById(R.id.item_account_server);
                username = itemView.findViewById(R.id.item_account_username);
                noteLastSyncTime = itemView.findViewById(R.id.item_account_last_sync_time_note);
                todoLastSyncTime = itemView.findViewById(R.id.item_account_last_sync_time_todo);
                enable = itemView.findViewById(R.id.item_account_enable);
                delete = itemView.findViewById(R.id.item_account_delete);
            }
        }

        public void setData(List<Account> data){
            notifyItemRangeRemoved(0,this.data.size());
            this.data.clear();
            for (Account datum : data) {
                this.data.add(accountPresenter.wrap(datum));
            }
            notifyItemRangeInserted(0,this.data.size());
        }

    }
}
