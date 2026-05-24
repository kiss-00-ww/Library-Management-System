package com.library.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.book.dto.PageResponse;
import com.library.book.entity.Notification;
import com.library.book.entity.User;
import com.library.book.mapper.NotificationMapper;
import com.library.book.service.NotificationService;
import com.library.book.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public long getUnreadCount(Long userId) {
        return this.count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    public PageResponse<Notification> getNotifications(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        Page<Notification> pageResult = this.page(new Page<>(page, size), wrapper);
        return PageResponse.ok(pageResult.getTotal(), pageResult.getRecords());
    }

    @Override
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = this.getById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setIsRead(1);
            this.updateById(notification);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1);
        this.update(wrapper);
    }

    @Override
    public void sendNotification(Long userId, String title, String content) {
        // 1. 插入系统通知
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType("SYSTEM");
        notification.setIsRead(0);
        this.save(notification);

        // 2. 尝试发送邮件
        try {
            User user = userService.getById(userId);
            if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                sendEmail(user.getEmail(), title, content);
            }
        } catch (Exception e) {
            log.warn("发送邮件失败，userId={}: {}", userId, e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String text) {
        if (mailSender == null) {
            log.info("[邮件模拟] 收件人: {}, 主题: {}, 内容: {}", to, subject, text);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@library.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("邮件发送成功: {}", to);
        } catch (Exception e) {
            log.warn("邮件发送失败，降级为日志: 收件人={}, 主题={}, 错误={}", to, subject, e.getMessage());
            log.info("[邮件模拟] 收件人: {}, 主题: {}, 内容: {}", to, subject, text);
        }
    }
}
