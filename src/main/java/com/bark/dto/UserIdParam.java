package com.bark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * 用户ID参数
 * @author ballen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIdParam {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
