package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.mapper.UserBatteryDepositMapper;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * (UserBatteryDeposit)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-06 13:40:21
 */
@Service("userBatteryDepositService")
@Slf4j
public class UserBatteryDepositServiceImpl implements UserBatteryDepositService {
    
    @Autowired
    private UserBatteryDepositMapper userBatteryDepositMapper;
    
    @Autowired
    private RedisService redisService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserBatteryDeposit selectByUidFromDB(Long uid) {
        return this.userBatteryDepositMapper.selectByUid(uid);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserBatteryDeposit selectByUidFromCache(Long uid) {
        UserBatteryDeposit cacheUserBatteryDeposit = redisService
                .getWithHash(CacheConstant.CACHE_USER_DEPOSIT + uid, UserBatteryDeposit.class);
        if (Objects.nonNull(cacheUserBatteryDeposit)) {
            return cacheUserBatteryDeposit;
        }
        
        UserBatteryDeposit userBatteryDeposit = this.selectByUidFromDB(uid);
        if (Objects.isNull(userBatteryDeposit)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_USER_DEPOSIT + uid, userBatteryDeposit);
        
        return userBatteryDeposit;
    }
    
    /**
     * 新增数据
     *
     * @param userBatteryDeposit 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insert(UserBatteryDeposit userBatteryDeposit) {
        int insert = this.userBatteryDepositMapper.insertOne(userBatteryDeposit);
        return insert;
    }

    @Override
    public UserBatteryDeposit insertOrUpdate(UserBatteryDeposit userBatteryDeposit) {
        int insert = this.userBatteryDepositMapper.insertOrUpdate(userBatteryDeposit);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + userBatteryDeposit.getUid());
            return null;
        });
        return userBatteryDeposit;
    }

    /**
     * 修改数据
     *
     * @param userBatteryDeposit 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateByUid(UserBatteryDeposit userBatteryDeposit) {
        int update = this.userBatteryDepositMapper.updateByUid(userBatteryDeposit);
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + userBatteryDeposit.getUid());
            return null;
        });
        return update;
    }
    
    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteByUid(Long uid) {
        int delete = this.userBatteryDepositMapper.deleteByUid(uid);
        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + uid);
            return null;
        });
        return delete;
    }

    @Override
    public Integer logicDeleteByUid(Long uid) {

        UserBatteryDeposit userBatteryDepositUpdate = new UserBatteryDeposit();
        userBatteryDepositUpdate.setUid(uid);
        userBatteryDepositUpdate.setDelFlag(UserBatteryDeposit.DEL_DEL);
        userBatteryDepositUpdate.setUpdateTime(System.currentTimeMillis());

        int update = this.userBatteryDepositMapper.updateByUid(userBatteryDepositUpdate);
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + uid);
            return null;
        });
        return update;
    }

    /**
     *同步车电一体数据
     *
     * @param uid 用户uid
     * @param mid 交押金时押金所属套餐id
     * @param orderId 押金订单号
     * @param batteryDeposit  押金金额
     * @return
     */
    @Override
    public Integer synchronizedUserBatteryDepositInfo(Long uid, Long mid, String orderId, BigDecimal batteryDeposit) {
        Integer result = null;
        UserBatteryDeposit userBatteryDeposit = this.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryDeposit)) {
            UserBatteryDeposit userBatteryDepositInsert = new UserBatteryDeposit();
            userBatteryDepositInsert.setUid(uid);
            userBatteryDepositInsert.setDid(0L);
            userBatteryDepositInsert.setOrderId(orderId);
            userBatteryDepositInsert.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
            userBatteryDepositInsert.setBatteryDeposit(batteryDeposit);
            userBatteryDepositInsert.setCreateTime(System.currentTimeMillis());
            userBatteryDepositInsert.setUpdateTime(System.currentTimeMillis());
            result = this.insert(userBatteryDepositInsert);
        } else {
            UserBatteryDeposit userBatteryDepositUpdate = new UserBatteryDeposit();
            userBatteryDepositUpdate.setUid(uid);
            userBatteryDepositUpdate.setDid(0L);
            userBatteryDepositUpdate.setOrderId(orderId);
            userBatteryDepositUpdate.setBatteryDeposit(batteryDeposit);
            userBatteryDepositUpdate.setUpdateTime(System.currentTimeMillis());
            result = this.updateByUid(userBatteryDepositUpdate);
        }

        return result;
    }
}
