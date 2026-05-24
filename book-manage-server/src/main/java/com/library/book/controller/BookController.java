package com.library.book.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.book.dto.PageResponse;
import com.library.book.dto.Response;
import com.library.book.entity.Book;
import com.library.book.service.BookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Book Management")
@RestController
@RequestMapping("/api")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/books")
    @ApiOperation("Get book list")
    public Response<PageResponse<Book>> getBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Book> result = bookService.getBooks(title, author, isbn, categoryId, status, page, size);
        return Response.ok(PageResponse.ok(result.getTotal(), result.getRecords()));
    }

    @GetMapping("/books/{id}")
    @ApiOperation("Get book detail")
    public Response<Book> getBook(@PathVariable Long id) {
        Book book = bookService.getBookDetail(id);
        return Response.ok(book);
    }

    @GetMapping("/categories")
    @ApiOperation("Get category list")
    public Response<List<com.library.book.entity.Category>> getCategories() {
        List<com.library.book.entity.Category> categories = bookService.getCategories();
        return Response.ok(categories);
    }

    @PostMapping("/admin/books")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Add a new book (Admin)")
    public Response<Boolean> addBook(@RequestBody Book book) {
        boolean result = bookService.addBook(book);
        return Response.ok(result);
    }

    @PutMapping("/admin/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Update a book (Admin)")
    public Response<Boolean> updateBook(@PathVariable Long id, @RequestBody Book book) {
        boolean result = bookService.updateBook(id, book);
        return Response.ok(result);
    }

    @DeleteMapping("/admin/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Delete a book (Admin)")
    public Response<Boolean> deleteBook(@PathVariable Long id) {
        boolean result = bookService.deleteBook(id);
        return Response.ok(result);
    }

    @PutMapping("/admin/books/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Toggle book status (on/off shelf) (Admin)")
    public Response<Boolean> toggleBookStatus(@PathVariable Long id) {
        boolean result = bookService.toggleBookStatus(id);
        return Response.ok(result);
    }
}
