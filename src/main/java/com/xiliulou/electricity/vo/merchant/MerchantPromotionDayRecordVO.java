package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 商户推广费详情
 * @date 2024/2/24 13:02:50
 */
@Data
public class MerchantPromotionDayRecordVO {
    
    private Long id;
    
    private Long merchantId;
    
    private Long inviterUid;
    
    private BigDecimal money;
    
    /**
     * 拉新产生的差额
     */
    private BigDecimal balanceFromFirst;
    
    /**
     * 续费产生的差额
     */
    private BigDecimal balanceFromRenew;
    
    /**
     * 返利类型: 0: 拉新,1：续费,2：差额,3：无费用
     */
    private Integer type;
    
    private String date;
    
    private Integer tenantId;
    
    private Integer delFlag;
    
    private Long createTime;
    
    private Long updateTime;
    
    private String remark;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 返利类型 拉新
     */
    public static final int LASH = 0;
    
    /**
     * 返利类型 续费
     */
    public static final int RENEW = 1;
    
    /**
     * 返利类型 差额
     */
    public static final int BALANCE = 2;
    
    /**
     * 返利类型 无数据
     */
    public static final int NO_DATA = 3;
    
    public static final String LASH_NAME = "拉新";
    
    public static final String RENEW_NAME = "续费";
    
    public static final String BALANCE_NAME = "差额";
    
}
