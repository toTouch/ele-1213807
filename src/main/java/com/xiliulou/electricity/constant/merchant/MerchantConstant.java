package com.xiliulou.electricity.constant.merchant;

/**
 * 商户常量类
 *
 * @author zzlong
 * @since 2024-02-04 09:14:33
 */
public interface MerchantConstant {
    /**
     * 状态：启用
     */
    public final static Integer ENABLE = 0;
    /**
     * 状态：禁用
     */
    public final static Integer DISABLE = 1;
    
    /**
     * 商户自动升级：开启
     */
    public final static Integer open = 0;
    /**
     * 商户自动升级：关闭
     */
    public final static Integer close = 1;
    
    /**
     * 邀请权限：0-开启
     */
    public final static Integer INVITE_AUTH_OPEN = 0;
    /**
     * 邀请权限：1-关闭
     */
    public final static Integer INVITE_AUTH_CLOSE = 1;
    
    /**
     * 邀请时效单位 0：分钟，1：小时
     */
    public final static Integer INVITATION_TIME_UNIT_MINUTES = 0;
    public final static Integer INVITATION_TIME_UNIT_HOURS = 1;
    
    /**
     * 类型 0：购买套餐，1：退租
     */
    public final static Integer TYPE_PURCHASE = 0;
    public final static Integer TYPE_REFUND = 1;
    
    /**
     * 升级条件 1：拉新人数，2：续费人数，3：全部
     */
    public final static Integer UPGRADE_CONDITION_INVITATION = 1;
    public final static Integer UPGRADE_CONDITION_RENEWAL = 2;
    public final static Integer UPGRADE_CONDITION_ALL = 3;
    
    /**
     * 返利类型 0:拉新，1：续费，2：差额
     */
    public final static Integer MERCHANT_REBATE_TYPE_INVITATION = 0;
    public final static Integer MERCHANT_REBATE_TYPE_RENEWAL = 1;
    public final static Integer MERCHANT_REBATE_TYPE_DISCREPANCY = 2;
    
    /**
     * 返利订单类型 0:新租，1:续费
     */
    public final static Integer MERCHANT_REBATE_ORDER_TYPE_NEW = 0;
    public final static Integer MERCHANT_REBATE_ORDER_TYPE_OLD = 1;
    
    /**
     * 返利状态 0-未结算,1-已结算,2-已退回,3-已失效
     */
    public final static Integer MERCHANT_REBATE_STATUS_NOT_SETTLE = 0;
    public final static Integer MERCHANT_REBATE_STATUS_SETTLED = 1;
    public final static Integer MERCHANT_REBATE_STATUS_RETURNED = 2;
    public final static Integer MERCHANT_REBATE_STATUS_EXPIRED = 3;
    
    /**
     * 续费返利记录中是否已退款 0:未退款，1:已退款
     */
    public final static Integer REBATE_IS_NOT_REFUND = 0;
    public final static Integer REBATE_IS_REFUND = 1;
    
    /**
     * 用户查询条件类型 1:查询全部, 2:查询正常, 3:查询即将逾期, 4:查询已过期
     */
    public final static Integer MERCHANT_JOIN_USER_TYPE_ALL = 1;
    public final static Integer MERCHANT_JOIN_USER_TYPE_NORMAL = 2;
    public final static Integer MERCHANT_JOIN_USER_TYPE_OVERDUE_SOON = 3;
    public final static Integer MERCHANT_JOIN_USER_TYPE_EXPIRED = 4;
    
    /**
     * 用户套餐过期天数，用于判断套餐即将过期
     */
    public final static Integer MERCHANT_JOIN_USER_PACKAGE_EXPIRE_DAY = 3;
    
    /**
     * 返利配置状态 0:关闭,1:开启
     */
    public final static Integer REBATE_DISABLE = 0;
    public final static Integer REBATE_ENABLE = 1;
    
    /**
     *  是否存在场地费 0：是，1：否
     */
    public final static Integer EXISTS_PLACE_FEE_YES = 0;
    public final static Integer EXISTS_PLACE_FEE_NO = 1;
    
    public final static Integer DEL_NORMAL = 0;
    public final static Integer DEL_DEL = 1;
    
    public final static Integer MERCHANT_QR_CODE_TYPE = 1;
    
    public final static Integer MERCHANT_EMPLOYEE_QR_CODE_TYPE = 2;
    
    public final static Integer MERCHANT_EMPLOYEE_MAX_SIZE = 20;
    
    Integer UN_BIND_MERCHANT_USER_TYPE = 0;
    
    Integer UN_BIND_CHANNEL_EMPLOYEE_USER_TYPE = 1;
}
