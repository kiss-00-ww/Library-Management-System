package com.library.book.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.book.dto.PageResponse;
import com.library.book.dto.StatisticsResponse;
import com.library.book.entity.BorrowRecord;

import java.util.List;
import java.util.Map;

public interface BorrowRecordService extends com.baomidou.mybatisplus.extension.service.IService<BorrowRecord> {
    BorrowRecord borrowBook(Long userId, Long bookId);
    BorrowRecord returnBook(Long recordId, Long operatorId, String operatorRole);
    BorrowRecord renewBook(Long recordId, Long userId);
    PageResponse<BorrowRecord> getMyBorrows(Long userId, String status, Integer page, Integer size);
    PageResponse<BorrowRecord> getAllBorrows(String status, Integer page, Integer size);
    List<BorrowRecord> getOverdueRecords();
    StatisticsResponse getStatistics();
    StatisticsResponse getPopularBooksStatistics();
    Map<String, Long> getBorrowTrend(int days);
    Map<String, Long> getBorrowCountsByUser(Long userId);
    BorrowRecord getActiveBorrowByUserAndBook(Long userId, Long bookId);
    List<Long> getActiveBorrowedBookIdsByUser(Long userId);
    List<Map<String, Object>> getBorrowCountByBook();
}
