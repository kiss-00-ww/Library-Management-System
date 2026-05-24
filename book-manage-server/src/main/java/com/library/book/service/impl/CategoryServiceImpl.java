package com.library.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.book.entity.Category;
import com.library.book.mapper.CategoryMapper;
import com.library.book.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
