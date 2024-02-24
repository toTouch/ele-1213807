package com.xiliulou.electricity.controller.app;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author zgw
 * @date 2022/6/1 10:53
 * @mood
 */
@RestController
@Slf4j
@RefreshScope
public class JsonAppElectricityController extends BaseController {
    @Autowired
    @Qualifier("hwOssService")
    StorageService storageService;
    @Autowired
    StorageConfig storageConfig;
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @GetMapping("/outer/oss/config")
    public R getOssConfig() {
        Map<String, Object> result = new HashMap<>(3);
        result.put("appId", CommonConstant.APP_ID);
        result.put("ossAccessKeyId", storageConfig.getAccessKeyId());
        result.put("ossAccessKeySecret", storageConfig.getAccessKeySecret());
        return R.ok(result);
    }

    /**
     * 保存机柜日志
     */
    @PostMapping("/app/electricity/save/log")
    public R addOfficeAccountMerchantFile(@RequestParam("file") MultipartFile file,
                                          @RequestParam("deviceName") String deviceName,
                                          @RequestParam("productKey") String productKey) {
        if (Objects.isNull(deviceName) || Objects.isNull(productKey)) {
            return R.fail("请传入正确的信息");
        }

        ElectricityCabinet cupboard = electricityCabinetService.queryByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(cupboard)) {
            return R.fail("机柜不存在");
        }

        String objectName = "logs/" + deviceName + file.getOriginalFilename();
        try {
            storageService.uploadFile(storageConfig.getBucketName(), objectName, file.getInputStream());

        } catch (Exception e) {
            log.error("上传失败", e);
            e.printStackTrace();
        }
        return R.ok();
    }
}
