package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName : MerchantPlaceFeeDailyRecord
 * @Description : 场地费日结记录表
 * @Author : zhangyongbo
 * @since: 2024-02-18
 */
@Data
@TableName("t_merchant_place_fee_daily_record")
public class MerchantPlaceFeeDailyRecord {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 出账日期
     */
    private Long statementDate;
    
    /**
     * 场地id
     */
    private Long placeId ;
    
    /**
     * 电柜ID
     */
    private Long eid ;
    
    /**
     * 电柜编号
     */
    private String sn;
    
    /**
     * 日场地费
     */
    private BigDecimal dayPlaceFee;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 删除标记(0-未删除，1-已删除)
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
}
