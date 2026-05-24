package com.library.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.book.entity.BookReview;
import com.library.book.mapper.BookReviewMapper;
import com.library.book.service.BookReviewService;
import org.springframework.stereotype.Service;

@Service
public class BookReviewServiceImpl extends ServiceImpl<BookReviewMapper, BookReview> implements BookReviewService {
}
