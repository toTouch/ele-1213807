package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.ElectricityCabinetServerOperRecord;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.CityMapper;
import com.xiliulou.electricity.mapper.ServiceFeeUserInfoMapper;
import com.xiliulou.electricity.service.CityService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 用户停卡绑定(TServiceFeeUserInfo)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
@Service("serviceFeeUserInfoService")
@Slf4j
public class ServiceFeeUserInfoServiceImpl implements ServiceFeeUserInfoService {

    @Resource
    ServiceFeeUserInfoMapper serviceFeeUserInfoMapper;

    @Autowired
    RedisService redisService;

    @Override
    public int insert(ServiceFeeUserInfo serviceFeeUserInfo) {
        return serviceFeeUserInfoMapper.insert(serviceFeeUserInfo);
    }

    @Override
    public int update(ServiceFeeUserInfo serviceFeeUserInfo) {
        return serviceFeeUserInfoMapper.update(serviceFeeUserInfo);
    }

    @Override
    public ServiceFeeUserInfo queryByUidFromCache(Long uid) {
        ServiceFeeUserInfo cache = redisService.getWithHash(CacheConstant.SERVICE_FEE_USER_INFO + uid, ServiceFeeUserInfo.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoMapper.selectOne(new LambdaQueryWrapper<ServiceFeeUserInfo>().eq(ServiceFeeUserInfo::getUid, uid).eq(ServiceFeeUserInfo::getDelFlag, ServiceFeeUserInfo.DEL_NORMAL));
        if (Objects.isNull(serviceFeeUserInfo)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.SERVICE_FEE_USER_INFO + uid, serviceFeeUserInfo);
        return serviceFeeUserInfo;
    }

    @Override
    public void updateByUid(ServiceFeeUserInfo serviceFeeUserInfo) {

        int update = serviceFeeUserInfoMapper.updateByUid(serviceFeeUserInfo);

        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
			redisService.delete(CacheConstant.SERVICE_FEE_USER_INFO + serviceFeeUserInfo.getUid());
            return null;
        });
        return;
    }


}
