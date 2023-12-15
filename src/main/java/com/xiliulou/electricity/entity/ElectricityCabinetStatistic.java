package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * 换电柜统计实体类
 *
 * @author zhangyongbo
 * @since 2023-12-14 11:00:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_electricity_cabinet_statistic")
public class ElectricityCabinetStatistic {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 统计
     */
    private String statisticDate;
    
    /**
     * 换电柜id
     */
    private Integer electricityCabinetId;
    
    /**
     * 换电柜名称
     */
    private String electricityCabinetName;
    
    /**
     * 使用频次
     */
    private Integer useFrequency;
    
    /**
     * 日均换电次数
     */
    private Double averageNumber;
    
    /**
     * 日均活跃度
     */
    private Double averageActivity;
    
    /**
     * 今日换电数量
     */
    private Integer todayNumber ;
    
    /**
     * 今日活跃度
     */
    private Integer todayActivity ;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 租户id
     */
    private Integer tenantId;
}
