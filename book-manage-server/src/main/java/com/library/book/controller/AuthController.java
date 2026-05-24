package com.library.book.controller;

import com.library.book.dto.LoginDTO;
import com.library.book.dto.RegisterDTO;
import com.library.book.dto.Response;
import com.library.book.entity.User;
import com.library.book.service.UserService;
import com.library.book.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Authentication")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/login")
    @ApiOperation("User login")
    public Response<String> login(@RequestBody LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        return Response.ok(token);
    }

    @PostMapping("/register")
    @ApiOperation("User registration")
    public Response<Boolean> register(@Valid @RequestBody RegisterDTO registerDTO) {
        boolean result = userService.register(registerDTO);
        return Response.ok(result);
    }

    @PostMapping("/logout")
    @ApiOperation("User logout")
    public Response<Void> logout() {
        SecurityContextHolder.clearContext();
        return Response.ok(null);
    }
}
