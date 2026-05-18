package com.trototvn.trototandroid.ui.main.myposts;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * PostCreateFragment - Screen to create a new rental post.
 * Features X-style design, file limit validators, location syncing, and multipart uploads.
 */
@AndroidEntryPoint
public class PostCreateFragment extends Fragment {

    private FragmentPostCreateBinding binding;
    private PostFormViewModel viewModel;
    private MediaAdapter mediaAdapter;

    private final List<Uri> selectedImages = new ArrayList<>();
    private Uri selectedVideo = null;

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
        // Back Navigation
        binding.ivBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Select buttons triggers
        binding.btnSelectCity.setOnClickListener(v -> showCitySelectionDialog());
        binding.btnSelectDistrict.setOnClickListener(v -> showDistrictSelectionDialog());
        binding.btnSelectWard.setOnClickListener(v -> showWardSelectionDialog());

        // Media picking triggers
        binding.btnAddImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.btnAddVideo.setOnClickListener(v -> videoPickerLauncher.launch("video/*"));
        binding.btnRemoveVideo.setOnClickListener(v -> viewModel.removeNewVideo());

        // Interior conditions bindings
        binding.chipInteriorFull.setOnClickListener(v -> viewModel.getInteriorStatus().setValue("Full"));
        binding.chipInteriorNone.setOnClickListener(v -> viewModel.getInteriorStatus().setValue("None"));

