package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 柜机地图VO
 * @date 2024/1/17 09:43:07
 */
@Data
public class ElectricityCabinetListMapVO {
    
    /**
     * 柜机ID
     */
    private Integer id;
    
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
    private Double longitude;
    
    /**
     * 柜机维度
     */
    private Double latitude;
    
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
    
    /**
     * 是否少电柜机:1：少电 0：正常
     */
    private Integer isLowCharge;
    
    /**
     * 是否多电柜机:1:多电 0：正常
     */
    private Integer isFulCharge;
    
    /**
     * 是否锁仓柜机：柜机中只要有一个仓被锁就算锁仓柜机
     */
    private Boolean isUnusable;

    /**
     * 柜机供电类型：0--市电，1--反向供电
     */
    private Integer powerType;
}
