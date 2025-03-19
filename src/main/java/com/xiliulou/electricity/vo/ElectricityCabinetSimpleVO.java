/**
 * Create date: 2024/4/3
 */

package com.xiliulou.electricity.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
     * 换电柜sn
     */
    private String sn;
    /**
     * 联系电话
     */
    private String servicePhone;
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
    
    
    private String businessTimeType;
    
    
    private Long beginTime;
    
    
    private Long endTime;
    
    /**
     * 柜子的可换(1)、可租(2)、可退(3)、反向供电(4) 标签
     */
    private List<Integer> label = new ArrayList<>();


    /**
     * 电池伏数，返回当前仓门的电池伏数集合
     */
    private List<String> batteryVoltageList;
    
    
    public final static Integer IS_EXCHANGE = 1;
    
    public final static Integer IS_RENT = 2;
    
    public final static Integer IS_RETURN = 3;
    
    public final static Integer IS_POWER_BACKUP = 4;
}
