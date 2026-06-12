package com.trototvn.trototandroid.ui.admin.moderators;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.admin.AddModeratorRequest;
import com.trototvn.trototandroid.data.model.admin.Moderator;
import com.trototvn.trototandroid.data.model.admin.ModeratorActionHistoryItem;
import com.trototvn.trototandroid.databinding.DialogAddModeratorBinding;
import com.trototvn.trototandroid.databinding.FragmentAdminModeratorsBinding;
import com.trototvn.trototandroid.ui.dialog.ConfirmDialog;
import com.trototvn.trototandroid.utils.SessionManager;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Manager-only moderator management: list/search, create, status toggle, password reset,
 * profile and action-history. Backend: api/admin/moderators*.
 */
@AndroidEntryPoint
public class AdminModeratorsFragment extends Fragment
        implements ModeratorAdapter.OnModeratorActionListener {

    @Inject
    SessionManager sessionManager;

    private FragmentAdminModeratorsBinding binding;
    private AdminModeratorsViewModel viewModel;
    private ModeratorAdapter adapter;
    private String currentKey = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminModeratorsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Defense in depth: Manager-only screen (also hidden from nav + enforced server-side)
        if (!sessionManager.isManager()) {
            Toast.makeText(requireContext(), "Chỉ quản lý mới truy cập được", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        viewModel = new ViewModelProvider(this).get(AdminModeratorsViewModel.class);

        adapter = new ModeratorAdapter(this);
        binding.rvModerators.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvModerators.setAdapter(adapter);

        binding.btnAdd.setOnClickListener(v -> showAddDialog());
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentKey = v.getText() != null ? v.getText().toString().trim() : "";
                viewModel.loadModerators(currentKey);
                return true;
            }
            return false;
        });

        observe();
        viewModel.loadModerators("");
    }

    private void observe() {
        viewModel.getModeratorsLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.tvEmpty.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    List<Moderator> list = resource.getData();
                    if (list == null || list.isEmpty()) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                        adapter.submitList(new java.util.ArrayList<>());
                    } else {
                        binding.tvEmpty.setVisibility(View.GONE);
                        adapter.submitList(list);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getActionLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(),
                        resource.getData() != null && !resource.getData().isEmpty()
                                ? resource.getData() : getString(R.string.admin_status_updated),
                        Toast.LENGTH_SHORT).show();
                viewModel.loadModerators(currentKey);
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getProfileLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.getStatus() != Resource.Status.SUCCESS) return;
            showProfileDialog(resource.getData());
        });

        viewModel.getHistoryLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.getStatus() != Resource.Status.SUCCESS) return;
            showHistoryDialog(resource.getData());
        });
    }

    // ===== Row actions =====

    @Override
    public void onProfile(Moderator moderator) {
        viewModel.loadProfile(moderator.getAdminId());
    }

    @Override
    public void onHistory(Moderator moderator) {
        viewModel.loadHistory(moderator.getAdminId());
    }

    @Override
    public void onResetPassword(Moderator moderator) {
        new ConfirmDialog(requireContext(),
                getString(R.string.admin_nav_moderators),
                getString(R.string.admin_reset_password_confirm),
                new ConfirmDialog.OnConfirmListener() {
                    @Override
                    public void onConfirm() {
                        viewModel.resetPassword(moderator.getAdminId());
                    }

                    @Override
                    public void onCancel() {
                    }
                }).show();
    }

    @Override
    public void onToggleStatus(Moderator moderator) {
        viewModel.toggleStatus(moderator);
    }

    // ===== Dialogs =====

    private void showAddDialog() {
        DialogAddModeratorBinding dialogBinding = DialogAddModeratorBinding.inflate(getLayoutInflater());
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_moderators_add)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.admin_confirm, null)
                .setNegativeButton(R.string.admin_cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    AddModeratorRequest req = validateAddForm(dialogBinding);
                    if (req != null) {
                        viewModel.createModerator(req);
                        dialog.dismiss();
                    }
                }));
        dialog.show();
    }

    @Nullable
    private AddModeratorRequest validateAddForm(DialogAddModeratorBinding b) {
        String firstName = text(b.etFirstName);
        String lastName = text(b.etLastName);
        String email = text(b.etEmail);
        String phone = text(b.etPhone);
        String birthday = text(b.etBirthday);
        String gender = b.rbFemale.isChecked() ? "Female" : "Male";

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
                || phone.isEmpty() || birthday.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (!phone.matches("^(0|84)(3|5|7|8|9)\\d{8}$")) {
            Toast.makeText(requireContext(), "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (!birthday.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            Toast.makeText(requireContext(), "Ngày sinh phải có dạng yyyy-MM-dd", Toast.LENGTH_SHORT).show();
            return null;
        }
        return new AddModeratorRequest(firstName, lastName, email, phone, gender, birthday);
    }

    private void showProfileDialog(Moderator m) {
        if (m == null || binding == null) return;
        String content = "Họ tên: " + m.getFullName()
                + "\nGiới tính: " + ("Male".equalsIgnoreCase(m.getGender()) ? "Nam" : "Nữ")
                + "\nNgày sinh: " + safe(m.getBirthday())
                + "\nNgày tham gia: " + safe(m.getJoinedAt())
                + "\nSĐT: " + (m.getAccount() != null ? safe(m.getAccount().getPhone()) : "")
                + "\nEmail: " + (m.getAccount() != null ? safe(m.getAccount().getEmail()) : "")
                + "\nTrạng thái: " + (m.isActive() ? "Hoạt động" : "Tạm khóa");
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(m.getFullName())
                .setMessage(content)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showHistoryDialog(List<ModeratorActionHistoryItem> items) {
        if (binding == null) return;
        StringBuilder sb = new StringBuilder();
        if (items == null || items.isEmpty()) {
            sb.append(getString(R.string.admin_history_empty));
        } else {
            for (ModeratorActionHistoryItem it : items) {
                String action = "Approved".equalsIgnoreCase(it.getActionType()) ? "Đã duyệt" : "Đã từ chối";
                sb.append("• ").append(safe(it.getPostTitle())).append(" — ").append(action).append('\n');
            }
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_history_title)
                .setMessage(sb.toString().trim())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private static String text(com.google.android.material.textfield.TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
