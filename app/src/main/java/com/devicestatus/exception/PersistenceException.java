package com.devicestatus.exception;

public final class PersistenceException extends Exception {
    public PersistenceException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
    public PersistenceException(final String msg) {
        super(msg);
    }
}