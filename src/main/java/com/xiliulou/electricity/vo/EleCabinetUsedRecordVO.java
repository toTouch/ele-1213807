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
     * 用户名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 类型
     */
    private String type;

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
    private String returnCellNo;

    /**
     * 租借编号
     */
    private String leaseBatterySn;

    /**
     * 租借仓门
     */
    private String leaseCellNo;

    /**
     * 创建时间
     */
    private String createTime;

}
