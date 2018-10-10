package com.accessibility.testapp.ui.helper.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.accessibility.logger.Logger;
import com.accessibility.logger.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Image loading request.
 *
 * @author Aleksandr Brazhkin
 */
public class ImageRequest {

    private static final Logger logger = LoggerFactory.getLogger(ImageRequest.class);

    private final Uri uri;
    private final ImageView imageView;
    private final ImageDownloader imageDownloader;
    private final Handler uiThread;
    private final int placeholderResId;
    private final int errorResId;
    private int targetWidth;
    private int targetHeight;

    ImageRequest(Uri uri,
                 ImageDownloader imageDownloader,
                 Handler uiThread,
                 ImageView imageView,
                 @DrawableRes int placeholderResId,
                 @DrawableRes int errorResId,
                 int targetWidth,
                 int targetHeight) {
        this.uri = uri;
        this.imageView = imageView;
        this.imageDownloader = imageDownloader;
        this.uiThread = uiThread;
        this.placeholderResId = placeholderResId;
        this.errorResId = errorResId;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    public ImageView getImageView() {
        return imageView;
    }

    void start() {
        InputStream is = null;
        try {
            if (isCanceled()) return;
            uiThread.post(() -> imageView.setImageResource(placeholderResId));
            is = imageDownloader.downloadImage(uri);

            if (isCanceled()) return;
            // Calculate inSampleSize
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);

            try {
                is.close();
            } catch (IOException e) {
                logger.error(e);
                is = null;
            }

            if (isCanceled()) return;
            is = imageDownloader.downloadImage(uri);

            if (isCanceled()) return;
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);

            if (isCanceled()) return;
            Bitmap croppedBitmap = ThumbnailUtils.extractThumbnail(bitmap, targetWidth, targetHeight);

            if (isCanceled()) return;
            uiThread.post(() -> imageView.setImageBitmap(croppedBitmap));
        } catch (IOException e) {
            logger.error(e);
            if (isCanceled()) return;
            uiThread.post(() -> imageView.setImageResource(errorResId));
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    boolean isCanceled() {
        return Thread.currentThread().isInterrupted();
    }
}
