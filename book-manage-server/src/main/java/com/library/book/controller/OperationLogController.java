package com.library.book.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.book.dto.PageResponse;
import com.library.book.dto.Response;
import com.library.book.entity.OperationLog;
import com.library.book.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Operation Log Management")
@RestController
@RequestMapping("/api/admin/logs")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping
    @ApiOperation("Get operation logs with pagination")
    public Response<PageResponse<OperationLog>> getLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<OperationLog> pageResult = operationLogService.getLogs(page, size, keyword);
        return Response.ok(PageResponse.ok(pageResult.getTotal(), pageResult.getRecords()));
    }
}
