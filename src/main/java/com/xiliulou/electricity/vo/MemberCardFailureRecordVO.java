package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-01-03-17:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberCardFailureRecordVO {

    private Integer id;
    /**
     * 套餐名称
     */
    private String cardName;

    private Long uid;

    /**
     * 电池类型
     */
    private String batteryType;

    /**
     * 失效套餐类型 1:换电套餐,2:租车套餐
     */
    private Integer type;

    /**
     * 电池押金
     */
    private BigDecimal deposit;

    /**
     * 套餐到期时间
     */
    private Long memberCardExpireTime;

    /**
     * 车辆SN码
     */
    private String carSn;

    /**
     * 车辆型号名称
     */
    private String carModelName;

    /**
     * 门店Id
     */
    private Long storeId;

    private String storeName;

    /**
     * 租车套餐类型
     */
    private String carMemberCardType;

    /**
     * 车辆租赁周期
     */
    private Integer validDays;

    private Long createTime;

    private Long updateTime;

    //租户id
    private Integer tenantId;
}
