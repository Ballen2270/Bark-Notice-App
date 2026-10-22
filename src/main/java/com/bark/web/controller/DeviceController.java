package com.bark.web.controller;

import com.bark.core.BasicResponse;
import com.bark.domain.DeviceConf;
import com.bark.dto.DeviceConfDTO;
import com.bark.dto.DeviceConfParam;
import com.bark.mapper.DeviceConfMapper;
import com.bark.utils.RandomStringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Ballen  2024/10/23 17:38
 */
@Slf4j
@RequestMapping("/device")
@RestController
@Validated
public class DeviceController {
    public final static String ACTIVE = "ACTIVE";
    public final static String STOP = "STOP";
    public final static String DEFAULT_ALGORITHM = "AES";
    public final static String DEFAULT_MODEL = "CBC";
    public final static String DEFAULT_PADDING = "PKCS7Padding";

    private  final DeviceConfMapper deviceConfMapper;
    public DeviceController(DeviceConfMapper deviceConfMapper) {
        this.deviceConfMapper = deviceConfMapper;
    }

    @GetMapping("query")
    public BasicResponse query(@RequestParam String deviceToken){
        DeviceConf conf = deviceConfMapper.selectByPrimaryKey(deviceToken);
        if (conf == null){
            return BasicResponse.errorToClient("设备不存在", null);
        }
        return BasicResponse.successToClient("查询成功", conf);
    }

    @GetMapping("queryAll")
    public BasicResponse queryAll(){
        List<DeviceConf> confs = deviceConfMapper.selectAll();
        List<DeviceConfDTO> dtos = confs.stream().map(DeviceConf::toDTO).collect(Collectors.toList());
        return BasicResponse.successToClient("查询成功", dtos);
    }

    @PostMapping("gen")
    public BasicResponse genDevice(@Valid @RequestBody DeviceConfParam param){
        DeviceConf deviceConf = deviceConfMapper.selectByPrimaryKey(param.getDeviceToken());
        if (deviceConf != null){
            deviceConf.setName(param.getName());
            deviceConf.setDeviceKey(param.getDeviceKey());
            deviceConf.setAlgorithm(StringUtils.isEmpty(param.getAlgorithm()) ? DEFAULT_ALGORITHM : param.getAlgorithm());
            deviceConf.setModel(StringUtils.isEmpty(param.getModel()) ? DEFAULT_MODEL : param.getModel());
            deviceConf.setPadding(StringUtils.isEmpty(param.getPadding()) ? DEFAULT_PADDING : param.getPadding());
            deviceConf.setEncodeKey(StringUtils.isEmpty(param.getEncodeKey()) ? RandomStringUtil.genRandomString(32) : param.getEncodeKey());
            deviceConf.setIv(StringUtils.isEmpty(param.getIv()) ? RandomStringUtil.genRandomString(16) : param.getIv());
            deviceConf.setStatus(ACTIVE);
            deviceConfMapper.updateByPrimaryKey(deviceConf);
            return BasicResponse.successToClient("修改成功", deviceConf);
        }
        DeviceConf newConf = new DeviceConf();
        newConf.setDeviceToken(param.getDeviceToken());
        newConf.setName(param.getName());
        newConf.setDeviceKey(param.getDeviceKey());
        newConf.setAlgorithm(StringUtils.isEmpty(param.getAlgorithm()) ? DEFAULT_ALGORITHM : param.getAlgorithm());
        newConf.setModel(StringUtils.isEmpty(param.getModel()) ? DEFAULT_MODEL : param.getModel());
        newConf.setPadding(StringUtils.isEmpty(param.getPadding()) ? DEFAULT_PADDING : param.getPadding());
        newConf.setEncodeKey(StringUtils.isEmpty(param.getEncodeKey()) ? RandomStringUtil.genRandomString(32) : param.getEncodeKey());
        newConf.setIv(StringUtils.isEmpty(param.getIv()) ? RandomStringUtil.genRandomString(16) : param.getIv());
        newConf.setStatus(ACTIVE);
        deviceConfMapper.insert(newConf);
        return BasicResponse.successToClient("添加成功", newConf);
    }

    @GetMapping("active")
    public BasicResponse active(@RequestParam String deviceToken){
        DeviceConf deviceConf = deviceConfMapper.selectByPrimaryKey(deviceToken);
        if (deviceConf == null){
            return BasicResponse.errorToClient("设备不存在", null);
        }
        deviceConf.setStatus(ACTIVE);
        deviceConfMapper.updateByPrimaryKey(deviceConf);
        return BasicResponse.successToClient("激活成功", null);
    }

    @GetMapping("stop")
    public BasicResponse stop(@RequestParam String deviceToken){
        DeviceConf deviceConf = deviceConfMapper.selectByPrimaryKey(deviceToken);
        if (deviceConf == null){
            return BasicResponse.errorToClient("设备不存在", null);
        }
        deviceConf.setStatus(STOP);
        deviceConfMapper.updateByPrimaryKey(deviceConf);
        return BasicResponse.successToClient("禁止成功", null);
    }
}
