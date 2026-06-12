package com.trototvn.trototandroid.ui.admin.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.admin.Moderator;
import com.trototvn.trototandroid.databinding.DialogChangePasswordBinding;
import com.trototvn.trototandroid.databinding.FragmentAdminProfileBinding;
import com.trototvn.trototandroid.ui.admin.AdminActivity;

import java.util.HashMap;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Admin self-profile: view/edit (diff-only PUT api/admin/me/profile), change password, logout.
 */
@AndroidEntryPoint
public class AdminProfileFragment extends Fragment {

    private FragmentAdminProfileBinding binding;
    private AdminProfileViewModel viewModel;
    private Moderator current;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminProfileViewModel.class);

        binding.btnSave.setOnClickListener(v -> save());
        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        binding.btnLogout.setOnClickListener(v -> {
            if (getActivity() instanceof AdminActivity) {
                ((AdminActivity) getActivity()).logout();
            }
        });

        observe();
        viewModel.loadProfile();
    }

    private void observe() {
        viewModel.getProfileLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    current = resource.getData();
                    bindProfile(current);
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getUpdateLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), R.string.admin_profile_updated, Toast.LENGTH_SHORT).show();
                viewModel.loadProfile();
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getPasswordLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindProfile(Moderator m) {
        if (m == null) return;
        binding.etFirstName.setText(m.getFirstName());
        binding.etLastName.setText(m.getLastName());
        binding.etBirthday.setText(m.getBirthday());
        if (m.getAccount() != null) {
            binding.etEmail.setText(m.getAccount().getEmail());
            binding.etPhone.setText(m.getAccount().getPhone());
        }
    }

    private void save() {
        if (current == null) return;
        Map<String, Object> diff = new HashMap<>();
        putIfChanged(diff, "firstName", text(binding.etFirstName), current.getFirstName());
        putIfChanged(diff, "lastName", text(binding.etLastName), current.getLastName());
        putIfChanged(diff, "birthday", text(binding.etBirthday), current.getBirthday());
        if (current.getAccount() != null) {
            putIfChanged(diff, "email", text(binding.etEmail), current.getAccount().getEmail());
            putIfChanged(diff, "phone", text(binding.etPhone), current.getAccount().getPhone());
        } else {
            diff.put("email", text(binding.etEmail));
            diff.put("phone", text(binding.etPhone));
        }

        if (diff.isEmpty()) {
            Toast.makeText(requireContext(), "Không có thay đổi", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.updateProfile(diff);
    }

    private void showChangePasswordDialog() {
        DialogChangePasswordBinding b = DialogChangePasswordBinding.inflate(getLayoutInflater());
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_profile_change_password)
                .setView(b.getRoot())
                .setPositiveButton(R.string.admin_confirm, null)
                .setNegativeButton(R.string.admin_cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String oldPw = text(b.etOldPassword);
                    String newPw = text(b.etNewPassword);
                    if (oldPw.isEmpty() || newPw.isEmpty()) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPw.length() < 8) {
                        Toast.makeText(requireContext(), "Mật khẩu mới tối thiểu 8 ký tự", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.changePassword(oldPw, newPw);
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private static void putIfChanged(Map<String, Object> map, String key, String newVal, String oldVal) {
        String safeNew = newVal != null ? newVal : "";
        String safeOld = oldVal != null ? oldVal : "";
        if (!safeNew.equals(safeOld)) {
            map.put(key, safeNew);
        }
    }

    private static String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
