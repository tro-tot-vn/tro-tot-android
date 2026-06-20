package com.trototvn.trototandroid.ui.main.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomePagerAdapter extends FragmentStateAdapter {

    private final boolean isLoggedIn;

    public HomePagerAdapter(@NonNull Fragment fragment, boolean isLoggedIn) {
        super(fragment);
        this.isLoggedIn = isLoggedIn;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new LatestPostsFragment();
        } else {
            return new RecommendedPostsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return isLoggedIn ? 2 : 1;
    }
}
