package com.library.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.book.entity.OperationLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface OperationLogService extends IService<OperationLog> {
    Page<OperationLog> getLogs(Integer page, Integer size, String keyword);
}
