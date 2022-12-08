package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.UserDeposit;
import com.xiliulou.electricity.mapper.UserDepositMapper;
import com.xiliulou.electricity.service.UserDepositService;
import com.xiliulou.electricity.utils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (UserDeposit)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-06 13:40:21
 */
@Service("userDepositService")
@Slf4j
public class UserDepositServiceImpl implements UserDepositService {
    
    @Autowired
    private UserDepositMapper userDepositMapper;
    
    @Autowired
    private RedisService redisService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserDeposit selectByUidFromDB(Long uid) {
        return this.userDepositMapper.selectByUid(uid);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserDeposit selectByUidFromCache(Long uid) {
        UserDeposit cacheUserDeposit = redisService
                .getWithHash(CacheConstant.CACHE_USER_DEPOSIT + uid, UserDeposit.class);
        if (Objects.nonNull(cacheUserDeposit)) {
            return cacheUserDeposit;
        }
        
        UserDeposit userDeposit = this.selectByUidFromDB(uid);
        if (Objects.isNull(userDeposit)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_USER_DEPOSIT + uid, userDeposit);
        
        return userDeposit;
    }
    
    /**
     * 新增数据
     *
     * @param userDeposit 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDeposit insert(UserDeposit userDeposit) {
        int insert = this.userDepositMapper.insertOne(userDeposit);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.saveWithHash(CacheConstant.CACHE_USER_DEPOSIT + userDeposit.getUid(), userDeposit);
            return null;
        });
        return userDeposit;
    }

    @Override
    public UserDeposit insertOrUpdate(UserDeposit userDeposit) {
        int insert = this.userDepositMapper.insertOrUpdate(userDeposit);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + userDeposit.getUid());
            return null;
        });
        return userDeposit;
    }

    /**
     * 修改数据
     *
     * @param userDeposit 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateByUid(UserDeposit userDeposit) {
        int update = this.userDepositMapper.updateByUid(userDeposit);
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + userDeposit.getUid());
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
        int delete = this.userDepositMapper.deleteByUid(uid);
        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + uid);
            return null;
        });
        return delete;
    }
}
