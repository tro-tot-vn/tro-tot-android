package com.trototvn.trototandroid.ui.admin.reports;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trototvn.trototandroid.databinding.ItemAdminReportBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the MOCK reports list. Static demo data only.
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    public interface OnReportClickListener {
        void onClick(ReportItem item);
    }

    private final List<ReportItem> items = new ArrayList<>();
    private final OnReportClickListener listener;

    public ReportAdapter(OnReportClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ReportItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminReportBinding binding = ItemAdminReportBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminReportBinding binding;
        private final OnReportClickListener listener;

        ViewHolder(ItemAdminReportBinding binding, OnReportClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(ReportItem item) {
            binding.tvCategory.setText(item.category);
            binding.tvSubject.setText(item.subject);
            binding.tvMeta.setText(item.reportType + " · " + item.reportedBy + " · " + item.date);

            String statusLabel;
            String color;
            switch (item.status) {
                case "resolved":
                    statusLabel = "Đã xử lý";
                    color = "#17BF63";
                    break;
                case "rejected":
                    statusLabel = "Đã bỏ qua";
                    color = "#657786";
                    break;
                default:
                    statusLabel = "Chờ xử lý";
                    color = "#F5A623";
                    break;
            }
            binding.tvStatus.setText(statusLabel);
            binding.tvStatus.setTextColor(Color.parseColor(color));

            binding.getRoot().setOnClickListener(v -> listener.onClick(item));
        }
    }
}
