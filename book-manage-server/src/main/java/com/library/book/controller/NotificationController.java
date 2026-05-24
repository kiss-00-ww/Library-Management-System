package com.library.book.controller;

import com.library.book.dto.PageResponse;
import com.library.book.dto.Response;
import com.library.book.entity.Notification;
import com.library.book.entity.User;
import com.library.book.service.NotificationService;
import com.library.book.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Notification")
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('READER', 'ADMIN')")
    @ApiOperation("获取未读消息数量")
    public Response<Long> getUnreadCount() {
        Long userId = getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);
        return Response.ok(count);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('READER', 'ADMIN')")
    @ApiOperation("获取消息列表")
    public Response<PageResponse<Notification>> getNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getCurrentUserId();
        PageResponse<Notification> result = notificationService.getNotifications(userId, page, size);
        return Response.ok(result);
    }

    @PutMapping("/read/{id}")
    @PreAuthorize("hasAnyRole('READER', 'ADMIN')")
    @ApiOperation("标记单条消息已读")
    public Response<Boolean> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        notificationService.markAsRead(userId, id);
        return Response.ok(true);
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('READER', 'ADMIN')")
    @ApiOperation("全部标记已读")
    public Response<Boolean> markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return Response.ok(true);
    }

    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            User user = userService.getUserByUsername(username);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            return user.getId();
        }
        throw new RuntimeException("User not authenticated");
    }
}
