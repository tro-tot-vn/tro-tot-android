package com.trototvn.trototandroid.ui.main.myposts;

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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.location.City;
import com.trototvn.trototandroid.data.model.location.District;
import com.trototvn.trototandroid.data.model.location.Ward;
import com.trototvn.trototandroid.data.model.location.WardListResponse;
import com.trototvn.trototandroid.data.model.post.FileType;
import com.trototvn.trototandroid.data.model.post.MultimediaFileDetail;
import com.trototvn.trototandroid.databinding.FragmentPostEditBinding;
import com.trototvn.trototandroid.databinding.ItemSelectedMediaBinding;
import com.trototvn.trototandroid.utils.Constants;

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
 * PostEditFragment - Edit an existing rental post.
 * Loads original post values, binds inputs, and updates post with multipart media payload.
 */
@AndroidEntryPoint
public class PostEditFragment extends Fragment {

    private FragmentPostEditBinding binding;
    private PostFormViewModel viewModel;
    private EditMediaAdapter mediaAdapter;
    private int postId = -1;

    private final List<EditMediaItem> mediaItems = new ArrayList<>();

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
        binding = FragmentPostEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PostFormViewModel.class);

        if (getArguments() != null) {
            postId = getArguments().getInt("postId", -1);
        }

        if (postId == -1) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin tin đăng", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        setupUI();
        setupObservers();

        // Trigger loading post data
        viewModel.loadPostDetailForEdit(postId);
    }

    private void setupUI() {
        binding.ivBack.setOnClickListener(v -> requireActivity().onBackPressed());

        binding.btnSelectCity.setOnClickListener(v -> showCitySelectionDialog());
        binding.btnSelectDistrict.setOnClickListener(v -> showDistrictSelectionDialog());
        binding.btnSelectWard.setOnClickListener(v -> showWardSelectionDialog());

        binding.btnAddImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.btnAddVideo.setOnClickListener(v -> videoPickerLauncher.launch("video/*"));
        binding.btnRemoveVideo.setOnClickListener(v -> viewModel.removeNewVideo());

        binding.chipInteriorFull.setOnClickListener(v -> viewModel.getInteriorStatus().setValue("Full"));
        binding.chipInteriorNone.setOnClickListener(v -> viewModel.getInteriorStatus().setValue("None"));

        // Set up list adapter combining old images & newly selected images
        mediaAdapter = new EditMediaAdapter(mediaItems, new EditMediaAdapter.OnMediaDeleteListener() {
            @Override
            public void onDeleteOld(int fileId) {
                viewModel.removeOldFile(fileId);
            }

            @Override
            public void onDeleteNew(int newIndex) {
                viewModel.removeNewImage(newIndex);
            }
        });

        binding.rvSelectedImages.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        binding.rvSelectedImages.setAdapter(mediaAdapter);

        binding.btnSubmitPost.setOnClickListener(v -> {
            // Commit text changes from inputs
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
        // Sync values to textfields
        viewModel.getInitialLoadState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.pbInitialLoading.setVisibility(View.VISIBLE);
                    binding.svFormContainer.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.pbInitialLoading.setVisibility(View.GONE);
                    binding.svFormContainer.setVisibility(View.VISIBLE);
                    if (state.getData() != null) {
                        binding.etTitle.setText(viewModel.getTitle().getValue());
                        binding.etPrice.setText(viewModel.getPrice().getValue());
                        binding.etAcreage.setText(viewModel.getAcreage().getValue());
                        binding.etDescription.setText(viewModel.getDescription().getValue());
                        binding.etStreetNumber.setText(viewModel.getStreetNumber().getValue());
                        binding.etStreet.setText(viewModel.getStreet().getValue());
                    }
                    break;
                case ERROR:
                    binding.pbInitialLoading.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Lỗi khi tải thông tin: " + state.getMessage(), Toast.LENGTH_LONG).show();
                    requireActivity().onBackPressed();
                    break;
            }
        });

        viewModel.getInteriorStatus().observe(getViewLifecycleOwner(), status -> {
            binding.chipInteriorFull.setChecked("Full".equalsIgnoreCase(status));
            binding.chipInteriorNone.setChecked("None".equalsIgnoreCase(status));
        });

        viewModel.getSelectedCity().observe(getViewLifecycleOwner(), city -> {
            binding.tvSelectedCity.setText(city != null ? city.getName() : "Chọn Tỉnh / Thành phố");
        });
        viewModel.getSelectedDistrict().observe(getViewLifecycleOwner(), district -> {
            binding.tvSelectedDistrict.setText(district != null ? district.getName() : "Chọn Quận / Huyện");
        });
        viewModel.getSelectedWard().observe(getViewLifecycleOwner(), ward -> {
            binding.tvSelectedWard.setText(ward != null ? ward.getName() : "Chọn Phường / Xã");
        });

        // Watch Media items (combining old files and new images)
        viewModel.getOldFiles().observe(getViewLifecycleOwner(), files -> updateCombinedMediaList());
        viewModel.getNewImages().observe(getViewLifecycleOwner(), uris -> updateCombinedMediaList());

        // Watch new video
        viewModel.getNewVideo().observe(getViewLifecycleOwner(), uri -> {
            if (uri != null) {
                binding.tvSelectedVideoPath.setText("Video mới: " + uri.getLastPathSegment());
                binding.tvSelectedVideoPath.setVisibility(View.VISIBLE);
                binding.btnRemoveVideo.setVisibility(View.VISIBLE);
            } else {
                // If no new video, check if there's an old video in oldFiles
                MultimediaFileDetail oldVideo = getOldVideoFile();
                if (oldVideo != null) {
                    binding.tvSelectedVideoPath.setText("Video gốc đã tải lên");
                    binding.tvSelectedVideoPath.setVisibility(View.VISIBLE);
                    binding.btnRemoveVideo.setVisibility(View.GONE);
                } else {
                    binding.tvSelectedVideoPath.setVisibility(View.GONE);
                    binding.btnRemoveVideo.setVisibility(View.GONE);
                }
            }
        });

        // Watch validation errors
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

        // Watch submission transactions
        viewModel.getSubmitTransactionState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.btnSubmitPost.setEnabled(false);
                    binding.btnSubmitPost.setText("ĐANG LƯU THAY ĐỔI...");
                    binding.tvFormErrorText.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.btnSubmitPost.setEnabled(true);
                    binding.btnSubmitPost.setText("LƯU THAY ĐỔI");
                    Toast.makeText(requireContext(), "Cập nhật bài viết thành công!", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).popBackStack();
                    break;
                case ERROR:
                    binding.btnSubmitPost.setEnabled(true);
                    binding.btnSubmitPost.setText("LƯU THAY ĐỔI");
                    binding.tvFormErrorText.setText("Lỗi: " + state.getMessage());
                    binding.tvFormErrorText.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private MultimediaFileDetail getOldVideoFile() {
        List<MultimediaFileDetail> files = viewModel.getOldFiles().getValue();
        if (files != null) {
            for (MultimediaFileDetail file : files) {
                if (file.getFileType() == FileType.VIDEO) {
                    return file;
                }
            }
        }
        return null;
    }

    private void updateCombinedMediaList() {
        mediaItems.clear();

        // 1. Add old files (type IMAGE)
        List<MultimediaFileDetail> oldList = viewModel.getOldFiles().getValue();
        if (oldList != null) {
            for (MultimediaFileDetail file : oldList) {
                if (file.getFileType() == FileType.IMAGE) {
                    EditMediaItem item = new EditMediaItem();
                    item.isOld = true;
                    item.oldFileId = file.getFileId();
                    item.oldUrl = Constants.BASE_URL + "api/files/" + file.getFileId();
                    mediaItems.add(item);
                }
            }
        }

        // 2. Add newly selected images
        List<Uri> newList = viewModel.getNewImages().getValue();
        if (newList != null) {
            for (int i = 0; i < newList.size(); i++) {
                EditMediaItem item = new EditMediaItem();
                item.isOld = false;
                item.newIndex = i;
                item.newUri = newList.get(i);
                mediaItems.add(item);
            }
        }

        mediaAdapter.notifyDataSetChanged();
        binding.tvImagesLabel.setText("Hình ảnh (" + mediaItems.size() + "/12)");
    }

    private void submitForm() {
        // Construct multipart parameters for newly selected images
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

        viewModel.submitEditPost(postId, newImageParts, newVideoPart);
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
            Timber.e(e, "Error constructing multipart part from uri in PostEditFragment");
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

    private void showCitySelectionDialog() {
        List<City> cities = viewModel.getCitiesLiveData().getValue();
        if (cities == null || cities.isEmpty()) return;

        String[] cityNames = new String[cities.size()];
        for (int i = 0; i < cities.size(); i++) {
            cityNames[i] = cities.get(i).getName();
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chọn Tỉnh / Thành phố")
                .setItems(cityNames, (dialog, which) -> viewModel.selectCity(cities.get(which)))
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
                .setItems(districtNames, (dialog, which) -> viewModel.selectDistrict(districts.get(which)))
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
                .setItems(wardNames, (dialog, which) -> viewModel.selectWard(wards.get(which)))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // --- Model Class for Combined Media Adapter ---
    private static class EditMediaItem {
        boolean isOld;
        int oldFileId;
        String oldUrl;

        int newIndex;
        Uri newUri;
    }

    // --- Embedded Selected Media Recycler Adapter for Editing ---
    private static class EditMediaAdapter extends RecyclerView.Adapter<EditMediaAdapter.MediaViewHolder> {

        private final List<EditMediaItem> mediaList;
        private final OnMediaDeleteListener deleteListener;

        public interface OnMediaDeleteListener {
            void onDeleteOld(int fileId);
            void onDeleteNew(int newIndex);
        }

        public EditMediaAdapter(List<EditMediaItem> mediaList, OnMediaDeleteListener deleteListener) {
            this.mediaList = mediaList;
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
            EditMediaItem item = mediaList.get(position);

            if (item.isOld) {
                // Load remote image via Glide
                Glide.with(holder.binding.ivMediaPreview.getContext())
                        .load(item.oldUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .placeholder(R.color.md_theme_light_surfaceVariant)
                        .into(holder.binding.ivMediaPreview);

                holder.binding.btnDeleteMedia.setOnClickListener(v -> deleteListener.onDeleteOld(item.oldFileId));
            } else {
                // Load local newly selected image URI
                holder.binding.ivMediaPreview.setImageURI(item.newUri);
                holder.binding.btnDeleteMedia.setOnClickListener(v -> deleteListener.onDeleteNew(item.newIndex));
            }
        }

        @Override
        public int getItemCount() {
            return mediaList.size();
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
