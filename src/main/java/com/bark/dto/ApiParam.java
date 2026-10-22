package com.bark.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * @Author Ballen  2024/10/23 15:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiParam {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "消息不能为空")
    private String body;
    private String group;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String url;
}
