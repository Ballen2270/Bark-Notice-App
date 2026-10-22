package com.bark.service.impl;

import tools.jackson.databind.json.JsonMapper;
import com.bark.domain.DeviceConf;
import com.bark.domain.NoticeLog;
import com.bark.dto.ApiParam;
import com.bark.dto.NoticeApiParam;
import com.bark.dto.NoticeResponse;
import com.bark.mapper.DeviceConfMapper;
import com.bark.mapper.NoticeLogMapper;
import com.bark.properties.ApiUrlProperties;
import com.bark.service.NotifyService;
import com.bark.utils.AESCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Ballen  2024/11/27 10:56
 */
@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ApiUrlProperties apiUrlProperties;

    @Resource
    DeviceConfMapper deviceConfMapper;

    @Resource
    NoticeLogMapper noticeLogMapper;


    public static final String URL_PATH_SEPARATOR = "/";
    public static final String FAILED = "FAILED";
    public static final String SUCCESS = "SUCCESS";
    public static final Integer API_SUCCESS = 200;

    /**
     * 用于加密前序列化通知参数
     */
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    /**
     * 通知设备
     *
     * @param deviceKey
     * @param key
     * @param iv
     * @param param
     * @return
     */
    @Override
    public boolean notify(String deviceKey, String key, String iv, ApiParam param) {
        String url = apiUrlProperties.getUrl() + URL_PATH_SEPARATOR + deviceKey;
        AESCodec aesCodec = new AESCodec();
        NoticeLog noticeLog = new NoticeLog();
        noticeLog.setDeviceKey(deviceKey);
        noticeLog.setUrl(url);
        noticeLog.setTitle(param.getTitle());
        noticeLog.setBody(param.getBody());
        noticeLog.setGroup(param.getGroup());
        String res= null;
        try{
            String ciphertext = aesCodec.encrypt(JSON_MAPPER.writeValueAsString(param), key, iv);
            res = restTemplate.postForObject(url, new NoticeApiParam(ciphertext, iv), String.class);
            log.info("notice deviceKey:{}, res:{}", deviceKey, res);
            noticeLog.setResult(res);
        }catch (Exception e){
            log.warn("notice失败, deviceKey:{}, e:{}",deviceKey, e);
            noticeLog.setStatus(FAILED);
            noticeLog.setResult(res);
            noticeLogMapper.insert(noticeLog);
            return false;
        }
        if (!StringUtils.isEmpty(res) && res.contains(String.valueOf(API_SUCCESS))){
            noticeLog.setStatus(SUCCESS);
            noticeLogMapper.insert(noticeLog);
            return true;
        }
        noticeLog.setStatus(FAILED);
        noticeLogMapper.insert(noticeLog);
        return false;
    }

    /**
     * 通知所有设备
     *
     * @param param
     * @return
     */
    @Override
    public NoticeResponse noticeAll(ApiParam param) {
        List<DeviceConf> devices = deviceConfMapper.selectAll();
        List<DeviceConf> success = devices.stream().parallel().filter(device -> notify(device.getDeviceKey(), device.getEncodeKey(), device.getIv(), param)).collect(Collectors.toList());
        return new NoticeResponse(devices.size(), success.size());
    }
}
