package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetPower;
import com.xiliulou.electricity.mapper.ElectricityCabinetPowerMapper;
import com.xiliulou.electricity.query.ElectricityCabinetPowerQuery;
import com.xiliulou.electricity.service.ElectricityCabinetPowerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

/**
 * 换电柜电量表(ElectricityCabinetPower)表服务实现类
 *
 * @author makejava
 * @since 2021-01-27 16:22:44
 */
@Service("electricityCabinetPowerService")
@Slf4j
public class ElectricityCabinetPowerServiceImpl implements ElectricityCabinetPowerService {
    @Resource
    private ElectricityCabinetPowerMapper electricityCabinetPowerMapper;




    @Override
    public Integer insertOrUpdate(ElectricityCabinetPower electricityCabinetPower) {
        return this.electricityCabinetPowerMapper.insertOrUpdate(electricityCabinetPower);
    }

    @Override
    public R queryList(ElectricityCabinetPowerQuery electricityCabinetPowerQuery) {
        return R.ok(electricityCabinetPowerMapper.queryList(electricityCabinetPowerQuery));
    }

}
