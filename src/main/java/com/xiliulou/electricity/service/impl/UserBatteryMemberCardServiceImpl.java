package com.xiliulou.electricity.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.enterprise.EnterprisePaymentStatusEnum;
import com.xiliulou.electricity.enums.thirdParthMall.MeiTuanRiderMallEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.UserBatteryMemberCardMapper;
import com.xiliulou.electricity.query.BatteryMemberCardExpiringSoonQuery;
import com.xiliulou.electricity.query.CarMemberCardExpiringSoonQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.thirdPartyMall.MeiTuanRiderMallOrderService;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.FailureMemberCardVo;
import com.xiliulou.electricity.vo.UserBatteryMemberCardChannelExitVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    UserBatteryDepositService userBatteryDepositService;
    
    @Autowired
    UserBatteryService userBatteryService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Resource
    EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Resource
    private CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;
    
    @Resource
    private MeiTuanRiderMallOrderService meiTuanRiderMallOrderService;
    
    private final ScheduledThreadPoolExecutor scheduledExecutor = ThreadUtil.createScheduledExecutor(2);
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Slave
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
    
    @Slave
    @Override
    public List<UserBatteryMemberCard> selectByMemberCardId(Integer id, Integer tenantId) {
        return userBatteryMemberCardMapper.selectList(
                new LambdaQueryWrapper<UserBatteryMemberCard>().eq(UserBatteryMemberCard::getMemberCardId, id).eq(UserBatteryMemberCard::getTenantId, tenantId)
                        .eq(UserBatteryMemberCard::getDelFlag, UserBatteryMemberCard.DEL_NORMAL));
    }
    
    @Slave
    @Override
    public List<UserBatteryMemberCard> batteryMemberCardExpire(Integer tenantId, Integer offset, Integer size, Long firstTime, Long lastTime) {
        return userBatteryMemberCardMapper.batteryMemberCardExpire(tenantId,offset, size, firstTime, lastTime);
    }
    
    
    @Override
    public List<CarMemberCardExpiringSoonQuery> carMemberCardExpire(Integer offset, Integer size, Long firstTime, Long lastTime) {
        return userBatteryMemberCardMapper.carMemberCardExpire(offset, size, firstTime, lastTime);
    }
    
    @Slave
    @Override
    public List<FailureMemberCardVo> queryMemberCardExpireUser(Integer offset, Integer size, Long nowTime) {
        return userBatteryMemberCardMapper.queryMemberCardExpireUser(offset, size, nowTime);
    }
    
    @Override
    public List<UserBatteryMemberCard> selectList(int offset, int size) {
        return userBatteryMemberCardMapper.selectByList(offset, size);
    }
    
    @Slave
    @Override
    public List<UserBatteryMemberCard> selectExpireList(int offset, int size, long memberCardExpireTime) {
        return userBatteryMemberCardMapper.selectExpireList(offset, size, memberCardExpireTime);
    }
    
    @Slave
    @Override
    public List<UserBatteryMemberCard> selectUseableList(int offset, int size) {
        return userBatteryMemberCardMapper.selectUseableList(offset, size);
    }
    
    @Slave
    @Override
    public List<UserBatteryMemberCard> selectUseableListByTenantIds(int offset, int size, List<Integer> tenantIds) {
        return userBatteryMemberCardMapper.selectUseableListByTenantIds(offset, size, tenantIds);
    }
    
    @Slave
    @Override
    public List<UserBatteryMemberCardChannelExitVo> selectExpireExitList(int offset, int size) {
        return userBatteryMemberCardMapper.selectExpireExitList(offset, size);
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
    
    @Slave
    @Override
    public Integer checkUserByMembercardId(Long id) {
        return userBatteryMemberCardMapper.checkUserByMembercardId(id);
    }
    
    /**
     * 获取用户套餐订单
     */
    @Slave
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
                    if (Objects.nonNull(batteryMemberCard) && BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode()
                            .equals(batteryMemberCard.getBusinessType())) {
                        enterpriseChannelUserService.updatePaymentStatusByUid(item.getUid(), EnterprisePaymentStatusEnum.PAYMENT_TYPE_EXPIRED.getCode());
                    }
                    
                    // 如果当前套餐是美团订单，则更新美团订单状态为已失效
                    MeiTuanRiderMallOrder meiTuanRiderMallOrder = meiTuanRiderMallOrderService.queryByOrderId(item.getOrderId(), item.getUid(), item.getTenantId());
                    if (Objects.nonNull(meiTuanRiderMallOrder)) {
                        MeiTuanRiderMallOrder updateMeiTuanRiderMallOrder = new MeiTuanRiderMallOrder();
                        updateMeiTuanRiderMallOrder.setOrderId(item.getOrderId());
                        updateMeiTuanRiderMallOrder.setOrderUseStatus(MeiTuanRiderMallEnum.ORDER_USE_STATUS_INVALID.getCode());
                        updateMeiTuanRiderMallOrder.setUpdateTime(System.currentTimeMillis());
                        updateMeiTuanRiderMallOrder.setTenantId(item.getTenantId());
                        meiTuanRiderMallOrderService.updateStatusByOrderId(updateMeiTuanRiderMallOrder);
                    }
                }
                
            });
            
            offset += size;
        }
    }
    
    /**
     * 暂停套餐 计算用户套餐余量
     *
     * @param userBatteryMemberCard
     * @param batteryMemberCard
     * @return
     */
    @Override
    public Long transforRemainingTime(UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard) {
        
        Long remainingTime = userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis();
        
        // 向上取整
        return Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY) ? (remainingTime + (24 * 60 * 60 * 1000 - 1)) / 24 / 60 / 60 / 1000
                : (remainingTime + (60 * 1000 - 1)) / 60 / 1000;
    }
    
    private void clearCache(Long uid) {
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
    
    @Override
    public void handleExpireMemberCard(String sessionId, ElectricityCabinetOrder electricityCabinetOrder) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCabinetOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE ERROR! userInfo is null!uid={},sessionId={},orderId={}", electricityCabinetOrder.getUid(), sessionId, electricityCabinetOrder.getOrderId());
            return;
        }
        
        UserBatteryMemberCard userBatteryMemberCard = this.selectByUidFromCache(electricityCabinetOrder.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("ELE WARN! userBatteryMemberCard is null!uid={},sessionId={},orderId={}", electricityCabinetOrder.getUid(), sessionId, electricityCabinetOrder.getOrderId());
            return;
        }
        
        //判断套餐是否限次
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            return;
        }
        
        if (!((Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0))) {
            return;
        }
        
        UserBatteryMemberCardPackage userBatteryMemberCardPackageLatest = userBatteryMemberCardPackageService.selectNearestByUid(userBatteryMemberCard.getUid());
        if (Objects.isNull(userBatteryMemberCardPackageLatest)) {
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
            userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(System.currentTimeMillis());
            this.updateByUid(userBatteryMemberCardUpdate);
        }
    }
    
    @Override
    public void deductionPackageNumberHandler(ElectricityCabinetOrder cabinetOrder, String sessionId) {
        // 通过订单的 UID 获取用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(cabinetOrder.getUid());
        
        // 扣减单电套餐次数
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            UserBatteryMemberCard userBatteryMemberCard = this.selectByUidFromCache(userInfo.getUid());
            if (Objects.nonNull(userBatteryMemberCard)) {
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
                if (Objects.nonNull(batteryMemberCard) && Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)) {
                    log.info("ELE INFO! deductionPackageNumberHandler deduct battery member card,sessionId is {}, orderId is {}", sessionId, cabinetOrder.getOrderId());
                    Integer row = this.minCount(userBatteryMemberCard);
                    if (row < 1) {
                        log.warn("SELF OPEN CELL  WARN! memberCard's count modify fail, uid={} ,mid={}", userBatteryMemberCard.getUid(), userBatteryMemberCard.getId());
                        throw new BizException("100213", "换电套餐剩余次数不足");
                    }
                }
            }
        }
        
        // 扣减车电一体套餐次数
        if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            log.info("ELE INFO! deductionPackageNumberHandler deduct car member card, sessionId is {}, orderId is {}", sessionId, cabinetOrder.getOrderId());
            if (!carRentalPackageMemberTermBizService.substractResidue(userInfo.getTenantId(), userInfo.getUid())) {
                throw new BizException("100213", "车电一体套餐剩余次数不足");
            }
        }
    }
    
    @Override
    public Integer deleteById(Long id) {
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardMapper.selectById(id);
        int delete = 0;
        if (Objects.nonNull(userBatteryMemberCard)) {
            delete = userBatteryMemberCardMapper.deleteById(id);
            DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
                redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userBatteryMemberCard.getUid());
                clearCache(userBatteryMemberCard.getUid());
            });
        }
        
        return delete;
    }
}
