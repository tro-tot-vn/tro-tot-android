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

    // Mirror the backend AddModeratorRequest constraints (admin.dto.ts)
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String PHONE_REGEX = "^(0|84)(3|5|7|8|9)\\d{8}$";

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
        b.tilFirstName.setError(null);
        b.tilLastName.setError(null);
        b.tilEmail.setError(null);
        b.tilPhone.setError(null);
        b.tilBirthday.setError(null);

        String firstName = text(b.etFirstName);
        String lastName = text(b.etLastName);
        String email = text(b.etEmail);
        String phone = text(b.etPhone);
        String birthday = text(b.etBirthday);
        String gender = b.rbFemale.isChecked() ? "Female" : "Male";

        boolean valid = true;
        if (firstName.isEmpty() || firstName.length() > 30) {
            b.tilFirstName.setError("Họ từ 1–30 ký tự");
            valid = false;
        }
        if (lastName.isEmpty() || lastName.length() > 30) {
            b.tilLastName.setError("Tên từ 1–30 ký tự");
            valid = false;
        }
        if (!email.matches(EMAIL_REGEX)) {
            b.tilEmail.setError("Email không hợp lệ");
            valid = false;
        }
        if (!phone.matches(PHONE_REGEX)) {
            b.tilPhone.setError("SĐT phải dạng 0xxxxxxxxx hoặc 84xxxxxxxxx");
            valid = false;
        }
        if (!isValidBirthday(birthday)) {
            b.tilBirthday.setError("Ngày sinh hợp lệ, dạng yyyy-MM-dd");
            valid = false;
        }
        if (!valid) {
            return null;
        }
        return new AddModeratorRequest(firstName, lastName, email, phone, gender, birthday);
    }

    /** Format yyyy-MM-dd, a real calendar date, not in the future. */
    private static boolean isValidBirthday(String s) {
        if (s == null || !s.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return false;
        }
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
        fmt.setLenient(false);
        try {
            java.util.Date date = fmt.parse(s);
            return date != null && !date.after(new java.util.Date());
        } catch (java.text.ParseException e) {
            return false;
        }
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
