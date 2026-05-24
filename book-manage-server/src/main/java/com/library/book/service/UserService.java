package com.library.book.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.book.dto.UserDTO;
import com.library.book.entity.User;

public interface UserService extends com.baomidou.mybatisplus.extension.service.IService<User> {
    boolean register(com.library.book.dto.RegisterDTO registerDTO);
    UserDTO getUserInfo();
    User getUserByUsername(String username);
    boolean updateProfile(UserDTO userDTO);
    boolean updateAvatar(String avatarUrl);
    Page<User> getUsers(String keyword, Integer status, Integer page, Integer size);
    boolean updateUserStatus(Long id, Integer status);
    boolean updateUserRole(Long id, String role);
    boolean deleteUser(Long id);
    boolean changePassword(String oldPassword, String newPassword);
}
