package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.mapper.ElectricityPayParamsMapper;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.klock.annotation.Klock;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 11:36
 **/
@Service
@Slf4j
public class ElectricityPayParamsServiceImpl extends ServiceImpl<ElectricityPayParamsMapper, ElectricityPayParams> implements ElectricityPayParamsService {

    @Autowired
    RedisService redisService;

    /**
     * 新增或修改
     *
     * @param electricityPayParams
     * @return
     */
    @Override
    @Klock(name = ElectricityCabinetConstant.CACHE_ELECTRICITY_BATTERY_MODEL, keys = {"milli"}, waitTime = 20)
    public R saveOrUpdateElectricityPayParams(ElectricityPayParams electricityPayParams, Long milli) {
        ElectricityPayParams electricityPayParamsDb = getElectricityPayParams();
        electricityPayParams.setUpdateTime(System.currentTimeMillis());
        if (Objects.isNull(electricityPayParamsDb)) {
            electricityPayParams.setCreateTime(System.currentTimeMillis());
            baseMapper.insert(electricityPayParams);
            redisService.deleteKeys(ElectricityCabinetConstant.CACHE_PAY_PARAMS);
            return R.ok();
        } else {
            if (ObjectUtil.notEqual(electricityPayParamsDb.getId(), electricityPayParams.getId())) {
                return R.fail("请求参数id,不合法!");
            }
            redisService.deleteKeys(ElectricityCabinetConstant.CACHE_PAY_PARAMS);
            baseMapper.updateById(electricityPayParams);
            return R.ok();
        }
    }

    /**
     * 获取支付参数
     * valid_days
     * @return
     */
    @Override
    public ElectricityPayParams getElectricityPayParams() {
        ElectricityPayParams electricityPayParams = null;
        electricityPayParams = redisService.getWithHash(ElectricityCabinetConstant.CACHE_PAY_PARAMS, ElectricityPayParams.class);
        if (Objects.isNull(electricityPayParams)) {
            electricityPayParams = baseMapper.selectOne(Wrappers.lambdaQuery());
            if (Objects.nonNull(electricityPayParams)) {
                redisService.saveWithHash(ElectricityCabinetConstant.CACHE_PAY_PARAMS, electricityPayParams);
            }
        }
        return electricityPayParams;
    }
}
