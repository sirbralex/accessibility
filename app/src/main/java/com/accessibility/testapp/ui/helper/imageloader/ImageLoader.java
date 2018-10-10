package com.accessibility.testapp.ui.helper.imageloader;

import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.accessibility.logger.Logger;
import com.accessibility.logger.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class for image loading.
 *
 * @author Aleksandr Brazhkin
 */
public class ImageLoader {

    private static final Logger logger = LoggerFactory.getLogger(ImageDownloader.class);

    private final ImageDownloader imageDownloader;
    private final Handler uiThread;
    private final Map<ImageView, Future> requests = new HashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public ImageLoader(ImageDownloader imageDownloader, Handler uiThread) {
        this.imageDownloader = imageDownloader;
        this.uiThread = uiThread;
    }

    /**
     * Starts image loading on path {@code path}
     *
     * @param path Image path
     */
    @NonNull
    public ImageRequestBuilder load(@NonNull String path) {
        return load(Uri.parse(path));
    }

    /**
     * Starts image loading on path {@code path}
     *
     * @param uri Image uri
     */
    @NonNull
    public ImageRequestBuilder load(@NonNull Uri uri) {
        return new ImageRequestBuilder(this, imageDownloader, uiThread, uri);
    }

    void onRequestCreated(@NonNull ImageRequest request) {
        Future oldFuture = requests.get(request.getImageView());
        if (oldFuture != null) {
            oldFuture.cancel(true);
        }
        Future future = executor.submit(() -> {
            request.start();
            if (!request.isCanceled()) {
                requests.remove(request.getImageView());
            }
        });
        requests.put(request.getImageView(), future);
    }

    public void cancel() {
        logger.trace("cancel");
        for (Map.Entry<ImageView, Future> entry : requests.entrySet()) {
            entry.getValue().cancel(true);
        }
        requests.clear();
    }

    public void cancelRequest(ImageView imageView) {
        logger.trace("cancelRequest");
        Future oldFuture = requests.get(imageView);
        if (oldFuture != null) {
            oldFuture.cancel(true);
        }
    }
}
