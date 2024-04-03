/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/4/3
 */

package com.xiliulou.electricity.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * description: 换电柜简单vo
 *
 * @author caobotao.cbt
 * @date 2024/4/3 11:27
 */
@Data
public class ElectricityCabinetSimpleVO implements Serializable {
    
    
    private static final long serialVersionUID = 5609151885578518827L;
    
    /**
     * 换电柜Id
     */
    private Integer id;
    
    /**
     * 换电柜名称
     */
    private String name;
    
    /**
     * 换电柜地址
     */
    private String address;
    
    /**
     * 地址经度
     */
    private Double longitude;
    
    /**
     * 地址纬度
     */
    private Double latitude;
    
    
    /**
     * 物联网连接状态（0--连网，1--断网）
     */
    private Integer onlineStatus;
    
    /**
     * 满电仓
     */
    private Integer fullyElectricityBattery;
    
    
    private Double distance;
    
}
