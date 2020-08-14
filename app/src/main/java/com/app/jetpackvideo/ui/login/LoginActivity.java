package com.app.jetpackvideo.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.common.UserManager;
import com.app.jetpackvideo.model.User;
import com.app.lib_network.ApiResponse;
import com.app.lib_network.ApiService;
import com.app.lib_network.JSONCallback;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private View actionClose;
    private View actionLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        actionClose = findViewById(R.id.action_close);
        actionLogin = findViewById(R.id.action_login);

        actionClose.setOnClickListener(this);
        actionLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_close) {
            finish();
        } else if (v.getId() == R.id.action_login) {
            login();
        }
    }

    private void login() {
        ApiService.get("/user/insert")
                .addParam("name", "、蓅哖╰伊人为谁笑")
                .addParam("avatar", "http://qzapp.qlogo.cn/qzapp/101794421/FE41683AD4ECF91B7736CA9DB8104A5C/100")
                .addParam("qqOpenId", "FE41683AD4ECF91B7736CA9DB8104A5C")
                .addParam("expires_time", System.currentTimeMillis() + 1000 * 3600 * 24 + 7)
                .execute(new JSONCallback<User>() {
                    @Override
                    public void onSuccess(ApiResponse<User> response) {
                        if (response.body != null) {
                            UserManager.get().save(response.body);
                            finish();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "登陆失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(ApiResponse<User> response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "登陆失败,msg:" + response.message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }
}