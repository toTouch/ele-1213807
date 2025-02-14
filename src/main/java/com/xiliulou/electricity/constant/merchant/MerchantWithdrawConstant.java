package com.xiliulou.electricity.constant.merchant;

import java.math.BigDecimal;

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

    /**
     * 提现最大金额限制: 新流程
     */
    public static final Integer WITHDRAW_MAX_AMOUNT_V2 = 200;
    
    public static final String WECHAT_BATCH_STATUS_FINISHED = "FINISHED";
    
    public static final String WECHAT_BATCH_STATUS_CLOSED = "CLOSED";
    
    public static final String WECHAT_BATCH_DETAIL_STATUS_SUCCESS = "SUCCESS";
    
    public static final String WECHAT_BATCH_DETAIL_STATUS_FAIL = "FAIL";
    
    public static final String WECHAT_TRANSFER_BATCH_NAME_SUFFIX = "商户转账";
    
    public static final String WECHAT_TRANSFER_BATCH_REMARK_SUFFIX = "推广费";
    
    public static final String WITHDRAW_FAILED_COMMON_REASON = "提现失败，请联系客服处理";

    public static final String WITHDRAW_TRANSFER_REMARK = "商户推广费";

    public static final String WITHDRAW_TRANSFER_SCENE_JOB = "岗位类型";

    public static final String WITHDRAW_TRANSFER_SCENE_JOB_CONTENT = "商户";

    public static final String WITHDRAW_TRANSFER_SCENE_REWARD = "报酬说明";

    public static final BigDecimal WITHDRAW_TRANSFER_DEFAULT = new BigDecimal("200");
}
