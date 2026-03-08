package com.trototvn.trototandroid.ui.main.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.trototvn.trototandroid.databinding.DialogSearchFilterBinding;

import java.util.Arrays;
import java.util.List;

/**
 * Bottom sheet dialog for search filters
 */
public class SearchFilterBottomDialog extends BottomSheetDialogFragment {

    private DialogSearchFilterBinding binding;
    private SearchViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = DialogSearchFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Share ViewModel with parent Fragment
        if (getParentFragment() != null) {
            viewModel = new ViewModelProvider(getParentFragment()).get(SearchViewModel.class);
        }

        setupCitySpinner();
        loadCurrentFilters();
        setupClickListeners();
    }

    private void setupCitySpinner() {
        List<String> cities = Arrays.asList("Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Cần Thơ", "Hải Phòng");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line,
                cities);
        binding.spinnerCity.setAdapter(adapter);
    }

    private void loadCurrentFilters() {
        if (viewModel == null)
            return;

        // City
        if (viewModel.getSelectedCity().getValue() != null) {
            binding.spinnerCity.setText(viewModel.getSelectedCity().getValue(), false);
        }

        // Price
        if (viewModel.getPriceMin().getValue() != null) {
            binding.etPriceMin.setText(String.valueOf(viewModel.getPriceMin().getValue()));
        }
        if (viewModel.getPriceMax().getValue() != null) {
            binding.etPriceMax.setText(String.valueOf(viewModel.getPriceMax().getValue()));
        }

        // Acreage
        if (viewModel.getAcreageMin().getValue() != null) {
            binding.etAcreageMin.setText(String.valueOf(viewModel.getAcreageMin().getValue()));
        }
        if (viewModel.getAcreageMax().getValue() != null) {
            binding.etAcreageMax.setText(String.valueOf(viewModel.getAcreageMax().getValue()));
        }

        // Interior (simplified logic)
        // TODO: Map string value from ViewModel to specific chip
    }

    private void setupClickListeners() {
        binding.btnApply.setOnClickListener(v -> {
            applyFilters();
            dismiss();
        });

        binding.btnReset.setOnClickListener(v -> {
            if (viewModel != null) {
                viewModel.clearFilters();
            }
            dismiss();
        });
    }

    private void applyFilters() {
        if (viewModel == null)
            return;

        // City
        String city = binding.spinnerCity.getText().toString();
        viewModel.setCity(city.isEmpty() ? null : city);

        // Price
        String minPriceStr = binding.etPriceMin.getText().toString();
        String maxPriceStr = binding.etPriceMax.getText().toString();
        Integer minPrice = minPriceStr.isEmpty() ? null : Integer.parseInt(minPriceStr);
        Integer maxPrice = maxPriceStr.isEmpty() ? null : Integer.parseInt(maxPriceStr);
        viewModel.setPriceRange(minPrice, maxPrice);

        // Acreage
        String minAcreageStr = binding.etAcreageMin.getText().toString();
        String maxAcreageStr = binding.etAcreageMax.getText().toString();
        Integer minAcreage = minAcreageStr.isEmpty() ? null : Integer.parseInt(minAcreageStr);
        Integer maxAcreage = maxAcreageStr.isEmpty() ? null : Integer.parseInt(maxAcreageStr);
        viewModel.setAcreageRange(minAcreage, maxAcreage);

        // Interior
        int checkedId = binding.chipGroupInterior.getCheckedChipId();
        if (checkedId == binding.chipNoInterior.getId()) {
            viewModel.setInteriorCondition("Nội thất cơ bản");
        } else if (checkedId == binding.chipFullInterior.getId()) {
            viewModel.setInteriorCondition("Đầy đủ nội thất");
        } else {
            viewModel.setInteriorCondition(null);
        }

        // Trigger search
        viewModel.search();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
