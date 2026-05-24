package com.library.book.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.book.dto.Response;
import com.library.book.entity.Book;
import com.library.book.entity.Category;
import com.library.book.mapper.BookMapper;
import com.library.book.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Category Management")
@RestController
@RequestMapping("/api/admin")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BookMapper bookMapper;

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Add category (Admin)")
    public Response<Boolean> addCategory(@RequestBody Category category) {
        boolean result = categoryService.save(category);
        return Response.ok(result);
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Update category (Admin)")
    public Response<Boolean> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        category.setId(id);
        boolean result = categoryService.updateById(category);
        return Response.ok(result);
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Delete category (Admin)")
    public Response<Boolean> deleteCategory(@PathVariable Integer id) {
        // 检查该分类下是否有图书
        long bookCount = bookMapper.selectCount(
            new LambdaQueryWrapper<Book>().eq(Book::getCategoryId, id)
        );
        if (bookCount > 0) {
            return Response.fail("该分类下还有" + bookCount + "本图书，无法删除，请先转移或删除相关图书");
        }
        boolean result = categoryService.removeById(id);
        return Response.ok(result);
    }
}
