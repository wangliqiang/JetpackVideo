package com.app.jetpackvideo.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.common.UserManager;
import com.app.jetpackvideo.model.Destination;
import com.app.jetpackvideo.model.User;
import com.app.jetpackvideo.utils.AppConfig;
import com.app.jetpackvideo.utils.NavGraphBuilder;
import com.app.jetpackvideo.utils.StatusBar;
import com.app.jetpackvideo.widget.AppBottomBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jaeger.library.StatusBarUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private NavController navController;
    private AppBottomBar navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = NavHostFragment.findNavController(fragment);
        NavGraphBuilder.builder(navController, this, fragment.getId());

        navView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        HashMap<String, Destination> destConfig = AppConfig.getDestConfig();
        Iterator<Map.Entry<String, Destination>> iterator = destConfig.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Destination> entry = iterator.next();
            Destination value = entry.getValue();
            if (value != null && !UserManager.get().isLogin() && value.needLogin && value.id == item.getItemId()) {
                UserManager.get().login(this).observe(this, new Observer<User>() {
                    @Override
                    public void onChanged(User user) {
                        navView.setSelectedItemId(item.getItemId());
                    }
                });
                return false;
            }
        }

        navController.navigate(item.getItemId());
        return !TextUtils.isEmpty(item.getTitle());
    }


    @Override
    public void onBackPressed() {
        //当前正在显示的页面destinationId
        int currentPageId = navController.getCurrentDestination().getId();

        //APP页面路导航结构图  首页的destinationId
        int homeDestId = navController.getGraph().getStartDestination();

        //如果当前正在显示的页面不是首页，而我们点击了返回键，则拦截。
        if (currentPageId != homeDestId) {
            navView.setSelectedItemId(homeDestId);
            return;
        }

        //否则 finish，此处不宜调用onBackPressed。因为navigation会操作回退栈,切换到之前显示的页面。
        finish();
    }
}