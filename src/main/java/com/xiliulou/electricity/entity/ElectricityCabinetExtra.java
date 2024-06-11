package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 柜机扩展表实体类
 * @date 2024/4/23 13:52:40
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_extra")
public class ElectricityCabinetExtra {
    
    /**
     * 柜机id
     */
    private Long eid;
    
    /**
     * 少电多电类型：0-正常（两种情况：一、柜机参数设置中，最少电池数量和最多电池数量都是0 二、既不属于少电也不属于多电） 1-少电 2-多电
     */
    private Integer batteryCountType;
    
    /**
     * 租电可选标签
     */
    private Integer rentTabType;
    
    /**
     * 退电可选标签
     */
    private Integer returnTabType;
    
    /**
     * 最小保留电池数量
     */
    private Integer minRetainBatteryCount;
    
    /**
     * 最大保留电池数量
     */
    private Integer maxRetainBatteryCount;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 删除：0 - 正常，1 - 删除
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
    
    public final static Integer EFFECT_ROWS_ZERO = 0;
    
    public final static Integer MIN_RETAIN_BATTERY = 1;
    
    public final static Integer MIN_RETAIN_EMPTY_CELL = 1;
}
