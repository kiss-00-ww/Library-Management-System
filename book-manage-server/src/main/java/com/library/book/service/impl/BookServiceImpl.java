package com.library.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.book.entity.Book;
import com.library.book.entity.BorrowRecord;
import com.library.book.entity.Category;
import com.library.book.handler.BusinessException;
import com.library.book.mapper.BookMapper;
import com.library.book.mapper.BorrowRecordMapper;
import com.library.book.mapper.CategoryMapper;
import com.library.book.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Override
    public Page<Book> getBooks(String title, String author, String isbn, Integer categoryId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like(Book::getTitle, title);
        }
        if (author != null && !author.isEmpty()) {
            wrapper.like(Book::getAuthor, author);
        }
        if (isbn != null && !isbn.isEmpty()) {
            wrapper.eq(Book::getIsbn, isbn);
        }
        if (categoryId != null) {
            wrapper.eq(Book::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Book::getStatus, status);
        }
        wrapper.orderByDesc(Book::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public Book getBookDetail(Long id) {
        return this.getById(id);
    }

    @Override
    public List<Book> getPopularBooks(int limit) {
        List<Map<String, Object>> popularRows = borrowRecordMapper.countAllBorrowByBook(limit);
        if (popularRows.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> popularIds = popularRows.stream()
                .map(row -> ((Number) row.get("book_id")).longValue())
                .collect(Collectors.toList());

        List<Book> books = this.listByIds(popularIds);
        Map<Long, Book> bookMap = books.stream().collect(Collectors.toMap(Book::getId, b -> b));
        return popularIds.stream()
                .map(bookMap::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean addBook(Book book) {
        // ISBN 唯一性校验
        if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
            long count = this.count(new LambdaQueryWrapper<Book>().eq(Book::getIsbn, book.getIsbn()));
            if (count > 0) {
                throw new BusinessException("ISBN已存在，请检查是否重复添加");
            }
        }
        book.setAvailableQuantity(book.getTotalQuantity());
        return this.save(book);
    }

    @Override
    public boolean updateBook(Long id, Book book) {
        Book existingBook = this.getById(id);
        if (existingBook == null) {
            throw new BusinessException("图书不存在");
        }
        book.setId(id);
        // availableQuantity 由系统自动计算：总量 - 当前借出数量
        // 不允许前端修改 availableQuantity
        book.setAvailableQuantity(null);
        // 如果修改了 totalQuantity，重新计算 availableQuantity
        if (book.getTotalQuantity() != null) {
            long activeBorrows = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                    .eq(BorrowRecord::getBookId, id)
                    .apply("status IN ('BORROWED', 'RENEWED', 'OVERDUE')")
            );
            book.setAvailableQuantity(book.getTotalQuantity() - (int) activeBorrows);
        }
        return this.updateById(book);
    }

    @Override
    public boolean deleteBook(Long id) {
        Book book = this.getById(id);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        // 检查是否有未归还的借阅记录
        long activeBorrows = borrowRecordMapper.selectCount(
            new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getBookId, id)
                .apply("status IN ('BORROWED', 'RENEWED', 'OVERDUE')")
        );
        if (activeBorrows > 0) {
            throw new BusinessException("该图书有未归还的借阅记录，无法删除");
        }
        return this.removeById(id);
    }

    @Override
    public boolean toggleBookStatus(Long id) {
        Book book = this.getById(id);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        // 上架/下架切换
        if (book.getStatus() == 1) {
            // 下架前检查是否有未归还的借阅记录
            long activeBorrows = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                    .eq(BorrowRecord::getBookId, id)
                    .apply("status IN ('BORROWED', 'RENEWED', 'OVERDUE')")
            );
            if (activeBorrows > 0) {
                throw new BusinessException("该图书有未归还的借阅记录，无法下架");
            }
            book.setStatus(0);
        } else {
            book.setStatus(1);
        }
        return this.updateById(book);
    }

    @Override
    public List<Category> getCategories() {
        return categoryMapper.selectList(null);
    }
}
