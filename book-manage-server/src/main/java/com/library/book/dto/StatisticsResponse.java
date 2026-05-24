package com.library.book.dto;

import com.library.book.entity.Book;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class StatisticsResponse {
    private Long totalBooks;
    private Long availableBooks;
    private Long borrowedBooks;
    private Long overdueCount;
    private Long totalUsers;
    private Long activeUsers;
    private Map<String, Long> categoryStats;
    private List<Book> popularBooks;
    private Map<Long, Long> popularBookBorrowCounts;
}
