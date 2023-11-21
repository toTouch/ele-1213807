package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.asset.ElectricityCabinetRequest;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @since 2023-11-21
 * @author zhangyongbo
 *
 */
@Service("electricityCabinetV2Service")
@Slf4j
public class ElectricityCabinetV2ServiceImpl implements ElectricityCabinetV2Service {
    
    @Override
    public R save(ElectricityCabinetRequest electricityCabinetRequest) {
        //校验场景min
        String manufacturerModel = electricityCabinetRequest.getManufacturerModel();
        
        return null;
    }
}
