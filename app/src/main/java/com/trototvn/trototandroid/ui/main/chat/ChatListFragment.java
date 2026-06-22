package com.trototvn.trototandroid.ui.main.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trototvn.trototandroid.databinding.FragmentChatListBinding;
import com.trototvn.trototandroid.ui.base.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * ChatListFragment - Màn hình danh sách tin nhắn (Inbox).
 */
@AndroidEntryPoint
public class ChatListFragment extends BaseFragment<FragmentChatListBinding>
        implements ConversationAdapter.OnConversationClickListener {

    @javax.inject.Inject
    com.trototvn.trototandroid.utils.SessionManager sessionManager;

    private ChatListViewModel viewModel;
    private ConversationAdapter adapter;

    private final android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);
    }

    @Override
    protected void setupViews() {
        // Toolbar
        binding.toolbar.setTitle("Tin nhắn");

        long myUserId = 0;
        try {
            if (sessionManager.getUserId() != null) {
                myUserId = Long.parseLong(sessionManager.getUserId());
            }
        } catch (NumberFormatException ignored) {
        }

        // Adapter & RecyclerView
        adapter = new ConversationAdapter(myUserId, this);
        binding.rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvConversations.setAdapter(adapter);

        // Scroll listener for pagination (load more)
        binding.rvConversations.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!viewModel.isLoading() && !viewModel.isLastPage()) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0) {
                            viewModel.loadMoreConversations();
                        }
                    }
                }
            }
        });

        // Search text watcher with 400ms debounce
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> viewModel.search(s.toString());
                searchHandler.postDelayed(searchRunnable, 400);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Pull to refresh
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.fetchConversationsFromApi(() -> {
                binding.swipeRefreshLayout.setRefreshing(false);
            });
        });
    }

    @Override
    protected void observeData() {
        viewModel.getConversationsLiveData().observe(getViewLifecycleOwner(), conversations -> {
            if (conversations != null) {
                adapter.submitList(conversations);
            }
        });
    }

    @Override
    public void onConversationClick(long conversationId, String partnerName) {
        // Chuyển sang màn hình Chat Detail
        Bundle bundle = new Bundle();
        bundle.putLong("conversationId", conversationId);
        bundle.putString("partnerName", partnerName);

        try {
            Navigation.findNavController(binding.getRoot())
                    .navigate(com.trototvn.trototandroid.R.id.action_list_to_detail, bundle);
        } catch (Exception e) {
            showToast("Navigation action not found. Please check nav_graph.xml");
        }
    }

    @Override
    public void onDestroyView() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        super.onDestroyView();
    }
}
