package com.library.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.book.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface BookMapper extends BaseMapper<Book> {

    @Select("SELECT COALESCE(SUM(total_quantity), 0) FROM book WHERE deleted = 0")
    long sumTotalQuantity();

    @Select("SELECT COALESCE(SUM(available_quantity), 0) FROM book WHERE deleted = 0")
    long sumAvailableQuantity();

    @Select("SELECT c.name AS category_name, COALESCE(SUM(b.total_quantity), 0) AS cnt FROM book b LEFT JOIN category c ON b.category_id = c.id WHERE b.deleted = 0 GROUP BY b.category_id, c.name")
    List<Map<String, Object>> countByCategory();
}
