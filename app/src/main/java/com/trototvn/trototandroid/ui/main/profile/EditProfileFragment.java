package com.trototvn.trototandroid.ui.main.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.trototvn.trototandroid.databinding.FragmentEditProfileBinding;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.profile.CustomerProfile;
import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import android.widget.AdapterView;
import javax.inject.Inject;
import com.trototvn.trototandroid.utils.LocationService;
import com.trototvn.trototandroid.data.model.location.City;
import com.trototvn.trototandroid.data.model.location.District;

@AndroidEntryPoint
public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private ProfileViewModel viewModel;
    private final Calendar selectedDate = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
    @Inject
    LocationService locationService;
    private List<City> cities;
    private City selectedCity;
    private District selectedDistrict;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Setup back button
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Setup gender spinner
        setupGenderSpinner();

        // Setup date picker
        setupDatePicker();

        // Setup location spinners
        setupLocationSpinners();

        // Load current profile data
        loadCurrentProfile();

        // Handle save button click
        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setupGenderSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Nam", "Nữ", "Khác"}
        );
        binding.spinnerGender.setAdapter(adapter);
    }

    private void setupDatePicker() {
        binding.etDateOfBirth.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chọn ngày sinh");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(16, 16, 16, 16);

        NumberPicker dayPicker = new NumberPicker(requireContext());
        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(31);
        dayPicker.setValue(selectedDate.get(Calendar.DAY_OF_MONTH));
        dayPicker.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        NumberPicker monthPicker = new NumberPicker(requireContext());
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(selectedDate.get(Calendar.MONTH) + 1);
        monthPicker.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        NumberPicker yearPicker = new NumberPicker(requireContext());
        yearPicker.setMinValue(1950);
        yearPicker.setMaxValue(Calendar.getInstance().get(Calendar.YEAR));
        yearPicker.setValue(selectedDate.get(Calendar.YEAR));
        yearPicker.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        layout.addView(dayPicker);
        layout.addView(monthPicker);
        layout.addView(yearPicker);

        builder.setView(layout);
        builder.setPositiveButton("OK", (dialog, which) -> {
            int day = dayPicker.getValue();
            int month = monthPicker.getValue() - 1; // Calendar.MONTH starts from 0
            int year = yearPicker.getValue();
            selectedDate.set(year, month, day);
            binding.etDateOfBirth.setText(dateFormat.format(selectedDate.getTime()));
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void setupLocationSpinners() {
        cities = locationService.getAllCities();
        if (cities == null) cities = new ArrayList<>();

        List<String> cityNames = new ArrayList<>();
        for (City city : cities) {
            cityNames.add(city.getName());
        }

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, cityNames);
        binding.spinnerCity.setAdapter(cityAdapter);

        binding.spinnerCity.setOnItemClickListener((parent, view, position, id) -> {
            selectedCity = cities.get(position);
            updateDistrictSpinner(selectedCity);
            binding.spinnerDistrict.setText("", false); // Reset district when city changes
            selectedDistrict = null;
        });
        
        binding.spinnerDistrict.setOnItemClickListener((parent, view, position, id) -> {
            if (selectedCity != null && selectedCity.getDistricts() != null) {
                selectedDistrict = selectedCity.getDistricts().get(position);
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
                android.R.layout.simple_dropdown_item_1line, districtNames);
        binding.spinnerDistrict.setAdapter(districtAdapter);
    }

    private void loadCurrentProfile() {
        viewModel.getProfile().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                populateProfileData(resource.getData());
                Timber.d("Profile loaded successfully");
            } else if (resource != null && resource.getStatus() == Resource.Status.LOADING) {
                Timber.d("Profile loading...");
            }
        });

        if (viewModel.getProfile().getValue() == null ||
                viewModel.getProfile().getValue().getStatus() != Resource.Status.SUCCESS) {
            viewModel.loadProfile();
        }
    }

    private void populateProfileData(CustomerProfile profile) {
        if (profile == null) {
            Timber.e("Profile data is null!");
            return;
        }

        Timber.d("Populating profile data: firstName=%s, lastName=%s, email=%s, gender=%s, birthday=%s",
                profile.getFirstName(), profile.getLastName(), profile.getEmail(), profile.getGender(), profile.getBirthday());

        if (profile.getFirstName() != null && !profile.getFirstName().isEmpty()) {
            binding.etFirstName.setText(profile.getFirstName());
        }

        if (profile.getLastName() != null && !profile.getLastName().isEmpty()) {
            binding.etLastName.setText(profile.getLastName());
        }

        // Email is stored in nested account object
        if (profile.getEmail() != null && !profile.getEmail().isEmpty()) {
            binding.etEmail.setText(profile.getEmail());
        }

        if (profile.getBio() != null && !profile.getBio().isEmpty()) {
            binding.etBio.setText(profile.getBio());
        }

        // Map from API values to Vietnamese labels
        if (profile.getGender() != null && !profile.getGender().isEmpty()) {
            String genderLabel = mapGenderToLabel(profile.getGender());
            binding.spinnerGender.setText(genderLabel, false);
        }

        if (profile.getBirthday() != null) {
            selectedDate.setTime(profile.getBirthday());
            binding.etDateOfBirth.setText(dateFormat.format(profile.getBirthday()));
        }
        
        if (profile.getCurrentCity() != null && !profile.getCurrentCity().isEmpty()) {
            binding.spinnerCity.setText(profile.getCurrentCity(), false);
            // Pre-select city and populate district adapter
            if (cities != null) {
                for (City city : cities) {
                    if (city.getName().equalsIgnoreCase(profile.getCurrentCity())) {
                        selectedCity = city;
                        updateDistrictSpinner(selectedCity);
                        break;
                    }
                }
            }
        }
        
        if (profile.getCurrentDistrict() != null && !profile.getCurrentDistrict().isEmpty()) {
            binding.spinnerDistrict.setText(profile.getCurrentDistrict(), false);
            if (selectedCity != null && selectedCity.getDistricts() != null) {
                for (District dist : selectedCity.getDistricts()) {
                    if (dist.getName().equalsIgnoreCase(profile.getCurrentDistrict())) {
                        selectedDistrict = dist;
                        break;
                    }
                }
            }
        }
    }

    private String mapGenderToLabel(String gender) {
        if (gender == null || gender.isEmpty()) {
            return "";
        }

        // Map from API values to Vietnamese labels
        switch (gender.toLowerCase()) {
            case "male":
                return "Nam";
            case "female":
                return "Nữ";
            case "other":
            case "khác":
                return "Khác";
            default:
                // If it's already a Vietnamese label, return as is
                return gender;
        }
    }

    private void saveProfile() {
        String firstName = binding.etFirstName.getText() != null ? binding.etFirstName.getText().toString().trim() : "";
        String lastName = binding.etLastName.getText() != null ? binding.etLastName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String gender = binding.spinnerGender.getText() != null ? binding.spinnerGender.getText().toString().trim() : "";
        String birthDate = binding.etDateOfBirth.getText() != null ? binding.etDateOfBirth.getText().toString().trim() : "";
        String bio = binding.etBio.getText() != null ? binding.etBio.getText().toString().trim() : "";
        String currentCityStr = binding.spinnerCity.getText() != null ? binding.spinnerCity.getText().toString().trim() : "";
        String currentDistrictStr = binding.spinnerDistrict.getText() != null ? binding.spinnerDistrict.getText().toString().trim() : "";

        if (firstName.isEmpty()) {
            binding.etFirstName.setError("Vui lòng nhập tên");
            return;
        }
        
        if (lastName.isEmpty()) {
            binding.etLastName.setError("Vui lòng nhập họ");
            return;
        }

        if (email.isEmpty()) {
            binding.etEmail.setError("Vui lòng nhập email");
            return;
        }

        if (gender.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            return;
        }

        CustomerProfile profileData = new CustomerProfile();
        profileData.setFirstName(firstName);
        profileData.setLastName(lastName);
        profileData.setGender(mapLabelToGender(gender));
        profileData.setBio(bio);
        profileData.setCurrentCity(currentCityStr);
        profileData.setCurrentDistrict(currentDistrictStr);

        // Email is stored in nested account object
        CustomerProfile.AccountInfo account = new CustomerProfile.AccountInfo();
        account.setEmail(email);
        profileData.setAccount(account);

        if (!birthDate.isEmpty()) {
            try {
                SimpleDateFormat dateFormatParse = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                profileData.setBirthday(dateFormatParse.parse(birthDate));
            } catch (ParseException e) {
                Timber.e(e, "Error parsing date");
                Toast.makeText(getContext(), "Lỗi định dạng ngày sinh", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        binding.btnSave.setEnabled(false);
        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), resource -> {
            binding.btnSave.setEnabled(true);

            if (resource.getStatus() == Resource.Status.LOADING) {
                Toast.makeText(getContext(), "Đang cập nhật thông tin...", Toast.LENGTH_SHORT).show();
            } else if (resource.getStatus() == Resource.Status.SUCCESS) {
                Toast.makeText(getContext(), "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigateUp();
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(getContext(), "Lỗi: " + resource.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.updateProfile(profileData);
    }

    private String mapLabelToGender(String label) {
        if (label == null || label.isEmpty()) {
            return "";
        }

        // Map from Vietnamese labels to API values
        switch (label.toLowerCase()) {
            case "nam":
                return "Male";
            case "nữ":
                return "Female";
            case "khác":
                return "Other";
            default:
                return label;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}