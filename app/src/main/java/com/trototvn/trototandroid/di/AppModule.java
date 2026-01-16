package com.trototvn.trototandroid.di;

import android.content.Context;

import com.trototvn.trototandroid.data.repository.PostRepository;
import com.trototvn.trototandroid.data.repository.PostRepositoryImpl;
import com.trototvn.trototandroid.data.repository.PostDetailRepository;
import com.trototvn.trototandroid.data.repository.PostDetailRepositoryImpl;
import com.trototvn.trototandroid.data.repository.ProfileRepository;
import com.trototvn.trototandroid.data.repository.ProfileRepositoryImpl;
import com.trototvn.trototandroid.data.repository.RatingRepository;
import com.trototvn.trototandroid.data.repository.RatingRepositoryImpl;
import com.trototvn.trototandroid.data.repository.SavedPostRepository;
import com.trototvn.trototandroid.data.repository.SavedPostRepositoryImpl;
import com.trototvn.trototandroid.utils.SessionManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public SessionManager provideSessionManager(@ApplicationContext Context context) {
        return new SessionManager(context);
    }

    @Provides
    @Singleton
    public PostRepository providePostRepository(PostRepositoryImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public ProfileRepository provideProfileRepository(ProfileRepositoryImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public PostDetailRepository providePostDetailRepository(PostDetailRepositoryImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public RatingRepository provideRatingRepository(RatingRepositoryImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public SavedPostRepository provideSavedPostRepository(SavedPostRepositoryImpl impl) {
        return impl;
    }
}
