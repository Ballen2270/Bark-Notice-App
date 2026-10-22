package com.bark.web.controller;

import com.bark.core.BasicResponse;
import com.bark.domain.User;
import com.bark.dto.UserIdParam;
import com.bark.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取所有用户
     * 
     * @return 用户列表
     */
    @GetMapping("/list")
    public BasicResponse<List<User>> getUserList() {
        List<User> users = userService.getAllUsers();
        return BasicResponse.successToClient("获取用户列表成功", users);
    }

    /**
     * 激活用户
     *
     * @param param 用户ID参数
     * @return 激活结果
     */
    @PostMapping("/activate")
    public BasicResponse<?> activateUser(@Valid @RequestBody UserIdParam param) {
        boolean result = userService.activateUser(param.getUserId());
        return result ? BasicResponse.successToClient("用户激活成功", null) : BasicResponse.errorToClient("用户激活失败", null);
    }

    /**
     * 停用用户
     *
     * @param param 用户ID参数
     * @return 停用结果
     */
    @PostMapping("/deactivate")
    public BasicResponse<?> deactivateUser(@Valid @RequestBody UserIdParam param) {
        boolean result = userService.deactivateUser(param.getUserId());
        return result ? BasicResponse.successToClient("用户停用成功", null) : BasicResponse.errorToClient("用户停用失败", null);
    }

    /**
     * 获取用户详情
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public BasicResponse<User> getUserDetail(@PathVariable("id") Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return BasicResponse.errorToClient("用户不存在", null);
        }
        return BasicResponse.successToClient("获取用户信息成功", user);
    }
}