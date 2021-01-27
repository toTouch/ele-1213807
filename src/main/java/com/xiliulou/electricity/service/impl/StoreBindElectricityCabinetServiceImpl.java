package com.xiliulou.electricity.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.StoreBindElectricityCabinet;
import com.xiliulou.electricity.mapper.StoreBindElectricityCabinetMapper;
import com.xiliulou.electricity.service.StoreBindElectricityCabinetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import java.util.List;

/**
 * (StoreBindElectricityCabinetBind)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Service("storeBindElectricityCabinetService")
@Slf4j
public class StoreBindElectricityCabinetServiceImpl implements StoreBindElectricityCabinetService {

    @Resource
    StoreBindElectricityCabinetMapper storeBindElectricityCabinetMapper;

    @Override
    public void deleteByStoreId(Integer storeId) {
        storeBindElectricityCabinetMapper.deleteByStoreId(storeId);
    }

    @Override
    public void insert(StoreBindElectricityCabinet storeBindElectricityCabinet) {
        storeBindElectricityCabinetMapper.insert(storeBindElectricityCabinet);
    }

    @Override
    public List<StoreBindElectricityCabinet> queryByStoreId(Integer storeId) {
        return storeBindElectricityCabinetMapper.selectList(new LambdaQueryWrapper<StoreBindElectricityCabinet>().eq(StoreBindElectricityCabinet::getStoreId,storeId));
    }
}