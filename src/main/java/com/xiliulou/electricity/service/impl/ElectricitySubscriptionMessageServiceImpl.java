package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.ElectricitySubscriptionMessage;
import com.xiliulou.electricity.mapper.ElectricitySubscriptionMessageMapper;
import com.xiliulou.electricity.service.ElectricitySubscriptionMessageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 17:57
 **/
@Service
@Slf4j
public class ElectricitySubscriptionMessageServiceImpl extends ServiceImpl<ElectricitySubscriptionMessageMapper, ElectricitySubscriptionMessage> implements ElectricitySubscriptionMessageService {

    @Autowired
    RedisService redisService;

    @Resource
    ElectricitySubscriptionMessageMapper electricitySubscriptionMessageMapper;

    /**
     * 保存订阅消息
     *
     * @param electricitySubscriptionMessage
     * @return
     */
    @Override
    public R saveElectricitySubscriptionMessage(ElectricitySubscriptionMessage electricitySubscriptionMessage) {

        Boolean getLockerSuccess = redisService.setNx(CacheConstant.ADMIN_OPERATE_LOCK_KEY,
                String.valueOf(System.currentTimeMillis()), 20 * 1000L, true);
        if (!getLockerSuccess) {
            return R.failMsg("操作频繁!");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        ElectricitySubscriptionMessage electricitySubscriptionMessageDb = getSubscriptionMessageByType(electricitySubscriptionMessage.getType(), tenantId);
        if (Objects.nonNull(electricitySubscriptionMessageDb)) {
            return R.failMsg("您已添加此订阅消息!");
        }
        electricitySubscriptionMessage.setCreateTime(System.currentTimeMillis());
        electricitySubscriptionMessage.setUpdateTime(System.currentTimeMillis());
        electricitySubscriptionMessage.setTenantId(tenantId);
        Integer raws = baseMapper.insert(electricitySubscriptionMessage);

        redisService.delete(CacheConstant.ADMIN_OPERATE_LOCK_KEY);
        if (raws > 0) {
            return R.ok();
        } else {
            return R.failMsg("新增失败!");
        }
    }

    /**
     * @param type
     * @return
     */
    @Override
    @Slave
    public ElectricitySubscriptionMessage getSubscriptionMessageByType(Integer type, Integer tenantId) {
        ElectricitySubscriptionMessage electricitySubscriptionMessage = null;
        electricitySubscriptionMessage = redisService.getWithHash(CacheConstant.CACHE_SUBSCRIPTION_MESSAGE + tenantId + type, ElectricitySubscriptionMessage.class);
        if (Objects.isNull(electricitySubscriptionMessage)) {
            electricitySubscriptionMessage = baseMapper.selectOne(Wrappers.<ElectricitySubscriptionMessage>lambdaQuery()
                    .eq(ElectricitySubscriptionMessage::getType, type)
                    .eq(ElectricitySubscriptionMessage::getTenantId, tenantId));
            if (Objects.nonNull(electricitySubscriptionMessage)) {
                redisService.saveWithHash(CacheConstant.CACHE_SUBSCRIPTION_MESSAGE + tenantId + type, electricitySubscriptionMessage);
            }
        }
        return electricitySubscriptionMessage;
    }

    /**
     * @param electricitySubscriptionMessage
     * @return
     */
    @Override
    public R updateElectricitySubscriptionMessage(ElectricitySubscriptionMessage electricitySubscriptionMessage) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        Boolean getLockerSuccess = redisService.setNx(CacheConstant.ADMIN_OPERATE_LOCK_KEY,
                String.valueOf(System.currentTimeMillis()), 20 * 1000L, true);
        if (!getLockerSuccess) {
            return R.failMsg("操作频繁!");
        }

        if (!Objects.equals(tenantId,electricitySubscriptionMessage.getTenantId())){
            return R.ok();
        }

        electricitySubscriptionMessage.setUpdateTime(System.currentTimeMillis());
        Integer raws = electricitySubscriptionMessageMapper.update(electricitySubscriptionMessage);
        redisService.delete(CacheConstant.ADMIN_OPERATE_LOCK_KEY);
        if (raws > 0) {
            return R.ok();
        } else {
            return R.failMsg("修改失败!");
        }

    }

    @Override
    @Slave
    public R getElectricitySubscriptionMessagePage(Integer type, Integer tenantId) {
        return R.ok(baseMapper.selectList(Wrappers.<ElectricitySubscriptionMessage>lambdaQuery()
                .eq(Objects.nonNull(type), ElectricitySubscriptionMessage::getType, type)
                .eq(ElectricitySubscriptionMessage::getTenantId, tenantId)));
    }
}
