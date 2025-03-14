package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author renhang
 */
@Data
public class ElectricityCabinetBoxLockPageVO {


    private Long id;

    private String sn;

    private String name;

    /**
     * 所属换电柜柜Id
     */
    private Integer electricityCabinetId;


    private String cellNo;
    /**
     * 锁仓类型
     */
    private Integer lockType;
    /**
     * 锁仓原因
     */
    private Integer lockReason;

    /**
     * 锁仓时间
     */
    private Long lockStatusChangeTime;

    private String address;

    private Long franchiseeId;

    private String franchiseeName;

    private Long storeId;

    private String storeName;

    private Integer tenantId;


    /**
     * 区域
     */
    private String areaName;

    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备产品
     */
    private String productKey;
    
    /**
     * 锁定在仓sn
     */
    private String lockSn;
    
    /**
     * 锁仓备注
     */
    private String remark;
}
