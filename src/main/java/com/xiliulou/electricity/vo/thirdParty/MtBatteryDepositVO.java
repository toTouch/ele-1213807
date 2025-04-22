package com.xiliulou.electricity.vo.thirdParty;

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
     * 套餐ID
     */
    private Long packageId;
    
    /**
     * 是否免押 0--是 1--否
     */
    private Integer freeDeposit;
    
    /**
     * 套餐电池型号
     */
    List<MtMemberCarBatteryTypeVO> midBatteryTypes;
    
    /**
     * 小程序押金模块是否展示:0-展示 1-不展示
     * 不展示的场景:已缴纳车电一体押金
     */
    private Integer isShow;
    
    public static final Integer IS_SHOW = 0;
    
    public static final Integer IS_NOT_SHOW = 1;
}
