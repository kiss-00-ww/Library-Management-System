package com.library.book.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("reservation")
public class Reservation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    @TableField(exist = false)
    private User user;

    private Long bookId;

    @TableField(exist = false)
    private Book book;

    private LocalDateTime reserveTime;

    private LocalDateTime expireTime;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
