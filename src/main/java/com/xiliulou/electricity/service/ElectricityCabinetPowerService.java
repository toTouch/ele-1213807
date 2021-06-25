package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetPower;
import com.xiliulou.electricity.query.ElectricityCabinetPowerQuery;


/**
 * 换电柜电量表(ElectricityCabinetPower)表服务接口
 *
 * @author makejava
 * @since 2021-01-27 16:22:44
 */
public interface ElectricityCabinetPowerService {


    Integer insertOrUpdate(ElectricityCabinetPower electricityCabinetPower);

    R queryList(ElectricityCabinetPowerQuery electricityCabinetPowerQuery);
}
