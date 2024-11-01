package com.xiliulou.electricity.handler.placeorder.chain;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.handler.placeorder.AbstractPlaceOrderHandler;
import com.xiliulou.electricity.handler.placeorder.context.PlaceOrderContext;
import com.xiliulou.electricity.service.ElectricityConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

import static com.xiliulou.electricity.constant.PlaceOrderConstant.PLACE_ORDER_DEPOSIT;

/**
 * @Description 押金校验处理节点，同时需要将后续处理需要的数据存入上下文对象context中
 * @Author: SongJP
 * @Date: 2024/10/25 16:23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepositVerificationHandler extends AbstractPlaceOrderHandler {
    
    private final DepositPlaceOrderHandler depositPlaceOrderHandler;
    
    private final ElectricityConfigService electricityConfigService;
    
    
    @PostConstruct
    public void init() {
        this.nextHandler = depositPlaceOrderHandler;
        this.nodePlaceOrderType = PLACE_ORDER_DEPOSIT;
    }
    
    @Override
    public void dealWithBusiness(PlaceOrderContext context, R<Object> result, Integer placeOrderType) {
        UserInfo userInfo = context.getUserInfo();
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.warn("BATTERY DEPOSIT WARN! user is rent deposit,uid={} ", userInfo.getUid());
            result = R.fail("ELECTRICITY.0049", "已缴纳押金");
            exit();
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            result = R.fail("302001", "单独缴纳押金已禁用，请刷新后重新购买");
            exit();
        }
        if (Objects.equals(placeOrderType, PLACE_ORDER_DEPOSIT) && Objects.equals(electricityConfig.getIsEnableSeparateDeposit(), ElectricityConfig.SEPARATE_DEPOSIT_CLOSE)) {
            result = R.fail("302001", "单独缴纳押金已禁用，请刷新后重新购买");
            exit();
        }
        
        fireProcess(context, result, placeOrderType);
    }
}
