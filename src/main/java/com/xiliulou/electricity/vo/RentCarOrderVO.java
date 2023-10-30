package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-28-16:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RentCarOrderVO {

    private Long id;

    /**
     * 订单Id
     */
    private String orderId;

    private Long uid;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 车辆编号
     */
    private String carSn;

    /**
     * 租车押金
     */
    private Double carDeposit;

    /**
     * 车辆型号
     */
    private String carModelName;

    /**
     * 订单类型(1--租车,2--还车)
     */
    private Integer type;

    /**
     * 交易方式
     */
    private Integer transactionType;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 创建时间
     */
    private Long updateTime;

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 加盟商id
     */
    private Long franchiseeId;


    private Integer tenantId;

    /**
     * 是否租电池
     */
    private Integer rentBattery;

    /**
     * 租赁方式
     */
    private String rentType;


}
