package com.accessibility.logger;

import android.util.Log;

/**
 * Логгер.
 *
 * @author Aleksandr Brazhkin
 */
class LoggerImpl implements Logger {

    private static final int MAX_LOG_TAG_LENGTH = 23;

    private final String name;

    LoggerImpl(String name) {
        this.name = getEffectiveName(name);
    }

    @Override
    public void trace(String msg) {
        trace(msg, null);
    }

    @Override
    public void trace(String msg, Throwable t) {
//        if (BuildConfig.DEBUG && Log.isLoggable(name, Log.VERBOSE)) {
        String fullMessage = getMessageForLog(msg, t);
        Log.v(name, fullMessage);
//        }
    }

    @Override
    public void debug(String msg) {
        debug(msg, null);
    }

    @Override
    public void debug(String msg, Throwable t) {
//        if (BuildConfig.DEBUG || Log.isLoggable(name, Log.DEBUG)) {
        String fullMessage = getMessageForLog(msg, t);
        Log.d(name, fullMessage);
//        }
    }

    @Override
    public void info(String msg) {
        info(msg, null);
    }

    @Override
    public void info(String msg, Throwable t) {
//        if (BuildConfig.DEBUG || Log.isLoggable(name, Log.INFO)) {
        String fullMessage = getMessageForLog(msg, t);
        Log.i(name, fullMessage);
//        }
    }

    @Override
    public void warn(String msg) {
        warn(msg, null);
    }

    @Override
    public void warn(String msg, Throwable t) {
//        if (BuildConfig.DEBUG || Log.isLoggable(name, Log.WARN)) {
        String fullMessage = getMessageForLog(msg, t);
        Log.w(name, fullMessage);
//        }
    }

    @Override
    public void error(String msg) {
        error(msg, null);
    }

    @Override
    public void error(String msg, Throwable t) {
//        if (BuildConfig.DEBUG || Log.isLoggable(name, Log.ERROR)) {
        String fullMessage = getMessageForLog(msg, t);
        Log.e(name, fullMessage);
//        }
    }

    @Override
    public void error(Throwable t) {
        error("", t);
    }

    private String getEffectiveName(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH) {
            return str.substring(0, MAX_LOG_TAG_LENGTH - 1);
        }
        return str;
    }

    private String getMessageForLog(String message, Throwable throwable) {
        String out = message;
        if (throwable != null)
            out = message + '\n' + Log.getStackTraceString(throwable);
        return out;
    }
}
