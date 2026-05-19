package com.trototvn.trototandroid.ui.main.myposts;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.location.City;
import com.trototvn.trototandroid.data.model.location.District;
import com.trototvn.trototandroid.data.model.location.Ward;
import com.trototvn.trototandroid.data.model.location.WardListResponse;
import com.trototvn.trototandroid.databinding.FragmentPostCreateBinding;
import com.trototvn.trototandroid.databinding.ItemSelectedMediaBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * PostCreateFragment - Screen to create a new rental post with premium web-aligned features.
 * Integrates image list with cover badges, unified address picker dialog, character counter limits,
 * video preview card player, and automated AI policy warning checks.
 */
@AndroidEntryPoint
public class PostCreateFragment extends Fragment {

    private FragmentPostCreateBinding binding;
    private PostFormViewModel viewModel;

    // Asynchronous dialog references to avoid memory leaks
    private AutoCompleteTextView dialogActvDistrict;
    private AutoCompleteTextView dialogActvWard;

    // Activity Result Launchers
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    long size = getUriSize(uri);
                    if (size > 5 * 1024 * 1024) { // 5MB limit
                        Toast.makeText(requireContext(), "Kích thước ảnh phải nhỏ hơn 5MB", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.addNewImage(uri);
                }
            }
    );

    private final ActivityResultLauncher<String> videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    long size = getUriSize(uri);
                    if (size > 25 * 1024 * 1024) { // 25MB limit
                        Toast.makeText(requireContext(), "Kích thước video phải nhỏ hơn 25MB", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.setNewVideo(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPostCreateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PostFormViewModel.class);

        setupUI();
        setupObservers();
    }

    private void setupUI() {
        binding.ivBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Dashed styled selectors triggers
        binding.btnUploadImagesCard.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.btnUploadVideoCard.setOnClickListener(v -> videoPickerLauncher.launch("video/*"));

        // Consolidated Address trigger
        binding.mcvAddressPickerCard.setOnClickListener(v -> showUnifiedAddressDialog());

        binding.chipInteriorFull.setOnClickListener(v -> viewModel.getInteriorStatus().setValue("Full"));
        binding.chipInteriorNone.setOnClickListener(v -> viewModel.getInteriorStatus().setValue("None"));

        // Setup horizontal recyclerview for selected images
        binding.rvNewImages.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));

        // Submit action
        binding.btnSubmitPost.setOnClickListener(v -> {
            // Commit text inputs from XML text fields
            viewModel.getTitle().setValue(binding.etTitle.getText().toString());
            viewModel.getPrice().setValue(binding.etPrice.getText().toString());
            viewModel.getAcreage().setValue(binding.etAcreage.getText().toString());
            viewModel.getDescription().setValue(binding.etDescription.getText().toString());

            if (viewModel.validateForm()) {
                submitForm();
            }
        });
    }

    private void setupObservers() {
        viewModel.getInteriorStatus().observe(getViewLifecycleOwner(), status -> {
            binding.chipInteriorFull.setChecked("Full".equalsIgnoreCase(status));
            binding.chipInteriorNone.setChecked("None".equalsIgnoreCase(status));
        });

        // Watch Address inputs to format Address Card
        viewModel.getSelectedCity().observe(getViewLifecycleOwner(), city -> updateFullAddressCard());
        viewModel.getSelectedDistrict().observe(getViewLifecycleOwner(), district -> {
            updateDistrictDropdown(viewModel.getDistrictsLiveData().getValue());
            updateFullAddressCard();
        });
        viewModel.getSelectedWard().observe(getViewLifecycleOwner(), ward -> updateFullAddressCard());
        viewModel.getStreet().observe(getViewLifecycleOwner(), street -> updateFullAddressCard());
        viewModel.getStreetNumber().observe(getViewLifecycleOwner(), num -> updateFullAddressCard());

        // Observer dynamic Ward loading API
        viewModel.getWardsLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                updateWardDropdown(resource.getData().getWards());
            }
        });

        // Watch newly selected images list to update recycler adapter & count labels
        viewModel.getNewImages().observe(getViewLifecycleOwner(), uris -> {
            if (uris == null || uris.isEmpty()) {
                binding.llNewImagesContainer.setVisibility(View.GONE);
                binding.tvImagesCardCount.setText("ĐĂNG TỐI ĐA 12 HÌNH");
            } else {
                binding.llNewImagesContainer.setVisibility(View.VISIBLE);
                NewImagesAdapter adapter = new NewImagesAdapter(uris, pos -> viewModel.removeNewImage(pos));
                binding.rvNewImages.setAdapter(adapter);
                binding.tvImagesCardCount.setText("ĐÃ CHỌN " + uris.size() + "/12 HÌNH");
            }
        });

        // Watch new video URIs to toggle 16:9 preview player layout
        viewModel.getNewVideo().observe(getViewLifecycleOwner(), uri -> {
            if (uri != null) {
                binding.mcvVideoPreviewCard.setVisibility(View.VISIBLE);
                binding.tvVideoNameLabel.setText("Video mới: " + uri.getLastPathSegment());
                binding.ivVideoThumbnail.setImageResource(R.color.md_theme_light_surfaceVariant);
                binding.ivPlayVideoBtn.setOnClickListener(v -> playVideo(uri));
                binding.btnRemoveVideoCard.setOnClickListener(v -> viewModel.removeNewVideo());
                binding.tvVideoCardStatus.setText("ĐÃ CHỌN 01 VIDEO");
            } else {
                binding.mcvVideoPreviewCard.setVisibility(View.GONE);
                binding.tvVideoCardStatus.setText("ĐĂNG TỐI ĐA 01 VIDEO");
            }
        });

        // Watch form validation errors
        viewModel.getFormErrors().observe(getViewLifecycleOwner(), errors -> {
            if (errors != null && !errors.isEmpty()) {
                StringBuilder errorSummary = new StringBuilder();
                if (errors.containsKey("images")) errorSummary.append("• ").append(errors.get("images")).append("\n");
                if (errors.containsKey("address")) errorSummary.append("• ").append(errors.get("address")).append("\n");
                if (errors.containsKey("interiorStatus")) errorSummary.append("• ").append(errors.get("interiorStatus")).append("\n");
                if (errors.containsKey("price")) errorSummary.append("• ").append(errors.get("price")).append("\n");
                if (errors.containsKey("acreage")) errorSummary.append("• ").append(errors.get("acreage")).append("\n");
                if (errors.containsKey("title")) errorSummary.append("• ").append(errors.get("title")).append("\n");
                if (errors.containsKey("description")) errorSummary.append("• ").append(errors.get("description")).append("\n");

                binding.tvFormErrorText.setText(errorSummary.toString().trim());
                binding.tvFormErrorText.setVisibility(View.VISIBLE);
            } else {
                binding.tvFormErrorText.setVisibility(View.GONE);
            }
        });

        // Watch post submission transactions
        viewModel.getSubmitTransactionState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.btnSubmitPost.setEnabled(false);
                    binding.btnSubmitPost.setText("ĐANG ĐĂNG TIN...");
                    binding.tvFormErrorText.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.btnSubmitPost.setEnabled(true);
                    binding.btnSubmitPost.setText("ĐĂNG TIN NGAY");
                    Toast.makeText(requireContext(), "Đăng tin trọ mới thành công!", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).popBackStack();
                    break;
                case ERROR:
                    binding.btnSubmitPost.setEnabled(true);
                    binding.btnSubmitPost.setText("ĐĂNG TIN NGAY");
                    if ("CONTENT_VIOLATION".equals(state.getMessage())) {
                        showContentViolationDialog();
                    } else {
                        binding.tvFormErrorText.setText("Lỗi: " + state.getMessage());
                        binding.tvFormErrorText.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        });
    }

    private void playVideo(Uri uri) {
        if (uri == null) return;
        android.widget.FrameLayout frameLayout = new android.widget.FrameLayout(requireContext());
        android.widget.VideoView videoView = new android.widget.VideoView(requireContext());
        android.widget.FrameLayout.LayoutParams lp = new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = android.view.Gravity.CENTER;
        videoView.setLayoutParams(lp);
        frameLayout.addView(videoView);

        android.widget.MediaController mediaController = new android.widget.MediaController(requireContext());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(frameLayout)
                .setOnDismissListener(d -> videoView.stopPlayback())
                .create();

        videoView.setOnPreparedListener(mp -> videoView.start());
        dialog.show();
    }

    private void showContentViolationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.icons8_close)
                .setTitle("Phát hiện vi phạm chính sách")
                .setMessage("Tiêu đề hoặc mô tả bài đăng của bạn chứa nội dung vi phạm tiêu chuẩn cộng đồng của hệ thống kiểm duyệt AI.\n\nVui lòng chỉnh sửa và thử lại.")
                .setPositiveButton("ĐỒNG Ý", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateFullAddressCard() {
        String number = viewModel.getStreetNumber().getValue();
        String street = viewModel.getStreet().getValue();
        Ward ward = viewModel.getSelectedWard().getValue();
        District district = viewModel.getSelectedDistrict().getValue();
        City city = viewModel.getSelectedCity().getValue();

        StringBuilder sb = new StringBuilder();
        if (number != null && !number.trim().isEmpty()) {
            sb.append(number.trim()).append(" ");
        }
        if (street != null && !street.trim().isEmpty()) {
            sb.append(street.trim()).append(", ");
        }
        if (ward != null) {
            sb.append(ward.getName()).append(", ");
        }
        if (district != null) {
            sb.append(district.getName()).append(", ");
        }
        if (city != null) {
            sb.append(city.getName());
        }

        String result = sb.toString().trim();
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1).trim();
        }

        if (result.isEmpty()) {
            binding.tvFullAddress.setText("Chưa thiết lập địa chỉ trọ");
        } else {
            binding.tvFullAddress.setText(result);
        }
    }

    private void showUnifiedAddressDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_unified_address, null);

        AutoCompleteTextView actvCity = dialogView.findViewById(R.id.actvCity);
        dialogActvDistrict = dialogView.findViewById(R.id.actvDistrict);
        dialogActvWard = dialogView.findViewById(R.id.actvWard);
        com.google.android.material.textfield.TextInputEditText etStreetName = dialogView.findViewById(R.id.etStreetName);
        com.google.android.material.textfield.TextInputEditText etHouseNumber = dialogView.findViewById(R.id.etHouseNumber);
        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelAddress);
        com.google.android.material.button.MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveAddress);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        City currentCity = viewModel.getSelectedCity().getValue();
        District currentDistrict = viewModel.getSelectedDistrict().getValue();
        Ward currentWard = viewModel.getSelectedWard().getValue();
        String currentStreet = viewModel.getStreet().getValue();
        String currentNumber = viewModel.getStreetNumber().getValue();

        if (currentCity != null) actvCity.setText(currentCity.getName(), false);
        if (currentDistrict != null) dialogActvDistrict.setText(currentDistrict.getName(), false);
        if (currentWard != null) dialogActvWard.setText(currentWard.getName(), false);
        if (currentStreet != null) etStreetName.setText(currentStreet);
        if (currentNumber != null) etHouseNumber.setText(currentNumber);

        // Bind City Dropdown
        List<City> cities = viewModel.getCitiesLiveData().getValue();
        if (cities != null) {
            String[] cityNames = new String[cities.size()];
            for (int i = 0; i < cities.size(); i++) cityNames[i] = cities.get(i).getName();
            android.widget.ArrayAdapter<String> cityAdapter = new android.widget.ArrayAdapter<>(
                    requireContext(), android.R.layout.simple_dropdown_item_1line, cityNames);
            actvCity.setAdapter(cityAdapter);
        }

        updateDistrictDropdown(viewModel.getDistrictsLiveData().getValue());
        Resource<WardListResponse> wardsRes = viewModel.getWardsLiveData().getValue();
        if (wardsRes != null && wardsRes.getStatus() == Resource.Status.SUCCESS && wardsRes.getData() != null) {
            updateWardDropdown(wardsRes.getData().getWards());
        }

        // Selection listeners
        actvCity.setOnItemClickListener((parent, v, position, id) -> {
            if (cities != null) {
                City selected = cities.get(position);
                viewModel.selectCity(selected);
                dialogActvDistrict.setText("", false);
                dialogActvWard.setText("", false);
            }
        });

        dialogActvDistrict.setOnItemClickListener((parent, v, position, id) -> {
            List<District> districts = viewModel.getDistrictsLiveData().getValue();
            if (districts != null) {
                District selected = districts.get(position);
                viewModel.selectDistrict(selected);
                dialogActvWard.setText("", false);
            }
        });

        dialogActvWard.setOnItemClickListener((parent, v, position, id) -> {
            Resource<WardListResponse> res = viewModel.getWardsLiveData().getValue();
            if (res != null && res.getStatus() == Resource.Status.SUCCESS && res.getData() != null) {
                List<Ward> list = res.getData().getWards();
                if (list != null) {
                    Ward selected = list.get(position);
                    viewModel.selectWard(selected);
                }
            }
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            cleanupDialogReferences();
        });

        btnSave.setOnClickListener(v -> {
            String streetName = etStreetName.getText() != null ? etStreetName.getText().toString().trim() : "";
            String houseNumber = etHouseNumber.getText() != null ? etHouseNumber.getText().toString().trim() : "";

            boolean isValid = true;
            if (viewModel.getSelectedCity().getValue() == null) {
                actvCity.setError("Vui lòng chọn Tỉnh / Thành phố");
                isValid = false;
            } else {
                actvCity.setError(null);
            }

            if (viewModel.getSelectedDistrict().getValue() == null) {
                dialogActvDistrict.setError("Vui lòng chọn Quận / Huyện");
                isValid = false;
            } else {
                dialogActvDistrict.setError(null);
            }

            if (viewModel.getSelectedWard().getValue() == null) {
                dialogActvWard.setError("Vui lòng chọn Phường / Xã");
                isValid = false;
            } else {
                dialogActvWard.setError(null);
            }

            if (streetName.isEmpty()) {
                etStreetName.setError("Vui lòng nhập tên đường");
                isValid = false;
            } else {
                etStreetName.setError(null);
            }

            if (houseNumber.isEmpty()) {
                etHouseNumber.setError("Vui lòng nhập số nhà");
                isValid = false;
            } else {
                etHouseNumber.setError(null);
            }

            if (isValid) {
                viewModel.getStreet().setValue(streetName);
                viewModel.getStreetNumber().setValue(houseNumber);
                updateFullAddressCard();
                dialog.dismiss();
                cleanupDialogReferences();
            }
        });

        dialog.show();
    }

    private void updateDistrictDropdown(List<District> districts) {
        if (dialogActvDistrict == null) return;
        if (districts != null && !districts.isEmpty()) {
            String[] names = new String[districts.size()];
            for (int i = 0; i < districts.size(); i++) names[i] = districts.get(i).getName();
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                    requireContext(), android.R.layout.simple_dropdown_item_1line, names);
            dialogActvDistrict.setAdapter(adapter);
        } else {
            dialogActvDistrict.setAdapter(null);
        }
    }

    private void updateWardDropdown(List<Ward> wards) {
        if (dialogActvWard == null) return;
        if (wards != null && !wards.isEmpty()) {
            String[] names = new String[wards.size()];
            for (int i = 0; i < wards.size(); i++) names[i] = wards.get(i).getName();
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                    requireContext(), android.R.layout.simple_dropdown_item_1line, names);
            dialogActvWard.setAdapter(adapter);
        } else {
            dialogActvWard.setAdapter(null);
        }
    }

    private void cleanupDialogReferences() {
        dialogActvDistrict = null;
        dialogActvWard = null;
    }

    private void submitForm() {
        List<MultipartBody.Part> newImageParts = new ArrayList<>();
        List<Uri> newUris = viewModel.getNewImages().getValue();
        if (newUris != null) {
            for (Uri uri : newUris) {
                MultipartBody.Part part = getMultipartFromUri(uri, "images", "image/*");
                if (part != null) {
                    newImageParts.add(part);
                }
            }
        }

        MultipartBody.Part newVideoPart = null;
        Uri newVideoUri = viewModel.getNewVideo().getValue();
        if (newVideoUri != null) {
            newVideoPart = getMultipartFromUri(newVideoUri, "video", "video/*");
        }

        viewModel.submitCreatePost(newImageParts, newVideoPart);
    }

    private MultipartBody.Part getMultipartFromUri(Uri uri, String paramName, String mimeType) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            File tempFile = new File(requireContext().getCacheDir(), "upload_" + System.currentTimeMillis() + "_" + uri.getLastPathSegment());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), tempFile);
            return MultipartBody.Part.createFormData(paramName, tempFile.getName(), requestFile);
        } catch (Exception e) {
            Timber.e(e, "Error constructing multipart part from uri in PostCreateFragment");
            return null;
        }
    }

    private long getUriSize(Uri uri) {
        try {
            android.database.Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                long size = cursor.getLong(sizeIndex);
                cursor.close();
                return size;
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return 0;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cleanupDialogReferences();
        binding = null;
    }

    // --- Modern selected images Recycler Adapter for Creating new posts ---
    private static class NewImagesAdapter extends RecyclerView.Adapter<NewImagesAdapter.ViewHolder> {
        private final List<Uri> uris;
        private final OnDeleteListener deleteListener;

        public interface OnDeleteListener {
            void onDelete(int position);
        }

        public NewImagesAdapter(List<Uri> uris, OnDeleteListener deleteListener) {
            this.uris = uris;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSelectedMediaBinding binding = ItemSelectedMediaBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Uri uri = uris.get(position);
            holder.binding.ivMediaPreview.setImageURI(uri);

            // First image selected is always highlighted as Cover Image (Ảnh bìa)
            holder.binding.tvCoverBadge.setVisibility(position == 0 ? View.VISIBLE : View.GONE);

            holder.binding.btnDeleteMedia.setOnClickListener(v -> deleteListener.onDelete(position));
        }

        @Override
        public int getItemCount() {
            return uris.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemSelectedMediaBinding binding;
            public ViewHolder(ItemSelectedMediaBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
