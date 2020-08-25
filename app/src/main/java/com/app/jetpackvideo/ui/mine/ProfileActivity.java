package com.app.jetpackvideo.ui.mine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.common.UserManager;
import com.app.jetpackvideo.databinding.ActivityProfileBinding;
import com.app.jetpackvideo.model.User;
import com.app.jetpackvideo.utils.StatusBar;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    public static final String TAB_TYPE_ALL = "tab_all";
    public static final String TAB_TYPE_FEED = "tab_feed";
    public static final String TAB_TYPE_COMMENT = "tab_comment";

    public static final String KEY_TAB_TYPE = "key_tab_type";
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    public static void startProfileActivity(Context context, String tabType) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(KEY_TAB_TYPE, tabType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        User user = UserManager.get().getUser();
        binding.setUser(user);
        binding.actionBack.setOnClickListener(v -> finish());

        String[] tabs = getResources().getStringArray(R.array.profile_tabs);
        viewPager = binding.viewPager;
        tabLayout = binding.tabLayout;
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return ProfileFragment.newInstance(getTabTypeByPosition(position));
            }

            private String getTabTypeByPosition(int position) {
                switch (position) {
                    case 0:
                        return TAB_TYPE_ALL;
                    case 1:
                        return TAB_TYPE_FEED;
                    case 2:
                        return TAB_TYPE_COMMENT;
                }
                return TAB_TYPE_ALL;
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, false, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(tabs[position]);
            }
        }).attach();

        int initTabPosition = getInitTabPosition();
        if (initTabPosition != 0) {
            viewPager.post(() -> {
                viewPager.setCurrentItem(initTabPosition, false);
            });
        }

        binding.appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                boolean expand = Math.abs(verticalOffset) < appBarLayout.getTotalScrollRange();
                binding.setExpand(expand);
            }
        });
    }

    private int getInitTabPosition() {
        String initTab = getIntent().getStringExtra(KEY_TAB_TYPE);

        switch (initTab) {
            case TAB_TYPE_ALL:
                return 0;
            case TAB_TYPE_FEED:
                return 1;
            case TAB_TYPE_COMMENT:
                return 2;
            default:
                return 0;
        }
    }
}