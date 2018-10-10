package com.accessibility.testapp.ui.helper.imageloader;

import android.net.Uri;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.widget.ImageView;

/**
 * Builder for {@link ImageRequest}.
 *
 * @author Aleksandr Brazhkin
 */
public class ImageRequestBuilder {

    private final ImageLoader imageLoader;
    private final ImageDownloader imageDownloader;
    private final Handler uiThread;
    private final Uri uri;
    private int placeholderResId;
    private int errorResId;
    private int targetWidth;
    private int targetHeight;

    ImageRequestBuilder(@NonNull ImageLoader imageLoader,
                        @NonNull ImageDownloader imageDownloader,
                        @NonNull Handler uiThread,
                        @NonNull Uri uri) {
        this.imageLoader = imageLoader;
        this.imageDownloader = imageDownloader;
        this.uiThread = uiThread;
        this.uri = uri;
    }

    @NonNull
    public ImageRequestBuilder placeholder(@DrawableRes int placeholderResId) {
        this.placeholderResId = placeholderResId;
        return this;
    }

    @NonNull
    public ImageRequestBuilder error(@DrawableRes int errorResId) {
        this.errorResId = errorResId;
        return this;
    }

    public ImageRequestBuilder resize(int targetWidth, int targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        return this;
    }

    @NonNull
    public ImageRequest into(@NonNull ImageView imageView) {
        ImageRequest imageRequest = new ImageRequest(
                uri,
                imageDownloader,
                uiThread,
                imageView,
                placeholderResId,
                errorResId,
                targetWidth,
                targetHeight
        );
        imageLoader.onRequestCreated(imageRequest);
        return imageRequest;
    }
}
