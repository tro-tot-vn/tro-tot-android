package com.trototvn.trototandroid.ui.main.viewhistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trototvn.trototandroid.databinding.FragmentViewHistoryBinding;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewHistoryFragment - Shows user's view history
 */
@AndroidEntryPoint
public class ViewHistoryFragment extends Fragment {

    private FragmentViewHistoryBinding binding;
    private ViewHistoryPostAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentViewHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup back button
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Setup RecyclerView
        setupRecyclerView();

        // Load mock data
        loadMockData();
    }

    private void setupRecyclerView() {
        adapter = new ViewHistoryPostAdapter((post, position) -> {
            Timber.d("Save clicked - Post: %s, Saved: %b", post.getTitle(), post.isSaved());
            // TODO: Call API to save/unsave post
        });

        binding.rvViewHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvViewHistory.setAdapter(adapter);
    }

    private void loadMockData() {
        List<PostItem> mockPosts = new ArrayList<>();

        // Mock data with real building image URLs
        mockPosts.add(new PostItem(1, "https://images.unsplash.com/photo-1545324418-cc1a9f4ef577?w=300&h=300&fit=crop", "Phòng trọ cao cấp gần Đại học Bách Khoa",
                5.0, 25, "Nguyễn Văn A", "", "TP. Hồ Chí Minh", false));

        mockPosts.add(new PostItem(2, "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=300&h=300&fit=crop", "Căn hộ 2 phòng ngủ view Bitexco",
                12.0, 65, "Trần Thị B", "", "TP. Hồ Chí Minh", true));

        mockPosts.add(new PostItem(3, "https://images.unsplash.com/photo-1493857671505-72967e2e2760?w=300&h=300&fit=crop", "Chung cư mini full nội thất khu K300",
                4.5, 30, "Lê Văn C", "", "TP. Hồ Chí Minh", false));

        mockPosts.add(new PostItem(4, "https://images.unsplash.com/photo-1552321554-5fefe8c9ef14?w=300&h=300&fit=crop", "Phòng cho thuê gần trạm Bình Thái",
                3.5, 20, "Phạm Thị D", "", "Thành phố Thủ Dầu Một", false));

        mockPosts.add(new PostItem(5, "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=300&h=300&fit=crop", "Nhà nguyên căn 3 tầng Vũng Tàu",
                15.0, 100, "Hoàng Văn E", "", "Thành phố Vũng Tàu", false));

        mockPosts.add(new PostItem(6, "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=300&h=300&fit=crop", "Studio khu vực Landmark 81",
                8.0, 45, "Đỗ Thị F", "", "TP. Hồ Chí Minh", true));

        mockPosts.add(new PostItem(7, "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=300&h=300&fit=crop", "Phòng trọ áng ánh mặt tiền đường Trần Hưng Đạo",
                6.5, 35, "Võ Văn G", "", "TP. Hồ Chí Minh", false));

        mockPosts.add(new PostItem(8, "https://images.unsplash.com/photo-1512917774080-9fff5ee48b60?w=300&h=300&fit=crop", "Căn hộ duplex full tiện nghi Quận 3",
                10.0, 55, "Bùi Thị H", "", "TP. Hồ Chí Minh", false));

        mockPosts.add(new PostItem(9, "https://images.unsplash.com/photo-1516214104703-d870798883c5?w=300&h=300&fit=crop", "Phòng tiện nghi gần ga Bình Thái",
                3.8, 22, "Đinh Văn I", "", "Thành phố Thủ Dầu Một", true));

        mockPosts.add(new PostItem(10, "https://images.unsplash.com/photo-1520932057147-318c26b6a55d?w=300&h=300&fit=crop", "Nhà nguyên căn 4 tầng District 2",
                18.0, 120, "Cương Thị J", "", "TP. Hồ Chí Minh", false));

        mockPosts.add(new PostItem(11, "https://images.unsplash.com/photo-1554224311-beee415c15c7?w=300&h=300&fit=crop", "Phòng cao cấp với view thành phố",
                7.0, 40, "Khánh Văn K", "", "TP. Hồ Chí Minh", false));

        mockPosts.add(new PostItem(12, "https://images.unsplash.com/photo-1494145904049-0dca59b4bbad?w=300&h=300&fit=crop", "Studio gác lửng Quận 1",
                5.5, 28, "Linh Thị L", "", "TP. Hồ Chí Minh", true));

        mockPosts.add(new PostItem(13, "https://images.unsplash.com/photo-1510677676181-0e20a1e03a6d?w=300&h=300&fit=crop", "Căn hộ penthouse view sông Sài Gòn",
                25.0, 150, "Minh Văn M", "", "TP. Hồ Chí Minh", false));

        mockPosts.add(new PostItem(14, "https://images.unsplash.com/photo-1505028106030-e09dffbaef67?w=300&h=300&fit=crop", "Phòng trọ sạch sẽ gần Trường ĐH TDTT",
                4.0, 24, "Nhân Thị N", "", "TP. Hồ Chí Minh", false));

        mockPosts.add(new PostItem(15, "https://images.unsplash.com/photo-1525597099696-06628a4cb8e5?w=300&h=300&fit=crop", "Chung cư cao cấp khu Thảo Điền",
                14.0, 80, "Phúc Văn O", "", "TP. Hồ Chí Minh", true));

        adapter.submitList(mockPosts);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

