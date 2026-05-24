package com.library.book.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String role;
    private Integer status;
    private String avatar;
    private LocalDateTime createTime;
}
