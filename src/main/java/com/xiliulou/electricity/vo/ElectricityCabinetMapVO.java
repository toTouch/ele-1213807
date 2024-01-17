package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 柜机地图VO
 * @date 2024/1/17 09:43:07
 */
@Data
public class ElectricityCabinetMapVO {
    
    /**
     * 柜机ID
     */
    private Long id;
    
    /**
     * 柜机名称
     */
    private String name;
    
    /**
     * 柜机地址
     */
    private String address;
    
    /**
     * 柜机经度
     */
    private BigDecimal longitude;
    
    /**
     * 柜机维度
     */
    private BigDecimal latitude;
    
    /**
     * 柜机在线状态：0：在线，1：离线
     */
    private Integer onlineStatus;
    
    /**
     * 柜机格口数量
     */
    private Integer boxNum;
    
    /**
     * 柜机仓内电池数量
     */
    private Integer batteryNum;
    
    /**
     * 柜机锁仓格口数
     */
    private Integer unusableBoxNum;
}
