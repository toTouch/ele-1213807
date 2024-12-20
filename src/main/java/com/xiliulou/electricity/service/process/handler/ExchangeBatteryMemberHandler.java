package com.xiliulou.electricity.service.process.handler;

import com.xiliulou.electricity.dto.ExchangeMemberResultDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @ClassName: ExchangeCarMemberHandler
 * @description:
 * @author: renhang
 * @create: 2024-11-20 10:19
 */
@Service("exchangeBatteryMemberHandler")
@Slf4j
public class ExchangeBatteryMemberHandler implements ExchangeBasicHandler {
    
    @Resource
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Resource
    private BatteryMemberCardService batteryMemberCardService;
    
    @Resource
    private ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Resource
    private ElectricityConfigService electricityConfigService;
    
    @Resource
    private CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;
    
    @Resource
    private ElectricityBatteryService electricityBatteryService;
    
    
    @Override
    @SuppressWarnings("all")
    public Triple<Boolean, String, Object> handler(UserInfo userInfo) {
        //判断用户押金
        Triple<Boolean, String, Object> checkUserDepositResult = checkUserDeposit(userInfo);
        if (Boolean.FALSE.equals(checkUserDepositResult.getLeft())) {
            return checkUserDepositResult;
        }
        
        //判断用户套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("ORDER WARN! user haven't memberCard uid={}", userInfo.getUid());
            return Triple.of(false, "100210", "用户未开通套餐");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("ORDER WARN! user's member card is stop! uid={}", userInfo.getUid());
            return Triple.of(false, "100211", "换电套餐停卡审核中");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("ORDER WARN! user's member card is stop! uid={}", userInfo.getUid());
            return Triple.of(false, "100211", "换电套餐已暂停");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("ORDER WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }
        
        //判断用户电池服务费
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("ORDER WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.100000", "存在电池服务费");
        }
        
        if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)
                && userBatteryMemberCard.getRemainingNumber() <= 0)) {
            log.warn("ORDER WARN! battery memberCard is Expire,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0023", "套餐已过期");
        }
        
        //判断车电关联是否可换电
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsOpenCarBatteryBind(), ElectricityConfig.ENABLE_CAR_BATTERY_BIND)) {
            if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
                try {
                    if (carRentalPackageMemberTermBizService.isExpirePackageOrder(userInfo.getTenantId(), userInfo.getUid())) {
                        log.warn("ORDER WARN! user car memberCard expire,uid={}", userInfo.getUid());
                        return Triple.of(false, "100233", "租车套餐已过期");
                    }
                } catch (Exception e) {
                    log.error("ORDER ERROR!acquire car membercard expire result fail,uid={}", userInfo.getUid(), e);
                }
            }
        }
        
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW) && Objects.isNull(electricityBattery)) {
            return Triple.of(false, "300900", "系统检测到当前用户未绑定电池，请检查");
        }
        
        return Triple.of(true, null, ExchangeMemberResultDTO.builder().electricityBattery(electricityBattery).electricityConfig(electricityConfig).build());
    }
    
    
    private Triple<Boolean, String, Object> checkUserDeposit(UserInfo userInfo) {
        if (Objects.isNull(userInfo.getFranchiseeId())) {
            log.warn("ORDER WARN! not found franchiseeUser! uid={}", userInfo.getUid());
            return Triple.of(false, "100207", "用户加盟商信息未找到");
        }
        
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.warn("ORDER WARN! user didn't pay a deposit,uid={},fid={}", userInfo.getUid(), userInfo.getFranchiseeId());
            return Triple.of(false, "100209", "用户未缴纳押金");
        }
        
        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("ORDER WARN! user not rent battery! uid={}", userInfo.getUid());
            return Triple.of(false, "100222", "用户还没有租借电池");
        }
        return Triple.of(true, null, null);
    }
}
