package com.library.book.controller;

import com.library.book.dto.PageResponse;
import com.library.book.dto.Response;
import com.library.book.entity.BorrowRecord;
import com.library.book.entity.Reservation;
import com.library.book.entity.User;
import com.library.book.service.ReservationService;
import com.library.book.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Reservation")
@RestController
@RequestMapping("/api/reader/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('READER', 'ADMIN')")
    @ApiOperation("预约图书")
    public Response<Reservation> createReservation(@RequestParam Long bookId) {
        Long userId = getCurrentUserId();
        Reservation reservation = reservationService.createReservation(userId, bookId);
        return Response.ok(reservation);
    }

    @PostMapping("/borrow/{reservationId}")
    @PreAuthorize("hasAnyRole('READER', 'ADMIN')")
    @ApiOperation("预约借阅")
    public Response<BorrowRecord> borrowFromReservation(@PathVariable Long reservationId) {
        Long userId = getCurrentUserId();
        BorrowRecord record = reservationService.borrowFromReservation(userId, reservationId);
        return Response.ok(record);
    }

    @PostMapping("/cancel/{reservationId}")
    @PreAuthorize("hasAnyRole('READER', 'ADMIN')")
    @ApiOperation("取消预约")
    public Response<Boolean> cancelReservation(@PathVariable Long reservationId) {
        Long userId = getCurrentUserId();
        reservationService.cancelReservation(userId, reservationId);
        return Response.ok(true);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('READER', 'ADMIN')")
    @ApiOperation("我的预约列表")
    public Response<PageResponse<Reservation>> getMyReservations(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getCurrentUserId();
        PageResponse<Reservation> result = reservationService.getMyReservations(userId, status, page, size);
        return Response.ok(result);
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
