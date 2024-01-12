package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.UserBatteryMemberCardPackageMapper;
import com.xiliulou.electricity.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * (UserBatteryMemberCardPackage)表服务实现类
 *
 * @author zzlong
 * @since 2023-07-12 14:44:01
 */
@Service("userBatteryMemberCardPackageService")
@Slf4j
public class UserBatteryMemberCardPackageServiceImpl implements UserBatteryMemberCardPackageService {
    
    @Resource
    private UserBatteryMemberCardPackageMapper userBatteryMemberCardPackageMapper;
    
    @Autowired
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private ElectricityMemberCardOrderService batteryMemberCardOrderService;
    
    @Autowired
    private UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    private UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    ExecutorService memberCardTransferThreadPool = XllThreadPoolExecutors.newFixedThreadPool("MEMBER_CARD_TRANSFER_POOL", 1, "member_card_transfer_thread");
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserBatteryMemberCardPackage queryByIdFromDB(Long id) {
        return this.userBatteryMemberCardPackageMapper.queryById(id);
    }
    
    @Override
    public List<UserBatteryMemberCardPackage> selectByUid(Long uid) {
        return this.userBatteryMemberCardPackageMapper.selectByUid(uid);
    }
    
    @Override
    public UserBatteryMemberCardPackage selectNearestByUid(Long uid) {
        return this.userBatteryMemberCardPackageMapper.selectNearestByUid(uid);
    }
    
    @Override
    public Integer insert(UserBatteryMemberCardPackage userBatteryMemberCardPackage) {
        return userBatteryMemberCardPackageMapper.insert(userBatteryMemberCardPackage);
    }
    
