package com.app.jetpackvideo.ui.sofa;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.app.jetpackvideo.databinding.FragmentSofaBinding;
import com.app.jetpackvideo.model.SofaTab;
import com.app.jetpackvideo.ui.home.HomeFragment;
import com.app.jetpackvideo.utils.AppConfig;
import com.app.lib_nav_annotation.FragmentDestination;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FragmentDestination(pageUrl = "main/tab/sofa")
public class SofaFragment extends Fragment {
    private static final String TAG = "SofaFragment";

    private FragmentSofaBinding binding;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private SofaTab tabConfig;
    private List<SofaTab.Tabs> tabs;

    private TabLayoutMediator mediator;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSofaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewPager2 = binding.viewPager;
        tabLayout = binding.tabLayout;

        tabConfig = getTabConfig();

        tabs = new ArrayList<>();

        for (SofaTab.Tabs tab : tabConfig.tabs) {
            if (tab.enable) {
                tabs.add(tab);
            }
        }

        viewPager2.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        viewPager2.setAdapter(new FragmentStateAdapter(getChildFragmentManager(), this.getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return getTabFragment(position);
            }

            @Override
            public int getItemCount() {
                return tabs.size();
            }
        });

        tabLayout.setTabGravity(tabConfig.tabGravity);

        mediator = new TabLayoutMediator(tabLayout, viewPager2, false, (tab, position) -> {
            tab.setCustomView(makeTabView(position));
        });
        mediator.attach();
        viewPager2.registerOnPageChangeCallback(onPageChangeCallback);

        viewPager2.post(() -> viewPager2.setCurrentItem(tabConfig.select));
    }

    ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            int tabCount = tabLayout.getTabCount();
            for (int i = 0; i < tabCount; i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                TextView customView = (TextView) tab.getCustomView();
                if (tab.getPosition() == position) {
                    customView.setTextSize(tabConfig.activeSize);
                    customView.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    customView.setTextSize(tabConfig.normalSize);
                    customView.setTypeface(Typeface.DEFAULT);
                }
            }
        }
    };

    private View makeTabView(int position) {
        TextView tabView = new TextView(getContext());
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};
        int[] colors = new int[]{Color.parseColor(tabConfig.activeColor), Color.parseColor(tabConfig.normalColor)};

        ColorStateList stateList = new ColorStateList(states, colors);
        tabView.setTextColor(stateList);
        tabView.setText(tabs.get(position).title);
        tabView.setTextSize(tabConfig.normalSize);
        return tabView;
    }

    private Fragment getTabFragment(int position) {
        return HomeFragment.newInstance(tabs.get(position).tag);
    }

    private SofaTab getTabConfig() {
        return AppConfig.getSofatabConfig();
    }


    @Override
    public void onDestroy() {
        mediator.detach();
        viewPager2.unregisterOnPageChangeCallback(onPageChangeCallback);
        super.onDestroy();
    }
}