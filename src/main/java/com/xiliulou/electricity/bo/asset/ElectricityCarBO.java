package com.xiliulou.electricity.bo.asset;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * @author HeYafeng
 * @description 车辆BO
 * @date 2023/11/27 14:02:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElectricityCarBO {

    private Long id;

    /**
     * sn码
     */
    private String sn;

    /**
     * 车辆型号
     */
    private String model;

    /**
     * 地址经度
     */
    private Double longitude;

    /**
     * 地址纬度
     */
    private Double latitude;

    /**
     * 车辆状态
     */
    private Integer status;

    private Long uid;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 绑定用户手机号
     */
    private String phone;

    /**
     * 绑定的电池型号
     */
    private String batterySn;

    /**
     * 门店Id
     */
    private Integer storeId;

    private Long franchiseeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 库房id
     */
    private Long warehouseId;
    
    /**
     * 库存状态；0,库存；1,已出库
     */
    private Integer stockStatus;

}
