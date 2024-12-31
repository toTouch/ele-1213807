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

    private Long eid;

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
}
