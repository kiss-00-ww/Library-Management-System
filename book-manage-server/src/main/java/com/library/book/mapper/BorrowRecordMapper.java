package com.library.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.book.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    @Select("SELECT COUNT(DISTINCT user_id) FROM borrow_record WHERE status IN ('BORROWED', 'RENEWED', 'OVERDUE')")
    long countActiveUsers();

    @Select("SELECT COUNT(*) FROM borrow_record WHERE status = 'OVERDUE'")
    long countOverdue();

    @Select("SELECT DATE(borrow_date) AS date_key, COUNT(*) AS cnt FROM borrow_record WHERE borrow_date >= #{startDate} GROUP BY DATE(borrow_date)")
    List<Map<String, Object>> countBorrowByDate(String startDate);

    @Select("SELECT book_id, COUNT(*) AS cnt FROM borrow_record WHERE borrow_date >= #{startDate} GROUP BY book_id ORDER BY cnt DESC LIMIT #{limit}")
    List<Map<String, Object>> countBorrowByBook(String startDate, int limit);

    @Select("SELECT book_id, COUNT(*) AS cnt FROM borrow_record GROUP BY book_id ORDER BY cnt DESC LIMIT #{limit}")
    List<Map<String, Object>> countAllBorrowByBook(int limit);
}
