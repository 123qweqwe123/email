package com.bdcor.services.mail.exception;

/**
 * Description:
 * Author: huangrupeng
 * Create: 17/5/4 下午4:49
 */
public class ServiceException extends RuntimeException{

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
