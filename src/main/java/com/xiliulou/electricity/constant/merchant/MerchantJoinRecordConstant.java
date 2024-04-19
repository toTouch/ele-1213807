package com.xiliulou.electricity.constant.merchant;

/**
 * @author HeYafeng
 * @description 商户参与记录常量
 * @date 2024/3/5 14:07:54
 */
public class MerchantJoinRecordConstant {
    
    /**
     * 已参与:扫码成功
     */
    public static final Integer STATUS_INIT = 1;
    
    /**
     * 邀请成功:购买套餐成功
     */
    public static final Integer STATUS_SUCCESS = 2;
    
    /**
     * 已过期:活动已过期
     */
    public static final Integer STATUS_EXPIRED = 3;
    
    /**
     * 已失效:(场景1：退租后 场景2：过了保护期，重新扫码后，需要将旧的记录改为已失效)
     */
    public static final Integer STATUS_INVALID = 4;
    
    /**
     * 邀请保护期未过期
     */
    public static final Integer PROTECTION_STATUS_NORMAL = 0;
    
    /**
     * 邀请保护期已过期
     */
    public static final Integer PROTECTION_STATUS_EXPIRED = 1;
    
    /**
     * 邀请人类型：1-商户本人
     */
    public static final Integer INVITER_TYPE_MERCHANT_SELF = 1;
    
    /**
     * 邀请人类型：2-场地员工
     */
    public static final Integer INVITER_TYPE_MERCHANT_PLACE_EMPLOYEE = 2;
    
    /**
     * 邀请人是否被修改：0-未修改（旧的记录）
     */
    public static final Integer MODIFY_INVITER_NO = 0;
    
    /**
     * 邀请人是否被修改：1-已修改（新的记录）
     */
    public static final Integer MODIFY_INVITER_YES = 1;
    
}
