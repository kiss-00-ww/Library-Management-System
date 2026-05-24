package com.library.book;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.library.book.mapper")
@EnableScheduling
public class BookManageApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookManageApplication.class, args);
    }
}
