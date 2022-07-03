package com.devicestatus.web.payload;

import java.util.List;

public class ApiResponse<T> {

    private List<String> errors;
    private T response;

    public ApiResponse() {
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

}
