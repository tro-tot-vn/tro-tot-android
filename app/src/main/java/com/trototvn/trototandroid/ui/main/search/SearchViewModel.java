package com.trototvn.trototandroid.ui.main.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.Post;
import com.trototvn.trototandroid.data.model.search.SearchParams;
import com.trototvn.trototandroid.data.model.search.SearchResponse;
import com.trototvn.trototandroid.data.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import timber.log.Timber;

/**
 * ViewModel for Search screen
 * Manages search query, filters, and pagination state
 */
@HiltViewModel
public class SearchViewModel extends ViewModel {

    private final PostRepository postRepository;
    private final CompositeDisposable disposable = new CompositeDisposable();

    // Search state
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<Resource<List<Post>>> searchResults = new MutableLiveData<>();
    private Integer searchLogId;

    // Filter state
    private final MutableLiveData<String> selectedCity = new MutableLiveData<>();
    private final MutableLiveData<String> selectedDistrict = new MutableLiveData<>();
    private final MutableLiveData<String> selectedWard = new MutableLiveData<>();
    private final MutableLiveData<Integer> priceMin = new MutableLiveData<>();
    private final MutableLiveData<Integer> priceMax = new MutableLiveData<>();
    private final MutableLiveData<Integer> acreageMin = new MutableLiveData<>();
    private final MutableLiveData<Integer> acreageMax = new MutableLiveData<>();
    private final MutableLiveData<String> interiorCondition = new MutableLiveData<>();

    // Pagination state
    private int currentPage = 0;
    private int totalPages = 0;
    private final MutableLiveData<Boolean> hasMorePages = new MutableLiveData<>(false);
    private final List<Post> allResults = new ArrayList<>();

    @Inject
    public SearchViewModel(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // ========== Getters ==========

    public LiveData<String> getQuery() {
        return query;
    }

    public LiveData<Resource<List<Post>>> getSearchResults() {
        return searchResults;
    }

    public LiveData<Boolean> getHasMorePages() {
        return hasMorePages;
    }

    public LiveData<String> getSelectedCity() {
        return selectedCity;
    }

    public LiveData<String> getSelectedDistrict() {
        return selectedDistrict;
    }

    public LiveData<Integer> getPriceMin() {
        return priceMin;
    }

    public LiveData<Integer> getPriceMax() {
        return priceMax;
    }

    public LiveData<Integer> getAcreageMin() {
        return acreageMin;
    }

    public LiveData<Integer> getAcreageMax() {
        return acreageMax;
    }

    public Integer getSearchLogId() {
        return searchLogId;
    }

    public LiveData<String> getInteriorCondition() {
        return interiorCondition;
    }

    // ========== Setters ==========

    public void setQuery(String query) {
        this.query.setValue(query);
    }

    public void setCity(String city) {
        this.selectedCity.setValue(city);
    }

    public void setDistrict(String district) {
        this.selectedDistrict.setValue(district);
    }

    public void setWard(String ward) {
        this.selectedWard.setValue(ward);
    }

    public void setPriceRange(Integer min, Integer max) {
        this.priceMin.setValue(min);
        this.priceMax.setValue(max);
    }

    public void setAcreageRange(Integer min, Integer max) {
        this.acreageMin.setValue(min);
        this.acreageMax.setValue(max);
    }

    public void setInteriorCondition(String condition) {
        this.interiorCondition.setValue(condition);
    }

    // ========== Actions ==========

    /**
     * Execute search with current filters (resets to page 1)
     */
    public void search() {
        String currentQuery = query.getValue();
        if (currentQuery == null || currentQuery.trim().isEmpty()) {
            searchResults.setValue(Resource.error("Vui lòng nhập từ khóa tìm kiếm", null));
            return;
        }

        // Reset pagination
        currentPage = 0;
        allResults.clear();
        searchLogId = null;

        executeSearch(1);
    }

    /**
     * Load next page of results
     */
    public void loadNextPage() {
        if (currentPage < totalPages) {
            executeSearch(currentPage + 1);
        }
    }

    /**
     * Clear all filters
     */
    public void clearFilters() {
        selectedCity.setValue(null);
        selectedDistrict.setValue(null);
        selectedWard.setValue(null);
        priceMin.setValue(null);
        priceMax.setValue(null);
        acreageMin.setValue(null);
        acreageMax.setValue(null);
        interiorCondition.setValue(null);
    }

    /**
     * Log a click on a search result
     */
    public void logClick(int postId) {
        if (searchLogId != null) {
            disposable.add(
                    postRepository.logSearchClick(searchLogId, postId)
                            .subscribe(
                                    resource -> Timber.d("Click logged successfully"),
                                    error -> Timber.e(error, "Failed to log click")));
        }
    }

    /**
     * Check if any filters are applied
     */
    public boolean hasActiveFilters() {
        return selectedCity.getValue() != null ||
                selectedDistrict.getValue() != null ||
                selectedWard.getValue() != null ||
                priceMin.getValue() != null ||
                priceMax.getValue() != null ||
                acreageMin.getValue() != null ||
                acreageMax.getValue() != null ||
                interiorCondition.getValue() != null;
    }

    private void executeSearch(int page) {
        if (page == 1) {
            searchResults.setValue(Resource.loading(null));
        }

        SearchParams params = buildSearchParams(page);

        disposable.add(
                postRepository.search(params)
                        .subscribe(
                                resource -> {
                                    if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                                        SearchResponse response = resource.getData();

                                        // Update pagination state
                                        currentPage = page;
                                        if (response.getPagination() != null) {
                                            totalPages = response.getPagination().getTotalPages();
                                            hasMorePages.setValue(response.getPagination().hasNextPage());
                                        }

                                        // Update search log ID
                                        if (response.getSearchLogId() != null) {
                                            searchLogId = response.getSearchLogId();
                                        }

                                        // Append or set results
                                        if (page == 1) {
                                            allResults.clear();
                                        }

                                        if (response.getData() != null) {
                                            allResults.addAll(response.getData());
                                        }

                                        searchResults.setValue(Resource.success(new ArrayList<>(allResults)));
                                    } else {
                                        if (page == 1) {
                                            searchResults.setValue(Resource.error("Không tìm thấy kết quả", null));
                                        }
                                        hasMorePages.setValue(false);
                                    }
                                },
                                error -> {
                                    Timber.e(error, "Search error");
                                    if (page == 1) {
                                        searchResults.setValue(Resource.error(
                                                error.getMessage() != null ? error.getMessage() : "Lỗi tìm kiếm",
                                                null));
                                    }
                                    hasMorePages.setValue(false);
                                }));
    }

    private SearchParams buildSearchParams(int page) {
        SearchParams params = new SearchParams(query.getValue());
        params.setPage(page);
        params.setPageSize(20);

        // Apply filters
        if (selectedCity.getValue() != null) {
            params.setCity(selectedCity.getValue());
        }
        if (selectedDistrict.getValue() != null) {
            params.setDistrict(selectedDistrict.getValue());
        }
        if (selectedWard.getValue() != null) {
            params.setWard(selectedWard.getValue());
        }
        if (priceMin.getValue() != null) {
            params.setPriceMin(priceMin.getValue());
        }
        if (priceMax.getValue() != null) {
            params.setPriceMax(priceMax.getValue());
        }
        if (acreageMin.getValue() != null) {
            params.setAcreageMin(acreageMin.getValue());
        }
        if (acreageMax.getValue() != null) {
            params.setAcreageMax(acreageMax.getValue());
        }
        if (interiorCondition.getValue() != null) {
            params.setInteriorCondition(interiorCondition.getValue());
        }

        return params;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
