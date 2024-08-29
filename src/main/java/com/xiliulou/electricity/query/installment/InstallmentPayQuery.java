package com.xiliulou.electricity.query.installment;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/29 10:28
 */
@Data
public class InstallmentPayQuery {
    
    private String productKey;
    
    private String deviceName;
    
    /**
     * 套餐id
     */
    @NotNull(message = "套餐Id不能为空!")
    private Long packageId;
    
    /**
     * 签约类型类型，0-换电，1-租车，2-车电一体
     */
    @NotNull(message = "套餐类型不能为空!")
    private Integer packageType;
    
    /**
     * 押金缴纳状态，0--未缴纳押金，1--已缴纳押金
     */
    private Integer batteryDepositStatus;
    
    /**
     * 保险id
     */
    private Integer insuranceId;
    
    /**
     * 加盟商id
     */
    @NotNull(message = "加盟商Id不能为空!")
    private Long franchiseeId;
}
