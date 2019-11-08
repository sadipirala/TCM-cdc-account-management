package com.thermofisher.cdcam.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

/**
 * CustomAsyncExceptionHandler
 */
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    static final Logger logger = LogManager.getLogger("CdcamApp");

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String exceptionAsString = sw.toString();
        logger.fatal(exceptionAsString);
        logger.fatal("Exception message - " + throwable.getMessage());
        logger.fatal("Method name - " + method.getName());
        for(final Object param : obj) {
            logger.fatal("Param - " + param);
        }   
    }
}