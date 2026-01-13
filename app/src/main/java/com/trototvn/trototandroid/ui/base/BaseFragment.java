package com.trototvn.trototandroid.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Base Fragment with ViewBinding support
 * 
 * @param <VB> ViewBinding type
 */
public abstract class BaseFragment<VB extends ViewBinding> extends Fragment {

    protected VB binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = getViewBinding(inflater, container);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        observeData();
    }

    /**
     * Initialize views, set listeners, etc.
     */
    protected abstract void setupViews();

    /**
     * Observe LiveData/ViewModel data
     */
    protected abstract void observeData();

    /**
     * Automatically inflate ViewBinding using reflection
     */
    @SuppressWarnings("unchecked")
    private VB getViewBinding(LayoutInflater inflater, ViewGroup container) {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            try {
                Class<VB> bindingClass = (Class<VB>) ((ParameterizedType) type).getActualTypeArguments()[0];
                Method method = bindingClass.getMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
                return (VB) method.invoke(null, inflater, container, false);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to inflate ViewBinding", e);
            }
        }
        throw new RuntimeException("Unable to get ViewBinding class");
    }

    /**
     * Show toast message
     */
    protected void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show loading state (override in child classes)
     */
    protected void showLoading(boolean isLoading) {
        // Override this in child fragments to show/hide loading
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
