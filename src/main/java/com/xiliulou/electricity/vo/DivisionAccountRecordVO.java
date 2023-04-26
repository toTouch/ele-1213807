package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-24-16:49
 */
@Data
public class DivisionAccountRecordVO {

    private Long id;
    /**
     * 分帐配置id
     */
    private String divisionAccountConfigName;
    /**
     * 套餐名称
     */
    private String membercardName;

    private String orderNo;

    private String userName;
    /**
     * 套餐价格
     */
    private BigDecimal price;
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
    /**
     * 支付时间
     */
    private Long payTime;
    /**
     * 运营商收益
     */
    private BigDecimal operatorIncome;
    /**
     * 加盟商收益
     */
    private BigDecimal franchiseeIncome;
    /**
     * 门店收益
     */
    private BigDecimal storeIncome;

    private Integer status;

    private Long createTime;


    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
}
