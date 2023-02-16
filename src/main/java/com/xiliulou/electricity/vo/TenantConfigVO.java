package com.xiliulou.electricity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-22-15:36
 */
@Data
public class TenantConfigVO {
    /**
     * 平台名称
     */
    private String name;
    /**
     * 订单间隔时间
     */
    private Integer orderTime;
    /**
     * 是否人工审核（0--是，1--否）
     */
    private Integer isManualReview;
    /**
     * 是否线上提现（0--是，1--否）
     */
    private Integer isWithdraw;

    //租户id
    private Integer tenantId;
    /**
     * 是否开启异常仓锁仓 （0--开启，1--关闭）
     */
    private Integer isOpenDoorLock;
    /**
     * 是否电池检测 （0--是，1--否）
     */
    private Integer isBatteryReview;
    /**
     * 是否开始暂停月卡功能 （0--关闭 1--开启）
     */
    private Integer disableMemberCard;
    /**
     * 是否开启低电量换电 （0--是 1--否）
     */
    private Integer isLowBatteryExchange;
    /**
     * 低电量换电模式
     */
    private String lowBatteryExchangeModel;
    /**
     * 是否可以自主开仓
     */
    private Integer isEnableSelfOpen;
    /**
     * 租户模板id
     */
    private List<String> templateConfigList;
    /**
     * 客服电话
     */
    private String servicePhone;

    /**
     * 是否迁移加盟商 1--关闭 2--开启
     */
    private Integer isMoveFranchisee;

    /**
     * 迁移加盟商
     */
    private String franchiseeMoveInfo;


}
