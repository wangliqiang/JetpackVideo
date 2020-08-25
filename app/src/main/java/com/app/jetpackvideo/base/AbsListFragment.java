package com.app.jetpackvideo.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.databinding.LayoutRefreshViewBinding;
import com.app.jetpackvideo.widget.EmptyView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbsListFragment<T, VM extends AbsViewModel<T>> extends Fragment implements OnRefreshListener, OnLoadMoreListener {

    protected LayoutRefreshViewBinding binding;
    protected RecyclerView recyclerView;
    protected SmartRefreshLayout refreshLayout;
    protected EmptyView emptyView;

    protected PagedListAdapter<T, RecyclerView.ViewHolder> adapter;

    protected VM viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutRefreshViewBinding.inflate(inflater, container, false);
        recyclerView = binding.recyclerView;
        refreshLayout = binding.refreshLayout;
        emptyView = binding.emptyView;

        refreshLayout.setEnableRefresh(true);
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setOnLoadMoreListener(this);

        adapter = getAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(null);

        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_divider));
        recyclerView.addItemDecoration(decoration);

        genericViewModel();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] arguments = type.getActualTypeArguments();
        if (arguments.length > 1) {
            Type argument = arguments[1];
            Class modelClass = ((Class) argument).asSubclass(AbsViewModel.class);
            viewModel = (VM) new ViewModelProvider(this).get(modelClass);
            viewModel.getPageData().observe(getViewLifecycleOwner(), pagedList -> submitList(pagedList));
            viewModel.getBoundaryPageData().observe(getViewLifecycleOwner(), hasData -> finishRefresh(hasData));
        }
    }

    private void genericViewModel() {
        //利用 子类传递的 泛型参数实例化出absViewModel 对象。
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] arguments = type.getActualTypeArguments();
        if (arguments.length > 1) {
            Type argument = arguments[1];
            Class modelClaz = ((Class) argument).asSubclass(AbsViewModel.class);
            viewModel = (VM) new ViewModelProvider(this).get(modelClaz);

            //触发页面初始化数据加载的逻辑
            viewModel.getPageData().observe(getViewLifecycleOwner(), pagedList -> submitList(pagedList));

            //监听分页时有无更多数据,以决定是否关闭上拉加载的动画
            viewModel.getBoundaryPageData().observe(getViewLifecycleOwner(), hasData -> finishRefresh(hasData));
        }
    }

    public void submitList(PagedList<T> pagedList) {
        if (pagedList.size() > 0) {
            adapter.submitList(pagedList);
        }
        finishRefresh(pagedList.size() > 0);
    }

    public void finishRefresh(boolean hasData) {
        PagedList<T> currentList = adapter.getCurrentList();
        hasData = hasData || currentList != null && currentList.size() > 0;
        RefreshState state = refreshLayout.getState();
        if (state.isFooter && state.isOpening) {
            refreshLayout.finishLoadMore();
        } else if (state.isHeader && state.isOpening) {
            refreshLayout.finishRefresh();
        }
        if (hasData) {
            emptyView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    public abstract PagedListAdapter getAdapter();
}
