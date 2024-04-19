package com.xiliulou.electricity.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.enums.enterprise.EnterprisePaymentStatusEnum;
import com.xiliulou.electricity.mapper.UserBatteryMemberCardMapper;
import com.xiliulou.electricity.query.BatteryMemberCardExpiringSoonQuery;
import com.xiliulou.electricity.query.CarMemberCardExpiringSoonQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.MemberCardFailureRecordService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.FailureMemberCardVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * (UserBatteryMemberCard)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-06 13:38:52
 */
@Service("userBatteryMemberCardService")
@Slf4j
public class UserBatteryMemberCardServiceImpl implements UserBatteryMemberCardService {

    @Resource
    private UserBatteryMemberCardMapper userBatteryMemberCardMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    MemberCardFailureRecordService memberCardFailureRecordService;

    @Autowired
    UserBatteryDepositService userBatteryDepositService;

    @Autowired
    UserBatteryService userBatteryService;

    @Autowired
    BatteryMemberCardService batteryMemberCardService;

    @Autowired
    UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Resource
    EnterpriseChannelUserService enterpriseChannelUserService;
    
    private final ScheduledThreadPoolExecutor scheduledExecutor = ThreadUtil.createScheduledExecutor(2);

    /**
     * 通过ID查询单条数据从DB
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserBatteryMemberCard selectByUidFromDB(Long uid) {
        return this.userBatteryMemberCardMapper.selectByUid(uid);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserBatteryMemberCard selectByUidFromCache(Long uid) {
        UserBatteryMemberCard cacheUserBatteryMemberCard = redisService.getWithHash(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + uid, UserBatteryMemberCard.class);
        if (Objects.nonNull(cacheUserBatteryMemberCard)) {
            return cacheUserBatteryMemberCard;
        }

        UserBatteryMemberCard userBatteryMemberCard = this.selectByUidFromDB(uid);
        if (Objects.isNull(userBatteryMemberCard)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + uid, userBatteryMemberCard);

        return userBatteryMemberCard;
    }

    /**
     * 新增数据
     *
     * @param userBatteryMemberCard 实例对象
     * @return 实例对象
     */
    @Override
    public UserBatteryMemberCard insert(UserBatteryMemberCard userBatteryMemberCard) {
        int insert = this.userBatteryMemberCardMapper.insert(userBatteryMemberCard);
        return userBatteryMemberCard;
    }

    /**
     * 修改数据
     *
     * @param userBatteryMemberCard 实例对象
     * @return 实例对象
     */
    @Override
    public Integer updateByUid(UserBatteryMemberCard userBatteryMemberCard) {
        int update = this.userBatteryMemberCardMapper.updateByUid(userBatteryMemberCard);

        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userBatteryMemberCard.getUid());
            clearCache(userBatteryMemberCard.getUid());
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
        int delete = this.userBatteryMemberCardMapper.deleteByUid(uid);

        DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + uid);
            clearCache(uid);
        });

        return delete;
    }

    @Override
    public Integer unbindMembercardInfoByUid(Long uid) {
        UserBatteryMemberCard userBatteryMemberCard = new UserBatteryMemberCard();
        userBatteryMemberCard.setUid(uid);
        userBatteryMemberCard.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
        userBatteryMemberCard.setMemberCardId(0L);
        userBatteryMemberCard.setOrderId("");
        userBatteryMemberCard.setOrderExpireTime(0L);
        userBatteryMemberCard.setOrderEffectiveTime(0L);
        userBatteryMemberCard.setMemberCardExpireTime(0L);
        userBatteryMemberCard.setRemainingNumber(0L);
        userBatteryMemberCard.setOrderRemainingNumber(0L);
        userBatteryMemberCard.setMemberCardStatus(0);
        userBatteryMemberCard.setDisableMemberCardTime(null);
        userBatteryMemberCard.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
        userBatteryMemberCard.setUpdateTime(System.currentTimeMillis());

        int update = this.userBatteryMemberCardMapper.unbindMembercardInfoByUid(userBatteryMemberCard);

        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + uid);
            clearCache(uid);
        });
        return update;
    }

    @Override
    public Integer minCount(UserBatteryMemberCard userBatteryMemberCard) {

        Integer update = userBatteryMemberCardMapper.minCount(userBatteryMemberCard.getId());
        
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userBatteryMemberCard.getUid());
            clearCache(userBatteryMemberCard.getUid());
        });

        return update;

    }

    @Override
    public Integer minCountForOffLineEle(UserBatteryMemberCard userBatteryMemberCard) {
        Integer update = userBatteryMemberCardMapper.minCountForOffLineEle(userBatteryMemberCard.getId());
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userBatteryMemberCard.getUid());
            clearCache(userBatteryMemberCard.getUid());
        });

        return update;
    }

    @Override
    public Integer deductionExpireTime(Long uid, Long time, Long updateTime) {
        Integer update = userBatteryMemberCardMapper.deductionExpireTime(uid, time, updateTime);
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + uid);
            clearCache(uid);
        });

        return update;
    }

    @Override
    public Integer plusCount(Long id) {
        Integer count = userBatteryMemberCardMapper.plusCount(id);
        DbUtils.dbOperateSuccessThenHandleCache(count, i -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + id);
            clearCache(id);
        });
        return count;
    }

    @Override
    public Integer updateByUidForDisableCard(UserBatteryMemberCard userBatteryMemberCard) {
        int update = this.userBatteryMemberCardMapper.updateByUidForDisableCard(userBatteryMemberCard);

        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userBatteryMemberCard.getUid());
            clearCache(userBatteryMemberCard.getUid());
        });

        return update;
    }

    @Override
    public List<UserBatteryMemberCard> selectByMemberCardId(Integer id, Integer tenantId) {
        return userBatteryMemberCardMapper.selectList(new LambdaQueryWrapper<UserBatteryMemberCard>().eq(UserBatteryMemberCard::getMemberCardId, id).eq(UserBatteryMemberCard::getTenantId, tenantId)
                .eq(UserBatteryMemberCard::getDelFlag, UserBatteryMemberCard.DEL_NORMAL));
    }

    @Override
    public List<BatteryMemberCardExpiringSoonQuery> batteryMemberCardExpire(Integer offset, Integer size,
                                                                            Long firstTime, Long lastTime) {
        return userBatteryMemberCardMapper.batteryMemberCardExpire(offset, size, firstTime, lastTime);
    }


    @Override
    public List<CarMemberCardExpiringSoonQuery> carMemberCardExpire(Integer offset, Integer size, Long firstTime,
                                                                    Long lastTime) {
        return userBatteryMemberCardMapper.carMemberCardExpire(offset, size, firstTime, lastTime);
    }

    @Override
    public List<FailureMemberCardVo> queryMemberCardExpireUser(Integer offset, Integer size, Long nowTime) {
        return userBatteryMemberCardMapper.queryMemberCardExpireUser(offset, size, nowTime);
    }

    @Override
    public List<UserBatteryMemberCard> selectList(int offset, int size) {
        return userBatteryMemberCardMapper.selectByList(offset, size);
    }
    
    @Override
    public List<UserBatteryMemberCard> selectExpireList(int offset, int size, long memberCardExpireTime) {
        return userBatteryMemberCardMapper.selectExpireList(offset, size, memberCardExpireTime);
    }

    @Override
    public List<UserBatteryMemberCard> selectUseableList(int offset, int size) {
        return userBatteryMemberCardMapper.selectUseableList(offset, size);
    }

    @Override
    public List<UserBatteryMemberCard> selectUseableListByTenantIds(int offset, int size, List<Integer> tenantIds) {
        return userBatteryMemberCardMapper.selectUseableListByTenantIds(offset, size , tenantIds);
    }

    /**
     * 校验用户电池套餐是否过期
     */
    @Override
    public Boolean verifyUserBatteryMembercardEffective(BatteryMemberCard batteryMemberCard, UserBatteryMemberCard userBatteryMemberCard) {
        if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            return Boolean.TRUE;
        }

        if (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public Integer checkUserByMembercardId(Long id) {
        return userBatteryMemberCardMapper.checkUserByMembercardId(id);
    }

    /**
     * 获取用户套餐订单
     */
    @Override
    public List<String> selectUserBatteryMemberCardOrder(Long uid) {
        List<String> orderList = new ArrayList<>();

        UserBatteryMemberCard userBatteryMemberCard = this.selectByUidFromCache(uid);
        if (!Objects.isNull(userBatteryMemberCard)) {
            orderList.add(userBatteryMemberCard.getOrderId());
        }


        List<UserBatteryMemberCardPackage> userBatteryMemberCardPackages = userBatteryMemberCardPackageService.selectByUid(uid);
        if (!CollectionUtils.isEmpty(userBatteryMemberCardPackages)) {
            orderList.addAll(userBatteryMemberCardPackages.stream().map(UserBatteryMemberCardPackage::getOrderId).collect(Collectors.toList()));
        }

        return orderList;
    }

    /**
     * 换电套餐过期  将订单状态更新为已失效
     */
    @Override
    public void batteryMembercardExpireUpdateStatusTask() {
        int offset = 0;
        int size = 200;

        while (true) {
            List<UserBatteryMemberCard> userBatteryMemberCardList = this.selectUseableList(offset, size);
            if (CollectionUtils.isEmpty(userBatteryMemberCardList)) {
                return;
            }

            userBatteryMemberCardList.forEach(item -> {
                //如果套餐过期更新订单状态为已失效
                if (Objects.nonNull(item.getMemberCardExpireTime()) && item.getMemberCardExpireTime() < System.currentTimeMillis() && StringUtils.isNotBlank(item.getOrderId())) {
                    ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
                    electricityMemberCardOrder.setOrderId(item.getOrderId());
                    electricityMemberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
                    electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
                    electricityMemberCardOrderService.updateStatusByOrderNo(electricityMemberCardOrder);
                    
                    //如果当前套餐是企业套餐，则将该骑手的代付状态更新为代付到期
                    BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMemberCardId());
                    if(Objects.nonNull(batteryMemberCard) && BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode().equals(batteryMemberCard.getBusinessType())){
                        enterpriseChannelUserService.updatePaymentStatusByUid(item.getUid(), EnterprisePaymentStatusEnum.PAYMENT_TYPE_EXPIRED.getCode());
                    }
                }

            });

            offset += size;
        }
    }

    /**
     * 暂停套餐 计算用户套餐余量
     * @param userBatteryMemberCard
     * @param batteryMemberCard
     * @return
     */
    @Override
    public Long transforRemainingTime(UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard) {
    
        Long remainingTime = userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis();
        
        // 向上取整
        return Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY) ? (remainingTime + (24*60*60*1000 - 1)) / 24 / 60 / 60 / 1000 : (remainingTime + (60*1000 - 1)) / 60 / 1000;
    }
    
    private void clearCache(Long uid){
        scheduledExecutor.schedule(() -> {
            if (redisService.hasKey(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + uid)) {
                redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + uid);
            }
        }, 1, TimeUnit.SECONDS);
    }
    
    @Slave
    @Override
    public Integer queryRenewalNumberByMerchantId(Long merchantId, Integer tenantId) {
        return userBatteryMemberCardMapper.selectRenewalNumberByMerchantId(merchantId, tenantId);
    }
    
}
