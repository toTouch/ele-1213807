package com.xiliulou.electricity.vo.thirdPartyMall;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author HeYafeng
 * @description 电池押金信息
 * @date 2024/8/29 14:26:38
 */
@Data
public class MtBatteryDepositVO {
    
    private Long uid;
    
    private Long franchiseeId;
    
    /**
     * 电池押金状态 0--未缴纳押金，1--已缴纳押金
     */
    private Integer batteryDepositStatus;
    
    /**
     * 押金金额
     */
    private BigDecimal batteryDeposit;
    
    /**
     * 交易方式 0 线上，1线下，2免押，3美团支付
     */
    private Integer batteryDepositPayType;
    
    /**
     * 退押状态
     */
    private Integer refundStatus;
    
    /**
     * 套餐电池型号
     */
    List<MtMemberCarBatteryTypeVO> midBatteryTypes;
    
}
