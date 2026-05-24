package com.library.book.dto;

import lombok.Data;

@Data
public class BookDTO {
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Integer categoryId;
    private String categoryName;
    private String publishDate;
    private String description;
    private String coverImage;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private String location;
    private Integer status;
}
