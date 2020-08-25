package com.app.jetpackvideo.ui.publish;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.common.UserManager;
import com.app.jetpackvideo.model.TagList;
import com.app.jetpackvideo.utils.PixUtils;
import com.app.lib_network.ApiResponse;
import com.app.lib_network.ApiService;
import com.app.lib_network.JSONCallback;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class TagBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private TagsAdapter tagsAdapter;
    private List<TagList> mTagList = new ArrayList<>();
    private OnTagItemSelectedListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_tag_bottom_sheet_dialog, null, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tagsAdapter = new TagsAdapter();
        recyclerView.setAdapter(tagsAdapter);

        dialog.setContentView(view);
        ViewGroup parent = (ViewGroup) view.getParent();
        BottomSheetBehavior<ViewGroup> behavior = BottomSheetBehavior.from(parent);
        behavior.setPeekHeight(PixUtils.getScreenHeight() / 3);
        behavior.setHideable(false);

        ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
        layoutParams.height = PixUtils.getScreenHeight() / 3 * 2;
        parent.setLayoutParams(layoutParams);

        queryTagList();

        return dialog;
    }

    @SuppressLint("RestrictedApi")
    private void queryTagList() {
        ApiService.get("/tag/queryTagList")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("pageCount", 100)
                .addParam("tagId", 0)
                .execute(new JSONCallback<List<TagList>>() {
                    @Override
                    public void onSuccess(ApiResponse<List<TagList>> response) {
                        if (response.body != null) {
                            List<TagList> list = response.body;
                            mTagList.addAll(list);
                            ArchTaskExecutor.getMainThreadExecutor().execute(() -> tagsAdapter.notifyDataSetChanged());
                        }
                    }

                    @Override
                    public void onError(ApiResponse<List<TagList>> response) {
                        ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
                            Toast.makeText(getContext(), response.message, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }


    class TagsAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setTextSize(13);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.color_000));
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setLayoutParams(new RecyclerView.LayoutParams(-1, PixUtils.dp2px(45)));

            return new RecyclerView.ViewHolder(textView) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TextView textView = (TextView) holder.itemView;
            TagList tagList = mTagList.get(position);
            textView.setText(tagList.title);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTagItemSelected(tagList);
                    dismiss();
                }
            });


        }

        @Override
        public int getItemCount() {
            return mTagList.size();
        }
    }

    public void setOnTagItemSelectedListener(OnTagItemSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnTagItemSelectedListener {
        void onTagItemSelected(TagList tagList);
    }
}
