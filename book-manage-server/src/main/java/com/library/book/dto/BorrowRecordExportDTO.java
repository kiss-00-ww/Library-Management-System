package com.library.book.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class BorrowRecordExportDTO {

    @ExcelProperty("记录ID")
    private Long id;

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("真实姓名")
    private String realName;

    @ExcelProperty("书名")
    private String bookTitle;

    @ExcelProperty("作者")
    private String bookAuthor;

    @ExcelProperty("ISBN")
    private String isbn;

    @ExcelProperty("借阅日期")
    private String borrowDate;

    @ExcelProperty("应还日期")
    private String dueDate;

    @ExcelProperty("归还日期")
    private String returnDate;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("续借次数")
    private Integer renewCount;

    @ExcelProperty("罚款金额")
    private String fineAmount;
}
