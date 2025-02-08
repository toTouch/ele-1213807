package com.xiliulou.electricity.service.process.handler;

import com.xiliulou.electricity.dto.ExchangeMemberResultDTO;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: ExchangeCarMemberHandler
 * @description:
 * @author: renhang
 * @create: 2024-11-20 10:19
 */
@Service("exchangeCarMemberHandler")
@Slf4j
public class ExchangeCarMemberHandler implements ExchangeBasicHandler {
    
    @Resource
    private CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;
    
    @Resource
    private CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;
    
    @Resource
    private ElectricityConfigService electricityConfigService;
    
    @Resource
    private ElectricityBatteryService electricityBatteryService;
    
    
    @Override
    @SuppressWarnings("all")
    public Triple<Boolean, String, Object> handler(UserInfo userInfo) {
        
        //判断车电一体套餐状态
        if (carRentalPackageMemberTermBizService.isExpirePackageOrder(userInfo.getTenantId(), userInfo.getUid())) {
            log.warn("EXCHANGE WARN! user memberCard disable,uid={}", userInfo.getUid());
            return Triple.of(false, "100210", "用户套餐不可用");
        }
        
        //判断用户电池服务费
        if (Boolean.TRUE.equals(carRenalPackageSlippageBizService.isExitUnpaid(userInfo.getTenantId(), userInfo.getUid()))) {
            log.warn("ORDER WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "300001", "存在滞纳金，请先缴纳");
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            log.warn("ORDER WARN! not found electricityConfig,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.100000", "系统异常");
        }
        
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW) && Objects.isNull(electricityBattery)) {
            return Triple.of(false, "300900", "系统检测到当前用户未绑定电池，请检查");
        }
        
        return Triple.of(true, null, ExchangeMemberResultDTO.builder().electricityBattery(electricityBattery).electricityConfig(electricityConfig).build());
    }
}
