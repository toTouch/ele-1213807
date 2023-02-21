package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.mapper.UserBatteryDepositMapper;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.utils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

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
    public UserBatteryDeposit insert(UserBatteryDeposit userBatteryDeposit) {
        int insert = this.userBatteryDepositMapper.insertOne(userBatteryDeposit);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.saveWithHash(CacheConstant.CACHE_USER_DEPOSIT + userBatteryDeposit.getUid(), userBatteryDeposit);
            return null;
        });
        return userBatteryDeposit;
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
}
