package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (ElePowerMonthRecord)实体类
 *
 * @author Eclair
 * @since 2023-07-18 10:20:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_power_month_record")
public class ElePowerMonthRecord {
    
    private Long id;
    
    private String sn;
    
    private String eName;
    
    private Long eid;
    
    private Integer storeId;
    
    private String storeName;
    
    private Integer franchiseeId;
    
    private String franchiseeName;
    /**
    * 月初耗电量
    */
    private Double monthStartPower;
    /**
    * 月末耗电量
    */
    private Double monthEndPower;
    /**
    * 本月耗电量
    */
    private Double monthSumPower;
    /**
    * 本月电费
    */
    private Double monthSumCharge;
    /**
    * 类别明细
    */
    private String jsonCharge;
    /**
    * 日期
    */
    private String date;
    
    private Integer tenantId;
    
    private Long createTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
