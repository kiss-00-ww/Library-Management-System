package com.library.book.service;

import com.library.book.entity.SystemConfig;

public interface SystemConfigService extends com.baomidou.mybatisplus.extension.service.IService<SystemConfig> {
    SystemConfig getByKey(String configKey);
}
