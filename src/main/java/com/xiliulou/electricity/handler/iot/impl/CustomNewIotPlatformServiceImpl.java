package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.config.NewIotConfig;
import com.xiliulou.iot.service.NewIotPlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : eclair
 * @date : 2023/7/11 10:06
 */
@Service()
@Slf4j
public class CustomNewIotPlatformServiceImpl implements NewIotPlatformService {
    public CustomNewIotPlatformServiceImpl() {
        log.info("new Custom Iot platform");
    }

    @Autowired
    NewIotConfig newIotConfig;

    @Override
    public boolean isNeedRequestNewIotInstance(String deviceName) {
        if (newIotConfig.getIsOpenAll()) {
            return true;
        }

        if (!DataUtil.collectionIsUsable(newIotConfig.getDeviceNames())) {
            return false;
        }

        return newIotConfig.getDeviceNames().contains(deviceName);
    }
}
