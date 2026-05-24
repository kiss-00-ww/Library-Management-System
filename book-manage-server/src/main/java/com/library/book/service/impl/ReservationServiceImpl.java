package com.library.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.book.config.SysConfigCache;
import com.library.book.dto.PageResponse;
import com.library.book.entity.*;
import com.library.book.handler.BusinessException;
import com.library.book.mapper.ReservationMapper;
import com.library.book.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowRecordService borrowRecordService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SysConfigCache sysConfigCache;

    @Override
    @Transactional
    public Reservation createReservation(Long userId, Long bookId) {
        // 校验用户
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账户已被禁用");
        }

        // 逾期停借检查
        long overdueCount = borrowRecordService.count(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId)
                .eq(BorrowRecord::getStatus, "OVERDUE"));
        if (overdueCount > 0) {
            throw new BusinessException("您有逾期图书未归还，请先归还并缴纳罚款");
        }

        // 校验图书
        Book book = bookService.getById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (book.getStatus() == 0) {
            throw new BusinessException("图书已下架");
        }
        // 有库存时提示用户直接借阅
        if (book.getAvailableQuantity() > 0) {
            throw new BusinessException("该图书当前有库存（可借" + book.getAvailableQuantity() + "册），请直接借阅");
        }

        // 校验用户是否已借阅该书（未归还）
        long borrowCount = borrowRecordService.count(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId)
                .eq(BorrowRecord::getBookId, bookId)
                .apply("status IN ('BORROWED', 'RENEWED', 'OVERDUE')"));
        if (borrowCount > 0) {
            throw new BusinessException("您已借阅该书，请先归还后再预约");
        }

        // 校验未同时预约同一本书
        long existCount = this.count(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .eq(Reservation::getBookId, bookId)
                .apply("status IN ('WAITING', 'NOTIFIED')"));
        if (existCount > 0) {
            throw new BusinessException("您已预约此书，请勿重复预约");
        }

        // 校验最大预约数
        int maxReserveCount = sysConfigCache.getInt("max_reserve_count", 3);
        long currentReserveCount = this.count(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .apply("status IN ('WAITING', 'NOTIFIED')"));
        if (currentReserveCount >= maxReserveCount) {
            throw new BusinessException("已达到最大预约数量（" + maxReserveCount + "册）");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setBookId(bookId);
        reservation.setReserveTime(LocalDateTime.now());
        reservation.setStatus("WAITING");
        this.save(reservation);

        reservation.setBook(book);
        reservation.setUser(user);
        return reservation;
    }

    @Override
    @Transactional
    public BorrowRecord borrowFromReservation(Long userId, Long reservationId) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException("只能操作自己的预约");
        }
        if (!"NOTIFIED".equals(reservation.getStatus())) {
            throw new BusinessException("该预约不在可借阅状态");
        }

        // 校验是否在有效期内
        if (reservation.getExpireTime() != null && LocalDateTime.now().isAfter(reservation.getExpireTime())) {
            reservation.setStatus("EXPIRED");
            this.updateById(reservation);
            throw new BusinessException("预约已过期");
        }

        // 校验库存
        Book book = bookService.getById(reservation.getBookId());
        if (book == null || book.getAvailableQuantity() <= 0) {
            throw new BusinessException("图书库存不足，无法借阅");
        }

        // 执行借阅
        BorrowRecord record = borrowRecordService.borrowBook(userId, reservation.getBookId());

        // 更新预约状态
        reservation.setStatus("FULFILLED");
        this.updateById(reservation);

        return record;
    }

    @Override
    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException("只能取消自己的预约");
        }
        if (!"WAITING".equals(reservation.getStatus())) {
            throw new BusinessException("只能取消等待中的预约");
        }

        reservation.setStatus("CANCELLED");
        this.updateById(reservation);
    }

    @Override
    public PageResponse<Reservation> getMyReservations(Long userId, String status, Integer page, Integer size) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getUserId, userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Reservation::getStatus, status);
        }
        wrapper.orderByDesc(Reservation::getCreateTime);

        Page<Reservation> pageResult = this.page(new Page<>(page, size), wrapper);
        fillReservationDetails(pageResult.getRecords());
        return PageResponse.ok(pageResult.getTotal(), pageResult.getRecords());
    }

    @Override
    @Transactional
    public void notifyNextReservation(Long bookId) {
        // 查询该书是否有 WAITING 状态的预约，按预约时间升序取第一条
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getBookId, bookId)
                .eq(Reservation::getStatus, "WAITING")
                .orderByAsc(Reservation::getReserveTime)
                .last("LIMIT 1");
        Reservation nextReservation = this.getOne(wrapper);

        if (nextReservation == null) {
            return;
        }

        // 更新预约状态为 NOTIFIED，设置过期时间
        int keepDays = sysConfigCache.getInt("reserve_keep_days", 7);
        nextReservation.setStatus("NOTIFIED");
        nextReservation.setExpireTime(LocalDateTime.now().plusDays(keepDays));
        this.updateById(nextReservation);

        // 生成系统通知
        Book book = bookService.getById(bookId);
        String bookTitle = book != null ? book.getTitle() : "未知图书";
        Notification notification = new Notification();
        notification.setUserId(nextReservation.getUserId());
        notification.setTitle("预约图书可借通知");
        notification.setContent("您预约的图书《" + bookTitle + "》已可借阅，请在" + keepDays + "天内前往借阅，逾期将自动取消。");
        notification.setType("SYSTEM");
        notification.setIsRead(0);
        notificationService.save(notification);
    }

    @Override
    @Transactional
    public void processExpiredNotified() {
        // 查找所有过期的 NOTIFIED 记录
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getStatus, "NOTIFIED")
                .lt(Reservation::getExpireTime, LocalDateTime.now());
        List<Reservation> expiredList = this.list(wrapper);

        for (Reservation reservation : expiredList) {
            reservation.setStatus("EXPIRED");
            this.updateById(reservation);

            // 通知该读者预约已过期
            Book book = bookService.getById(reservation.getBookId());
            String bookTitle = book != null ? book.getTitle() : "未知图书";
            Notification notification = new Notification();
            notification.setUserId(reservation.getUserId());
            notification.setTitle("预约已过期");
            notification.setContent("您预约的图书《" + bookTitle + "》已超过保留期限，预约已自动取消。");
            notification.setType("SYSTEM");
            notification.setIsRead(0);
            notificationService.save(notification);

            // 通知下一位预约者
            notifyNextReservation(reservation.getBookId());
        }
    }

    private void fillReservationDetails(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return;
        }

        List<Long> bookIds = reservations.stream().map(Reservation::getBookId).distinct().collect(Collectors.toList());
        Map<Long, Book> bookMap = bookService.listByIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, b -> b));

        List<Long> userIds = reservations.stream().map(Reservation::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        for (Reservation r : reservations) {
            r.setBook(bookMap.get(r.getBookId()));
            r.setUser(userMap.get(r.getUserId()));
        }
    }
}
