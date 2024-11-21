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
     * 是否开启选仓换电 （0--开启 1--关闭）
     */
    private Integer isSelectionExchange;
    
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
     * 客服电话
     */
    private List<ServicePhoneVO> servicePhones;
    
    /**
     * 是否迁移加盟商 1--关闭 2--开启
     */
    private Integer isMoveFranchisee;
    
    /**
     * 迁移加盟商
     */
    private String franchiseeMoveInfo;
    
    /**
     * 押金缴纳类型 (0：缴纳押金，1：电池免押金，2：租车免押金，3：车辆电池免押金)
     */
    private Integer freeDepositType;
    
    /**
     * 是否开启车辆控制 0--是 0--否
     */
    private Integer isOpenCarControl;
    
    /**
     * 是否开启电子签名 0--是 1--否
     */
    private Integer isEnableEsign;
    
    /**
     * 冻结是否强制退资产 0--是 1--否
     */
    private Integer allowFreezeWithAssets;
    
    /**
     * 打开微信客服 0-是 1-否
     */
    private Integer wxCustomer;
    
    /**
     * 打开支付宝客服 0-是 1-否
     */
    private Integer alipayCustomer;
    
    /**
     * 套餐冻结自动审核，0-关闭自动审核，其他为自动审核最大天数限制
     */
    private Integer freezeAutoReviewDays;
    
    /**
     * 套餐冻结次数限制，0-不限次，其他为用户端申请冻结最大次数
     */
    private Integer packageFreezeCount;
    
    /**
     * 与套餐冻结次数限制参数 packageFreezeCount 联动
     * 套餐冻结最大天数限制，packageFreezeCount 为 0 时最大天数限制为60，packageFreezeCount 为其他数值时根据 packageFreezeDays 限制申请冻结最大天数
     */
    private Integer packageFreezeDays;
    
    /**
     * 套餐过期保护期，单位:小时，保护期结束后产生套餐过期滞纳金，默认24小时
     */
    private Integer expiredProtectionTime;
}
