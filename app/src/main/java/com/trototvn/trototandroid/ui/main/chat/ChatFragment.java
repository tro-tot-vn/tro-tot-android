package com.trototvn.trototandroid.ui.main.chat;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trototvn.trototandroid.databinding.FragmentChatBinding;
import com.trototvn.trototandroid.ui.base.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * ChatFragment - Placeholder for chat feature
 */
@AndroidEntryPoint
public class ChatFragment extends BaseFragment<FragmentChatBinding> {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialization logic if needed
    }

    @Override
    protected void setupViews() {
        // Setup initial UI state
    }

    @Override
    protected void observeData() {
        // Observe ViewModel if added later
    }
}
