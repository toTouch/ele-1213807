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
public class JsonOuterElectricityCabinetController {

    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    StorageConfig storageConfig;

    @Qualifier("minioService")
    @Autowired
    StorageService storageService;

    @Value("${ele.apk.version:1.1.1}")
    String apkVersion;
    @Value("${ele.apk.url:https://ele.xiliulou.com/apk}")
    String apkUrl;

    @Value("${ele.qijiapk.version:1.1.1}")
    String qijiApkVersion;
    @Value("${ele.qijiapk.url:https://ele.xiliulou.com/apk}")
    String qijiUrl;

    /**
     * app检查版本
     *
     * @return
     */
    @GetMapping(value = "/outer/defaultVersion")
    public R getLastedAppVersion() {
        Map<String, Object> result = Maps.newHashMap();
        result.put("version", apkVersion);
        result.put("dir", apkUrl);
        return R.ok(result);
    }

    /**
     * app检查版本
     *
     * @return
     */
    @GetMapping(value = "/outer/defaultVersion/qiji")
    public R getLastedAppVersionForqiji() {
        Map<String, Object> result = Maps.newHashMap();
        result.put("version", qijiApkVersion);
        result.put("dir", qijiUrl);
        return R.ok(result);
    }

    //上传日志
    @PostMapping(value = "/outer/electricityCabinet/log")
    public R receiverAppLog(@RequestParam("productKey") String productKey,
                            @RequestParam("deviceName") String deviceName,
                            @RequestParam("file") MultipartFile file)  {

//        ElectricityCabinet electricityCabinet=electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey,deviceName);
//        if(Objects.isNull(electricityCabinet)){
//            return R.fail("ELECTRICITY.0005", "未找到换电柜");
//        }

        File tmpFile = null;
        ZipFile zipFile = null;
        try {
            tmpFile = new File("/tmp/tmp.zip");
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            file.transferTo(tmpFile);
            zipFile = new ZipFile(tmpFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String innerFileName = deviceName + "_" + "electricityCabinet" + "_" + zipEntry.getName();
                try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
                    String bucketName = storageConfig.getMinioBucketName();
                    storageService.uploadFile(bucketName, innerFileName, inputStream);
                }

            }
        } catch (Exception e) {
            log.error("unzip error! ", e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    log.error("unzip error! ", e);
                }
            }
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }

        return R.ok();
    }

    /**
     * 查询换电柜 按三元组
     *
     * @return
     */
    @GetMapping(value = "/outer/electricityCabinet")
    public R queryByDevice(@RequestParam("productKey") String productKey, @RequestParam("deviceName") String deviceName) {
        return electricityCabinetService.queryByDeviceOuter(productKey, deviceName);
    }

    /**
     * 电池电量上报
     *
     * @return
     */
    @PostMapping(value = "/outer/batteryReport")
    public R batteryReport(@RequestBody BatteryReportQuery batteryReportQuery) {
        return electricityCabinetService.batteryReport(batteryReportQuery);
    }

    /**
     * 电池电量上报
     *
     * @return
     */
    @GetMapping(value = "/outer/checkBattery")
    public R checkBattery(@RequestParam("productKey") String productKey, @RequestParam("deviceName") String deviceName,
            @RequestParam("batterySn") String batterySn,@RequestParam(value = "isParseBattery", required = false) Boolean isParseBattery) {
        return electricityCabinetService.checkBattery(productKey,deviceName,batterySn,isParseBattery);
    }

    /**
     * 电池电量上报GPS倒序
     *
     * @return
     */
//    @PostMapping(value = "/outer/batteryReportForGps")
//    public R batteryReportForGps(@RequestBody BatteryReportQuery batteryReportQuery) {
//        return electricityCabinetService.batteryReport(batteryReportQuery);
//    }

    /**
     * 三元组前置检测
     * @param apiRequestQuery
     * @return
     */
    @PostMapping("/outer/checkDevice")
    public R queryDeviceIsUnActiveFStatus(@RequestBody ApiRequestQuery apiRequestQuery){
        return electricityCabinetService.queryDeviceIsUnActiveFStatus(apiRequestQuery);
    }


    /**
     * 网络检测检测
     * @return
     */
    @GetMapping("/outer/heartbeat/check")
    public R queryHeartbeat(){
        return R.ok();
    }



}
