package com.trototvn.trototandroid.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Base Activity with ViewBinding support
 * 
 * @param <VB> ViewBinding type
 */
public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {

    protected VB binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = getViewBinding();
        setContentView(binding.getRoot());
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
    private VB getViewBinding() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            try {
                Class<VB> bindingClass = (Class<VB>) ((ParameterizedType) type).getActualTypeArguments()[0];
                Method method = bindingClass.getMethod("inflate", LayoutInflater.class);
                return (VB) method.invoke(null, getLayoutInflater());
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show loading state (override in child classes)
     */
    protected void showLoading(boolean isLoading) {
        // Override this in child activities to show/hide loading dialog
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
