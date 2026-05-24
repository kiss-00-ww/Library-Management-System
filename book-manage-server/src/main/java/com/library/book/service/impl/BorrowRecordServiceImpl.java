package com.library.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.book.dto.PageResponse;
import com.library.book.dto.StatisticsResponse;
import com.library.book.entity.Book;
import com.library.book.entity.BorrowRecord;
import com.library.book.config.SysConfigCache;
import com.library.book.handler.BusinessException;
import com.library.book.mapper.BorrowRecordMapper;
import com.library.book.service.BookService;
import com.library.book.service.BorrowRecordService;
import com.library.book.service.ReservationService;
import com.library.book.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BorrowRecordServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowRecordService {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private SysConfigCache sysConfigCache;

    @Lazy
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private com.library.book.mapper.BookMapper bookMapper;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    /**
     * 检查用户是否有逾期未归还的借阅记录，若有则抛出业务异常
     */
    private void checkOverdueBlock(Long userId) {
        long overdueCount = this.count(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId)
                .eq(BorrowRecord::getStatus, "OVERDUE"));
        if (overdueCount > 0) {
            throw new BusinessException("您有逾期图书未归还，请先归还并缴纳罚款");
        }
    }

    @Override
    @Transactional
    public BorrowRecord borrowBook(Long userId, Long bookId) {
        // 逾期停借检查
        checkOverdueBlock(userId);

        com.library.book.entity.User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账户已被禁用，无法借书");
        }

        Book book = bookService.getById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (book.getStatus() == 0) {
            throw new BusinessException("图书已下架");
        }
        if (book.getAvailableQuantity() <= 0) {
            throw new BusinessException("图书库存不足");
        }

        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getUserId, userId)
                .eq(BorrowRecord::getBookId, bookId)
                .apply("status IN ('BORROWED', 'RENEWED', 'OVERDUE')");
        if (this.count(wrapper) > 0) {
            throw new BusinessException("您已借阅此书，请先归还再借");
        }

        // 从配置读取最大借阅数量，根据角色区分
        String role = user.getRole();
        int maxBorrowCount;
        int borrowDays;
        int renewMaxTimes = sysConfigCache.getInt("renew_max_times", 1);
        if ("TEACHER".equals(role)) {
            maxBorrowCount = sysConfigCache.getInt("max_borrow_count_teacher", 10);
            borrowDays = sysConfigCache.getInt("borrow_days_teacher", 60);
        } else {
            maxBorrowCount = sysConfigCache.getInt("max_borrow_count_student", 5);
            borrowDays = sysConfigCache.getInt("borrow_days_student", 30);
        }

        long borrowCount = this.count(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId)
                .apply("status IN ('BORROWED', 'RENEWED', 'OVERDUE')"));
        if (borrowCount >= maxBorrowCount) {
            throw new BusinessException("已达到最大借阅数量（" + maxBorrowCount + "册）");
        }

        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(borrowDays));
        record.setStatus("BORROWED");
        record.setRenewCount(0);
        record.setFineAmount(BigDecimal.ZERO);
        this.save(record);

        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookService.updateById(book);

        record.setBook(book);
        record.setUser(user);
        record.setMaxRenewCount(renewMaxTimes);
        return record;
    }

    @Override
    @Transactional
    public BorrowRecord returnBook(Long recordId, Long operatorId, String operatorRole) {
        BorrowRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }
        if ("RETURNED".equals(record.getStatus())) {
            throw new BusinessException("该书已归还");
        }

        // 权限校验：只有借阅者本人或管理员可以归还
        if (!"ADMIN".equals(operatorRole) && !record.getUserId().equals(operatorId)) {
            throw new BusinessException("只能归还自己借阅的图书");
        }

        Book book = bookService.getById(record.getBookId());
        if (book == null) {
            throw new BusinessException("图书不存在");
        }

        LocalDateTime returnDate = LocalDateTime.now();

        // 归还时，如果定时任务已累加罚款则使用累加值；否则按天数计算
        BigDecimal fine = record.getFineAmount() != null ? record.getFineAmount() : BigDecimal.ZERO;
        if (returnDate.isAfter(record.getDueDate()) && fine.compareTo(BigDecimal.ZERO) == 0) {
            long overdueDays = java.time.Duration.between(record.getDueDate(), returnDate).toDays();
            BigDecimal finePerDay = sysConfigCache.getBigDecimal("fine_rate_per_day", new BigDecimal("0.1"));
            fine = finePerDay.multiply(BigDecimal.valueOf(overdueDays));

            // 罚款上限
            if (book.getPrice() != null && fine.compareTo(book.getPrice()) > 0) {
                fine = book.getPrice();
            }
        }

        record.setReturnDate(returnDate);
        record.setStatus("RETURNED");
        record.setFineAmount(fine);
        record.setReturnOperator(operatorId);
        this.updateById(record);

        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        bookService.updateById(book);

        // 归还成功后，触发预约通知
        reservationService.notifyNextReservation(book.getId());

        record.setBook(book);
        return record;
    }

    @Override
    @Transactional
    public BorrowRecord renewBook(Long recordId, Long userId) {
        // 逾期停借检查
        checkOverdueBlock(userId);

        BorrowRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("只能续借自己的借阅记录");
        }
        if ("RETURNED".equals(record.getStatus())) {
            throw new BusinessException("该书已归还，无法续借");
        }

        // 从配置读取续借参数
        int renewMaxTimes = sysConfigCache.getInt("renew_max_times", 1);
        int renewDays = sysConfigCache.getInt("renew_days", 30);
        int renewWindowDays = sysConfigCache.getInt("renew_window_days", 7);

        if (record.getRenewCount() >= renewMaxTimes) {
            throw new BusinessException("该借阅已续借" + renewMaxTimes + "次，无法再次续借");
        }

        // 续借窗口期检查：只能在到期前N天内续借
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime renewWindowStart = record.getDueDate().minusDays(renewWindowDays);
        if (now.isBefore(renewWindowStart)) {
            throw new BusinessException("只能在到期前" + renewWindowDays + "天内续借");
        }
        if (now.isAfter(record.getDueDate())) {
            throw new BusinessException("已超过还书日期，无法续借");
        }

        record.setDueDate(record.getDueDate().plusDays(renewDays));
        record.setStatus("RENEWED");
        record.setRenewCount(record.getRenewCount() + 1);
        this.updateById(record);

        return record;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void updateOverdueStatus() {
        BigDecimal finePerDay = sysConfigCache.getBigDecimal("fine_rate_per_day", new BigDecimal("0.1"));

        // 1. 将到期的借阅记录标记为 OVERDUE
        LambdaQueryWrapper<BorrowRecord> overdueWrapper = new LambdaQueryWrapper<>();
        overdueWrapper.apply("status IN ('BORROWED', 'RENEWED')")
                .lt(BorrowRecord::getDueDate, LocalDateTime.now());
        List<BorrowRecord> newOverdueRecords = this.list(overdueWrapper);
        for (BorrowRecord record : newOverdueRecords) {
            record.setStatus("OVERDUE");
            this.updateById(record);
        }

        // 2. 对所有 OVERDUE 状态的记录累加罚款
        LambdaQueryWrapper<BorrowRecord> fineWrapper = new LambdaQueryWrapper<>();
        fineWrapper.eq(BorrowRecord::getStatus, "OVERDUE");
        List<BorrowRecord> overdueRecords = this.list(fineWrapper);

        for (BorrowRecord record : overdueRecords) {
            BigDecimal currentFine = record.getFineAmount() != null ? record.getFineAmount() : BigDecimal.ZERO;
            BigDecimal newFine = currentFine.add(finePerDay);

            // 罚款上限：图书原价，如果 price 为 null 则无上限
            Book book = bookService.getById(record.getBookId());
            if (book != null && book.getPrice() != null) {
                if (newFine.compareTo(book.getPrice()) > 0) {
                    newFine = book.getPrice();
                }
            }

            record.setFineAmount(newFine);
            this.updateById(record);
        }

        // 3. 处理过期的 NOTIFIED 预约记录
        reservationService.processExpiredNotified();
    }

    private void fillRecordDetails(List<BorrowRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        int renewMaxTimes = sysConfigCache.getInt("renew_max_times", 1);
        
        List<Long> bookIds = records.stream().map(BorrowRecord::getBookId).collect(Collectors.toList());
        List<Book> books = bookService.listByIds(bookIds);
        Map<Long, Book> bookMap = books.stream().collect(Collectors.toMap(Book::getId, book -> book));
        
        List<Long> userIds = records.stream().map(BorrowRecord::getUserId).distinct().collect(Collectors.toList());
        List<com.library.book.entity.User> users = userService.listByIds(userIds);
        Map<Long, com.library.book.entity.User> userMap = users.stream().collect(Collectors.toMap(com.library.book.entity.User::getId, u -> u));
        
        for (BorrowRecord record : records) {
            record.setBook(bookMap.get(record.getBookId()));
            record.setUser(userMap.get(record.getUserId()));
            record.setMaxRenewCount(renewMaxTimes);
        }
    }

    @Override
    public PageResponse<BorrowRecord> getMyBorrows(Long userId, String status, Integer page, Integer size) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getUserId, userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BorrowRecord::getStatus, status);
        }
        wrapper.orderByDesc(BorrowRecord::getCreateTime);

        Page<BorrowRecord> pageResult = this.page(new Page<>(page, size), wrapper);
        fillRecordDetails(pageResult.getRecords());
        return PageResponse.ok(pageResult.getTotal(), pageResult.getRecords());
    }

    @Override
    public PageResponse<BorrowRecord> getAllBorrows(String status, Integer page, Integer size) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BorrowRecord::getStatus, status);
        }
        wrapper.orderByDesc(BorrowRecord::getCreateTime);

        Page<BorrowRecord> pageResult = this.page(new Page<>(page, size), wrapper);
        fillRecordDetails(pageResult.getRecords());
        return PageResponse.ok(pageResult.getTotal(), pageResult.getRecords());
    }

    @Override
    public List<BorrowRecord> getOverdueRecords() {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("status IN ('BORROWED', 'RENEWED', 'OVERDUE')")
                .lt(BorrowRecord::getDueDate, LocalDateTime.now());
        List<BorrowRecord> records = this.list(wrapper);
        fillRecordDetails(records);
        return records;
    }

    @Override
    public StatisticsResponse getStatistics() {
        StatisticsResponse response = new StatisticsResponse();

        // 使用数据库聚合查询替代全表加载
        long totalBooks = bookMapper.sumTotalQuantity();
        long availableBooks = bookMapper.sumAvailableQuantity();
        response.setTotalBooks(totalBooks);
        response.setAvailableBooks(availableBooks);
        response.setBorrowedBooks(totalBooks - availableBooks);

        // 分类统计：使用数据库聚合
        Map<String, Long> categoryStats = new java.util.LinkedHashMap<>();
        List<Map<String, Object>> categoryRows = bookMapper.countByCategory();
        for (Map<String, Object> row : categoryRows) {
            String name = row.get("category_name") != null ? row.get("category_name").toString() : "未分类";
            Long cnt = ((Number) row.get("cnt")).longValue();
            categoryStats.put(name, cnt);
        }
        response.setCategoryStats(categoryStats);

        // 热门图书：使用数据库聚合查询近30天借阅次数
        String thirtyDaysAgo = LocalDateTime.now().minusDays(30).toString();
        List<Map<String, Object>> popularRows = borrowRecordMapper.countBorrowByBook(thirtyDaysAgo, 10);
        Map<Long, Long> borrowCountMap = new java.util.HashMap<>();
        if (!popularRows.isEmpty()) {
            for (Map<String, Object> row : popularRows) {
                Long bookId = ((Number) row.get("book_id")).longValue();
                Long cnt = ((Number) row.get("cnt")).longValue();
                borrowCountMap.put(bookId, cnt);
            }
            List<Long> popularIds = popularRows.stream()
                    .map(row -> ((Number) row.get("book_id")).longValue())
                    .collect(Collectors.toList());
            List<Book> popularBooks = bookService.listByIds(popularIds);
            Map<Long, Book> bookMap = popularBooks.stream().collect(Collectors.toMap(Book::getId, b -> b));
            List<Book> sortedBooks = popularIds.stream()
                    .map(bookMap::get)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
            response.setPopularBooks(sortedBooks);
        } else {
            response.setPopularBooks(java.util.Collections.emptyList());
        }
        response.setPopularBookBorrowCounts(borrowCountMap);

        // 逾期数量：使用数据库 COUNT
        response.setOverdueCount(borrowRecordMapper.countOverdue());

        // 用户统计
        response.setTotalUsers(userService.count());
        // 活跃用户：当前有借阅的独立用户数
        response.setActiveUsers(borrowRecordMapper.countActiveUsers());

        return response;
    }

    @Override
    public StatisticsResponse getPopularBooksStatistics() {
        StatisticsResponse response = new StatisticsResponse();

        // 使用数据库聚合查询替代全表加载
        String thirtyDaysAgo = LocalDateTime.now().minusDays(30).toString();
        List<Map<String, Object>> popularRows = borrowRecordMapper.countBorrowByBook(thirtyDaysAgo, 10);

        Map<Long, Long> borrowCountMap = new java.util.HashMap<>();
        if (!popularRows.isEmpty()) {
            for (Map<String, Object> row : popularRows) {
                Long bookId = ((Number) row.get("book_id")).longValue();
                Long cnt = ((Number) row.get("cnt")).longValue();
                borrowCountMap.put(bookId, cnt);
            }
            List<Long> popularBookIds = popularRows.stream()
                    .map(row -> ((Number) row.get("book_id")).longValue())
                    .collect(Collectors.toList());
            List<Book> popularBooks = bookService.listByIds(popularBookIds);
            Map<Long, Book> bookMap = popularBooks.stream().collect(Collectors.toMap(Book::getId, b -> b));
            List<Book> sortedBooks = popularBookIds.stream()
                    .map(bookMap::get)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
            response.setPopularBooks(sortedBooks);
        } else {
            response.setPopularBooks(java.util.Collections.emptyList());
        }
        response.setPopularBookBorrowCounts(borrowCountMap);

        return response;
    }

    @Override
    public Map<String, Long> getBorrowTrend(int days) {
        LocalDateTime start = LocalDateTime.now().minusDays(days).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Map<String, Object>> rows = borrowRecordMapper.countBorrowByDate(start.toString());

        Map<java.time.LocalDate, Long> dateCountMap = new java.util.HashMap<>();
        for (Map<String, Object> row : rows) {
            java.sql.Date sqlDate = (java.sql.Date) row.get("date_key");
            Long cnt = ((Number) row.get("cnt")).longValue();
            dateCountMap.put(sqlDate.toLocalDate(), cnt);
        }

        java.util.LinkedHashMap<String, Long> result = new java.util.LinkedHashMap<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            java.time.LocalDate date = today.minusDays(i);
            result.put(date.toString(), dateCountMap.getOrDefault(date, 0L));
        }
        return result;
    }

    @Override
    public Map<String, Long> getBorrowCountsByUser(Long userId) {
        Map<String, Long> counts = new java.util.LinkedHashMap<>();
        // 全部
        counts.put("ALL", this.count(new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getUserId, userId)));
        // 借阅中
        counts.put("BORROWED", this.count(new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getUserId, userId).eq(BorrowRecord::getStatus, "BORROWED")));
        // 已续借
        counts.put("RENEWED", this.count(new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getUserId, userId).eq(BorrowRecord::getStatus, "RENEWED")));
        // 已逾期
        counts.put("OVERDUE", this.count(new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getUserId, userId).eq(BorrowRecord::getStatus, "OVERDUE")));
        // 已归还
        counts.put("RETURNED", this.count(new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getUserId, userId).eq(BorrowRecord::getStatus, "RETURNED")));
        return counts;
    }

    @Override
    public BorrowRecord getActiveBorrowByUserAndBook(Long userId, Long bookId) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getUserId, userId)
               .eq(BorrowRecord::getBookId, bookId)
               .apply("status IN ('BORROWED', 'RENEWED', 'OVERDUE')")
               .last("LIMIT 1");
        BorrowRecord record = this.getOne(wrapper);
        if (record != null) {
            int renewMaxTimes = sysConfigCache.getInt("renew_max_times", 1);
            record.setMaxRenewCount(renewMaxTimes);
            Book book = bookService.getById(bookId);
            record.setBook(book);
        }
        return record;
    }

    @Override
    public List<Long> getActiveBorrowedBookIdsByUser(Long userId) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getUserId, userId)
               .apply("status IN ('BORROWED', 'RENEWED', 'OVERDUE')")
               .select(BorrowRecord::getBookId);
        List<BorrowRecord> records = this.list(wrapper);
        return records.stream().map(BorrowRecord::getBookId).distinct().collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getBorrowCountByBook() {
        return borrowRecordMapper.countAllBorrowByBook(Integer.MAX_VALUE);
    }
}