    /**
     * 修改数据
     *
     * @param userBatteryMemberCardPackage 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserBatteryMemberCardPackage userBatteryMemberCardPackage) {
        return this.userBatteryMemberCardPackageMapper.update(userBatteryMemberCardPackage);
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
        return this.userBatteryMemberCardPackageMapper.deleteById(id) > 0;
    }
    
    @Override
    public Integer deleteByOrderId(String orderId) {
        return this.userBatteryMemberCardPackageMapper.deleteByOrderId(orderId);
    }
    
    @Override
    public Integer deleteByUid(Long uid) {
        return this.userBatteryMemberCardPackageMapper.deleteByUid(uid);
    }
    
    @Override
    public UserBatteryMemberCardPackage selectByOrderNo(String orderId) {
        return this.userBatteryMemberCardPackageMapper.selectByOrderNo(orderId);
    }
    
    @Override
    public Integer checkUserBatteryMemberCardPackageByUid(Long uid) {
        return this.userBatteryMemberCardPackageMapper.checkUserBatteryMemberCardPackageByUid(uid);
    }
    
    /**
     * 用户端懒加载处理换电套餐资源包转换
     */
    @Override
    public Triple<Boolean, String, Object> batteryMembercardTransform(Long uid) {
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("BATTERY MEMBER TRANSFORM ERROR! not found user,uid={}", uid);
            return Triple.of(true, null, null);
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("BATTERY MEMBER TRANSFORM WARN! user is unUsable,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("BATTERY MEMBER TRANSFORM WARN! user not auth,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }
        
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.warn("BATTERY MEMBER TRANSFORM WARN! not pay deposit,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || !(Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE) || Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW_REFUSE))) {
            log.warn("BATTERY MEMBER TRANSFORM WARN! not found userBatteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard) || Objects.isNull(userBatteryMemberCard.getOrderExpireTime()) || Objects.isNull(userBatteryMemberCard.getOrderRemainingNumber())) {
            log.warn("BATTERY MEMBER TRANSFORM WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(true, null, null);
        }
        
        if (!(userBatteryMemberCard.getOrderExpireTime() < System.currentTimeMillis() + 60 * 1000L || (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)
                && userBatteryMemberCard.getOrderRemainingNumber() <= 0))) {
            return Triple.of(true, null, null);
        }
        
        UserBatteryMemberCardPackage userBatteryMemberCardPackageLatest = this.selectNearestByUid(userBatteryMemberCard.getUid());
        if (Objects.isNull(userBatteryMemberCardPackageLatest)) {
            return Triple.of(true, null, null);
        }
        
        updateUserBatteryMembercardInfo(userBatteryMemberCard, userBatteryMemberCardPackageLatest);
        
        return Triple.of(true, null, null);
    }
    
    /**
     * 定时任务处理换电套餐资源包转换
     */
    @Override
    public void handlerTransferBatteryMemberCardPackage() {
        int offset = 0;
        int size = 200;
        
        while (true) {
            List<UserBatteryMemberCard> userBatteryMemberCardList = userBatteryMemberCardService.selectUseableList(offset, size);
            if (CollectionUtils.isEmpty(userBatteryMemberCardList)) {
                return;
            }
            
            userBatteryMemberCardList.parallelStream().forEach(item -> {
                if (!(Objects.equals(item.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE) || Objects.equals(item.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW_REFUSE))) {
                    return;
                }
                
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMemberCardId());
                if (Objects.isNull(batteryMemberCard)) {
                    return;
                }
                
                if (!(item.getOrderExpireTime() < System.currentTimeMillis() + 60 * 1000L || (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)
                        && item.getOrderRemainingNumber() <= 0))) {
                    return;
                }
                
                UserBatteryMemberCardPackage userBatteryMemberCardPackageLatest = this.selectNearestByUid(item.getUid());
                if (Objects.isNull(userBatteryMemberCardPackageLatest)) {
                    return;
                }
                
                updateUserBatteryMembercardInfo(item, userBatteryMemberCardPackageLatest);
                
            });
            
            offset += size;
        }
    }
    
    private void updateUserBatteryMembercardInfo(UserBatteryMemberCard userBatteryMemberCard, UserBatteryMemberCardPackage userBatteryMemberCardPackageLatest) {
        //更新当前用户绑定的套餐数据
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setOrderId(userBatteryMemberCardPackageLatest.getOrderId());
        userBatteryMemberCardUpdate.setMemberCardId(userBatteryMemberCardPackageLatest.getMemberCardId());
        userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime() + userBatteryMemberCardPackageLatest.getMemberCardExpireTime());
        userBatteryMemberCardUpdate.setOrderRemainingNumber(userBatteryMemberCardPackageLatest.getRemainingNumber());
        
        //获取原套餐的套餐剩余次数
        Long orderRemainingNumber = userBatteryMemberCard.getOrderRemainingNumber();
        if (Objects.nonNull(orderRemainingNumber) && orderRemainingNumber > 0) {
            userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() - orderRemainingNumber);
        }
        
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        
        //删除资源包
        this.deleteByOrderId(userBatteryMemberCardPackageLatest.getOrderId());
        
        //更新原来绑定的套餐订单状态
        ElectricityMemberCardOrder oldMemberCardOrder = new ElectricityMemberCardOrder();
        oldMemberCardOrder.setOrderId(userBatteryMemberCard.getOrderId());
        oldMemberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
        oldMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        batteryMemberCardOrderService.updateStatusByOrderNo(oldMemberCardOrder);
        
        //更新新绑定的套餐订单的状态
        ElectricityMemberCardOrder currentMemberCardOrder = new ElectricityMemberCardOrder();
        currentMemberCardOrder.setOrderId(userBatteryMemberCardPackageLatest.getOrderId());
        currentMemberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);
        currentMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        batteryMemberCardOrderService.updateStatusByOrderNo(currentMemberCardOrder);
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(userBatteryMemberCard.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("TRANSFER BATTERY MEMBERCARD PACKAGE ERROR!not found userInfo,uid={}", userBatteryMemberCard.getUid());
            return;
        }
        
        ElectricityMemberCardOrder electricityMemberCardOrder = batteryMemberCardOrderService.selectByOrderNo(userBatteryMemberCardPackageLatest.getOrderId());
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.error("TRANSFER BATTERY MEMBERCARD PACKAGE ERROR!not found userInfo,uid={},orderId={}", userBatteryMemberCard.getUid(),
                    userBatteryMemberCardPackageLatest.getOrderId());
            return;
        }
        
        //更新用户电池型号
        userBatteryTypeService.updateUserBatteryType(electricityMemberCardOrder, userInfo);
    }
    
    public void asyncHandleUpdateUserBatteryMemberCardInfo(UserBatteryMemberCard userBatteryMemberCard, UserInfo userInfo){
        memberCardTransferThreadPool.execute(()->{
            updateUserBatteryMemberCardPackageInfo(userBatteryMemberCard,userInfo);
        });
    }
    
    @Override
    public Integer deleteChannelMemberCardByUid(Long uid) {
        return this.userBatteryMemberCardPackageMapper.deleteChannelMemberCardByUid(uid);
    }
    
    
    private void updateUserBatteryMemberCardPackageInfo(UserBatteryMemberCard userBatteryMemberCard, UserInfo userInfo) {
        UserBatteryMemberCardPackage userBatteryMemberCardPackageLatest = this.selectNearestByUid(userBatteryMemberCard.getUid());
        if (Objects.isNull(userBatteryMemberCardPackageLatest)) {
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            return;
        }
        
        //更新当前用户绑定的套餐数据
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setOrderId(userBatteryMemberCardPackageLatest.getOrderId());
        userBatteryMemberCardUpdate.setMemberCardId(userBatteryMemberCardPackageLatest.getMemberCardId());
        userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis() + userBatteryMemberCardPackageLatest.getMemberCardExpireTime());
        userBatteryMemberCardUpdate.setMemberCardExpireTime(
                userBatteryMemberCard.getMemberCardExpireTime() - (userBatteryMemberCard.getOrderExpireTime() - System.currentTimeMillis()));
        userBatteryMemberCardUpdate.setOrderRemainingNumber(userBatteryMemberCardPackageLatest.getRemainingNumber());
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        
        //删除资源包
        userBatteryMemberCardPackageService.deleteByOrderId(userBatteryMemberCardPackageLatest.getOrderId());
        
        //更新原来绑定的套餐订单状态
        ElectricityMemberCardOrder oldMemberCardOrder = new ElectricityMemberCardOrder();
        oldMemberCardOrder.setOrderId(userBatteryMemberCard.getOrderId());
        oldMemberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
        oldMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        batteryMemberCardOrderService.updateStatusByOrderNo(oldMemberCardOrder);
        
        //更新新绑定的套餐订单的状态
        ElectricityMemberCardOrder currentMemberCardOrder = new ElectricityMemberCardOrder();
        currentMemberCardOrder.setOrderId(userBatteryMemberCardPackageLatest.getOrderId());
        currentMemberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);
        currentMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        batteryMemberCardOrderService.updateStatusByOrderNo(currentMemberCardOrder);
        
        ElectricityMemberCardOrder electricityMemberCardOrder = batteryMemberCardOrderService.selectByOrderNo(userBatteryMemberCardPackageLatest.getOrderId());
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.warn("TRANSFER BATTERY MEMBER CARD PACKAGE ERROR!not found user member card order Info,uid={},orderId={}", userBatteryMemberCard.getUid(),
                    userBatteryMemberCardPackageLatest.getOrderId());
            return;
        }
        
        //更新用户电池型号
        userBatteryTypeService.updateUserBatteryType(electricityMemberCardOrder, userInfo);
    }
}
