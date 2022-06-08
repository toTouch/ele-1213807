package com.xiliulou.electricity.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 换电柜电池表(ElectricityCar)实体类
 *
 * @author makejava
 * @since 2022-6-7 14:44:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElectricityCarVO {

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

}
