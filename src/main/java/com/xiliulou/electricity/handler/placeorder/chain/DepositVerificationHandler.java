package com.xiliulou.electricity.handler.placeorder.chain;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.PlaceOrderConstant;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.UserDelRecord;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.UserStatusEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.handler.placeorder.AbstractPlaceOrderHandler;
import com.xiliulou.electricity.handler.placeorder.context.PlaceOrderContext;
import com.xiliulou.electricity.service.userinfo.UserDelRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

import static com.xiliulou.electricity.enums.PlaceOrderTypeEnum.PLACE_ORDER_DEPOSIT;

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
    private final UserDelRecordService userDelRecordService;
    
    
    @PostConstruct
    public void init() {
        this.nextHandler = depositPlaceOrderHandler;
        this.nodePlaceOrderType = PLACE_ORDER_DEPOSIT.getType();
    }
    
    @Override
    public void dealWithBusiness(PlaceOrderContext context, R<Object> result, Integer placeOrderType) {
        UserInfo userInfo = context.getUserInfo();
        Long uid = userInfo.getUid();
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.warn("BATTERY DEPOSIT WARN! user is rent deposit,uid={} ", uid);
            throw new BizException("ELECTRICITY.0049", "已缴纳押金");
        }
        
        if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            throw new BizException("110211", "用户已缴纳车电一体押金");
        }

        if (Objects.equals(placeOrderType, PLACE_ORDER_DEPOSIT) && Objects.equals(context.getElectricityConfig().getIsEnableSeparateDeposit(),
                ElectricityConfig.SEPARATE_DEPOSIT_CLOSE)) {
            throw new BizException("302001", "单独缴纳押金已禁用，请刷新后重新购买");
        }
        
        if(Objects.equals(context.getPlaceOrderQuery().getPayType(), PlaceOrderConstant.OFFLINE_PAYMENT)) {
            // 是否为"注销中"
            UserDelRecord userDelRecord = userDelRecordService.queryByUidAndStatus(uid, List.of(UserStatusEnum.USER_STATUS_CANCELLING.getCode()));
            if (Objects.nonNull(userDelRecord)) {
                log.warn("BATTERY DEPOSIT WARN! userAccount is cancelling, uid={}", uid);
                throw new BizException("120163", "账号处于注销缓冲期内，无法操作");
            }
        }
        
        fireProcess(context, result, placeOrderType);
    }
}
