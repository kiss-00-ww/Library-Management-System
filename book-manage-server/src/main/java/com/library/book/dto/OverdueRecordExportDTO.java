package com.library.book.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class OverdueRecordExportDTO {

    @ExcelProperty("记录ID")
    private Long id;

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("真实姓名")
    private String realName;

    @ExcelProperty("书名")
    private String bookTitle;

    @ExcelProperty("借阅日期")
    private String borrowDate;

    @ExcelProperty("应还日期")
    private String dueDate;

    @ExcelProperty("逾期天数")
    private Long overdueDays;

    @ExcelProperty("罚款金额")
    private String fineAmount;
}
