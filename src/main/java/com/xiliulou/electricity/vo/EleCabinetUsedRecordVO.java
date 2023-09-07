package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/7/3 11:44
 * @Description:
 */
@Data
public class EleCabinetUsedRecordVO {

    /**
     * 换电柜ID
     */
    private Long id;

    /**
     * 用户UID
     */
    private Long uid;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 归还电池编号
     */
    private String returnBatterySn;

    /**
     * 归还舱仓门
     */
    private Integer returnCellNo;

    /**
     * 租借编号
     */
    private String leaseBatterySn;

    /**
     * 租借仓门
     */
    private Integer leaseCellNo;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 电柜ID
     */
    private Long eid;

}
