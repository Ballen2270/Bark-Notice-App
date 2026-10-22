package com.bark.web.controller;

import com.bark.core.BasicResponse;
import com.bark.domain.DeviceConf;
import com.bark.dto.*;
import com.bark.mapper.DeviceConfMapper;
import com.bark.properties.ApiUrlProperties;
import com.bark.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.validation.Valid;

/**
 * @Author Ballen 2024/2/6 12:38
 */
@Slf4j
@RequestMapping("/")
@RestController
@Validated
public class NoticeController {

    public static final String REGISTER_PRE = "/register?devicetoken=";
    public static final String REGISTER_KEY_PRE = "&key=";
    public static final String PING = "ping";
    public static final String URL_PATH_SEPARATOR = "/";
    public static final Integer API_SUCCESS = 200;
    public final static String ACTIVE = "ACTIVE";

    private final RestTemplate restTemplate;
    private final NotifyService notifyService;
    private final DeviceConfMapper deviceConfMapper;
    private final ApiUrlProperties apiUrlProperties;

    public NoticeController(RestTemplate restTemplate, NotifyService notifyService, DeviceConfMapper deviceConfMapper,
            ApiUrlProperties apiUrlProperties) {
        this.restTemplate = restTemplate;
        this.notifyService = notifyService;
        this.deviceConfMapper = deviceConfMapper;
        this.apiUrlProperties = apiUrlProperties;
    }

    @GetMapping("ping")
    public PingResponse ping() {
        return restTemplate.getForObject(apiUrlProperties.getUrl() + URL_PATH_SEPARATOR + PING, PingResponse.class);
    }

    @GetMapping("register")
    public RegisterResponse register(@RequestParam String devicetoken, @RequestParam String key) {
        String registerUrl = genRegisterUrl(devicetoken, key);
        RegisterResponse res = restTemplate.getForObject(registerUrl, RegisterResponse.class);
        if (res == null || !API_SUCCESS.equals(res.getCode())) {
            return res;
        }
        DeviceConf deviceConf = deviceConfMapper.selectByPrimaryKey(devicetoken);
        if (deviceConf != null) {
            deviceConf.setDeviceKey(res.getData().getDeviceKey());
            deviceConfMapper.updateByPrimaryKey(deviceConf);
        } else {
            DeviceConf newConf = new DeviceConf();
            newConf.setDeviceToken(devicetoken);
            newConf.setDeviceKey(res.getData().getDeviceKey());
            newConf.setStatus(ACTIVE);
            deviceConfMapper.insert(newConf);
        }
        return res;
    }

    private String genRegisterUrl(String devicetoken, String key) {
        StringBuilder sb = new StringBuilder(apiUrlProperties.getUrl()).append(REGISTER_PRE).append(devicetoken);
        ;
        if (!StringUtils.isEmpty(key)) {
            sb.append(REGISTER_KEY_PRE).append(key);
        }
        return sb.toString();
    }

    @GetMapping("notice")
    public BasicResponse notice(@RequestParam String title, @RequestParam String body,
            @RequestParam(required = false) String group) {
        return BasicResponse.successToClient("发送成功", notifyService.noticeAll(new ApiParam(title, body, group, null)));
    }

    @GetMapping("notice/{title}/{body}")
    public BasicResponse noticePathVariable(@PathVariable("title") String title, @PathVariable("body") String body,
            @RequestParam(required = false) String group) {
        return BasicResponse.successToClient("发送成功", notifyService.noticeAll(new ApiParam(title, body, group, null)));
    }

    @PostMapping("notice")
    public BasicResponse notice(@Valid @RequestBody ApiParam param) {
        return BasicResponse.successToClient("发送成功", notifyService.noticeAll(param));
    }
}
