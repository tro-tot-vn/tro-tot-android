package com.trototvn.trototandroid.ui.main.myposts;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.location.City;
import com.trototvn.trototandroid.data.model.location.District;
import com.trototvn.trototandroid.data.model.location.Ward;
import com.trototvn.trototandroid.data.model.location.WardListResponse;
import com.trototvn.trototandroid.data.model.post.PostDetail;
import com.trototvn.trototandroid.data.model.post.MultimediaFileDetail;
import com.trototvn.trototandroid.data.repository.PostRepository;
import com.trototvn.trototandroid.utils.LocationService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * PostFormViewModel - Combined state machine for Post Creation and Editing.
 * Handles validation, local dynamic city/district selection, dynamic ward API loading,
 * and multipart submission transactions.
 */
@HiltViewModel
public class PostFormViewModel extends ViewModel {

    private final PostRepository postRepository;
    private final LocationService locationService;
    private final CompositeDisposable disposable = new CompositeDisposable();

    // Location States
    private final MutableLiveData<List<City>> citiesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<District>> districtsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<WardListResponse>> wardsLiveData = new MutableLiveData<>();

    // Selected locations
    private final MutableLiveData<City> selectedCity = new MutableLiveData<>();
    private final MutableLiveData<District> selectedDistrict = new MutableLiveData<>();
    private final MutableLiveData<Ward> selectedWard = new MutableLiveData<>();

    // Form inputs & validators
    private final MutableLiveData<String> title = new MutableLiveData<>("");
    private final MutableLiveData<String> description = new MutableLiveData<>("");
    private final MutableLiveData<String> price = new MutableLiveData<>("");
    private final MutableLiveData<String> acreage = new MutableLiveData<>("");
    private final MutableLiveData<String> interiorStatus = new MutableLiveData<>(""); // "Full" (Đầy đủ), "None" (Nhà trống)
    private final MutableLiveData<String> streetNumber = new MutableLiveData<>("");
    private final MutableLiveData<String> street = new MutableLiveData<>("");

    // Media states
    private final MutableLiveData<List<Uri>> newImages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Uri> newVideo = new MutableLiveData<>(null);
    private final MutableLiveData<List<MultimediaFileDetail>> oldFiles = new MutableLiveData<>(new ArrayList<>());

    // Validation & submission transaction states
    private final MutableLiveData<Map<String, String>> formErrors = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Resource<Void>> submitTransactionState = new MutableLiveData<>();
    private final MutableLiveData<Resource<PostDetail>> initialLoadState = new MutableLiveData<>();

    @Inject
    public PostFormViewModel(PostRepository postRepository, LocationService locationService) {
        this.postRepository = postRepository;
        this.locationService = locationService;
        loadCities();
    }

    public LiveData<List<City>> getCitiesLiveData() { return citiesLiveData; }
    public LiveData<List<District>> getDistrictsLiveData() { return districtsLiveData; }
    public LiveData<Resource<WardListResponse>> getWardsLiveData() { return wardsLiveData; }

    public LiveData<City> getSelectedCity() { return selectedCity; }
    public LiveData<District> getSelectedDistrict() { return selectedDistrict; }
    public LiveData<Ward> getSelectedWard() { return selectedWard; }

    public MutableLiveData<String> getTitle() { return title; }
    public MutableLiveData<String> getDescription() { return description; }
    public MutableLiveData<String> getPrice() { return price; }
    public MutableLiveData<String> getAcreage() { return acreage; }
    public MutableLiveData<String> getInteriorStatus() { return interiorStatus; }
    public MutableLiveData<String> getStreetNumber() { return streetNumber; }
    public MutableLiveData<String> getStreet() { return street; }

    public LiveData<List<Uri>> getNewImages() { return newImages; }
    public LiveData<Uri> getNewVideo() { return newVideo; }
    public LiveData<List<MultimediaFileDetail>> getOldFiles() { return oldFiles; }

    public LiveData<Map<String, String>> getFormErrors() { return formErrors; }
    public LiveData<Resource<Void>> getSubmitTransactionState() { return submitTransactionState; }
    public LiveData<Resource<PostDetail>> getInitialLoadState() { return initialLoadState; }

    private void loadCities() {
        citiesLiveData.setValue(locationService.getAllCities());
    }

