package com.library.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.book.entity.BookReview;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookReviewMapper extends BaseMapper<BookReview> {
}
