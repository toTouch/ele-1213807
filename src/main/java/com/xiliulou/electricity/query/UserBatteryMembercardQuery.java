package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-15-17:46
 */
@Data
public class UserBatteryMembercardQuery {
    @NotNull(message = "用户id不能为空!")
    private Long uid;

    @NotNull(message = "套餐id不能为空!")
    private Long membercardId;

    /**
     * 套餐有效时间
     */
    private Long validDays;

    /**
     * 套餐次数
     */
    private Long useCount;


    private Long memberCardExpireTime;




}
