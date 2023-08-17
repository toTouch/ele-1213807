package com.xiliulou.electricity.query.car;

import lombok.Data;

import java.io.Serializable;

/**
 * 车辆数据查询条件
 */
@Data
public class CarDataConditionReq implements Serializable {
    /**
     * 起始
     */
    private Integer offset = 0;
    /**
     * 页大小
     */
    private Integer size = 10;
    /**
     * 车辆sn
     */
    private String carSn;
    /**
     * 车辆型号
     */
    private String carModel;
    /**
     * 加盟商ID
     */
    private int franchiseeId;
    /**
     * 门店ID
     */
    private int storeId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 手机号码
     */
    private String phone;
    /**
     * @see com.xiliulou.electricity.enums.car.CarDataQueryEnum
     * 查询类型
     */
    private Integer queryType;

    private Integer tenantId;

}
