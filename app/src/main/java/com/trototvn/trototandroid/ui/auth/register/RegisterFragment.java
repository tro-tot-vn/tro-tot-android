package com.trototvn.trototandroid.ui.auth.register;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.databinding.FragmentRegisterBinding;
import com.trototvn.trototandroid.ui.base.BaseFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * RegisterFragment - Clean MVVM implementation with comprehensive validation
 */
@AndroidEntryPoint
public class RegisterFragment extends BaseFragment<FragmentRegisterBinding> {

    private RegisterViewModel viewModel;
    private final Calendar birthday = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private String selectedGender = "Male";
    private String selectedCity = "";
    private String selectedDistrict = "";
    private String selectedJob = "";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void setupViews() {
        setupGenderDropdown();
        setupJobDropdown();
        setupCityDropdown();
        setupBirthdayPicker();

        // Register button click
        binding.btnRegister.setOnClickListener(v -> handleRegister());

        // Login link click
        binding.tvLoginLink.setOnClickListener(v -> navigateToLogin());
    }

    @Override
    protected void observeData() {
        // Observe register result
        viewModel.getRegisterResult().observe(getViewLifecycleOwner(), this::handleRegisterResult);

        // Observe validation errors
        viewModel.getPhoneError().observe(getViewLifecycleOwner(), error -> 
                binding.tilPhone.setError(error));
        
        viewModel.getEmailError().observe(getViewLifecycleOwner(), error -> 
                binding.tilEmail.setError(error));
        
        viewModel.getFirstNameError().observe(getViewLifecycleOwner(), error -> 
                binding.tilFirstName.setError(error));
        
        viewModel.getLastNameError().observe(getViewLifecycleOwner(), error -> 
                binding.tilLastName.setError(error));
        
        viewModel.getPasswordError().observe(getViewLifecycleOwner(), error -> 
                binding.tilPassword.setError(error));
    }

    /**
     * Setup gender dropdown
     */
    private void setupGenderDropdown() {
        String[] genders = {"Nam", "Nữ"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                genders
        );
        binding.actvGender.setAdapter(adapter);
        binding.actvGender.setText("Nam", false);
        binding.actvGender.setOnItemClickListener((parent, view, position, id) -> {
            selectedGender = position == 0 ? "Male" : "Female";
        });
    }

    /**
     * Setup job dropdown
     */
    private void setupJobDropdown() {
        String[] jobs = {"Sinh viên", "Đã đi làm"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                jobs
        );
        binding.actvJob.setAdapter(adapter);
        binding.actvJob.setOnItemClickListener((parent, view, position, id) -> {
            selectedJob = position == 0 ? "Student" : "Employed";
        });
    }

    /**
     * Setup city dropdown (simplified - using hardcoded cities)
     */
    private void setupCityDropdown() {
        String[] cities = {
                "Hồ Chí Minh",
                "Hà Nội",
                "Đà Nẵng",
                "Cần Thơ",
                "Hải Phòng"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                cities
        );
        binding.actvCity.setAdapter(adapter);
        binding.actvCity.setOnItemClickListener((parent, view, position, id) -> {
            selectedCity = cities[position];
            setupDistrictDropdown(selectedCity);
        });
    }

    /**
     * Setup district dropdown based on selected city
     */
    private void setupDistrictDropdown(String city) {
        String[] districts;
        
        // Simplified district data - in production, load from JSON or API
        switch (city) {
            case "Hồ Chí Minh":
                districts = new String[]{"Quận 1", "Quận 2", "Quận 3", "Quận 4", "Quận 5", 
                        "Quận 6", "Quận 7", "Quận 8", "Quận 9", "Quận 10"};
                break;
            case "Hà Nội":
                districts = new String[]{"Ba Đình", "Hoàn Kiếm", "Đống Đa", "Hai Bà Trưng",
                        "Cầu Giấy", "Thanh Xuân", "Tây Hồ"};
                break;
            default:
                districts = new String[]{"Quận 1", "Quận 2", "Quận 3"};
                break;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                districts
        );
        binding.actvDistrict.setAdapter(adapter);
        binding.actvDistrict.setText("", false);
        binding.actvDistrict.setOnItemClickListener((parent, view, position, id) -> {
            selectedDistrict = districts[position];
        });
    }

    /**
     * Setup birthday picker
     */
    private void setupBirthdayPicker() {
        binding.etBirthday.setOnClickListener(v -> showDatePicker());
        binding.tilBirthday.setEndIconOnClickListener(v -> showDatePicker());
    }

    /**
     * Show date picker dialog
     */
    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    birthday.set(year, month, dayOfMonth);
                    binding.etBirthday.setText(dateFormat.format(birthday.getTime()));
                },
                birthday.get(Calendar.YEAR),
                birthday.get(Calendar.MONTH),
                birthday.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set max date to 18 years ago
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -18);
        dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        
        dialog.show();
    }

    /**
     * Handle register button click
     */
    private void handleRegister() {
        String phone = getTextOrEmpty(binding.etPhone);
        String email = getTextOrEmpty(binding.etEmail);
        String firstName = getTextOrEmpty(binding.etFirstName);
        String lastName = getTextOrEmpty(binding.etLastName);
        String birthdayStr = getTextOrEmpty(binding.etBirthday);
        String password = getTextOrEmpty(binding.etPassword);

        viewModel.register(
                phone, email, firstName, lastName,
                birthdayStr, selectedGender, password,
                selectedCity, selectedDistrict, selectedJob
        );
    }

    /**
     * Handle register result from ViewModel
     */
    private void handleRegisterResult(Resource<?> resource) {
        switch (resource.getStatus()) {
            case LOADING:
                showLoading(true);
                hideError();
                break;

            case SUCCESS:
                showLoading(false);
                showToast("Đăng ký thành công! Vui lòng đăng nhập.");
                navigateToLogin();
                break;

            case ERROR:
                showLoading(false);
                showError(resource.getMessage());
                break;
        }
    }

    /**
     * Navigate to login screen
     */
    private void navigateToLogin() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_registerFragment_to_loginFragment);
    }

    /**
     * Show loading state
     */
    @Override
    protected void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!isLoading);
    }

    /**
     * Show error banner
     */
    private void showError(String message) {
        binding.cardError.setVisibility(View.VISIBLE);
        binding.tvErrorMessage.setText(message != null ? message : "Đã xảy ra lỗi");
    }

    /**
     * Hide error banner
     */
    private void hideError() {
        binding.cardError.setVisibility(View.GONE);
    }

    /**
     * Helper to get text from EditText or empty string
     */
    private String getTextOrEmpty(android.widget.EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
