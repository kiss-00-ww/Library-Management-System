package com.library.book.service;

import com.library.book.dto.PageResponse;
import com.library.book.entity.Notification;

public interface NotificationService extends com.baomidou.mybatisplus.extension.service.IService<Notification> {

    /**
     * 获取未读消息数量
     */
    long getUnreadCount(Long userId);

    /**
     * 获取消息列表（分页）
     */
    PageResponse<Notification> getNotifications(Long userId, Integer page, Integer size);

    /**
     * 标记单条消息已读
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * 全部标记已读
     */
    void markAllAsRead(Long userId);

    /**
     * 发送系统通知（同时尝试发邮件）
     */
    void sendNotification(Long userId, String title, String content);
}
