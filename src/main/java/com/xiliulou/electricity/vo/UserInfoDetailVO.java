package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * 小程序首页UserInfo视图对象
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-22-15:59
 */
@Data
public class UserInfoDetailVO {
    //审核状态(0--等待审核中,1--审核被拒绝,2--审核通过)
    private Integer authStatus;
    /**
     * 服务状态 (0--初始化,1--已实名认证)
     */
    private Integer serviceStatus;
    /**
     * 电池服务费
     */
    private EleBatteryServiceFeeVO batteryServiceFee;
    /**
     * 获取用户状态（离线换电）
     */
    private UserFrontDetectionVO userFrontDetection;
}
