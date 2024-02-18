package com.xiliulou.electricity.controller.admin.merchant;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import com.xiliulou.electricity.query.CallBackQuery;
import com.xiliulou.electricity.service.ElectricityCabinetFileService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.storage.config.StorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-14-14:07
 */
@RestController
public class JsonAdminMerchantPlaceFileController extends BaseController {
    @Autowired
    ElectricityCabinetFileService electricityCabinetFileService;
    
    @Autowired
    StorageConfig storageConfig;
    
    @PostMapping("/admin/merchant/place/file/call/back")
    public R callBack(@RequestBody CallBackQuery callBackQuery) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        if (ObjectUtil.isEmpty(callBackQuery.getFileNameList())) {
            return R.ok();
        }
        
        //换电柜
        if (ObjectUtil.equal(callBackQuery.getFileType(), ElectricityCabinetFile.TYPE_MERCHANT_PLACE)) {
            if (Objects.isNull(callBackQuery.getOtherId())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }
        }
        
        //先删除
        electricityCabinetFileService.deleteByDeviceInfo(callBackQuery.getOtherId(), callBackQuery.getFileType(), storageConfig.getIsUseOSS());
        
        
        //再新增
        if (Objects.equals(StorageConfig.IS_USE_OSS, storageConfig.getIsUseOSS())) {
            int index = 1;
            for (String fileName : callBackQuery.getFileNameList()) {
                ElectricityCabinetFile electricityCabinetFile = ElectricityCabinetFile.builder()
                        .createTime(System.currentTimeMillis())
                        .updateTime(System.currentTimeMillis())
                        .otherId(callBackQuery.getOtherId())
                        .type(callBackQuery.getFileType())
                        .url(StorageConfig.HTTPS + storageConfig.getBucketName() + "." + storageConfig.getOssEndpoint() + "/" + fileName)
                        .name(fileName)
                        .sequence(index)
                        .isOss(StorageConfig.IS_USE_OSS)
                        .tenantId(tenantId).build();
                electricityCabinetFileService.insert(electricityCabinetFile);
                index = index + 1;
            }
            
        } else {
            int index = 1;
            for (String fileName : callBackQuery.getFileNameList()) {
                ElectricityCabinetFile electricityCabinetFile = ElectricityCabinetFile.builder()
                        .createTime(System.currentTimeMillis())
                        .updateTime(System.currentTimeMillis())
                        .otherId(callBackQuery.getOtherId())
                        .type(callBackQuery.getFileType())
                        .bucketName(storageConfig.getBucketName())
                        .name(fileName)
                        .sequence(index)
                        .isOss(StorageConfig.IS_USE_MINIO)
                        .tenantId(tenantId).build();
                electricityCabinetFileService.insert(electricityCabinetFile);
                index = index + 1;
            }
        }
        
        return R.ok();
    }

}
