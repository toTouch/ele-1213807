package com.xiliulou.electricity.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.UserBatteryDepositBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.mapper.UserBatteryDepositMapper;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    
    private final ScheduledThreadPoolExecutor scheduledExecutor = ThreadUtil.createScheduledExecutor(2);
    
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
     * 删除了的也能查出来
     *
     * @param uid
     * @return
     */
    @Slave
    @Override
    public UserBatteryDeposit queryByUid(Long uid) {
        return this.userBatteryDepositMapper.selectOne(new LambdaQueryWrapper<UserBatteryDeposit>().eq(UserBatteryDeposit::getUid, uid));
    }
    
    @Override
    public Integer update(UserBatteryDeposit userBatteryDeposit) {
        Integer update = userBatteryDepositMapper.update(userBatteryDeposit);
        
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + userBatteryDeposit.getUid());
            clearCache(userBatteryDeposit.getUid());
        });
        return update;
    }
    
    @Override
    public Integer deleteById(Long id) {
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositMapper.selectById(id);
        int delete = 0;
        if (Objects.nonNull(userBatteryDeposit)) {
            delete = userBatteryDepositMapper.deleteById(id);
            
            DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
                redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + userBatteryDeposit.getUid());
                clearCache(userBatteryDeposit.getUid());
            });
        }
        
        return delete;
    }
    
    @Slave
    @Override
    public List<UserBatteryDeposit> listByUidList(Integer tenantId, List<Long> uidList) {
        return userBatteryDepositMapper.selectListByUidList(tenantId, uidList);
    }
    
    @Slave
    @Override
    public List<UserBatteryDepositBO> listPayTypeByUidList(Integer tenantId, List<Long> uidList) {
        return userBatteryDepositMapper.selectPayTypeByUidList(tenantId, uidList);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserBatteryDeposit selectByUidFromCache(Long uid) {
        UserBatteryDeposit cacheUserBatteryDeposit = redisService.getWithHash(CacheConstant.CACHE_USER_DEPOSIT + uid, UserBatteryDeposit.class);
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
    public Integer insert(UserBatteryDeposit userBatteryDeposit) {
        return this.userBatteryDepositMapper.insertOne(userBatteryDeposit);
    }
    
    @Override
    public UserBatteryDeposit insertOrUpdate(UserBatteryDeposit userBatteryDeposit) {
        int insert = this.userBatteryDepositMapper.insertOrUpdate(userBatteryDeposit);
        DbUtils.dbOperateSuccessThenHandleCache(insert, i -> {
            redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + userBatteryDeposit.getUid());
            clearCache(userBatteryDeposit.getUid());
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
    public Integer updateByUid(UserBatteryDeposit userBatteryDeposit) {
        int update = this.userBatteryDepositMapper.updateByUid(userBatteryDeposit);
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + userBatteryDeposit.getUid());
            clearCache(userBatteryDeposit.getUid());
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
    public Integer deleteByUid(Long uid) {
        int delete = this.userBatteryDepositMapper.deleteByUid(uid);
        DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
            redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + uid);
            clearCache(uid);
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
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + uid);
            clearCache(uid);
        });
        return update;
    }
    
    /**
     * 同步车电一体数据
     *
     * @param uid            用户uid
     * @param mid            交押金时押金所属套餐id
     * @param orderId        押金订单号
     * @param batteryDeposit 押金金额
     * @return
     */
    @Override
    public Integer synchronizedUserBatteryDepositInfo(Long uid, Long mid, String orderId, BigDecimal batteryDeposit) {
        Integer result = null;
        UserBatteryDeposit userBatteryDeposit = this.queryByUid(uid);
        if (Objects.isNull(userBatteryDeposit)) {
            UserBatteryDeposit userBatteryDepositInsert = new UserBatteryDeposit();
            userBatteryDepositInsert.setUid(uid);
            userBatteryDepositInsert.setDid(0L);
            userBatteryDepositInsert.setOrderId(orderId);
            userBatteryDepositInsert.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
            userBatteryDepositInsert.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
            userBatteryDepositInsert.setBatteryDeposit(batteryDeposit);
            userBatteryDepositInsert.setCreateTime(System.currentTimeMillis());
            userBatteryDepositInsert.setUpdateTime(System.currentTimeMillis());
            result = this.insert(userBatteryDepositInsert);
        } else {
            UserBatteryDeposit userBatteryDepositUpdate = new UserBatteryDeposit();
            userBatteryDepositUpdate.setUid(uid);
            userBatteryDepositUpdate.setDid(0L);
            userBatteryDepositUpdate.setOrderId(orderId);
            userBatteryDepositUpdate.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
            userBatteryDepositUpdate.setBatteryDeposit(batteryDeposit);
            userBatteryDepositUpdate.setUpdateTime(System.currentTimeMillis());
            result = this.updateByUid(userBatteryDepositUpdate);
        }
        
        return result;
    }
    
    private void clearCache(Long uid) {
        scheduledExecutor.schedule(() -> {
            if (redisService.hasKey(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + uid)) {
                redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + uid);
            }
            
            if (redisService.hasKey(CacheConstant.CACHE_USER_DEPOSIT + uid)) {
                redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + uid);
            }
        }, 1, TimeUnit.SECONDS);
    }
}
