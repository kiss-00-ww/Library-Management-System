package com.library.book.controller;

import com.library.book.dto.Response;
import com.library.book.dto.StatisticsResponse;
import com.library.book.service.BorrowRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "Statistics")
@RestController
@RequestMapping("/api/admin/statistics")
public class StatisticsController {

    @Autowired
    private BorrowRecordService borrowRecordService;

    @GetMapping("/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Get inventory statistics")
    public Response<StatisticsResponse> getInventoryStatistics() {
        StatisticsResponse statistics = borrowRecordService.getStatistics();
        return Response.ok(statistics);
    }

    @GetMapping("/popular")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Get popular books")
    public Response<StatisticsResponse> getPopularBooks() {
        StatisticsResponse statistics = borrowRecordService.getPopularBooksStatistics();
        return Response.ok(statistics);
    }

    @GetMapping("/trend")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Get borrow trend by days")
    public Response<Map<String, Long>> getBorrowTrend(@RequestParam(defaultValue = "30") int days) {
        Map<String, Long> trend = borrowRecordService.getBorrowTrend(days);
        return Response.ok(trend);
    }
}
