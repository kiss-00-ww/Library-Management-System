package com.library.book.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.book.dto.PageResponse;
import com.library.book.dto.Response;
import com.library.book.entity.BorrowRecord;
import com.library.book.entity.User;
import com.library.book.service.BorrowRecordService;
import com.library.book.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "Borrow Record Management")
@RestController
@RequestMapping("/api")
public class BorrowRecordController {

    @Autowired
    private BorrowRecordService borrowRecordService;

    @Autowired
    private UserService userService;

    @PostMapping("/borrow/{bookId}")
    @ApiOperation("Borrow a book")
    public Response<BorrowRecord> borrowBook(@PathVariable Long bookId) {
        Long userId = getCurrentUserId();
        BorrowRecord record = borrowRecordService.borrowBook(userId, bookId);
        return Response.ok(record);
    }

    @PostMapping("/return/{recordId}")
    @ApiOperation("Return a book")
    public Response<BorrowRecord> returnBook(@PathVariable Long recordId) {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        BorrowRecord record = borrowRecordService.returnBook(recordId, userId, role);
        return Response.ok(record);
    }

    @PostMapping("/renew/{recordId}")
    @ApiOperation("Renew a borrow record")
    public Response<BorrowRecord> renewBook(@PathVariable Long recordId) {
        Long userId = getCurrentUserId();
        BorrowRecord record = borrowRecordService.renewBook(recordId, userId);
        return Response.ok(record);
    }

    @GetMapping("/borrow/my")
    @ApiOperation("Get current user's borrow records")
    public Response<PageResponse<BorrowRecord>> getMyBorrows(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getCurrentUserId();
        PageResponse<BorrowRecord> result = borrowRecordService.getMyBorrows(userId, status, page, size);
        return Response.ok(result);
    }

    @GetMapping("/borrow/my/counts")
    @ApiOperation("Get current user's borrow record counts by status")
    public Response<Map<String, Long>> getMyBorrowCounts() {
        Long userId = getCurrentUserId();
        Map<String, Long> counts = borrowRecordService.getBorrowCountsByUser(userId);
        return Response.ok(counts);
    }

    @GetMapping("/borrow/check")
    @ApiOperation("Check current user's borrow status for a specific book")
    public Response<BorrowRecord> checkBorrowStatus(@RequestParam Long bookId) {
        Long userId = getCurrentUserId();
        BorrowRecord record = borrowRecordService.getActiveBorrowByUserAndBook(userId, bookId);
        return Response.ok(record);
    }

    @GetMapping("/borrow/my/bookIds")
    @ApiOperation("Get current user's borrowed book IDs")
    public Response<List<Long>> getMyBorrowedBookIds() {
        Long userId = getCurrentUserId();
        List<Long> bookIds = borrowRecordService.getActiveBorrowedBookIdsByUser(userId);
        return Response.ok(bookIds);
    }

    @GetMapping("/admin/borrows")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Get all borrow records (Admin)")
    public Response<PageResponse<BorrowRecord>> getAllBorrows(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResponse<BorrowRecord> result = borrowRecordService.getAllBorrows(status, page, size);
        return Response.ok(result);
    }

    @GetMapping("/admin/borrows/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Get overdue records (Admin)")
    public Response<List<BorrowRecord>> getOverdueRecords() {
        List<BorrowRecord> records = borrowRecordService.getOverdueRecords();
        return Response.ok(records);
    }

    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication authentication =
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()).getUsername();
            User user = userService.getUserByUsername(username);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            return user.getId();
        }
        throw new RuntimeException("User not authenticated");
    }

    private String getCurrentUserRole() {
        org.springframework.security.core.Authentication authentication =
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .findFirst()
                .orElse("READER");
        }
        return "READER";
    }
}
