package com.app.jetpackvideo.ui.find;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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