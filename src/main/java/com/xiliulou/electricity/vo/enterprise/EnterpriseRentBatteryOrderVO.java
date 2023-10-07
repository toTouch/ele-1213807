package com.xiliulou.electricity.vo.enterprise;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/26 11:17
 */

@Data
public class EnterpriseRentBatteryOrderVO {

    private Long id;

    private String orderNo;

    private Long uid;

    private String name;

    private String phone;
    /**
     * 订单类型(1--租电池,2--还电池,3--后台绑电池,4--后台解绑电池)
     */
    private Integer type;
    /**
     * 租电池押金
     */
    private BigDecimal batteryDeposit;
    /**
     * 订单的状态
     */
    private String status;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;



}
