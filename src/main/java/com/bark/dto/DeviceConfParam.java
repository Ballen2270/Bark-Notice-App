package com.bark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * @Author Ballen  2024/10/23 17:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceConfParam {
    @NotBlank(message = "device token不能为空")
    private String deviceToken;
    @NotBlank(message = "device name不能为空")
    private String name;
    @NotBlank(message = "device key不能为空")
    private String deviceKey;
    private String algorithm;
    private String model;
    private String padding;
    private String encodeKey;
    private String iv;
}
