package com.library.book.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class BookImportDTO {

    @ExcelProperty("ISBN")
    private String isbn;

    @ExcelProperty("书名")
    private String title;

    @ExcelProperty("作者")
    private String author;

    @ExcelProperty("出版社")
    private String publisher;

    @ExcelProperty("分类名称")
    private String categoryName;

    @ExcelProperty("出版日期")
    private String publishDate;

    @ExcelProperty("总数量")
    private Integer totalQuantity;

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("价格")
    private String price;

    @ExcelProperty("简介")
    private String description;
}
