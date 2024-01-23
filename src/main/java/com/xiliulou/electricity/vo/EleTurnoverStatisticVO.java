package com.xiliulou.electricity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName : EleTurnoverStatisticVO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-01-23
 */
@Data
public class EleTurnoverStatisticVO {
    /**
     * 统计日期
     */
    private Long statisticDate;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 门店id
     */
    private Long storeId;
    
    /**
     * 当日营业额
     */
    private BigDecimal turnoverAmount;
    
    /**
     * 微信商户收入
     */
    private BigDecimal wechatMerchantIncome;
    
    /**
     * 当日总租金
     */
    private BigDecimal rentAmountTotal;
    
    /**
     * 线上租金
     */
    private BigDecimal rentAmountOnline;
    
    /**
     * 线下租金
     */
    private BigDecimal rentAmountOffLine;
    
    /**
     * 当日实缴押金
     */
    private BigDecimal depositAmount;
    
    /**
     * 当日增值服务
     */
    private BigDecimal insuranceAmount;
    
    /**
     * 当日缴纳滞纳金总和
     */
    private BigDecimal overdueAmountTotal;
    
    /**
     * 线上缴纳滞纳金
     */
    private BigDecimal overdueAmountOnline;
    
    /**
     * 线下缴纳滞纳金
     */
    private BigDecimal overdueAmountOffline;
    
    /**
     * 当日退款租金
     */
    private BigDecimal refundRentAmount;
    
    /**
     * 当日退款实缴押金
     */
    private BigDecimal refundDepositAmount;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 修改时间
     */
    private Long updateTime;
}
