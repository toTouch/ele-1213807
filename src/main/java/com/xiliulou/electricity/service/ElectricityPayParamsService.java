package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import org.springframework.web.multipart.MultipartFile;

public interface ElectricityPayParamsService extends IService<ElectricityPayParams> {
    R saveOrUpdateElectricityPayParams(ElectricityPayParams electricityPayParams);

    ElectricityPayParams queryFromCache(Integer tenantId);
    
    /**
     * 上传支付证书
     * @param file file
     * @param type type
     * @return R
     */
    R uploadFile(MultipartFile file,Integer type);

    R getTenantId(String appId);

    ElectricityPayParams selectTenantId(String appId);

    ElectricityPayParams queryByTenantId(Integer tenantId);
}
