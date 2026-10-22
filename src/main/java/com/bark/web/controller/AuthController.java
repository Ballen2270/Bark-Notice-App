package com.bark.web.controller;

import com.bark.core.BasicResponse;
import com.bark.domain.User;
import com.bark.dto.CheckInitRes;
import com.bark.dto.ChangePasswordParam;
import com.bark.dto.LoginParam;
import com.bark.dto.LoginResponse;
import com.bark.service.UserService;
import com.bark.utils.JwtUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 认证控制器
 *
 * @author ballen
 */
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final UserService userService;

    private final JwtUtils jwtUtils;

    public AuthController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 检查系统是否已初始化
     */
    @GetMapping("/checkInit")
    public BasicResponse<CheckInitRes> chekInit() {
        return BasicResponse.successToClient("检查初始化请求成功", userService.checkInit());
    }

    /**
     * 系统初始化注册
     */
    @PostMapping("/initRegister")
    public BasicResponse<?> initRegister(@Valid @RequestBody User user) {
        // 检查是否已经初始化
        if (userService.checkInit().getIsInit()) {
            return BasicResponse.errorToClient("系统已初始化，不能重复注册", null);
        }

        User registeredUser = userService.register(user);
        String token = jwtUtils.generateToken(registeredUser.getId(), user.getUsername());
        // 返回用户信息和令牌
        LoginResponse data = new LoginResponse(user, token);
        return BasicResponse.successToClient("初始化注册成功", data);
    }

    /**
     * 用户登录
     *
     * @param param   登录参数
     * @param request HTTP请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public BasicResponse<?> login(@Valid @RequestBody LoginParam param, HttpServletRequest request) {
        // 获取客户端IP
        String ip = getClientIp(request);

        // 登录验证
        User user = userService.login(param.getUsername(), param.getPassword(), param.getCaptcha(), param.getCaptchaKey(), ip);

        // 生成JWT令牌
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        // 返回用户信息和令牌
        LoginResponse data = new LoginResponse(user, token);

        return BasicResponse.successToClient("登录成功", data);
    }

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public BasicResponse<?> register(@Valid @RequestBody User user) {
        User registeredUser = userService.register(user);
        return BasicResponse.successToClient("注册成功", registeredUser);
    }

    /**
     * 修改密码
     *
     * @param param   修改密码参数
     * @param request HTTP请求
     * @return 修改结果
     */
    @PostMapping("/changePassword")
    public BasicResponse<?> changePassword(@Valid @RequestBody ChangePasswordParam param, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) {
            return BasicResponse.errorToClient("未授权", null);
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        boolean result = userService.changePassword(userId, param.getOldPassword(), param.getNewPassword());
        return result ? BasicResponse.successToClient("密码修改成功", null) : BasicResponse.errorToClient("密码修改失败", null);
    }

    /**
     * 获取当前用户信息
     * 
     * @param request HTTP请求
     * @return 用户信息
     */
    @GetMapping("/info")
    public BasicResponse<?> info(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) {
            return BasicResponse.errorToClient("未授权", null);
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        User user = userService.getUserById(userId);

        return BasicResponse.successToClient("获取用户信息成功", user);
    }

    /**
     * 从请求中获取JWT令牌
     * 
     * @param request HTTP请求
     * @return JWT令牌
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取客户端IP
     * 
     * @param request HTTP请求
     * @return 客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 多个代理的情况下，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}