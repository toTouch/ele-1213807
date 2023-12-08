package com.xiliulou.electricity.entity;


import com.xiliulou.electricity.validator.CreateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 电池型号(BatteryModel)实体类
 *
 * @author Eclair
 * @since 2023-04-11 10:59:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_battery_model")
public class BatteryModel {
    /**
     * id
     */
    private Long id;

    private Long mid;
    /**
     * 电池型号
     */
    private Integer batteryModel;
    /**
     * 电池型号
     */
    private String batteryType;
    /**
     * 电池电压
     */
    private Double batteryV;
    /**
     * 电池短型号
     */
    private String batteryVShort;

    /**
     * 1--系统 1--自定义
     */
    private Integer type;

    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    private Integer tenantId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
    
    /**
     * 品牌名称
     */
    private String brandName;
    
    /**
     * 电池容量
     */
    private Integer capacity;
    
    /**
     * 电池接入协议 0：未知 1：铁塔
     */
    private Integer accessProtocol;
    
    /**
     * 电池尺寸
     */
    private String size;
    
    /**
     * 电池重量（Kg)
     */
    private Double weight;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    public static final Integer TYPE_SYSTEM = 1;
    public static final Integer TYPE_CUSTOMIZE = 2;

}
