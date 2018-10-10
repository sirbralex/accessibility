package com.accessibility.testapp.ui.helper.imageloader;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.accessibility.logger.Logger;
import com.accessibility.logger.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Aleksandr Brazhkin
 */
public class ImageDownloader {

    private static final Logger logger = LoggerFactory.getLogger(ImageDownloader.class);

    private final Cache cache;

    public ImageDownloader(Cache cache) {
        this.cache = cache;
    }

    @NonNull
    InputStream downloadImage(Uri uri) throws IOException {
        logger.trace("downloadImage, uri: " + uri);
        String urlStr = uri.toString();
        try {
            InputStream cachedIs = cache.getEntry(urlStr);
            if (cachedIs != null) {
                return cachedIs;
            }
        } catch (Exception e) {
            logger.error("uri: " + uri, e);
            throw e;
        }
        HttpURLConnection urlConnection = null;
        InputStream is = null;
        try {
            URL url = new URL(uri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(urlConnection.getInputStream());
            cache.saveEntry(urlStr, is);
            InputStream cachedIs = cache.getEntry(urlStr);
            if (cachedIs != null) {
                return cachedIs;
            } else {
                throw new FileNotFoundException();
            }
        } catch (Exception e) {
            logger.error("uri: " + uri, e);
            cache.deleteEntry(urlStr);
            throw e;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    logger.error("uri: " + uri, e);
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
