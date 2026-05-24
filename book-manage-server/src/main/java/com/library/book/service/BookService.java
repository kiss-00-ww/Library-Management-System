package com.library.book.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.book.entity.Book;

public interface BookService extends com.baomidou.mybatisplus.extension.service.IService<Book> {
    Page<Book> getBooks(String title, String author, String isbn, Integer categoryId, Integer status, Integer page, Integer size);
    Book getBookDetail(Long id);
    java.util.List<Book> getPopularBooks(int limit);
    boolean addBook(Book book);
    boolean updateBook(Long id, Book book);
    boolean deleteBook(Long id);
    boolean toggleBookStatus(Long id);
    java.util.List<com.library.book.entity.Category> getCategories();
}
