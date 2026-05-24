package com.library.book.dto;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.library.book.entity.Book;
import com.library.book.entity.Category;
import com.library.book.service.BookService;
import com.library.book.service.CategoryService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class BookImportListener implements ReadListener<BookImportDTO> {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final Map<String, Integer> categoryNameToIdMap;

    private int successCount = 0;
    private int failCount = 0;
    private final List<String> failReasons = new ArrayList<>();

    public BookImportListener(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        // 预加载分类映射
        List<Category> categories = categoryService.list();
        this.categoryNameToIdMap = new HashMap<>();
        for (Category cat : categories) {
            categoryNameToIdMap.put(cat.getName(), cat.getId());
        }
    }

    @Override
    public void invoke(BookImportDTO data, AnalysisContext context) {
        int rowIndex = context.readRowHolder().getRowIndex() + 1;
        try {
            // 校验必填字段
            if (data.getIsbn() == null || data.getIsbn().trim().isEmpty()) {
                failCount++;
                failReasons.add("第" + rowIndex + "行：ISBN不能为空");
                return;
            }
            if (data.getTitle() == null || data.getTitle().trim().isEmpty()) {
                failCount++;
                failReasons.add("第" + rowIndex + "行：书名不能为空");
                return;
            }
            if (data.getAuthor() == null || data.getAuthor().trim().isEmpty()) {
                failCount++;
                failReasons.add("第" + rowIndex + "行：作者不能为空");
                return;
            }

            // 校验ISBN唯一性
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Book> isbnWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            isbnWrapper.eq(Book::getIsbn, data.getIsbn().trim());
            if (bookService.count(isbnWrapper) > 0) {
                failCount++;
                failReasons.add("第" + rowIndex + "行：ISBN " + data.getIsbn() + " 已存在");
                return;
            }

            // 校验分类
            Integer categoryId = null;
            if (data.getCategoryName() != null && !data.getCategoryName().trim().isEmpty()) {
                categoryId = categoryNameToIdMap.get(data.getCategoryName().trim());
                if (categoryId == null) {
                    failCount++;
                    failReasons.add("第" + rowIndex + "行：分类「" + data.getCategoryName() + "」不存在");
                    return;
                }
            }

            // 构建Book对象
            Book book = new Book();
            book.setIsbn(data.getIsbn().trim());
            book.setTitle(data.getTitle().trim());
            book.setAuthor(data.getAuthor().trim());
            book.setPublisher(data.getPublisher() != null ? data.getPublisher().trim() : null);
            book.setCategoryId(categoryId);
            book.setTotalQuantity(data.getTotalQuantity() != null ? data.getTotalQuantity() : 1);
            book.setAvailableQuantity(book.getTotalQuantity());
            book.setLocation(data.getLocation() != null ? data.getLocation().trim() : null);
            book.setStatus(1);

            if (data.getPrice() != null && !data.getPrice().trim().isEmpty()) {
                try {
                    book.setPrice(new BigDecimal(data.getPrice().trim()));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }

            if (data.getPublishDate() != null && !data.getPublishDate().trim().isEmpty()) {
                try {
                    book.setPublishDate(LocalDateTime.parse(data.getPublishDate().trim() + "T00:00:00",
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                } catch (Exception e) {
                    // ignore parse error
                }
            }

            book.setDescription(data.getDescription() != null ? data.getDescription().trim() : null);

            bookService.save(book);
            successCount++;
        } catch (Exception e) {
            failCount++;
            failReasons.add("第" + rowIndex + "行：" + e.getMessage());
            log.error("导入第{}行失败", rowIndex, e);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("图书导入完成：成功{}条，失败{}条", successCount, failCount);
    }

    public int getSuccessCount() { return successCount; }
    public int getFailCount() { return failCount; }
    public List<String> getFailReasons() { return failReasons; }
}
