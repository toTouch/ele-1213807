package com.xiliulou.electricity.constant.profitsharing;

/**
 * @author maxiaodong
 * @date 2024/8/23 15:18
 * @desc
 */
public class ProfitSharingTradeOrderConstant {
    public static final String CHANNEL_ALI = "ALIPAY";
    
    public static final String CHANNEL_WE_CHAT = "WECHAT";
    
    /**
     * 处理状态：0-初始化，1-待发起分账，2-分账发起成功，3-分账发起失败，4-已失效
     */
    public static final Integer PROCESS_STATE_INIT = 0;
    
    public static final Integer PROCESS_STATE_PENDING = 1;
    
    public static final Integer PROCESS_STATE_SUCCESS = 2;
    
    public static final Integer PROCESS_STATE_FAIL = 3;
    
    public static final Integer PROCESS_STATE_INVALID = 4;
    
    public static final Integer IS_REFUND_YES = 0;
    
    public static final Integer IS_REFUND_NO = 1;
    
    /**
     * 是否混合支付：0-是，1-否
     */
    public static final Integer WHETHER_MIXED_PAY_YES = 0;
    
    public static final Integer WHETHER_MIXED_PAY_NO = 1;
    
    /**
     * 解冻描述
     */
    public static final String UNFREEZE_DESC = "解冻全部剩余资金";
    
    
}
