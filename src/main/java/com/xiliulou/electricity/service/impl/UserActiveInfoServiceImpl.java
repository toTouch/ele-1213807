package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.UserActiveInfo;
import com.xiliulou.electricity.entity.UserBattery;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.UserActiveInfoMapper;
import com.xiliulou.electricity.query.UserActiveInfoQuery;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.UserActiveInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.UserActiveInfoVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (UserActiveInfo)表服务实现类
 *
 * @author Hardy
 * @since 2023-03-01 10:15:11
 */
@Service("userActiveInfoService")
@Slf4j
public class UserActiveInfoServiceImpl implements UserActiveInfoService {
    
    @Resource
    private UserActiveInfoMapper userActiveInfoMapper;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserActiveInfo queryByIdFromDB(Long id) {
        return this.userActiveInfoMapper.queryById(id);
    }
    
    @Override
    public UserActiveInfo queryByUIdFromDB(Long uid) {
        return this.userActiveInfoMapper.queryByUId(uid);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     */
    @Override
    public UserActiveInfo queryByUIdFromCache(Long uid) {
        UserActiveInfo userActiveInfo = redisService
                .getWithHash(CacheConstant.USER_ACTIVE_INFO_CACHE + uid, UserActiveInfo.class);
        redisService.expire(CacheConstant.USER_ACTIVE_INFO_CACHE + uid, CacheConstant.CACHE_EXPIRE_MONTH, true);
        
        if (Objects.nonNull(userActiveInfo)) {
            return userActiveInfo;
        }
        
        UserActiveInfo userActiveInfoFromDB = queryByUIdFromDB(uid);
        if (Objects.isNull(userActiveInfoFromDB)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.USER_ACTIVE_INFO_CACHE + uid, userActiveInfoFromDB);
        
        return userActiveInfo;
    }
    
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<UserActiveInfo> queryAllByLimit(int offset, int limit) {
        return this.userActiveInfoMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param userActiveInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserActiveInfo insert(UserActiveInfo userActiveInfo) {
        this.userActiveInfoMapper.insertOne(userActiveInfo);
        return userActiveInfo;
    }
    
    /**
     * 修改数据
     *
     * @param userActiveInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserActiveInfo userActiveInfo) {
        return this.userActiveInfoMapper.update(userActiveInfo);
        
    }
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.userActiveInfoMapper.deleteById(id) > 0;
    }
    
    @Override
    public UserActiveInfo insertOrUpdate(UserActiveInfo userActiveInfo) {
        int insert = this.userActiveInfoMapper.insertOrUpdate(userActiveInfo);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.delete(CacheConstant.USER_ACTIVE_INFO_CACHE + userActiveInfo.getUid());
            return null;
        });
        return userActiveInfo;
    }
    
    @Override
    public UserActiveInfo userActiveRecord(UserInfo userInfo) {
        UserActiveInfo userActiveInfo = new UserActiveInfo();
        userActiveInfo.setUid(userInfo.getUid());
        userActiveInfo.setUserName(userInfo.getName());
        userActiveInfo.setPhone(userInfo.getPhone());
        userActiveInfo.setTenantId(TenantContextHolder.getTenantId());
        userActiveInfo.setActiveTime(System.currentTimeMillis());
        userActiveInfo.setCreateTime(System.currentTimeMillis());
        userActiveInfo.setUpdateTime(System.currentTimeMillis());
        insertOrUpdate(userActiveInfo);
        return userActiveInfo;
    }
    
    @Override
    @DS("slave_1")
    public R queryList(UserActiveInfoQuery query) {
        //默认为30天
        int day = Objects.isNull(query.getDay()) ? 30 : query.getDay();
        query.setLimitTime(System.currentTimeMillis() - day * 24 * 3600000);
        
        List<UserActiveInfoVo> userActiveInfoList = userActiveInfoMapper.queryList(query);
        if (CollectionUtils.isEmpty(userActiveInfoList)) {
            return R.ok(new ArrayList<>());
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        userActiveInfoList.parallelStream().forEach(item -> {
            BigDecimal batteryServiceFee = eleBatteryServiceFeeOrderService.queryUserTurnOver(tenantId, item.getUid());
            item.setBatteryServiceFee(batteryServiceFee);
        });
        return R.ok(userActiveInfoList);
    }
    
    @Override
    @DS("slave_1")
    public R queryCount(UserActiveInfoQuery query) {
        int day = Objects.isNull(query.getDay()) ? 30 : query.getDay();
        query.setLimitTime(System.currentTimeMillis() - day * 24 * 3600000);
        
        Long count = userActiveInfoMapper.queryCount(query);
        return R.ok(count);
    }
}