    public void selectCity(City city) {
        selectedCity.setValue(city);
        selectedDistrict.setValue(null);
        selectedWard.setValue(null);
        if (city != null) {
            districtsLiveData.setValue(locationService.getDistrictsByCityId(city.getId()));
        } else {
            districtsLiveData.setValue(new ArrayList<>());
        }
        wardsLiveData.setValue(null);
    }

    public void selectDistrict(District district) {
        selectedDistrict.setValue(district);
        selectedWard.setValue(null);
        if (district != null) {
            loadWards(district.getId());
        } else {
            wardsLiveData.setValue(null);
        }
    }

    public void selectWard(Ward ward) {
        selectedWard.setValue(ward);
    }

    private void loadWards(String districtId) {
        wardsLiveData.setValue(Resource.loading(null));
        disposable.add(postRepository.getWards(districtId)
                .subscribe(resource -> {
                    wardsLiveData.setValue(resource);
                }, throwable -> {
                    Timber.e(throwable, "Error loading wards");
                    wardsLiveData.setValue(Resource.error(throwable.getMessage(), null));
                }));
    }

    // Media modifiers
    public void addNewImage(Uri uri) {
        List<Uri> current = new ArrayList<>(newImages.getValue() != null ? newImages.getValue() : new ArrayList<>());
        int oldImageCount = 0;
        if (oldFiles.getValue() != null) {
            for (MultimediaFileDetail file : oldFiles.getValue()) {
                if (file.getFileType() == com.trototvn.trototandroid.data.model.post.FileType.IMAGE) {
                    oldImageCount++;
                }
            }
        }

        if (current.size() + oldImageCount >= 12) {
            Map<String, String> errors = new HashMap<>(formErrors.getValue() != null ? formErrors.getValue() : new HashMap<>());
            errors.put("images", "Chỉ được đăng tối đa 12 hình ảnh");
            formErrors.setValue(errors);
            return;
        }

        current.add(uri);
        newImages.setValue(current);

        // Clear error if any
        Map<String, String> errors = new HashMap<>(formErrors.getValue() != null ? formErrors.getValue() : new HashMap<>());
        errors.remove("images");
        formErrors.setValue(errors);
    }

    public void removeNewImage(int index) {
        List<Uri> current = new ArrayList<>(newImages.getValue() != null ? newImages.getValue() : new ArrayList<>());
        if (index >= 0 && index < current.size()) {
            current.remove(index);
            newImages.setValue(current);
        }
    }

    public void setNewVideo(Uri uri) {
        newVideo.setValue(uri);
    }

    public void removeNewVideo() {
        newVideo.setValue(null);
    }

