package com.bark.web.controller;

import com.bark.core.BasicResponse;
import com.bark.dto.ApiParam;
import com.bark.dto.jellyfin.JellyFinWebhookParam;
import com.bark.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.text.MessageFormat;


/**
 * @Author Ballen  2024/11/27 11:01
 */
@Slf4j
@RequestMapping("/jellyfin")
@RestController
@Validated
public class JellyfinController {
    private final static String ITEM_ADDED = "ItemAdded";
    private final static String PLAYBACK_START = "PlaybackStart";
    private final static String PLAYBACK_STOP = "PlaybackStop";
    private final static String GROUP = "Jellyfin";
    private final static String TITLE = "\uD83C\uDFAC Jellyfin {0}";

    private final static String TV_BODY = "电视剧：{0}.S{1}E{2}.{3}";
    private final static String MOVIE_BODY = "电影：{0}({1})";
    private final static String DEVICE_BODY = "\n设备：{0}";
    private final static String USER_BODY = "用户：{0}\n";

    private final static String ITEM_ADDED_KEY = "增加";
    private final static String PLAYBACK_START_KEY = "播放";
    private final static String PLAYBACK_STOP_KEY = "暂停";
    private final static String ITEM_TYPE_EPISODE = "Episode";
    private final static String ITEM_TYPE_MOVIE = "Movie";

    private final NotifyService notifyService;

    public JellyfinController(NotifyService notifyService) {
        this.notifyService = notifyService;
    }


    @PostMapping("notice")
    public BasicResponse notice(@Valid @RequestBody JellyFinWebhookParam param){
        ApiParam apiParam = new ApiParam();
        apiParam.setGroup(GROUP);
        switch (param.getItemType()){
            case ITEM_TYPE_EPISODE:
                apiParam.setBody(MessageFormat.format(TV_BODY, param.getSeriesName(), param.getSeasonNumber(), param.getEpisodeNumber(), param.getName()));
                break;
            case ITEM_TYPE_MOVIE:
                apiParam.setBody(MessageFormat.format(MOVIE_BODY, param.getName(), param.getYear()));
                break;
            default:
                log.warn("不支持的ItemType:{}", param.getItemType());
                break;
        }
        switch (param.getNotificationType()){
            case ITEM_ADDED:
                apiParam.setTitle(MessageFormat.format(TITLE, ITEM_ADDED_KEY));
                break;
            case PLAYBACK_START:
                apiParam.setTitle(MessageFormat.format(TITLE, PLAYBACK_START_KEY));
                apiParam.setBody(MessageFormat.format(USER_BODY, param.getNotificationUsername()).concat(apiParam.getBody()).concat(MessageFormat.format(DEVICE_BODY, param.getDeviceName())));
                break;
            case PLAYBACK_STOP:
                apiParam.setTitle(MessageFormat.format(TITLE, PLAYBACK_STOP_KEY));
                apiParam.setBody(MessageFormat.format(USER_BODY, param.getNotificationUsername()).concat(apiParam.getBody()).concat(MessageFormat.format(DEVICE_BODY, param.getDeviceName())));
                break;
            default:
                apiParam.setTitle(MessageFormat.format(TITLE, ""));
                log.warn("不支持的NotificationType:{}", param.getNotificationType());
                break;
        }
        return BasicResponse.successToClient("发送成功", notifyService.noticeAll(apiParam));
    }
}
