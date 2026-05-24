package com.library.book.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class PopularBookExportDTO {

    @ExcelProperty("排名")
    private Integer rank;

    @ExcelProperty("书名")
    private String title;

    @ExcelProperty("作者")
    private String author;

    @ExcelProperty("ISBN")
    private String isbn;

    @ExcelProperty("借阅次数")
    private Integer borrowCount;
}
