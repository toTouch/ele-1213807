package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zgw
 * @date 2023/2/14 11:03
 * @mood
 */
@Data
public class CarMemberCardOrderAddAndUpdate {
    
    /**
     * 套餐到期时间
     */
    @NotNull(message = "套餐到期时间不能为空")
    private Long memberCardExpireTime;
    
    /**
     * 用户Id
     */
    @NotNull(message = "用户id不能为空!")
    private Long uid;
    
    //    /**
    //     * 车辆型号
    //     */
    //    @NotNull(message = "车辆型号不能为空!")
    //    private Integer carModelId;
    
    //    /**
    //     * 租赁方式
    //     */
    //    private String rentType;
    //
    //    /**
    //     * 周期
    //     */
    //    private Integer validDays;
}
