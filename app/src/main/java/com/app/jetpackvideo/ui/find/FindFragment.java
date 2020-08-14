package com.app.jetpackvideo.ui.find;

import android.os.Bundle;
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
import com.app.lib_nav_annotation.FragmentDestination;

@FragmentDestination(pageUrl = "main/tab/find")
public class FindFragment extends Fragment {
    private static final String TAG = "FindFragment";
    private FindViewModel findViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        findViewModel =
                new ViewModelProvider(this).get(FindViewModel.class);
        View root = inflater.inflate(R.layout.fragment_find, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        findViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        Log.d(TAG, "onCreateView");
        return root;
    }
}