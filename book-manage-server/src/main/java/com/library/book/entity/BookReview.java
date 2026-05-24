package com.library.book.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("book_review")
public class BookReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    @TableField(exist = false)
    private User user;

    private Long bookId;

    @TableField(exist = false)
    private Book book;

    private Integer rating;

    private String content;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
