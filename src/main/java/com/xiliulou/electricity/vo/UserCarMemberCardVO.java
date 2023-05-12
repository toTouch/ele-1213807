package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-29-16:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCarMemberCardVO {

    /**
     * 用户Id
     */
    private Long uid;
    /**
     * 车辆SN
     */
    private String carSN;
    /**
     * 车辆型号
     */
    private String carModelName;
    
    private Long carModelId;
    /**
     * 套餐类型
     */
    private String memberCardType;
    /**
     * 套餐名称
     */
    private String cardName;
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
    /**
     * 有效天数
     */
    private Integer validDays;
    /**
     * 套餐到期时间
     */
    private Long memberCardExpireTime;
    /**
     * 门店名称
     */
    private String storeName;
    /**
     * 车辆押金
     */
    private BigDecimal carDeposit;
    
    /**
     * 车辆押金缴纳时间
     */
    private Long payDepositTime;
    /**
     * 地址经度
     */
    private Double longitude;
    
    /**
     * 地址纬度
     */
    private Double latitude;
    
    /**
     * 地址更新时间
     */
    private Long pointUpdateTime;
    
    /**
     * 归还车辆状态
     */
    private Integer returnCarStatus;
    
    /**
     * 锁状态
     */
    private Integer lockType;
}
