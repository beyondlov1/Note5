package com.beyond.note5.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.beyond.note5.R;
import com.beyond.note5.bean.User;

/**
 * @author: beyond
 * @date: 2019/1/29
 */

public class LoginActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        View loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView usernameView = findViewById(R.id.username);
                TextView passwordView = findViewById(R.id.password);

                User user  = login();
                if (user  == null){
                    return;
                }
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private User login() {

        return null;
    }
}
