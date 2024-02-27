package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/2/26 21:06
 * @desc
 */
@Data
public class ChannelEmployeePromotionMonthExportVO {
    
    /**
     * 出账年月
     */
    private String month;
    
    /**
     * 渠道员
     */
    private String channelEmployeeName;
    
    /**
     * 月拉新返现汇总(元)
     */
    private BigDecimal monthFirstSumFee;
    
    /**
     * 月续费返现汇总(元)
     */
    private BigDecimal monthRenewSumFee;
    
    /**
     * 类型: 拉新，续费，差额
     */
    private String type;
    
    /**
     * 返现
     */
    private BigDecimal returnMoney;
    
    /**
     * 结算时间
     */
    private String settleDate;
}
