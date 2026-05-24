package com.library.book.controller;

import com.library.book.dto.PasswordDTO;
import com.library.book.dto.Response;
import com.library.book.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Password Management")
@RestController
@RequestMapping("/api")
public class PasswordController {

    @Autowired
    private UserService userService;

    @PutMapping("/user/password")
    @ApiOperation("Change password")
    public Response<Boolean> changePassword(@Valid @RequestBody PasswordDTO passwordDTO) {
        boolean result = userService.changePassword(passwordDTO.getOldPassword(), passwordDTO.getNewPassword());
        return Response.ok(result);
    }
}
