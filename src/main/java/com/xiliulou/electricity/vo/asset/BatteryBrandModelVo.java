package com.xiliulou.electricity.vo.asset;

import lombok.Data;

@Data
public class BatteryBrandModelVo {
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
     * 标准电压
     */
    private Integer standardV;
    
    /**
     * 充电电压
     */
    private Integer chargeV;
    
    /**
     * 串数
     */
    private Integer number;
    
    /**
     * 电池型号
     */
    private String batteryType;
    
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
    
}
