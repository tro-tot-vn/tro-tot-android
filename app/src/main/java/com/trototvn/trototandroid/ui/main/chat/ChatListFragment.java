package com.trototvn.trototandroid.ui.main.chat;

import android.os.Bundle;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trototvn.trototandroid.data.local.entity.ConversationEntity;
import com.trototvn.trototandroid.databinding.FragmentChatListBinding;
import com.trototvn.trototandroid.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * ChatListFragment - Màn hình danh sách tin nhắn (Inbox).
 */
@AndroidEntryPoint
public class ChatListFragment extends BaseFragment<FragmentChatListBinding>
        implements ConversationAdapter.OnConversationClickListener {

    private ConversationAdapter adapter;

    @Override
    protected void setupViews() {
        // Toolbar
        binding.toolbar.setTitle("Tin nhắn");

        // Adapter & RecyclerView
        adapter = new ConversationAdapter(this);
        binding.rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvConversations.setAdapter(adapter);

        // Mock data to test UI
        setupMockData();
    }

    @Override
    protected void observeData() {
        // Logic observe từ ViewModel sẽ được viết sau khi nối Repository
    }

    private void setupMockData() {
        List<ConversationEntity> mockList = new ArrayList<>();

        mockList.add(new ConversationEntity(
                1001L,
                "Nguyễn Văn A",
                "Chào bạn, món hàng này còn không?",
                2,
                new Date(),
                new Date()));

        mockList.add(new ConversationEntity(
                1002L,
                "Trần Thị B",
                "Cảm ơn bạn nhiều nhé!",
                0,
                new Date(),
                new Date()));

        mockList.add(new ConversationEntity(
                1003L,
                "Lê Văn C",
                "Bạn có thể bớt giá chút được không?",
                1,
                new Date(),
                new Date()));

        adapter.submitList(mockList);
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
