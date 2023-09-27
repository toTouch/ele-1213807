package com.xiliulou.electricity.vo.enterprise;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/26 10:21
 */

@Data
public class EnterprisePackageOrderVO {

    private Long uid;

    private String name;

    private String phone;

    private BigDecimal payAmount;

    private BigDecimal batteryDeposit;

    private String orderNo;

    private Long packageId;

    private String packageName;

    private Long createTime;

    private Long updateTime;

}
