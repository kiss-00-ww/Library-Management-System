package com.library.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.book.dto.PageResponse;
import com.library.book.entity.OperationLog;
import com.library.book.mapper.OperationLogMapper;
import com.library.book.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public Page<OperationLog> getLogs(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(OperationLog::getModule, keyword)
                   .or()
                   .like(OperationLog::getContent, keyword);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }
}
