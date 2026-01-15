package com.trototvn.trototandroid.data.model;

/**
 * Generic API response wrapper
 * @param <T> Data type
 */
public class ResponseData<T> {
    private String message;
    private T data;
    private Object error; // Can be String or array of validation errors

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }
}
