package com.library.book.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.book.dto.*;
import com.library.book.entity.Book;
import com.library.book.entity.BorrowRecord;
import com.library.book.entity.Category;
import com.library.book.entity.User;
import com.library.book.service.BookService;
import com.library.book.service.BorrowRecordService;
import com.library.book.service.CategoryService;
import com.library.book.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "Report Export")
@RestController
@RequestMapping("/api/admin/report")
public class ReportExportController {

    @Autowired
    private BorrowRecordService borrowRecordService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("导出报表")
    public void exportReport(
            @RequestParam String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) throws IOException {

        String fileName = URLEncoder.encode(getReportName(type), "UTF-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        switch (type) {
            case "borrow":
                exportBorrowRecords(startDate, endDate, response);
                break;
            case "popular":
                exportPopularBooks(response);
                break;
            case "overdue":
                exportOverdueRecords(response);
                break;
            case "circulation":
                exportBookCirculation(response);
                break;
            default:
                throw new IllegalArgumentException("不支持的报表类型: " + type);
        }
    }

    private String getReportName(String type) {
        switch (type) {
            case "borrow": return "借阅记录报表";
            case "popular": return "热门图书排行";
            case "overdue": return "逾期记录报表";
            case "circulation": return "图书流通率报表";
            default: return "报表";
        }
    }

    private void exportBorrowRecords(String startDate, String endDate, HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(BorrowRecord::getBorrowDate, LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(BorrowRecord::getBorrowDate, LocalDateTime.parse(endDate + "T23:59:59"));
        }
        wrapper.orderByDesc(BorrowRecord::getBorrowDate);

        List<BorrowRecord> records = borrowRecordService.list(wrapper);
        fillRecordDetails(records);

        List<BorrowRecordExportDTO> exportList = records.stream().map(r -> {
            BorrowRecordExportDTO dto = new BorrowRecordExportDTO();
            dto.setId(r.getId());
            dto.setUsername(r.getUser() != null ? r.getUser().getUsername() : "");
            dto.setRealName(r.getUser() != null ? r.getUser().getRealName() : "");
            dto.setBookTitle(r.getBook() != null ? r.getBook().getTitle() : "");
            dto.setBookAuthor(r.getBook() != null ? r.getBook().getAuthor() : "");
            dto.setIsbn(r.getBook() != null ? r.getBook().getIsbn() : "");
            dto.setBorrowDate(r.getBorrowDate() != null ? r.getBorrowDate().format(FORMATTER) : "");
            dto.setDueDate(r.getDueDate() != null ? r.getDueDate().format(FORMATTER) : "");
            dto.setReturnDate(r.getReturnDate() != null ? r.getReturnDate().format(FORMATTER) : "");
            dto.setStatus(translateStatus(r.getStatus()));
            dto.setRenewCount(r.getRenewCount());
            dto.setFineAmount(r.getFineAmount() != null ? r.getFineAmount().toString() : "0");
            return dto;
        }).collect(Collectors.toList());

        EasyExcel.write(response.getOutputStream(), BorrowRecordExportDTO.class)
                .sheet("借阅记录")
                .doWrite(exportList);
    }

    private void exportPopularBooks(HttpServletResponse response) throws IOException {
        List<Book> popularBooks = bookService.getPopularBooks(50);
        // 使用借阅记录统计历史总借阅次数
        List<Map<String, Object>> borrowCountRows = borrowRecordService.getBorrowCountByBook();
        Map<Long, Long> borrowCountMap = new java.util.HashMap<>();
        for (Map<String, Object> row : borrowCountRows) {
            Long bookId = ((Number) row.get("book_id")).longValue();
            Long cnt = ((Number) row.get("cnt")).longValue();
            borrowCountMap.put(bookId, cnt);
        }

        List<PopularBookExportDTO> exportList = new ArrayList<>();
        for (int i = 0; i < popularBooks.size(); i++) {
            Book book = popularBooks.get(i);
            PopularBookExportDTO dto = new PopularBookExportDTO();
            dto.setRank(i + 1);
            dto.setTitle(book.getTitle());
            dto.setAuthor(book.getAuthor());
            dto.setIsbn(book.getIsbn());
            dto.setBorrowCount(borrowCountMap.getOrDefault(book.getId(), 0L).intValue());
            exportList.add(dto);
        }

        EasyExcel.write(response.getOutputStream(), PopularBookExportDTO.class)
                .sheet("热门图书")
                .doWrite(exportList);
    }

