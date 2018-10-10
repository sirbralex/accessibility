package com.accessibility.testapp.ui.helper.imageloader;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.accessibility.logger.Logger;
import com.accessibility.logger.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Cache implementation.
 * It's just very simple solution without
 * synchronization blocks and cache size management.
 *
 * @author Aleksandr Brazhkin
 */
public class Cache {

    private static final Logger logger = LoggerFactory.getLogger(ImageDownloader.class);

    private static final String INTERNAL_CACHE_DIR = "image_loader";
    private static final String EXTERNAL_CACHE_DIR = "Documents/TestPics";
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private final File internalCacheDir;
    private final File externalCacheDir;
    private final MessageDigest digest;

    public Cache(Context context) {
        this.internalCacheDir = new File(context.getCacheDir(), INTERNAL_CACHE_DIR);
        this.externalCacheDir = new File(Environment.getExternalStorageDirectory(), EXTERNAL_CACHE_DIR);

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
        }
        this.digest = digest;
    }

    private File getCacheDir() {
        if (externalCacheDir.canWrite()) {
            return externalCacheDir;
        } else {
            return internalCacheDir;
        }
    }

    @Nullable
    InputStream getEntry(@NonNull String key) throws IOException {
        File file = new File(getCacheDir(), getHash(key));
        return file.exists() ? new FileInputStream(file) : null;
    }

    void saveEntry(@NonNull String key, @NonNull InputStream is) throws IOException {
        File cacheDir = getCacheDir();
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                logger.error("Could not create dir: " + cacheDir);
            }
        }
        File file = new File(cacheDir, getHash(key));
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedIOException();
                }
                os.write(buffer, 0, read);
            }
            os.flush();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }

    void deleteEntry(@NonNull String key) {
        File file = new File(getCacheDir(), getHash(key));
        if (file.exists()) {
            if (!file.delete()) {
                logger.error("Could not delete file: " + file);
            }
        }
    }

    @NonNull
    private String getHash(@NonNull String key) {
        digest.update(key.getBytes());
        return bytesToHex(digest.digest());
    }

    private static String bytesToHex(byte[] bytes) {
        if (bytes == null)
            return "";

        char[] hexChars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            int index = i * 2;

            hexChars[index++] = HEX_ARRAY[value >>> 4];
            hexChars[index] = HEX_ARRAY[value & 0x0F];
        }

        return new String(hexChars);
    }

}
