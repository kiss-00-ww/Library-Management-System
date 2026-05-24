package com.library.book.controller;

import com.library.book.config.SysConfigCache;
import com.library.book.dto.Response;
import com.library.book.entity.SystemConfig;
import com.library.book.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "System Config Management")
@RestController
@RequestMapping("/api/admin/config")
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private SysConfigCache sysConfigCache;

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Get all configs (Admin)")
    public Response<List<SystemConfig>> getConfigList() {
        List<SystemConfig> configs = systemConfigService.list();
        return Response.ok(configs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Get config by ID (Admin)")
    public Response<SystemConfig> getConfig(@PathVariable Long id) {
        SystemConfig config = systemConfigService.getById(id);
        return Response.ok(config);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Batch update configs (Admin)")
    public Response<Boolean> batchUpdate(@RequestBody Map<String, String> configMap) {
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            SystemConfig config = systemConfigService.getByKey(entry.getKey());
            if (config != null) {
                config.setConfigValue(entry.getValue());
                systemConfigService.updateById(config);
            }
        }
        // 刷新缓存，使配置实时生效
        sysConfigCache.refresh();
        return Response.ok(true);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Add config (Admin)")
    public Response<Boolean> addConfig(@RequestBody SystemConfig config) {
        boolean result = systemConfigService.save(config);
        if (result) {
            sysConfigCache.refresh();
        }
        return Response.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Delete config (Admin)")
    public Response<Boolean> deleteConfig(@PathVariable Long id) {
        boolean result = systemConfigService.removeById(id);
        if (result) {
            sysConfigCache.refresh();
        }
        return Response.ok(result);
    }

    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Refresh config cache (Admin)")
    public Response<Boolean> refreshCache() {
        sysConfigCache.refresh();
        return Response.ok(true);
    }
}
