package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-01-03-9:51
 */
@Data
public class UserCarDetail {
    /**
     * 是否缴纳租车押金 0：是，1：否
     */
    private Integer  isCarDeposit;
    /**
     * 是否购买租车套餐 0：是，1：否
     */
    private Integer  isCarMemberCard;
    /**
     * 租车套餐是否过期 0：是，1：否
     */
    private Integer  isCarMemberCardExpire;
    /**
     * 是否绑定车辆 0：是，1：否
     */
    private Integer  isRentCar;

}
