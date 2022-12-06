package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.UserBatteryServiceFee;
import com.xiliulou.electricity.mapper.UserBatteryServiceFeeMapper;
import com.xiliulou.electricity.service.UserBatteryServiceFeeService;
import com.xiliulou.electricity.utils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (UserBatteryServiceFee)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-06 13:39:51
 */
@Service("userBatteryServiceFeeService")
@Slf4j
public class UserBatteryServiceFeeServiceImpl implements UserBatteryServiceFeeService {
    
    @Autowired
    private UserBatteryServiceFeeMapper userBatteryServiceFeeMapper;
    @Autowired
    private RedisService redisService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserBatteryServiceFee selectByUidFromDB(Long uid) {
        return this.userBatteryServiceFeeMapper.selectByUid(uid);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserBatteryServiceFee selectByUidFromCache(Long uid) {
        UserBatteryServiceFee cacheUserBatteryServiceFee = redisService
                .getWithHash(CacheConstant.CACHE_USER_BATTERY_SERVICE_FEE + uid, UserBatteryServiceFee.class);
        if(Objects.nonNull(cacheUserBatteryServiceFee)){
            return  cacheUserBatteryServiceFee;
        }
    
        UserBatteryServiceFee userBatteryServiceFee = this.selectByUidFromDB(uid);
        if(Objects.isNull(userBatteryServiceFee)){
            return null;
        }
    
        redisService.saveWithHash(CacheConstant.CACHE_USER_BATTERY_SERVICE_FEE + uid, userBatteryServiceFee);
        
        return userBatteryServiceFee;
    }
    
    /**
     * 新增数据
     *
     * @param userBatteryServiceFee 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserBatteryServiceFee insert(UserBatteryServiceFee userBatteryServiceFee) {
        int insert = this.userBatteryServiceFeeMapper.insertOne(userBatteryServiceFee);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.saveWithHash(CacheConstant.CACHE_USER_BATTERY_SERVICE_FEE + userBatteryServiceFee.getUid(), userBatteryServiceFee);
            return null;
        });
        return userBatteryServiceFee;
    }
    
    /**
     * 修改数据
     *
     * @param userBatteryServiceFee 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateByUid(UserBatteryServiceFee userBatteryServiceFee) {
        int update= this.userBatteryServiceFeeMapper.updateByUid(userBatteryServiceFee);
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_SERVICE_FEE + userBatteryServiceFee.getUid());
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
        int delete= this.userBatteryServiceFeeMapper.deleteByUid(uid);
        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_SERVICE_FEE + uid);
            return null;
        });
        return delete;
    }
}
