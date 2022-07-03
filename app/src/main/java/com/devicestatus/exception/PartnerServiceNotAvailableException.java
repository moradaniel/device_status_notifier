package com.devicestatus.exception;

public final class PartnerServiceNotAvailableException extends Exception {
    public PartnerServiceNotAvailableException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
    public PartnerServiceNotAvailableException(final String msg) {
        super(msg);
    }
}