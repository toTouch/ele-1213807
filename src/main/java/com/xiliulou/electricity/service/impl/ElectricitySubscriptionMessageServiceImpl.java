package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricitySubscriptionMessage;
import com.xiliulou.electricity.mapper.ElectricitySubscriptionMessageMapper;
import com.xiliulou.electricity.service.ElectricitySubscriptionMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * 保存订阅消息
     *
     * @param electricitySubscriptionMessage
     * @return
     */
    @Override
    public R saveElectricitySubscriptionMessage(ElectricitySubscriptionMessage electricitySubscriptionMessage) {
        Boolean getLockerSuccess = redisService.setNx(ElectricityCabinetConstant.ADMIN_OPERATE_LOCK_KEY,
                String.valueOf(System.currentTimeMillis()), 20 * 1000L, true);
        if (!getLockerSuccess) {
            return R.failMsg("操作频繁!");
        }
        ElectricitySubscriptionMessage electricitySubscriptionMessageDb = getSubscriptionMessageByType(electricitySubscriptionMessage.getType());
        if (Objects.nonNull(electricitySubscriptionMessageDb)) {
            return R.failMsg("您已添加此订阅消息!");
        }
        electricitySubscriptionMessage.setCreateTime(System.currentTimeMillis());
        electricitySubscriptionMessage.setUpdateTime(System.currentTimeMillis());
        Integer raws = baseMapper.insert(electricitySubscriptionMessage);

        redisService.delete(ElectricityCabinetConstant.ADMIN_OPERATE_LOCK_KEY);
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
    @DS("slave_1")
    public ElectricitySubscriptionMessage getSubscriptionMessageByType(Integer type) {
        ElectricitySubscriptionMessage electricitySubscriptionMessage = null;
        electricitySubscriptionMessage = redisService.getWithHash(ElectricityCabinetConstant.CACHE_SUBSCRIPTION_MESSAGE + type, ElectricitySubscriptionMessage.class);
        if (Objects.isNull(electricitySubscriptionMessage)) {
            electricitySubscriptionMessage = baseMapper.selectOne(Wrappers.<ElectricitySubscriptionMessage>lambdaQuery()
                    .eq(ElectricitySubscriptionMessage::getType, type));
            if (Objects.nonNull(electricitySubscriptionMessage)) {
                redisService.saveWithHash(ElectricityCabinetConstant.CACHE_SUBSCRIPTION_MESSAGE + type, electricitySubscriptionMessage);
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
        Boolean getLockerSuccess = redisService.setNx(ElectricityCabinetConstant.ADMIN_OPERATE_LOCK_KEY,
                String.valueOf(System.currentTimeMillis()), 20 * 1000L, true);
        if (!getLockerSuccess) {
            return R.failMsg("操作频繁!");
        }
        ElectricitySubscriptionMessage electricitySubscriptionMessageDb = baseMapper.selectOne(Wrappers.<ElectricitySubscriptionMessage>lambdaQuery()
                .eq(ElectricitySubscriptionMessage::getType, electricitySubscriptionMessage.getType())
                .ne(ElectricitySubscriptionMessage::getId, electricitySubscriptionMessage.getId()));
        if (Objects.nonNull(electricitySubscriptionMessageDb)) {
            return R.failMsg("您已添加此订阅消息!");
        }
        electricitySubscriptionMessage.setCreateTime(null);
        electricitySubscriptionMessage.setUpdateTime(System.currentTimeMillis());
        Integer raws = baseMapper.updateById(electricitySubscriptionMessage);
        redisService.delete(ElectricityCabinetConstant.ADMIN_OPERATE_LOCK_KEY);
        if (raws > 0) {
            return R.ok();
        } else {
            return R.failMsg("修改失败!");
        }

    }

    /**
     * 删除 缓存
     *
     * @param type
     */
    @Override
    public void delSubscriptionMessageCacheByType(Integer type) {
        redisService.delete(ElectricityCabinetConstant.CACHE_SUBSCRIPTION_MESSAGE + type);
    }

    @Override
    @DS("slave_1")
    public R getElectricitySubscriptionMessagePage(Integer type) {
        return R.ok(baseMapper.selectList(Wrappers.<ElectricitySubscriptionMessage>lambdaQuery()
                .eq(Objects.nonNull(type), ElectricitySubscriptionMessage::getType, type)));
    }
}
