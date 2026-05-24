package com.library.book.dto;

import lombok.Data;
import java.util.List;

@Data
public class PageResponse<T> {
    private Long total;
    private List<T> records;

    public static <T> PageResponse<T> ok(Long total, List<T> records) {
        PageResponse<T> response = new PageResponse<>();
        response.setTotal(total);
        response.setRecords(records);
        return response;
    }
}
