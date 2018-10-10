package com.accessibility.logger;

/**
 * Логгер.
 *
 * @author Aleksandr Brazhkin
 */
public interface Logger {
    void trace(String msg);

    void trace(String msg, Throwable t);

    void debug(String msg);

    void debug(String msg, Throwable t);

    void info(String msg);

    void info(String msg, Throwable t);

    void warn(String msg);

    void warn(String msg, Throwable t);

    void error(String msg);

    void error(String msg, Throwable t);

    void error(Throwable t);
}
