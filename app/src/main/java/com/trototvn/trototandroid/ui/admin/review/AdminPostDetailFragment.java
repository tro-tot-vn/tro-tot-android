package com.trototvn.trototandroid.ui.admin.review;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.admin.AdminPost;
import com.trototvn.trototandroid.data.model.admin.ModeratePostRequest;
import com.trototvn.trototandroid.data.model.admin.PostModerationHistoryItem;
import com.trototvn.trototandroid.databinding.FragmentAdminPostDetailBinding;
import com.trototvn.trototandroid.ui.postdetail.PostImageAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Single-post moderation detail. Approve/Reject -> POST api/admin/posts/{id}/moderate.
 * The post is received as a Gson JSON string in args (no get-pending-by-id endpoint);
 * moderation history is fetched by postId.
 */
@AndroidEntryPoint
public class AdminPostDetailFragment extends Fragment {

    private FragmentAdminPostDetailBinding binding;
    private AdminPostDetailViewModel viewModel;
    private AdminHistoryAdapter historyAdapter;

    private int postId;
    private AdminPost post;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminPostDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminPostDetailViewModel.class);

        if (getArguments() != null) {
            postId = getArguments().getInt("postId", 0);
            String json = getArguments().getString("postJson");
            if (json != null) {
                try {
                    post = new Gson().fromJson(json, AdminPost.class);
                } catch (Exception ignored) {
                }
            }
        }

        if (post == null) {
            Toast.makeText(requireContext(), "Không tải được tin đăng", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        bindPost();
        setupGallery();
        setupHistory();
        setupActionPanel();
        observe();

        viewModel.loadHistory(postId);
    }

    private void bindPost() {
        binding.tvTitle.setText(post.getTitle());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        binding.tvPrice.setText(formatter.format(post.getPrice()) + "/tháng");

        String interior = "None".equalsIgnoreCase(post.getInteriorCondition())
                ? "Không nội thất" : "Đầy đủ nội thất";
        binding.tvMeta.setText((int) post.getAcreage() + " m² · " + interior);
        binding.tvAddress.setText(post.getFullAddress());
        binding.tvDescription.setText(post.getDescription());

        if (post.getOwner() != null) {
            binding.tvOwnerName.setText("Chủ tin: " + post.getOwner().getFullName());
            if (post.getOwner().getAccount() != null) {
                binding.tvOwnerPhone.setText("SĐT: " + post.getOwner().getAccount().getPhone());
                binding.tvOwnerEmail.setText("Email: " + post.getOwner().getAccount().getEmail());
            }
        }
    }

    private void setupGallery() {
        List<String> imageUrls = new ArrayList<>();
        if (post.getMultimediaFiles() != null) {
            for (AdminPost.MultimediaFile f : post.getMultimediaFiles()) {
                if (!f.isVideo()) {
                    imageUrls.add(f.getUrl());
                }
            }
        }
        if (imageUrls.isEmpty()) {
            binding.rvImages.setVisibility(View.GONE);
            return;
        }
        PostImageAdapter adapter = new PostImageAdapter();
        binding.rvImages.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvImages.setAdapter(adapter);
        adapter.setImages(imageUrls);
    }

    private void setupHistory() {
        historyAdapter = new AdminHistoryAdapter();
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistory.setAdapter(historyAdapter);
    }

    private void setupActionPanel() {
        binding.toggleAction.check(R.id.btnApprove);
        binding.cbHate.setVisibility(View.GONE);

        binding.toggleAction.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            boolean reject = checkedId == R.id.btnReject;
            binding.cbHate.setVisibility(reject ? View.VISIBLE : View.GONE);
            if (!reject) {
                binding.tilReason.setError(null);
            }
        });

        binding.btnSubmit.setOnClickListener(v -> submit());
    }

    private void submit() {
        boolean reject = binding.toggleAction.getCheckedButtonId() == R.id.btnReject;
        String reason = binding.etReason.getText() != null
                ? binding.etReason.getText().toString().trim() : "";

        if (reject && reason.isEmpty()) {
            binding.tilReason.setError(getString(R.string.admin_reason_required));
            return;
        }
        binding.tilReason.setError(null);

        ModeratePostRequest request = reject
                ? ModeratePostRequest.reject(reason, binding.cbHate.isChecked())
                : ModeratePostRequest.approve(reason);

        viewModel.moderate(postId, request);
    }

    private void observe() {
        viewModel.getHistoryLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.getStatus() != Resource.Status.SUCCESS) return;
            List<PostModerationHistoryItem> items = resource.getData();
            if (items == null || items.isEmpty()) {
                binding.tvHistoryEmpty.setVisibility(View.VISIBLE);
                historyAdapter.submitList(new ArrayList<>());
            } else {
                binding.tvHistoryEmpty.setVisibility(View.GONE);
                historyAdapter.submitList(items);
            }
        });

        viewModel.getModerateLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.btnSubmit.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    boolean reject = binding.toggleAction.getCheckedButtonId() == R.id.btnReject;
                    Toast.makeText(requireContext(),
                            getString(reject ? R.string.admin_moderate_success_rejected
                                    : R.string.admin_moderate_success_approved),
                            Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigateUp();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSubmit.setEnabled(true);
                    Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
