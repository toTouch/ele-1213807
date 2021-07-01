package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import org.springframework.web.multipart.MultipartFile;

public interface ElectricityPayParamsService {
    R saveOrUpdateElectricityPayParams(ElectricityPayParams electricityPayParams);

    ElectricityPayParams queryFromCache(Integer tenantId);

    R uploadFile(MultipartFile file);
}
