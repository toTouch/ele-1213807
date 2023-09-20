package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-27-16:27
 */
@Data
public class UserInfoResultVO {

    //实名认证审核状态 -1:初始化，0：等待审核中,1：审核被拒绝,2：审核通过
    private Integer authStatus;

    /**
     * 加盟商
     */
    private Long franchiseeId;

    /**
     * 电池免押状态
     */
    private Integer batteryFreeStatus;

    /**
     * 电池免押时间
     */
    private Long batteryFreeApplyTime;

    /**
     * 车辆免押状态
     */
    private Integer carFreeStatus;

    /**
     * 车辆免押时间
     */
    private Long carFreeApplyTime;

    private UserBatteryDetail userBatteryDetail;

    private UserCarDetail UserCarDetail;

    /**
     * 获取用户状态（离线换电）
     */
    private UserFrontDetectionVO userFrontDetection;


    public static final Integer YES = 0;
    public static final Integer NO = 1;

}
