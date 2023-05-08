package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.UserBattery;
import com.xiliulou.electricity.mapper.UserBatteryMapper;
import com.xiliulou.electricity.service.UserBatteryService;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * (UserBattery)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-06 13:39:24
 */
@Service("userBatteryService")
@Slf4j
public class UserBatteryServiceImpl implements UserBatteryService {
    
    @Autowired
    private UserBatteryMapper userBatteryMapper;
    @Autowired
    private RedisService redisService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserBattery selectByUidFromDB(Long uid) {
        return this.userBatteryMapper.selectByUid(uid);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserBattery selectByUidFromCache(Long uid) {
        UserBattery cacheUserBattery = redisService.getWithHash(CacheConstant.CACHE_USER_BATTERY + uid, UserBattery.class);
        if(Objects.nonNull(cacheUserBattery)){
            return  cacheUserBattery;
        }
    
        UserBattery userBattery = this.selectByUidFromDB(uid);
        if(Objects.isNull(userBattery)){
            return null;
        }
    
        redisService.saveWithHash(CacheConstant.CACHE_USER_BATTERY + uid, userBattery);
        
        return userBattery;
    }

    /**
     * 新增数据
     *
     * @param userBattery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserBattery insert(UserBattery userBattery) {
        int insert = this.userBatteryMapper.insertOne(userBattery);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.saveWithHash(CacheConstant.CACHE_USER_BATTERY + userBattery.getUid(), userBattery);
            return null;
        });
        return userBattery;
    }

    @Override
    public UserBattery insertOrUpdate(UserBattery userBattery) {
        int insert = this.userBatteryMapper.insertOrUpdate(userBattery);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY + userBattery.getUid());
            return null;
        });
        return userBattery;
    }

    /**
     * 修改数据
     *
     * @param userBattery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateByUid(UserBattery userBattery) {
        int update= this.userBatteryMapper.updateByUid(userBattery);
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY + userBattery.getUid());
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
        int delete= this.userBatteryMapper.deleteByUid(uid);
        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY + uid);
            return null;
        });
        return delete;
    }

    @Override
    public List<UserBattery> selectBatteryTypeByFranchiseeId(Long id) {
        return this.userBatteryMapper.selectBatteryTypeByFranchiseeId(id);
    }
}
