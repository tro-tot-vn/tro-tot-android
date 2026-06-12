package com.trototvn.trototandroid.ui.admin.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.trototvn.trototandroid.databinding.FragmentAdminReportsBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Reports placeholder (Phase 5). UI-only MOCK — there is NO backend for reports yet.
 * Data is hardcoded sample data (see {@link ReportItem#mockData()}); actions are non-functional.
 */
@AndroidEntryPoint
public class AdminReportsFragment extends Fragment {

    private FragmentAdminReportsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ReportAdapter adapter = new ReportAdapter(this::showDetail);
        binding.rvReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvReports.setAdapter(adapter);
        adapter.setItems(ReportItem.mockData());
    }

    private void showDetail(ReportItem item) {
        String content = "Loại: " + item.category
                + "\nĐối tượng: " + item.subject
                + "\nLý do: " + item.reportType
                + "\nNgười báo cáo: " + item.reportedBy
                + "\nNgày: " + item.date
                + "\n\n(Dữ liệu demo — chức năng xử lý chưa khả dụng)";
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chi tiết báo cáo #" + item.id)
                .setMessage(content)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
