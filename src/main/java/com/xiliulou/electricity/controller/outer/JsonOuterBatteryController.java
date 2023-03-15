package com.xiliulou.electricity.controller.outer;

import com.google.common.collect.Maps;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.BatteryReportQuery;
import com.xiliulou.electricity.query.api.ApiRequestQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Slf4j
@RestController
@RefreshScope
public class JsonOuterBatteryController {

    @Autowired
    ElectricityCabinetService electricityCabinetService;

    /**
     * 电池电量上报
     *
     * @return
     */
    @PostMapping(value = "/outer/battery/info/report")
    public R batteryReport(@RequestBody BatteryReportQuery batteryReportQuery) {
        return electricityCabinetService.batteryReport(batteryReportQuery);
    }




}
