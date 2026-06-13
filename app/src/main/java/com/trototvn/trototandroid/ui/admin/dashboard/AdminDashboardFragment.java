package com.trototvn.trototandroid.ui.admin.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.admin.DashboardStats;
import com.trototvn.trototandroid.databinding.FragmentAdminDashboardBinding;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Admin dashboard: pending / approved-this-week / rejected-this-week stat cards + bar chart.
 * Backend: GET api/admin/dashboard-stats.
 */
@AndroidEntryPoint
public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;
    private AdminDashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);

        setupChart();
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadStats());

        viewModel.getStatsLiveData().observe(getViewLifecycleOwner(), this::render);
        viewModel.loadStats();
    }

    private void render(Resource<DashboardStats> resource) {
        if (resource == null) return;
        switch (resource.getStatus()) {
            case LOADING:
                if (!binding.swipeRefresh.isRefreshing()) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                }
                break;
            case SUCCESS:
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                bindStats(resource.getData());
                break;
            case ERROR:
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void bindStats(DashboardStats stats) {
        if (stats == null) return;
        binding.tvPendingCount.setText(String.valueOf(stats.getTotalPendingPost()));
        binding.tvApprovedCount.setText(String.valueOf(stats.getTotalApprovedPostInWeek()));
        binding.tvRejectedCount.setText(String.valueOf(stats.getTotalRejectedPostInWeek()));
        updateChart(stats.getTotalApprovedPostInWeek(), stats.getTotalRejectedPostInWeek());
    }

    private void setupChart() {
        BarChart chart = binding.barChart;
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);

        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setGranularity(1f);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setDrawGridLines(false);
        x.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Đã duyệt", "Từ chối"}));
    }

    private void updateChart(int approved, int rejected) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, approved));
        entries.add(new BarEntry(1f, rejected));

        BarDataSet set = new BarDataSet(entries, "");
        set.setColors(Color.parseColor("#17BF63"), Color.parseColor("#FF3B30"));
        set.setValueTextSize(12f);
        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        BarData data = new BarData(set);
        data.setBarWidth(0.5f);

        binding.barChart.setData(data);
        binding.barChart.setFitBars(true);
        binding.barChart.animateY(600);
        binding.barChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
