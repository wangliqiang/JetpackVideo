package com.app.jetpackvideo.ui.mine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.common.UserManager;
import com.app.jetpackvideo.databinding.FragmentMineBinding;
import com.app.jetpackvideo.model.User;
import com.app.lib_nav_annotation.FragmentDestination;
import com.jaeger.library.StatusBarUtil;

@FragmentDestination(pageUrl = "main/tab/mine")
public class MineFragment extends Fragment {

    private FragmentMineBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        User user = UserManager.get().getUser();
        binding.setUser(user);

        UserManager.get().refresh().observe(getViewLifecycleOwner(), newUser -> {
            if (newUser != null) {
                binding.setUser(newUser);
            }
        });

        binding.actionLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.fragment_my_logout))
                    .setPositiveButton(getString(R.string.fragment_my_logout_ok), (dialog, which) -> {
                        dialog.dismiss();
                        UserManager.get().logout();
                        MineFragment.this.getActivity().onBackPressed();
                    })
                    .setNegativeButton(getString(R.string.fragment_my_logout_cancel), null)
                    .create().show();
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }
}