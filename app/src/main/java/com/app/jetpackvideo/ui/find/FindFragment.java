package com.app.jetpackvideo.ui.find;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.model.SofaTab;
import com.app.jetpackvideo.ui.sofa.SofaFragment;
import com.app.jetpackvideo.utils.AppConfig;
import com.app.lib_nav_annotation.FragmentDestination;

@FragmentDestination(pageUrl = "main/tab/find")
public class FindFragment extends SofaFragment {

    @Override
    protected Fragment getTabFragment(int position) {
        SofaTab.Tabs tab = getTabConfig().tabs.get(position);
        TagListFragment fragment = TagListFragment.newInstance(tab.tag);
        return fragment;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        String tagType = childFragment.getArguments().getString(TagListFragment.KEY_TAG_TYPE);
        ;
        if (TextUtils.equals(tagType, "onlyFollow")) {
            new ViewModelProvider(childFragment).get(TagListViewModel.class)
                    .getSwitchTabLiveData().observe(this, new Observer() {
                @Override
                public void onChanged(Object o) {
                    viewPager2.setCurrentItem(1);
                }
            });
        }
    }

    @Override
    protected SofaTab getTabConfig() {
        return AppConfig.getFindTabConfig();
    }
}