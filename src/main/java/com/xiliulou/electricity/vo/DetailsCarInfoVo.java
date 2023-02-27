package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zgw
 * @date 2023/2/13 19:39
 * @mood
 */
@Data
public class DetailsCarInfoVo {
    
    private Long uid;
    
    ////用户车辆押金
    
    /**
     * 加盟商Id
     */
    private Long franschiseeId;
    
    /**
     * 加盟商名
     */
    private String franschiseeName;
    
    /**
     * 门店Id
     */
    private Long storeId;
    
    /**
     * 门店名
     */
    private String storeName;
    
    /**
     * 电池押金状态
     */
    private Integer carDepositStatus;
    
    /**
     * 车辆押金
     */
    private BigDecimal carDeposit;
    
    /**
     * 缴纳时间
     */
    private Long payDepositTime;
    
    ////用户车辆套餐
    
    /**
     * 套餐类型
     */
    private String memberCardType;
    
    /**
     * 套餐名称
     */
    private String cardName;
    
    /**
     * 租赁周期
     */
    private Integer validDays;
    
    /**
     * 租赁时间
     */
    private Long memberCardCreateTime;
    
    /**
     * 到期时间
     */
    private Long memberCardExpireTime;
    
    /**
     * 剩余时间
     */
    private Long carDays;
    
    ////用户车辆
    
    /**
     * 车辆型号id
     */
    private Integer carModelId;
    
    /**
     * 车辆型号
     */
    private String carModelName;
    
    /**
     * 车辆sn码
     */
    private String carSn;
}
