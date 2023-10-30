package com.xiliulou.electricity.vo.userinfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEleInfoVO {

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邀请人
     */
    private String inviterUserName;

    /**
     * 套餐购买次数
     */
    private Integer cardPayCount;

    /**
     * 当前电池
     */
    private String sn;

    /**
     * 电池租赁状态
     */
    private Integer batteryRentStatus;


    /**
     * 电池押金状态
     */
    private Integer batteryDepositStatus;

    /**
     * 套餐id
     */
    private Long memberCardId;

    /**
     * 换电套餐名称
     */
    private String memberCardName;

    /**
     * 套餐状态
     */
    private Integer memberCardStatus;

    /**
     * 套餐冻结状态0 正常，1 冻结 （套餐状态 1 为冻结，其他为正常）
     */
    private Integer memberCardFreezeStatus;

    /**
     * 月卡剩余次数
     */
    private Long remainingNumber;

    /**
     * 套餐到期时间
     */
    private Long memberCardExpireTime;

    /**
     * 增值服务状态
     */
    private Integer isUse;

    /**
     * 增值服务到期时间
     */
    private Long insuranceExpireTime;

    /**
     * 租户
     */
    private Integer tenantId;

    private Long userInfoId;

    /**
     * 加盟商id
     */
    private Long franchiseeId;

    /**
     * 加盟商id
     */
    private Long uid;

    private String franchiseeName;

    /**
     * 用户认证时间
     */
    private Long userCertificationTime;

    /**
     * 可用状态（0-禁用，1-可用）
     */
    private Integer usableStatus;

    /**
     * 0:不限制,1:限制
     */
    private Integer limitCount;

    /**
     * 使用次数
     */
    private Long useCount;
    
    /**
     * 所属企业名称
     */
    private String enterpriseName;

}