    private void exportOverdueRecords(HttpServletResponse response) throws IOException {
        List<BorrowRecord> records = borrowRecordService.getOverdueRecords();
        fillRecordDetails(records);

        LocalDateTime now = LocalDateTime.now();
        List<OverdueRecordExportDTO> exportList = records.stream().map(r -> {
            OverdueRecordExportDTO dto = new OverdueRecordExportDTO();
            dto.setId(r.getId());
            dto.setUsername(r.getUser() != null ? r.getUser().getUsername() : "");
            dto.setRealName(r.getUser() != null ? r.getUser().getRealName() : "");
            dto.setBookTitle(r.getBook() != null ? r.getBook().getTitle() : "");
            dto.setBorrowDate(r.getBorrowDate() != null ? r.getBorrowDate().format(FORMATTER) : "");
            dto.setDueDate(r.getDueDate() != null ? r.getDueDate().format(FORMATTER) : "");
            dto.setOverdueDays(r.getDueDate() != null ? ChronoUnit.DAYS.between(r.getDueDate(), now) : 0);
            dto.setFineAmount(r.getFineAmount() != null ? r.getFineAmount().toString() : "0");
            return dto;
        }).collect(Collectors.toList());

        EasyExcel.write(response.getOutputStream(), OverdueRecordExportDTO.class)
                .sheet("逾期记录")
                .doWrite(exportList);
    }

    private void exportBookCirculation(HttpServletResponse response) throws IOException {
        List<Book> books = bookService.list();
        List<Category> categories = categoryService.list();
        Map<Integer, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // 使用数据库聚合查询替代全表加载
        List<Map<String, Object>> borrowCountRows = borrowRecordService.getBorrowCountByBook();
        Map<Long, Long> borrowCountMap = new HashMap<>();
        for (Map<String, Object> row : borrowCountRows) {
            Long bookId = ((Number) row.get("book_id")).longValue();
            Long cnt = ((Number) row.get("cnt")).longValue();
            borrowCountMap.put(bookId, cnt);
        }

        List<BookCirculationExportDTO> exportList = books.stream().map(book -> {
            BookCirculationExportDTO dto = new BookCirculationExportDTO();
            dto.setTitle(book.getTitle());
            dto.setAuthor(book.getAuthor());
            dto.setIsbn(book.getIsbn());
            dto.setCategory(categoryMap.getOrDefault(book.getCategoryId(), "未分类"));
            dto.setTotalQuantity(book.getTotalQuantity());
            long borrowCount = borrowCountMap.getOrDefault(book.getId(), 0L);
            dto.setBorrowCount((int) borrowCount);
            double rate = book.getTotalQuantity() > 0 ? (borrowCount * 100.0 / book.getTotalQuantity()) : 0;
            dto.setCirculationRate(String.format("%.1f", rate));
            return dto;
        }).sorted((a, b) -> b.getBorrowCount() - a.getBorrowCount())
                .collect(Collectors.toList());

        EasyExcel.write(response.getOutputStream(), BookCirculationExportDTO.class)
                .sheet("图书流通率")
                .doWrite(exportList);
    }

    private void fillRecordDetails(List<BorrowRecord> records) {
        if (records == null || records.isEmpty()) return;

        List<Long> bookIds = records.stream().map(BorrowRecord::getBookId).distinct().collect(Collectors.toList());
        Map<Long, Book> bookMap = bookIds.isEmpty() ? Collections.emptyMap() :
                bookService.listByIds(bookIds).stream().collect(Collectors.toMap(Book::getId, b -> b));

        List<Long> userIds = records.stream().map(BorrowRecord::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

        for (BorrowRecord r : records) {
            r.setBook(bookMap.get(r.getBookId()));
            r.setUser(userMap.get(r.getUserId()));
        }
    }

    private String translateStatus(String status) {
        switch (status) {
            case "BORROWED": return "借阅中";
            case "RENEWED": return "已续借";
            case "RETURNED": return "已归还";
            case "OVERDUE": return "已逾期";
            default: return status;
        }
    }
}
