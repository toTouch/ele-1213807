package com.xiliulou.electricity.handler.placeorder.chain;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.PlaceOrderConstant;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.UserDelRecord;
import com.xiliulou.electricity.enums.UserStatusEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.handler.placeorder.AbstractPlaceOrderHandler;
import com.xiliulou.electricity.handler.placeorder.context.PlaceOrderContext;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.userinfo.UserDelRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

import static com.xiliulou.electricity.constant.PlaceOrderConstant.PLACE_ORDER_INSURANCE;

/**
 * @Description 保险校验处理节点，同时需要将后续处理需要的数据存入上下文对象context中
 * @Author: SongJP
 * @Date: 2024/10/25 17:14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InsuranceVerificationHandler extends AbstractPlaceOrderHandler {
    
    private final InsurancePlaceOrderHandler insurancePlaceOrderHandler;
    
    private final FranchiseeInsuranceService franchiseeInsuranceService;
    
    private final UserDelRecordService userDelRecordService;
    
    @PostConstruct
    public void init() {
        this.nextHandler = insurancePlaceOrderHandler;
        this.nodePlaceOrderType = PLACE_ORDER_INSURANCE;
    }
    
    @Override
    public void dealWithBusiness(PlaceOrderContext context, R<Object> result, Integer placeOrderType) {
        Long uid = context.getUserInfo().getUid();
        Integer insuranceId = context.getPlaceOrderQuery().getInsuranceId();
        
        // 查询保险
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(insuranceId);
        
        if (Objects.isNull(franchiseeInsurance) || !Objects.equals(franchiseeInsurance.getInsuranceType(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY)) {
            log.error("CREATE INSURANCE_ORDER ERROR,NOT FOUND MEMBER_CARD BY ID={},uid={}", insuranceId, uid);
            throw new BizException("100305", "未找到保险!");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            log.error("CREATE INSURANCE_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID={},uid={}", insuranceId, uid);
            throw new BizException("100306", "保险已禁用!");
        }
        
        if (Objects.isNull(franchiseeInsurance.getPremium())) {
            log.error("CREATE INSURANCE_ORDER ERROR! payAmount is null ！franchiseeId={},uid={}", insuranceId, uid);
            throw new BizException("100305", "未找到保险");
        }
    
        if(Objects.equals(context.getPlaceOrderQuery().getPayType(), PlaceOrderConstant.OFFLINE_PAYMENT)) {
            // 是否为"注销中"
            UserDelRecord userDelRecord = userDelRecordService.queryByUidAndStatus(uid, List.of(UserStatusEnum.USER_STATUS_CANCELLING.getCode()));
            if (Objects.nonNull(userDelRecord)) {
                log.warn("CREATE INSURANCE_ORDER ERROR! userAccount is cancelling, uid={}", uid);
                throw new BizException("120163", "账号处于注销缓冲期内，无法操作");
            }
        }
        
        context.setFranchiseeInsurance(franchiseeInsurance);
        fireProcess(context, result, placeOrderType);
    }
}
