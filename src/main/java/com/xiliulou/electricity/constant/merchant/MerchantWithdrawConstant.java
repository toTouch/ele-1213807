package com.xiliulou.electricity.constant.merchant;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/22 10:17
 */
public class MerchantWithdrawConstant {
    
    /**
     * 审核中
     */
    public static final Integer REVIEW_IN_PROGRESS = 1;
    
    /**
     * 审核拒绝
     */
    public static final Integer REVIEW_REFUSED = 2;
    
    /**
     * 审核通过
     */
    public static final Integer REVIEW_SUCCESS = 3;
    
    /**
     * 提现中
     */
    public static final Integer WITHDRAW_IN_PROGRESS = 4;
    
    /**
     * 提现成功
     */
    public static final Integer WITHDRAW_SUCCESS = 5;
    
    /**
     * 提现失败
     */
    public static final Integer WITHDRAW_FAIL = 6;
    
    /**
     * 提现类型 微信
     */
    public static final Integer WITHDRAW_TYPE_WECHAT = 1;
    
    /**
     * 提现类型 宝付
     */
    public static final Integer WITHDRAW_TYPE_BAOFU = 2;
    
    /**
     * 提现最大金额限制
     */
    public static final Integer WITHDRAW_MAX_AMOUNT = 500;
    
    public static final String WECHAT_BATCH_STATUS_FINISHED = "FINISHED";
    
    public static final String WECHAT_BATCH_STATUS_CLOSED = "CLOSED";
    
    public static final String WECHAT_BATCH_DETAIL_STATUS_SUCCESS = "SUCCESS";
    
    public static final String WECHAT_BATCH_DETAIL_STATUS_FAIL = "FAIL";
    
}
