package com.bark.dto.jellyfin;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * @Author Ballen  2024/11/27 10:33
 */
@Data
public class JellyFinWebhookParam {
    /**
     * 通知类型
     */
    @NotBlank(message = "通知类型不能为空")
    private String notificationType;

    /**
     * 媒体类型 Episode
     */
    @NotBlank(message = "媒体类型不能为空")
    private String itemType;

    /**
     * 系列名称
     */
    private String seriesName;

    /**
     * 季度编号
     */
    private String seasonNumber;

    /**
     * 集编号
     */
    private String episodeNumber;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 年份
     */
    private String year;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 用户名称
     */
    private String notificationUsername;
}