        // Text bindings
        binding.etTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) viewModel.getTitle().setValue(binding.etTitle.getText().toString());
        });
        binding.etPrice.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) viewModel.getPrice().setValue(binding.etPrice.getText().toString());
        });
        binding.etAcreage.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) viewModel.getAcreage().setValue(binding.etAcreage.getText().toString());
        });
        binding.etDescription.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) viewModel.getDescription().setValue(binding.etDescription.getText().toString());
        });
        binding.etStreetNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) viewModel.getStreetNumber().setValue(binding.etStreetNumber.getText().toString());
        });
        binding.etStreet.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) viewModel.getStreet().setValue(binding.etStreet.getText().toString());
        });

        // Horizontal Images recycler view setup
        mediaAdapter = new MediaAdapter(selectedImages, position -> viewModel.removeNewImage(position));
        binding.rvSelectedImages.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        binding.rvSelectedImages.setAdapter(mediaAdapter);

        // Submit action trigger
        binding.btnSubmitPost.setOnClickListener(v -> {
            // Commit all current values from text fields
            viewModel.getTitle().setValue(binding.etTitle.getText().toString());
            viewModel.getPrice().setValue(binding.etPrice.getText().toString());
            viewModel.getAcreage().setValue(binding.etAcreage.getText().toString());
            viewModel.getDescription().setValue(binding.etDescription.getText().toString());
            viewModel.getStreetNumber().setValue(binding.etStreetNumber.getText().toString());
            viewModel.getStreet().setValue(binding.etStreet.getText().toString());

            if (viewModel.validateForm()) {
                submitForm();
            }
        });
    }

    private void setupObservers() {
        // Sync interior chip highlights
        viewModel.getInteriorStatus().observe(getViewLifecycleOwner(), status -> {
            binding.chipInteriorFull.setChecked("Full".equalsIgnoreCase(status));
            binding.chipInteriorNone.setChecked("None".equalsIgnoreCase(status));
        });

        // Sync cities, districts, wards selectors text
        viewModel.getSelectedCity().observe(getViewLifecycleOwner(), city -> {
            binding.tvSelectedCity.setText(city != null ? city.getName() : "Chọn Tỉnh / Thành phố");
        });
        viewModel.getSelectedDistrict().observe(getViewLifecycleOwner(), district -> {
            binding.tvSelectedDistrict.setText(district != null ? district.getName() : "Chọn Quận / Huyện");
        });
        viewModel.getSelectedWard().observe(getViewLifecycleOwner(), ward -> {
            binding.tvSelectedWard.setText(ward != null ? ward.getName() : "Chọn Phường / Xã");
        });

        // Observer Selected Media Lists
        viewModel.getNewImages().observe(getViewLifecycleOwner(), uris -> {
            selectedImages.clear();
            if (uris != null) {
                selectedImages.addAll(uris);
            }
            mediaAdapter.notifyDataSetChanged();
            binding.tvImagesLabel.setText("Hình ảnh (" + selectedImages.size() + "/12)");
        });

        viewModel.getNewVideo().observe(getViewLifecycleOwner(), uri -> {
            selectedVideo = uri;
            if (uri != null) {
                binding.tvSelectedVideoPath.setText("Video: " + uri.getLastPathSegment());
                binding.tvSelectedVideoPath.setVisibility(View.VISIBLE);
                binding.btnRemoveVideo.setVisibility(View.VISIBLE);
            } else {
                binding.tvSelectedVideoPath.setVisibility(View.GONE);
                binding.btnRemoveVideo.setVisibility(View.GONE);
            }
        });

        // Watch validation errors
        viewModel.getFormErrors().observe(getViewLifecycleOwner(), errors -> {
            if (errors != null && !errors.isEmpty()) {
                StringBuilder errorSummary = new StringBuilder();
                if (errors.containsKey("images")) {
                    errorSummary.append("• ").append(errors.get("images")).append("\n");
                }
                if (errors.containsKey("address")) {
                    errorSummary.append("• ").append(errors.get("address")).append("\n");
                }
                if (errors.containsKey("interiorStatus")) {
                    errorSummary.append("• ").append(errors.get("interiorStatus")).append("\n");
                }
                if (errors.containsKey("price")) {
                    errorSummary.append("• ").append(errors.get("price")).append("\n");
                }
                if (errors.containsKey("acreage")) {
                    errorSummary.append("• ").append(errors.get("acreage")).append("\n");
                }
                if (errors.containsKey("title")) {
                    errorSummary.append("• ").append(errors.get("title")).append("\n");
                }
                if (errors.containsKey("description")) {
                    errorSummary.append("• ").append(errors.get("description")).append("\n");
                }

                binding.tvFormErrorText.setText(errorSummary.toString().trim());
                binding.tvFormErrorText.setVisibility(View.VISIBLE);
            } else {
                binding.tvFormErrorText.setVisibility(View.GONE);
            }
        });

        // Watch submission transactions
        viewModel.getSubmitTransactionState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.btnSubmitPost.setEnabled(false);
                    binding.btnSubmitPost.setText("ĐANG XỬ LÝ ĐĂNG TIN...");
                    binding.tvFormErrorText.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.btnSubmitPost.setEnabled(true);
                    binding.btnSubmitPost.setText("ĐĂNG TIN NGAY");
                    Toast.makeText(requireContext(), "Đăng tin thuê trọ thành công!", Toast.LENGTH_LONG).show();
                    // Go back to listing screen
                    Navigation.findNavController(requireView()).popBackStack();
                    break;
                case ERROR:
                    binding.btnSubmitPost.setEnabled(true);
                    binding.btnSubmitPost.setText("ĐĂNG TIN NGAY");
                    binding.tvFormErrorText.setText("Lỗi: " + state.getMessage());
                    binding.tvFormErrorText.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void submitForm() {
        // Construct multipart parts
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (int i = 0; i < selectedImages.size(); i++) {
            MultipartBody.Part part = getMultipartFromUri(selectedImages.get(i), "images", "image/*");
            if (part != null) {
                imageParts.add(part);
            }
        }

        MultipartBody.Part videoPart = null;
        if (selectedVideo != null) {
            videoPart = getMultipartFromUri(selectedVideo, "video", "video/*");
        }

        viewModel.submitCreatePost(imageParts, videoPart);
    }

    // Helper: copy local URI to cache and create retrofitted part
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
            Timber.e(e, "Error creating multipart body from local URI");
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

    // Address selection triggers
    private void showCitySelectionDialog() {
        List<City> cities = viewModel.getCitiesLiveData().getValue();
        if (cities == null || cities.isEmpty()) return;

        String[] cityNames = new String[cities.size()];
        for (int i = 0; i < cities.size(); i++) {
            cityNames[i] = cities.get(i).getName();
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chọn Tỉnh / Thành phố")
                .setItems(cityNames, (dialog, which) -> {
                    viewModel.selectCity(cities.get(which));
                })
                .show();
    }

    private void showDistrictSelectionDialog() {
        List<District> districts = viewModel.getDistrictsLiveData().getValue();
        if (districts == null || districts.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn Tỉnh / Thành phố trước", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] districtNames = new String[districts.size()];
        for (int i = 0; i < districts.size(); i++) {
            districtNames[i] = districts.get(i).getName();
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chọn Quận / Huyện")
                .setItems(districtNames, (dialog, which) -> {
                    viewModel.selectDistrict(districts.get(which));
                })
                .show();
    }

    private void showWardSelectionDialog() {
        Resource<WardListResponse> wardsRes = viewModel.getWardsLiveData().getValue();
        if (wardsRes == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn Quận / Huyện trước", Toast.LENGTH_SHORT).show();
            return;
        }

        if (wardsRes.getStatus() == Resource.Status.LOADING) {
            Toast.makeText(requireContext(), "Đang tải danh sách Phường / Xã...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (wardsRes.getStatus() == Resource.Status.ERROR || wardsRes.getData() == null) {
            Toast.makeText(requireContext(), "Lỗi khi tải danh sách Phường / Xã. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Ward> wards = wardsRes.getData().getWards();
        if (wards == null || wards.isEmpty()) return;

        String[] wardNames = new String[wards.size()];
        for (int i = 0; i < wards.size(); i++) {
            wardNames[i] = wards.get(i).getName();
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chọn Phường / Xã")
                .setItems(wardNames, (dialog, which) -> {
                    viewModel.selectWard(wards.get(which));
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // --- Embedded Selected Media Recycler Adapter ---
    private static class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

        private final List<Uri> imageList;
        private final OnDeleteClickListener deleteListener;

        public interface OnDeleteClickListener {
            void onDelete(int position);
        }

        public MediaAdapter(List<Uri> imageList, OnDeleteClickListener deleteListener) {
            this.imageList = imageList;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSelectedMediaBinding itemBinding = ItemSelectedMediaBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new MediaViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
            Uri imageUri = imageList.get(position);
            holder.binding.ivMediaPreview.setImageURI(imageUri);
            holder.binding.btnDeleteMedia.setOnClickListener(v -> deleteListener.onDelete(position));
        }

        @Override
        public int getItemCount() {
            return imageList.size();
        }

        static class MediaViewHolder extends RecyclerView.ViewHolder {
            ItemSelectedMediaBinding binding;

            public MediaViewHolder(ItemSelectedMediaBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
