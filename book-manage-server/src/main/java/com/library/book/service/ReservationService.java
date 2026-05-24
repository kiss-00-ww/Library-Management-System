package com.library.book.service;

import com.library.book.dto.PageResponse;
import com.library.book.entity.BorrowRecord;
import com.library.book.entity.Reservation;

public interface ReservationService extends com.baomidou.mybatisplus.extension.service.IService<Reservation> {

    /**
     * 预约图书
     */
    Reservation createReservation(Long userId, Long bookId);

    /**
     * 预约借阅（NOTIFIED状态的预约转为借阅）
     */
    BorrowRecord borrowFromReservation(Long userId, Long reservationId);

    /**
     * 取消预约
     */
    void cancelReservation(Long userId, Long reservationId);

    /**
     * 获取我的预约列表
     */
    PageResponse<Reservation> getMyReservations(Long userId, String status, Integer page, Integer size);

    /**
     * 处理归还后的预约通知
     */
    void notifyNextReservation(Long bookId);

    /**
     * 处理过期的NOTIFIED预约（定时任务调用）
     */
    void processExpiredNotified();
}
