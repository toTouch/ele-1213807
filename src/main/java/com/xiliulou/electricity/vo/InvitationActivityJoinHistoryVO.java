package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.enums.PackageTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-06-13:44
 */
@Data
public class InvitationActivityJoinHistoryVO {

    private Long id;
    /**
     * 邀请用户uid
     */
    private String userName;

    private String phone;

    private Long uid;
    private Long joinUid;
    /**
     * 参与用户uid
     */
    private String joinUserName;

    private String joinUserPhone;
    /**
     * 参与开始时间
     */
    private Long startTime;
    /**
     * 参与过期时间
     */
    private Long expiredTime;
    /**
     * 活动id
     */
    private Long activityId;
    /**
     * 参与状态 1--初始化，2--已参与，3--已过期
     */
    private Integer status;


    private Integer payCount;

    private BigDecimal money;
    /**
     * 创建时间
     */
    private Long createTime;

    private String activityName;
    
    /**
     * 套餐ID (20241209 加该字段)
     */
    private Long packageId;
    
    /**
     * 套餐类型 (1 - 换电， 2 - 租车, 3 - 车电一体)  (20241209 加该字段)
     * @see PackageTypeEnum
     */
    private Integer packageType;
    
    private Long franchiseeId;
    
    private String franchiseeName;

}
