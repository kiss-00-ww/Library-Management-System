package com.library.book.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class BookCirculationExportDTO {

    @ExcelProperty("书名")
    private String title;

    @ExcelProperty("作者")
    private String author;

    @ExcelProperty("ISBN")
    private String isbn;

    @ExcelProperty("分类")
    private String category;

    @ExcelProperty("总数量")
    private Integer totalQuantity;

    @ExcelProperty("借出次数")
    private Integer borrowCount;

    @ExcelProperty("流通率(%)")
    private String circulationRate;
}
