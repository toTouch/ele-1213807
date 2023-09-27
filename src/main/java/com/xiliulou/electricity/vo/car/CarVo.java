package com.xiliulou.electricity.vo.car;

import com.xiliulou.electricity.enums.car.CarRentalStateEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 车辆信息
 *
 * @author xiaohui.song
 **/
@Data
public class CarVo implements Serializable {
    
    private static final long serialVersionUID = -6974069090885954269L;
    
    /**
     * 车辆<code>SN</code>码
     */
    private String carSn;
    
    /**
     * 车辆型号名称
     */
    private String carModelName;
    
    /**
     * 所属门店名称
     */
    private String storeName;
    
    /**
     * 车辆经度
     */
    private Double longitude;
    
    /**
     * 车辆纬度
     */
    private Double latitude;
    
    /**
     * 地址更新时间
     */
    private Long pointUpdateTime;
    
    /**
     * 还车订单状态
     * <pre>
     *     1-审核中
     *     2-成功
     *     3-审核拒绝
     * </pre>
     *
     * @see CarRentalStateEnum
     */
    private Integer carRentalState;
    
    /**
     * 车辆锁状态
     */
    private Integer lockType;
    
}
