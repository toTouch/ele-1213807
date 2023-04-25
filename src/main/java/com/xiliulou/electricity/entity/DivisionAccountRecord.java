package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * (DivisionAccountRecord)实体类
 *
 * @author Eclair
 * @since 2023-04-24 16:23:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_division_account_record")
public class DivisionAccountRecord {

    private Long id;
    /**
     * 分帐配置id
     */
    private Long divisionAccountConfigId;
    /**
     * 套餐名称
     */
    private String membercardName;

    private String orderNo;

    private Long uid;
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

    private Integer tenantId;

    private Integer status;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;


    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = 2;
}
