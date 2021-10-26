
package com.xiliulou.electricity.controller.user;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import com.xiliulou.electricity.service.ElectricityCabinetFileService;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 换电柜文件表(TElectricityCabinetFile)表控制层
 *
 * @author makejava
 * @since 2020-11-27 10:17:18
 */

@RestController
@Slf4j
public class JsonUserElectricityCabinetFileController {
    /**
     * 服务对象
     */

    @Autowired
    ElectricityCabinetFileService electricityCabinetFileService;
    @Autowired
    StorageConfig storageConfig;
    @Qualifier("aliyunOssService")
    @Autowired
    StorageService storageService;


    /**
     * 获取文件信息
     */

    @GetMapping("/user/electricityCabinetFileService/getFile")
    public R getFile( @RequestParam(value = "otherId", required = false) Long otherId,
                      @RequestParam("fileType") Integer fileType) {
        List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService.queryByDeviceInfo(otherId, fileType,storageConfig.getIsUseOSS());
        if (ObjectUtil.isEmpty(electricityCabinetFileList)) {
            return R.ok();
        }
        List<ElectricityCabinetFile> electricityCabinetFiles = new ArrayList<>();
        for (ElectricityCabinetFile electricityCabinetFile : electricityCabinetFileList) {
            if (Objects.equals(StorageConfig.IS_USE_OSS, storageConfig.getIsUseOSS())) {
                electricityCabinetFile.setUrl(storageService.getOssFileUrl(storageConfig.getBucketName(), electricityCabinetFile.getName(), System.currentTimeMillis() + 10 * 60 * 1000L));
            }
            electricityCabinetFiles.add(electricityCabinetFile);
        }
        return R.ok(electricityCabinetFiles);
    }


    /**
     * minio获取文件
     */

    @GetMapping("/user/electricityCabinetFileService/getMinioFile/{fileName}")
    public void getMinioFile(@PathVariable String fileName, HttpServletResponse response) {
        electricityCabinetFileService.getMinioFile(fileName, response);
    }



}