    public void removeOldFile(int fileId) {
        List<MultimediaFileDetail> current = new ArrayList<>(oldFiles.getValue() != null ? oldFiles.getValue() : new ArrayList<>());
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).getFileId() == fileId) {
                current.remove(i);
                break;
            }
        }
        oldFiles.setValue(current);
    }

    /**
     * Pre-populates all inputs from the existing post details for Sửa Tin (Editing).
     */
    public void loadPostDetailForEdit(int postId) {
        initialLoadState.setValue(Resource.loading(null));
        disposable.add(postRepository.getDetailMyPost(postId)
                .subscribe(resource -> {
                    initialLoadState.setValue(resource);
                    if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                        PostDetail detail = resource.getData();
                        title.setValue(detail.getTitle());
                        description.setValue(detail.getDescription());
                        price.setValue(String.valueOf((long) detail.getPrice()));
                        acreage.setValue(String.valueOf((long) detail.getAcreage()));
                        interiorStatus.setValue(detail.getInteriorCondition());
                        streetNumber.setValue(detail.getStreetNumber());
                        street.setValue(detail.getStreet());
                        oldFiles.setValue(detail.getMultimediaFiles() != null ? detail.getMultimediaFiles() : new ArrayList<>());

                        // Address synchronization
                        syncAddressFromPostDetail(detail);
                    }
                }, throwable -> {
                    Timber.e(throwable, "Error pre-loading post details for edit");
                    initialLoadState.setValue(Resource.error(throwable.getMessage(), null));
                }));
    }

    private void syncAddressFromPostDetail(PostDetail detail) {
        List<City> cities = citiesLiveData.getValue();
        if (cities == null) return;

        City matchedCity = null;
        for (City city : cities) {
            if (city.getName().equalsIgnoreCase(detail.getCity())) {
                matchedCity = city;
                break;
            }
        }

        if (matchedCity != null) {
            selectCity(matchedCity);
            List<District> districts = districtsLiveData.getValue();
            if (districts != null) {
                District matchedDistrict = null;
                for (District d : districts) {
                    if (d.getName().equalsIgnoreCase(detail.getDistrict())) {
                        matchedDistrict = d;
                        break;
                    }
                }
                if (matchedDistrict != null) {
                    selectDistrict(matchedDistrict);

                    // Since ward is loaded dynamically from network, wait for network results to bind Ward
                    final District finalMatchedDistrict = matchedDistrict;
                    disposable.add(postRepository.getWards(finalMatchedDistrict.getId())
                            .subscribe(resource -> {
                                if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                                    List<Ward> wards = resource.getData().getWards();
                                    Ward matchedWard = null;
                                    if (wards != null) {
                                        for (Ward w : wards) {
                                            if (w.getName().equalsIgnoreCase(detail.getWard())) {
                                                matchedWard = w;
                                                break;
                                            }
                                        }
                                    }
                                    if (matchedWard != null) {
                                        selectedWard.setValue(matchedWard);
                                    }
                                }
                            }, throwable -> Timber.e(throwable, "Error matching dynamic ward")));
                }
            }
        }
    }

    /**
     * Validates form and updates formErrors livedata.
     * Returns true if all fields are valid.
     */
    public boolean validateForm() {
        Map<String, String> errors = new HashMap<>();

        // Images check
        int totalImages = (newImages.getValue() != null ? newImages.getValue().size() : 0);
        if (oldFiles.getValue() != null) {
            for (MultimediaFileDetail file : oldFiles.getValue()) {
                if (file.getFileType() == com.trototvn.trototandroid.data.model.post.FileType.IMAGE) {
                    totalImages++;
                }
            }
        }
        if (totalImages == 0) {
            errors.put("images", "Vui lòng tải lên ít nhất 1 hình ảnh");
        }

        // Location Check
        if (selectedCity.getValue() == null || selectedDistrict.getValue() == null ||
                selectedWard.getValue() == null || getEmpty(streetNumber.getValue()).isEmpty() ||
                getEmpty(street.getValue()).isEmpty()) {
            errors.put("address", "Vui lòng chọn và điền địa chỉ đầy đủ");
        }

        // Interior status
        if (getEmpty(interiorStatus.getValue()).isEmpty()) {
            errors.put("interiorStatus", "Vui lòng chọn tình trạng nội thất");
        }

        // Acreage
        String acreageStr = acreage.getValue();
        if (getEmpty(acreageStr).isEmpty()) {
            errors.put("acreage", "Vui lòng nhập diện tích");
        } else {
            try {
                double val = Double.parseDouble(acreageStr);
                if (val <= 0) {
                    errors.put("acreage", "Diện tích phải là số dương");
                }
            } catch (NumberFormatException e) {
                errors.put("acreage", "Diện tích phải là định dạng số");
            }
        }

        // Price
        String priceStr = price.getValue();
        if (getEmpty(priceStr).isEmpty()) {
            errors.put("price", "Vui lòng nhập giá thuê");
        } else {
            try {
                double val = Double.parseDouble(priceStr);
                if (val <= 0) {
                    errors.put("price", "Giá thuê phải là số dương");
                }
            } catch (NumberFormatException e) {
                errors.put("price", "Giá thuê phải là định dạng số");
            }
        }

        // Title
        String titleStr = title.getValue();
        if (getEmpty(titleStr).isEmpty()) {
            errors.put("title", "Vui lòng nhập tiêu đề tin đăng");
        } else if (titleStr.length() > 70) {
            errors.put("title", "Tiêu đề không được vượt quá 70 ký tự");
        }

        // Description
        String descStr = description.getValue();
        if (getEmpty(descStr).isEmpty()) {
            errors.put("description", "Vui lòng nhập mô tả chi tiết");
        } else if (descStr.length() > 1000) {
            errors.put("description", "Mô tả không được vượt quá 1000 ký tự");
        }

        formErrors.setValue(errors);
        return errors.isEmpty();
    }

    private String getEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private RequestBody toTextRequestBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value != null ? value : "");
    }

    /**
     * Executes create post multi-part API call.
     */
    public void submitCreatePost(List<MultipartBody.Part> images, MultipartBody.Part video) {
        if (!validateForm()) {
            submitTransactionState.setValue(Resource.error("Vui lòng sửa các lỗi nhập liệu", null));
            return;
        }

        submitTransactionState.setValue(Resource.loading(null));

        RequestBody reqTitle = toTextRequestBody(title.getValue());
        RequestBody reqDesc = toTextRequestBody(description.getValue());
        RequestBody reqPrice = toTextRequestBody(price.getValue());
        RequestBody reqAcreage = toTextRequestBody(acreage.getValue());
        RequestBody reqStreetNumber = toTextRequestBody(streetNumber.getValue());
        RequestBody reqStreet = toTextRequestBody(street.getValue());
        RequestBody reqWard = toTextRequestBody(selectedWard.getValue() != null ? selectedWard.getValue().getName() : "");
        RequestBody reqDistrict = toTextRequestBody(selectedDistrict.getValue() != null ? selectedDistrict.getValue().getName() : "");
        RequestBody reqCity = toTextRequestBody(selectedCity.getValue() != null ? selectedCity.getValue().getName() : "");
        RequestBody reqInterior = toTextRequestBody(interiorStatus.getValue());

        disposable.add(postRepository.createPost(
                reqTitle, reqDesc, reqPrice, reqAcreage, reqStreetNumber, reqStreet,
                reqWard, reqDistrict, reqCity, reqInterior, images, video)
                .subscribe(resource -> {
                    submitTransactionState.setValue(resource);
                }, throwable -> {
                    Timber.e(throwable, "Error creating post");
                    submitTransactionState.setValue(Resource.error(throwable.getMessage(), null));
                }));
    }

    /**
     * Executes edit post multi-part API call.
     */
    public void submitEditPost(int postId, List<MultipartBody.Part> images, MultipartBody.Part video) {
        if (!validateForm()) {
            submitTransactionState.setValue(Resource.error("Vui lòng sửa các lỗi nhập liệu", null));
            return;
        }

        submitTransactionState.setValue(Resource.loading(null));

        RequestBody reqTitle = toTextRequestBody(title.getValue());
        RequestBody reqDesc = toTextRequestBody(description.getValue());
        RequestBody reqPrice = toTextRequestBody(price.getValue());
        RequestBody reqAcreage = toTextRequestBody(acreage.getValue());
        RequestBody reqStreetNumber = toTextRequestBody(streetNumber.getValue());
        RequestBody reqStreet = toTextRequestBody(street.getValue());
        RequestBody reqWard = toTextRequestBody(selectedWard.getValue() != null ? selectedWard.getValue().getName() : "");
        RequestBody reqDistrict = toTextRequestBody(selectedDistrict.getValue() != null ? selectedDistrict.getValue().getName() : "");
        RequestBody reqCity = toTextRequestBody(selectedCity.getValue() != null ? selectedCity.getValue().getName() : "");
        RequestBody reqInterior = toTextRequestBody(interiorStatus.getValue());

        // Construct oldFiles list JSON
        List<Integer> oldIds = new ArrayList<>();
        if (oldFiles.getValue() != null) {
            for (MultimediaFileDetail file : oldFiles.getValue()) {
                oldIds.add(file.getFileId());
            }
        }
        String oldFilesJson = new com.google.gson.Gson().toJson(oldIds);
        RequestBody reqOldFiles = toTextRequestBody(oldFilesJson);

        disposable.add(postRepository.editPost(
                postId, reqTitle, reqDesc, reqPrice, reqAcreage, reqStreetNumber, reqStreet,
                reqWard, reqDistrict, reqCity, reqInterior, reqOldFiles, images, video)
                .subscribe(resource -> {
                    submitTransactionState.setValue(resource);
                }, throwable -> {
                    Timber.e(throwable, "Error editing post");
                    submitTransactionState.setValue(Resource.error(throwable.getMessage(), null));
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
