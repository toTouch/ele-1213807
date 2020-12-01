package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityPayParams;

public interface ElectricityPayParamsService {
    R saveOrUpdateElectricityPayParams(ElectricityPayParams electricityPayParams, Long milli);

    ElectricityPayParams getElectricityPayParams();
}
