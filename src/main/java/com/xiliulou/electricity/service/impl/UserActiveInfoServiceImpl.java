package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.dto.bms.BatteryInfoDto;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.UserActiveInfo;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.UserActiveInfoMapper;
import com.xiliulou.electricity.query.UserActiveInfoQuery;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryDataService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserActiveInfoService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeVO;
import com.xiliulou.electricity.vo.UserActiveInfoVo;
import com.xiliulou.electricity.web.query.battery.BatteryInfoQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private ElectricityBatteryDataService electricityBatteryDataService;
    
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
        UserActiveInfo userActiveInfo = redisService.getWithHash(CacheConstant.USER_ACTIVE_INFO_CACHE + uid, UserActiveInfo.class);
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
    @Slave
    public R queryList(UserActiveInfoQuery query) {
        //默认为30天
        long day = Objects.isNull(query.getDay()) ? 30 : query.getDay();
        query.setLimitTime(System.currentTimeMillis() - day * 24 * 3600000);
        
        List<UserActiveInfoVo> userActiveInfoList = userActiveInfoMapper.queryList(query);
        if (CollectionUtils.isEmpty(userActiveInfoList)) {
            return R.ok(new ArrayList<>());
        }
        
        List<Object> list = userActiveInfoList.parallelStream().peek(item -> {
            EleBatteryServiceFeeVO eleBatteryServiceFeeVO = serviceFeeUserInfoService.queryUserBatteryServiceFee(item.getUid());
            item.setBatteryServiceFee(Objects.nonNull(eleBatteryServiceFeeVO) ? eleBatteryServiceFeeVO.getUserBatteryServiceFee() : BigDecimal.ZERO);
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            item.setFranchiseeName(Objects.isNull(franchisee) ? null : franchisee.getName());
            
            BatteryInfoQuery batteryInfoQuery = new BatteryInfoQuery();
            batteryInfoQuery.setSn(item.getBatterySn());
            BatteryInfoDto batteryInfoDto = electricityBatteryDataService.callBatteryServiceQueryBatteryInfo(batteryInfoQuery, query.getTenant());
            if (Objects.nonNull(batteryInfoDto)) {
                item.setSoc(batteryInfoDto.getSoc());
            }
            
        }).collect(Collectors.toList());
        
        return R.ok(list);
    }
    
    @Override
    @Slave
    public R queryCount(UserActiveInfoQuery query) {
        long day = Objects.isNull(query.getDay()) ? 30 : query.getDay();
        query.setLimitTime(System.currentTimeMillis() - day * 24 * 3600000);
        
        Long count = userActiveInfoMapper.queryCount(query);
        return R.ok(count);
    }
    
    /**
     * 更新用户手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    @Override
    public Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone) {
        return userActiveInfoMapper.updatePhoneByUid(tenantId, uid, newPhone);
    }
}
