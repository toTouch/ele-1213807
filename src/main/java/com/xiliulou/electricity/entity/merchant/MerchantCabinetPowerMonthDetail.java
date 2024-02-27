package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 柜机电费月度统计详情表
 * @date 2024/1/31 15:42:50
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@TableName("t_merchant_cabinet_power_month_detail")
public class MerchantCabinetPowerMonthDetail {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 商户ID
     */
    private Long merchantId;
    
    /**
     * 场地ID
     */
    private Long placeId;
    
    /**
     * 电柜ID
     */
    private Long eid;
    
    /**
     * 电柜编号
     */
    private String sn;
    
    /**
     * 出账年月 yyyy-MM
     */
    private String date;
    
    /**
     * 开始度数
     */
    private Double startPower;
    
    /**
     * 结束度数
     */
    private Double endPower;
    
    /**
     * 用电量
     */
    private Double sumPower;
    
    /**
     * 用电电费
     */
    private Double sumCharge;
    
    /**
     * 电价规则
     */
    private String jsonRule;
    
    /**
     * 开始时间
     */
    private Long beginTime;
    
    /**
     * 结束时间
     */
    private Long endTime;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 0--正常 1--删除
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
