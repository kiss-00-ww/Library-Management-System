package com.library.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Response<T> ok(T data) {
        return new Response<>(200, "success", data);
    }

    public static <T> Response<T> fail(String message) {
        return new Response<>(400, message, null);
    }
}
