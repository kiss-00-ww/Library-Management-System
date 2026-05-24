package com.library.book.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("borrow_record")
public class BorrowRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    @TableField(exist = false)
    private User user;

    private Long bookId;

    @TableField(exist = false)
    private Book book;

    private LocalDateTime borrowDate;

    private LocalDateTime dueDate;

    private LocalDateTime returnDate;

    private String status;

    private Integer renewCount;

    private BigDecimal fineAmount;

    private Long returnOperator;

    private LocalDateTime lastRemindTime;

    @TableField(exist = false)
    private Integer maxRenewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
