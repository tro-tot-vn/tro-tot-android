package com.trototvn.trototandroid.ui.main.chat;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trototvn.trototandroid.databinding.FragmentChatListBinding;
import com.trototvn.trototandroid.ui.base.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * ChatListFragment - Màn hình danh sách tin nhắn (Inbox).
 */
@AndroidEntryPoint
public class ChatListFragment extends BaseFragment<FragmentChatListBinding>
        implements ConversationAdapter.OnConversationClickListener {

    private ChatListViewModel viewModel;
    private ConversationAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);
    }

    @Override
    protected void setupViews() {
        // Toolbar
        binding.toolbar.setTitle("Tin nhắn");

        // Adapter & RecyclerView
        adapter = new ConversationAdapter(this);
        binding.rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvConversations.setAdapter(adapter);
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

        // Giả định action ID là R.id.action_chatList_to_chatDetail
        // User có thể đổi ID này tùy theo nav_graph.xml của họ
        try {
            Navigation.findNavController(binding.getRoot())
                    .navigate(com.trototvn.trototandroid.R.id.action_list_to_detail, bundle);
        } catch (Exception e) {
            showToast("Navigation action not found. Please check nav_graph.xml");
            // FragmentManager fallback nếu NavController lỗi (tùy dự án)
        }
    }
}
