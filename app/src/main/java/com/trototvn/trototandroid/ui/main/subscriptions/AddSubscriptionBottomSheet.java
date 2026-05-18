package com.trototvn.trototandroid.ui.main.subscriptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.trototvn.trototandroid.data.model.location.City;
import com.trototvn.trototandroid.data.model.location.District;
import com.trototvn.trototandroid.databinding.DialogAddSubscriptionBinding;
import com.trototvn.trototandroid.utils.LocationService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddSubscriptionBottomSheet extends BottomSheetDialogFragment {

    public interface OnSaveListener {
        void onSave(String city, String district);
    }

    private DialogAddSubscriptionBinding binding;
    private final OnSaveListener listener;

    @Inject
    LocationService locationService;

    private List<City> cities;
    private City selectedCity;
    private District selectedDistrict;

    public AddSubscriptionBottomSheet(OnSaveListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddSubscriptionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSpinners();

        binding.btnSave.setOnClickListener(v -> {
            if (selectedCity == null || selectedDistrict == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn Tỉnh/Thành phố và Quận/Huyện", Toast.LENGTH_SHORT).show();
                return;
            }
            if (listener != null) {
                listener.onSave(selectedCity.getName(), selectedDistrict.getName());
            }
            dismiss();
        });
    }

    private void setupSpinners() {
        cities = locationService.getAllCities();
        if (cities == null) cities = new ArrayList<>();

        List<String> cityNames = new ArrayList<>();
        for (City city : cities) {
            cityNames.add(city.getName());
        }

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, cityNames);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCity.setAdapter(cityAdapter);

        binding.spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCity = cities.get(position);
                updateDistrictSpinner(selectedCity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCity = null;
            }
        });
    }

    private void updateDistrictSpinner(City city) {
        if (city == null || city.getDistricts() == null) return;

        List<String> districtNames = new ArrayList<>();
        for (District district : city.getDistricts()) {
            districtNames.add(district.getName());
        }

        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, districtNames);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDistrict.setAdapter(districtAdapter);

        binding.spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDistrict = city.getDistricts().get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDistrict = null;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

