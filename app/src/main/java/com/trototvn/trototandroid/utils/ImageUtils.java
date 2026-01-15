package com.trototvn.trototandroid.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.trototvn.trototandroid.R;

/**
 * Image loading utilities using Glide
 */
public class ImageUtils {

    /**
     * Load image from URL with default placeholder
     */
    public static void loadImage(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /**
     * Load image with custom placeholder
     */
    public static void loadImage(Context context, String url, ImageView imageView,
                                  @DrawableRes int placeholder) {
        Glide.with(context)
                .load(url)
                .placeholder(placeholder)
                .error(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /**
     * Load circular image (for avatars)
     */
    public static void loadCircularImage(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /**
     * Load thumbnail image (smaller size for lists)
     */
    public static void loadThumbnail(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .thumbnail(0.25f)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /**
     * Load image with specific size
     */
    public static void loadImageWithSize(Context context, String url, ImageView imageView,
                                         int width, int height) {
        Glide.with(context)
                .load(url)
                .override(width, height)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /**
     * Clear image from ImageView
     */
    public static void clearImage(Context context, ImageView imageView) {
        Glide.with(context).clear(imageView);
    }

    /**
     * Preload image (for better performance)
     */
    public static void preloadImage(Context context, String url) {
        Glide.with(context)
                .load(url)
                .preload();
    }
}
