package com.library.book.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.book.dto.PageResponse;
import com.library.book.dto.Response;
import com.library.book.dto.UserDTO;
import com.library.book.entity.User;
import com.library.book.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Api(tags = "User Management")
@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @org.springframework.beans.factory.annotation.Value("${upload.dir:${user.dir}/uploads}")
    private String uploadBaseDir;

    @GetMapping("/auth/info")
    @ApiOperation("Get current user info")
    public Response<UserDTO> getUserInfo() {
        UserDTO userDTO = userService.getUserInfo();
        return Response.ok(userDTO);
    }

    @PutMapping("/user/profile")
    @ApiOperation("Update current user profile")
    public Response<Boolean> updateProfile(@RequestBody UserDTO userDTO) {
        boolean result = userService.updateProfile(userDTO);
        return Response.ok(result);
    }

    @PostMapping("/user/avatar")
    @ApiOperation("Upload avatar")
    public Response<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Response.fail("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".jpg";

        String fileName = UUID.randomUUID().toString() + ext;
        String uploadDir = uploadBaseDir + "/avatars/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            File dest = new File(uploadDir + fileName);
            file.transferTo(dest);
            String avatarUrl = "/uploads/avatars/" + fileName;
            userService.updateAvatar(avatarUrl);
            return Response.ok(avatarUrl);
        } catch (IOException e) {
            return Response.fail("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Get user list (Admin)")
    public Response<PageResponse<User>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<User> result = userService.getUsers(keyword, status, page, size);
        return Response.ok(PageResponse.ok(result.getTotal(), result.getRecords()));
    }

    @PutMapping("/admin/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Update user status (Admin)")
    public Response<Boolean> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean result = userService.updateUserStatus(id, status);
        return Response.ok(result);
    }

    @PutMapping("/admin/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Update user role (Admin)")
    public Response<Boolean> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        if (!"ADMIN".equals(role) && !"READER".equals(role) && !"TEACHER".equals(role)) {
            return Response.fail("无效的角色类型");
        }
        boolean result = userService.updateUserRole(id, role);
        return Response.ok(result);
    }

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Delete user (Admin)")
    public Response<Boolean> deleteUser(@PathVariable Long id) {
        boolean result = userService.deleteUser(id);
        return Response.ok(result);
    }
}
